package com.kcibald.services.user.handlers

import com.kcibald.services.user.*
import com.kcibald.services.user.dao.DBAccess
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.AuthenticationRequest
import com.kcibald.services.user.proto.AuthenticationResponse
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.core.eventbus.requestAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class AuthenticationInterfaceKtTest {

    private lateinit var vertx: Vertx
    private lateinit var eventBus: EventBus
    private val eventBusAddress = "_authentication_test_address"

    @BeforeEach
    fun setup(vertx: Vertx) {
        this.vertx = vertx
        this.eventBus = vertx.eventBus()
    }

    @Test
    fun auth_success() = runBlocking {
        val expectedUser = createUser()
        val expectedEmail = "example@example.com"
        val password = "password!"
        val bytes = hashPassword(vertx, password)

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
                assertEquals(expectedEmail, email)
                return expectedUser to bytes
            }
        }
        prepareAndRegistryTarget(dbAccess)

        val request = AuthenticationRequest(expectedEmail, password)
        val response = exchangeRequest(request)

        val successResponse = response.result as AuthenticationResponse.Result.SuccessUser
        assertEquals(expectedUser.transform(), successResponse.successUser)

        Unit
    }

    @Test
    fun auth_credential_incorrect() = runBlocking {
        val expectedUser = createUser()
        val expectedEmail = "example@example.com"
        val password = "password!"
        val bytes = hashPassword(vertx, "not $password")

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
                assertEquals(expectedEmail, email)
                return expectedUser to bytes
            }
        }
        prepareAndRegistryTarget(dbAccess)

        val request = AuthenticationRequest(expectedEmail, password)
        val response = exchangeRequest(request)

        val failureResponse = response.result as AuthenticationResponse.Result.CommonAuthenticationError
        assertEquals(
            AuthenticationResponse.AuthenticationErrorType.INVALID_CREDENTIAL,
            failureResponse.commonAuthenticationError
        )

        Unit
    }

    @Test
    fun auth_user_not_found() = runBlocking {
        val expectedEmail = "example@example.com"
        val password = "password!"

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
                assertEquals(expectedEmail, email)
                return null
            }
        }
        prepareAndRegistryTarget(dbAccess)

        val request = AuthenticationRequest(expectedEmail, password)
        val response = exchangeRequest(request)

        val failureResponse = response.result as AuthenticationResponse.Result.CommonAuthenticationError
        assertEquals(
            AuthenticationResponse.AuthenticationErrorType.USER_NOT_FOUND,
            failureResponse.commonAuthenticationError
        )

        Unit
    }

    @Test
    fun auth_db_exception() = runBlocking {
        val expectedEmail = "example@example.com"
        val password = "password!"

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
                assertEquals(expectedEmail, email)
                throw Exception("OH NO")
            }
        }
        prepareAndRegistryTarget(dbAccess)

        val request = AuthenticationRequest(expectedEmail, password)
        val response = exchangeRequest(request)

        val failureResponse = response.result as AuthenticationResponse.Result.SystemErrorMessage
        assertEquals(
            "database error",
            failureResponse.systemErrorMessage
        )

        Unit
    }

    @Test
    fun auth_db_failure() = runBlocking {
        val expectedEmail = "example@example.com"
        val password = "password!"

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
                assertEquals(expectedEmail, email)
                throw Throwable("OH NO")
            }
        }
        prepareAndRegistryTarget(dbAccess)

        val request = AuthenticationRequest(expectedEmail, password)
        val response = exchangeRequest(request)

        val failureResponse = response.result as AuthenticationResponse.Result.SystemErrorMessage
        assertEquals(
            "unexpected internal error",
            failureResponse.systemErrorMessage
        )

        Unit
    }

    private suspend fun exchangeRequest(request: AuthenticationRequest): AuthenticationResponse {
        val response = eventBus.requestAwait<ByteArray>(eventBusAddress, request.protoMarshal())
        return AuthenticationResponse.protoUnmarshal(response.body())
    }

    private suspend fun prepareAndRegistryTarget(dbAccess: DBAccess) {
        val config = Config { addSpec(MasterConfigSpec.AuthenticationConfig) }
            .from.map.kv(mapOf("auth.event_bus_name" to eventBusAddress))
        val sharedRuntimeData = SharedRuntimeData(vertx, config, dbAccess)
        val target = AuthenticationInterface(sharedRuntimeData)
        initializeAndSetTarget(target)
    }

    private lateinit var target: AuthenticationInterface

    private suspend fun initializeAndSetTarget(target: AuthenticationInterface) {
        this.target = target
        target.bind(eventBus)
    }

    private fun createUser(): SafeUser = SafeUser(
        genRandomString(5),
        genRandomString(10),
        genRandomString(10),
        genRandomString(10)
    )

}