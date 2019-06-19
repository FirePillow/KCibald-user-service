package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.coroutineHandler
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.DescribeUserRequest
import com.kcibald.services.user.proto.DescribeUserResponse
import com.kcibald.services.user.proto.Empty
import com.kcibald.services.user.proto.User
import com.kcibald.services.user.transform
import com.kcibald.utils.i
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory

internal class DescribeUserInterface(sharedRuntimeData: SharedRuntimeData) : ServiceInterface(sharedRuntimeData) {
    private val logger = LoggerFactory.getLogger(DescribeUserInterface::class.java)!!

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.DescribeUserConfig.event_bus_name]
        logger.i { "registering Describe User Interface on event bus address: $eventBusAddress" }
        val consumer = eventBus.consumer<Buffer>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, unexpectedErrorEventResult) {
            val request = DescribeUserRequest.protoUnmarshal(it.body().bytes)
            return@coroutineHandler try {
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
    }

    internal suspend fun queryByURLKey(urlKey: String): SafeUser? =
        runtimeData.dbAccess.getUserWithUrlKey(urlKey)

    internal suspend fun queryByID(id: String): SafeUser? =
        runtimeData.dbAccess.getUserWithId(id)

    internal suspend fun queryByUserName(userName: String): List<User> {
        return runtimeData
            .dbAccess
            .getUserWithName(userName)
            .map(SafeUser::transform)
    }

}

internal val unexpectedErrorEventResult = ProtobufEventResult(
    DescribeUserResponse(
        result = DescribeUserResponse.Result_.SystemErrorMessage(
            "unexpected error"
        )
    )
)

internal val databaseErrorEventResult = ProtobufEventResult(
    DescribeUserResponse(
        result = DescribeUserResponse.Result_.SystemErrorMessage(
            "database error"
        )
    )
)

internal val userNotFoundEventResult = ProtobufEventResult(
    DescribeUserResponse(
        DescribeUserResponse.Result_.UserNotFound(Empty())
    )
)

internal fun packIndividual(result: SafeUser?): ProtobufEventResult<DescribeUserResponse> {
    return if (result != null) {
        ProtobufEventResult(
            DescribeUserResponse(
                DescribeUserResponse.Result_.SingleUserResult(
                    DescribeUserResponse.SuccessSingleUserResult(result.transform())
                )
            )
        )
    } else {
        userNotFoundEventResult
    }
}

internal fun packMultiple(result: List<User>): EventResult {
    return if (result.isNotEmpty())
        ProtobufEventResult(
            DescribeUserResponse(
                DescribeUserResponse.Result_.MultiUserResult(
                    DescribeUserResponse.SuccessMultiUserResult(result)
                )
            )
        )
    else
        userNotFoundEventResult
}
