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
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.LoggerFactory

internal class AuthenticationInterface(sharedRuntimeData: SharedRuntimeData) : ServiceInterface(sharedRuntimeData) {
    private val logger = LoggerFactory.getLogger(AuthenticationInterface::class.java)

    override suspend fun bind(eventBus: EventBus) {
        val eventBusAddress = runtimeData.config[MasterConfigSpec.AuthenticationConfig.event_bus_name]
        val consumer = eventBus.consumer<ByteArray>(eventBusAddress)
        consumer.coroutineHandler(runtimeData.vertx, unexpectedErrorResponse, ::handleEvent)
    }

    private suspend fun handleEvent(message: Message<ByteArray>): ProtobufEventResult<AuthenticationResponse> {
        logger.d { "authentication request inbound" }
        val request = AuthenticationRequest.protoUnmarshal(message.body())
        val email = request.userEmail
        val password = request.plainPassword

        logger.d { "accessing db for user with email $email" }

        val dbResult = try {
            runtimeData.dbAccess.getUserAndPasswordWithEmail(email)
        } catch (e: Exception) {
            logger.warn("database failure when processing authentication request for user with email: $email", e)
            return databaseErrorResponse
        }

        return processDBResult(dbResult, email, password)
    }

    private suspend fun processDBResult(
        dbResult: Pair<SafeUser, ByteArray>?,
        email: String,
        password: String
    ): ProtobufEventResult<AuthenticationResponse> {
        if (dbResult != null) {
            logger.d { "user with email $email exists, checking password and authority" }
            val (user, hash) = dbResult
            if (passwordMatches(runtimeData.vertx, hash, password)) {
                logger.d { "user with email $email have input correct password, continue" }
                return createSuccessAuthenticationResponse(user)
            } else {
                logger.d { "user with email $email have invalid credentials" }
                return invalidCredentialFailureAuthenticationResponse
            }
        } else {
            return userNotFoundAuthenticationResponse
        }
    }

    private fun createSuccessAuthenticationResponse(user: SafeUser) =
        ProtobufEventResult(AuthenticationResponse(SuccessUser(user.transform())))

    private val invalidCredentialFailureAuthenticationResponse = ProtobufEventResult(
        AuthenticationResponse(
            CommonAuthenticationError(INVALID_CREDENTIAL)
        )
    )

    private val userNotFoundAuthenticationResponse = ProtobufEventResult(
        AuthenticationResponse(
            CommonAuthenticationError(USER_NOT_FOUND)
        )
    )

    private val databaseErrorResponse = ProtobufEventResult(
        AuthenticationResponse(
            SystemErrorMessage("database error")
        )
    )

    private val unexpectedErrorResponse = ProtobufEventResult(
        AuthenticationResponse(
            SystemErrorMessage("unexpected internal error")
        )
    )
}