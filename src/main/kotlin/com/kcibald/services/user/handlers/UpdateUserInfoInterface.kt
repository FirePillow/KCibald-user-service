package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.coroutineHandler
import com.kcibald.services.user.proto.UpdateUserInfoRequest
import com.kcibald.services.user.proto.UpdateUserInfoResponse
import com.kcibald.utils.i
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.LoggerFactory

internal class UpdateUserInfoInterface(runtimeData: SharedRuntimeData) : ServiceInterface(runtimeData) {
    private val logger = LoggerFactory.getLogger(UpdateUserInfoInterface::class.java)!!
    private val dbAccess = runtimeData.dbAccess

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.UpdateUserInfoConfig.event_bus_name]
        val consumer = eventBus.consumer<Buffer>(eventBusAddress)
        logger.i { "registering Update User Interface on event bus address: $eventBusAddress" }
        consumer.coroutineHandler(runtimeData.vertx, sysErrorProtobufEventResult, ::handleEvent)
    }

    private suspend fun handleEvent(message: Message<Buffer>): EventResult {
        val request = UpdateUserInfoRequest.protoUnmarshal(message.body().bytes)
        return when (val target = request.target) {
            is UpdateUserInfoRequest.Target.UserName ->
                updateUserName(request, target)
            is UpdateUserInfoRequest.Target.Signature ->
                updateSignature(request, target)
            is UpdateUserInfoRequest.Target.AvatarKey ->
                updateAvatarKey(request, target)
            is UpdateUserInfoRequest.Target.Password ->
                updatePassword(request, target)
            else ->
                throw AssertionError("should not reach here")
        }
    }

    private suspend fun updatePassword(
        request: UpdateUserInfoRequest,
        target: UpdateUserInfoRequest.Target.Password
    ): ProtobufEventResult<UpdateUserInfoResponse> = doUpdateInternal(request.queryBy) { userId, urlKey ->
        val (before, after) = target.password
        dbAccess.updatePassword(
            before,
            after,
            userId = userId,
            urlKey = urlKey
        )
    }

    private suspend fun updateAvatarKey(
        request: UpdateUserInfoRequest,
        target: UpdateUserInfoRequest.Target.AvatarKey
    ): ProtobufEventResult<UpdateUserInfoResponse> = doUpdateInternal(request.queryBy) { userId, urlKey ->
        dbAccess.updateAvatar(target.avatarKey, userId = userId, urlKey = urlKey)
    }

    private suspend fun updateSignature(
        request: UpdateUserInfoRequest,
        target: UpdateUserInfoRequest.Target.Signature
    ): ProtobufEventResult<UpdateUserInfoResponse> = doUpdateInternal(request.queryBy) { userId, urlKey ->
        dbAccess.updateSignature(target.signature, userId = userId, urlKey = urlKey)
    }

    private suspend fun updateUserName(
        request: UpdateUserInfoRequest,
        target: UpdateUserInfoRequest.Target.UserName
    ): ProtobufEventResult<UpdateUserInfoResponse> = doUpdateInternal(request.queryBy) { userId, urlKey ->
        dbAccess.updateUserName(target.userName, userId = userId, urlKey = urlKey)
    }

    private inline fun doUpdateInternal(
        queryBy: UpdateUserInfoRequest.QueryBy?,
        block: (String?, String?) -> Boolean
    ): ProtobufEventResult<UpdateUserInfoResponse> {
        var userId: String? = null
        var urlKey: String? = null
        when (queryBy) {
            is UpdateUserInfoRequest.QueryBy.UserId ->
                userId = queryBy.userId
            is UpdateUserInfoRequest.QueryBy.UrlKey ->
                urlKey = queryBy.urlKey
            null ->
                throw NullPointerException()
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

    private val successProtobufResult = ProtobufEventResult(
        UpdateUserInfoResponse(
            UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS
        )
    )

    private val unsafeUpdateProtobufResult = ProtobufEventResult(
        UpdateUserInfoResponse(
            UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE
        )
    )

    private val dbErrorProtobufEventResult = ProtobufEventResult(
        UpdateUserInfoResponse(
            UpdateUserInfoResponse.GeneralResponseTypes.DB_ERROR
        )
    )

    private val sysErrorProtobufEventResult = ProtobufEventResult(
        UpdateUserInfoResponse(
            UpdateUserInfoResponse.GeneralResponseTypes.INTERNAL_ERROR
        )
    )


}