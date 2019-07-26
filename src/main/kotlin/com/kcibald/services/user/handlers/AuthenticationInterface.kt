package com.kcibald.services.user.handlers

import com.kcibald.services.user.*
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.AuthenticationRequest
import com.kcibald.services.user.proto.AuthenticationResponse
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.INVALID_CREDENTIAL
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.USER_NOT_FOUND
import com.kcibald.services.user.proto.AuthenticationResponse.Result.*
import com.kcibald.utils.d
import io.vertx.core.eventbus.EventBus
import io.vertx.core.logging.LoggerFactory

internal class AuthenticationInterface(sharedRuntimeData: SharedRuntimeData) : ServiceInterface(sharedRuntimeData) {
    private val logger = LoggerFactory.getLogger(AuthenticationInterface::class.java)

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.AuthenticationConfig.event_bus_name]
        val consumer = eventBus.consumer<ByteArray>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, unexpectedErrorResponse) {
            logger.d { "authentication request inbound" }
            val request = AuthenticationRequest.protoUnmarshal(it.body())
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
                if (passwordMatches(runtimeData.vertx, hash, password)) {
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

internal fun createSuccessAuthenticationResponse(user: SafeUser) =
    ProtobufEventResult(AuthenticationResponse(SuccessUser(user.transform())))

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