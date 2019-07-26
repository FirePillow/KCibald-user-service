package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.coroutineHandler
import com.kcibald.services.user.proto.UpdateUserInfoRequest
import com.kcibald.services.user.proto.UpdateUserInfoResponse
import com.kcibald.utils.i
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory

internal class UpdateUserInfoInterface(runtimeData: SharedRuntimeData) : ServiceInterface(runtimeData) {
    private val logger = LoggerFactory.getLogger(UpdateUserInfoInterface::class.java)!!
    private val dbAccess = runtimeData.dbAccess

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.UpdateUserInfoConfig.event_bus_name]
        val consumer = eventBus.consumer<Buffer>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, sysErrorProtobufEventResult) {
            logger.i { "registering Update User Interface on event bus address: $eventBusAddress" }
            val request = UpdateUserInfoRequest.protoUnmarshal(it.body().bytes)
            return@coroutineHandler when (val target = request.target) {
                is UpdateUserInfoRequest.Target.UserName ->
                    doUpdateInternal(request.queryBy) { userId, urlKey ->
                        dbAccess.updateUserName(target.userName, userId = userId, urlKey = urlKey)
                    }
                is UpdateUserInfoRequest.Target.Signature ->
                    doUpdateInternal(request.queryBy) { userId, urlKey ->
                        dbAccess.updateSignature(target.signature, userId = userId, urlKey = urlKey)
                    }

                is UpdateUserInfoRequest.Target.AvatarKey ->
                    doUpdateInternal(request.queryBy) { userId, urlKey ->
                        dbAccess.updateUserName(target.avatarKey, userId = userId, urlKey = urlKey)
                    }

                is UpdateUserInfoRequest.Target.Password ->
                    doUpdateInternal(request.queryBy) { userId, urlKey ->
                        val (before, after) = target.password
                        dbAccess.updatePassword(
                            before,
                            after,
                            userId = userId,
                            urlKey = urlKey
                        )
                    }
                else ->
                    throw AssertionError("should not reach here")
            }
        }
    }

    private inline fun doUpdateInternal(
        queryBy: UpdateUserInfoRequest.QueryBy?,
        block: (String?, String?) -> Boolean
    ): ProtobufEventResult<UpdateUserInfoResponse> {
        var userId: String? = null
        var urlKey: String? = null
        when (queryBy) {
            is UpdateUserInfoRequest.QueryBy.UserId -> {
                userId = queryBy.userId
            }
            is UpdateUserInfoRequest.QueryBy.UrlKey -> {
                urlKey = queryBy.urlKey
            }
            null -> throw NullPointerException()
        }
        val success = try {
            block(userId, urlKey)
        } catch (e: Exception) {
            logger.warn("DB error occurred when processing user info update request", e)
            return dbErrorProtobufEventResult
        }

        return if (success) {
            successProtobufResult
        } else {
            unsafeUpdateProtobufResult
        }
    }

}

internal val successProtobufResult = ProtobufEventResult(
    UpdateUserInfoResponse(
        UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS
    )
)

internal val unsafeUpdateProtobufResult = ProtobufEventResult(
    UpdateUserInfoResponse(
        UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE
    )
)

internal val dbErrorProtobufEventResult = ProtobufEventResult(
    UpdateUserInfoResponse(
        UpdateUserInfoResponse.GeneralResponseTypes.DB_ERROR
    )
)

internal val sysErrorProtobufEventResult = ProtobufEventResult(
    UpdateUserInfoResponse(
        UpdateUserInfoResponse.GeneralResponseTypes.INTERNAL_ERROR
    )
)
