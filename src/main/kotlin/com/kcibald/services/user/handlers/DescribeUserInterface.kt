package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.coroutineHandler
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.DescribeUserRequest
import com.kcibald.services.user.proto.DescribeUserResponse
import com.kcibald.services.user.proto.Empty
import com.kcibald.services.user.transform
import com.kcibald.utils.i
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.LoggerFactory

internal class DescribeUserInterface(sharedRuntimeData: SharedRuntimeData) : ServiceInterface(sharedRuntimeData) {
    private val logger = LoggerFactory.getLogger(DescribeUserInterface::class.java)!!

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.DescribeUserConfig.event_bus_name]
        logger.i { "registering Describe User Interface on event bus address: $eventBusAddress" }
        val consumer = eventBus.consumer<ByteArray>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, unexpectedErrorEventResult, ::handleEvent)
    }

    private suspend fun handleEvent(message: Message<ByteArray>): EventResult {
        val request = DescribeUserRequest.protoUnmarshal(message.body())
        return try {
            when (val by = request.by) {
                is DescribeUserRequest.By.UrlKey -> packIndividual(queryByURLKey(by.urlKey))
                is DescribeUserRequest.By.ID -> packIndividual(queryByID(by.iD))
                is DescribeUserRequest.By.UserName -> packMultiple(queryByUserName(by.userName))
                else -> throw AssertionError("unreachable branch")
            }
        } catch (e: Exception) {
            logger.warn("unexpected database error, exception: $e", e)
            databaseErrorEventResult
        }
    }

    private suspend fun queryByURLKey(urlKey: String): SafeUser? =
        runtimeData.dbAccess.getUserWithUrlKey(urlKey)

    private suspend fun queryByID(id: String): SafeUser? =
        runtimeData.dbAccess.getUserWithId(id)

    private suspend fun queryByUserName(userName: String): List<SafeUser> =
        runtimeData
            .dbAccess
            .getUserWithName(userName)

    private val unexpectedErrorEventResult = ProtobufEventResult(
        DescribeUserResponse(
            result = DescribeUserResponse.Result.SystemErrorMessage(
                "unexpected error"
            )
        )
    )

    private val databaseErrorEventResult = ProtobufEventResult(
        DescribeUserResponse(
            result = DescribeUserResponse.Result.SystemErrorMessage(
                "database error"
            )
        )
    )

    private val userNotFoundEventResult = ProtobufEventResult(
        DescribeUserResponse(
            DescribeUserResponse.Result.UserNotFound(Empty())
        )
    )

    private fun packIndividual(result: SafeUser?): ProtobufEventResult<DescribeUserResponse> {
        return if (result != null) {
            ProtobufEventResult(
                DescribeUserResponse(
                    DescribeUserResponse.Result.SingleUserResult(
                        DescribeUserResponse.SuccessSingleUserResult(result.transform())
                    )
                )
            )
        } else {
            userNotFoundEventResult
        }
    }

    private fun packMultiple(result: List<SafeUser>): EventResult {
        return if (result.isNotEmpty()) {
            val processed = result.map(SafeUser::transform)
            ProtobufEventResult(
                DescribeUserResponse(
                    DescribeUserResponse.Result.MultiUserResult(
                        DescribeUserResponse.SuccessMultiUserResult(processed)
                    )
                )
            )
        } else {
            userNotFoundEventResult
        }
    }

}
