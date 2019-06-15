package com.kcibald.services.user.handlers

import at.favre.lib.crypto.bcrypt.BCrypt
import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.coroutineHandler
import com.kcibald.services.user.dao.SafeUserInternal
import com.kcibald.services.user.dao.urlKeyKey
import com.kcibald.services.user.proto.AuthenticationRequest
import com.kcibald.services.user.proto.AuthenticationResponse
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.INVALID_CREDENTIAL
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.USER_NOT_FOUND
import com.kcibald.services.user.proto.AuthenticationResponse.Result.*
import com.kcibald.utils.d
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.executeBlockingAwait

internal class AuthenticationInterface(sharedRuntimeData: SharedRuntimeData) : ServiceInterface(sharedRuntimeData) {
    private val logger = LoggerFactory.getLogger(AuthenticationInterface::class.java)
    private val bcryptVerifier = BCrypt.verifyer()

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.AuthenticationConfig.event_bus_name]
        val consumer = eventBus.consumer<Buffer>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, unexpectedErrorResponse) {
            logger.d { "authentication request inbound" }
            val request = AuthenticationRequest.protoUnmarshal(it.body().bytes)
            val email = request.userEmail
            val password = request.plainPassword

            logger.d { "accessing db for user with email $email" }

            val dbResult = try {
                runtimeData.dbAccess.getUserAndPasswordWithEmail(email)
            } catch (e: Exception) {
                logger.warn("database failure when processing authentication request for user with email: $email", e)
                return@coroutineHandler databaseErrorResponse
            }

            if (dbResult != null) {
                logger.d { "user with email $email exists, checking password and authority" }
                val (user, hash) = dbResult
                val result = runtimeData.vertx.executeBlockingAwait<BCrypt.Result> { future ->
                    future.complete(
                        bcryptVerifier.verify(password.toByteArray(), hash)
                    )
                }!!
                if (result.verified) {
                    logger.d { "user with email $email have input correct password, continue" }
                    return@coroutineHandler createSuccessAuthenticationResponse(user)
                } else {
                    logger.d { "user with email $email have invalid credentials" }
                    return@coroutineHandler invalidCredentialFailureAuthenticationResponse
                }
            } else {
                return@coroutineHandler userNotFoundAuthenticationResponse
            }
        }
    }

}

internal fun createSuccessAuthenticationResponse(user: SafeUserInternal)
        : ProtobufEventResult<AuthenticationResponse> {
    val payloadUser = com.kcibald.services.user.proto.User(
        userId = user.user_id,
        userName = user.user_name,
        urlKey = urlKeyKey,
        signature = user.signature,
        avatarKey = user.avatar_key
    )
    return ProtobufEventResult(AuthenticationResponse(SuccessUser(payloadUser)))
}

internal val invalidCredentialFailureAuthenticationResponse = ProtobufEventResult(
    AuthenticationResponse(
        CommonAuthenticationError(INVALID_CREDENTIAL)
    )
)

internal val userNotFoundAuthenticationResponse = ProtobufEventResult(
    AuthenticationResponse(
        CommonAuthenticationError(USER_NOT_FOUND)
    )
)

internal val databaseErrorResponse = ProtobufEventResult(
    AuthenticationResponse(
        SystemErrorMessage("database error")
    )
)

internal val unexpectedErrorResponse = ProtobufEventResult(
    AuthenticationResponse(
        SystemErrorMessage("unexpected internal error during processing result")
    )
)