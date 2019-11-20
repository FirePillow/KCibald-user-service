package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.dao.DBAccess
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.genRandomString
import com.kcibald.services.user.proto.DescribeUserRequest
import com.kcibald.services.user.proto.DescribeUserResponse
import com.kcibald.services.user.transform
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.core.eventbus.requestAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class DescribeUserInterfaceKtTest {

    private lateinit var vertx: Vertx
    private lateinit var eventBus: EventBus
    private val eventBusAddress = "_describe_user_test"

    @BeforeEach
    fun setup(vertx: Vertx) {
        this.vertx = vertx
        this.eventBus = vertx.eventBus()
    }

    @Test
    fun describe_user_by_urlKey() = runBlocking {
        val expectedUser = createTestUser()
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithUrlKey(urlKey: String): SafeUser? {
                assertEquals(expectedUser.urlKey, urlKey)
                return expectedUser
            }
        }
        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.UrlKey(expectedUser.urlKey))
        val response = exchangeRequest(request)

        val result = response.result as DescribeUserResponse.Result.SingleUserResult
        val user = result.singleUserResult.result ?: fail("no result")

        user.assertEqualsTo(expectedUser)

        Unit
    }

    @Test
    fun describe_user_by_urlKey_not_found() = runBlocking {
        val expectedUser: SafeUser? = null
        val expectedUrlKey = "urlkeyyyyyy"
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithUrlKey(urlKey: String): SafeUser? {
                assertEquals(expectedUrlKey, urlKey)
                return expectedUser
            }
        }
        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.UrlKey(expectedUrlKey))
        val response = exchangeRequest(request)

        assert(response.result is DescribeUserResponse.Result.UserNotFound)

        Unit
    }

    @Test
    fun describe_user_by_id() = runBlocking {
        val expectedUser = createTestUser()
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithId(id: String): SafeUser? {
                assertEquals(expectedUser.userId, id)
                return expectedUser
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.ID(expectedUser.userId))
        val response = exchangeRequest(request)

        val result = response.result as DescribeUserResponse.Result.SingleUserResult
        val user = result.singleUserResult.result ?: fail("no result")

        user.assertEqualsTo(expectedUser)

        Unit
    }

    @Test
    fun describe_user_by_id_not_found() = runBlocking {
        val expectedUser = createTestUser()
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithId(id: String): SafeUser? {
                assertEquals(expectedUser.userId, id)
                return null
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.ID(expectedUser.userId))
        val response = exchangeRequest(request)

        assert(response.result is DescribeUserResponse.Result.UserNotFound)

        Unit
    }

    @Test
    fun describe_user_by_username_single() = runBlocking {
        val expectedUser = createTestUser()
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithName(name: String): List<SafeUser> {
                assertEquals(expectedUser.userName, name)
                return listOf(expectedUser)
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.UserName(expectedUser.userName))
        val response = exchangeRequest(request)

        val result = response.result as DescribeUserResponse.Result.MultiUserResult
        val users = result.multiUserResult.result

        assert(users.size == 1)
        val user = users.first()

        user.assertEqualsTo(expectedUser)

        Unit
    }

    @Test
    fun describe_user_by_username_muti() = runBlocking {
        val expectedName = "super fancy user name"
        val inputUsers = (1..20).map {
            SafeUser(
                genRandomString(5),
                expectedName,
                genRandomString(10),
                genRandomString(5)
            )
        }
        val expectedUser = inputUsers.map(SafeUser::transform)

        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithName(name: String): List<SafeUser> {
                assertEquals(expectedName, name)
                return inputUsers
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.UserName(expectedName))
        val response = exchangeRequest(request)

        val result = response.result as DescribeUserResponse.Result.MultiUserResult
        val users = result.multiUserResult.result

        assertEquals(expectedUser, users)

        Unit
    }

    @Test
    fun describe_user_by_username_none() = runBlocking {
        val expectedUserName = "oh no there's no such user"
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithName(name: String): List<SafeUser> {
                assertEquals(expectedUserName, name)
                return listOf()
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.UserName(expectedUserName))
        val response = exchangeRequest(request)

        assert(response.result is DescribeUserResponse.Result.UserNotFound)

        Unit
    }

    @Test
    fun describe_user_db_exception() = runBlocking {
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithId(id: String): SafeUser? {
                throw Exception("OOOOOOOOOOOOO NO")
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.ID(genRandomString(5)))
        val response = exchangeRequest(request)

        val error = response.result as DescribeUserResponse.Result.SystemErrorMessage
        assertEquals("database error", error.systemErrorMessage)

        Unit
    }

    @Test
    fun describe_user_db_error() = runBlocking {
        val dbAccess = object : TestDBAccess() {
            override suspend fun getUserWithId(id: String): SafeUser? {
                throw Error("OOOOOOOOOOOOO NO")
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = DescribeUserRequest(DescribeUserRequest.By.ID(genRandomString(5)))
        val response = exchangeRequest(request)

        val error = response.result as DescribeUserResponse.Result.SystemErrorMessage
        assertEquals("unexpected error", error.systemErrorMessage)

        Unit
    }

    private fun createTestUser(): SafeUser {
        return SafeUser(
            userId = "userId-xxjkadfjakdjlf-${genRandomString(5)}",
            userName = "super user name-${genRandomString(5)}",
            signature = "lalala signature-${genRandomString(5)}",
            avatarKey = "avatar-${genRandomString(5)}"
        )
    }

    private suspend fun exchangeRequest(request: DescribeUserRequest): DescribeUserResponse {
        val response = eventBus.requestAwait<ByteArray>(eventBusAddress, request.protoMarshal())
        return DescribeUserResponse.protoUnmarshal(response.body())
    }

    private lateinit var target: DescribeUserInterface

    private suspend fun prepareAndRegistryInterface(dbAccess: DBAccess) {
        val config = Config { addSpec(MasterConfigSpec.DescribeUserConfig) }
            .from.map.kv(mapOf("describe_user.event_bus_name" to eventBusAddress))

        val sharedRuntimeData = SharedRuntimeData(vertx, config, dbAccess)
        val target = DescribeUserInterface(sharedRuntimeData)
        initializeAndRegistryTarget(target)
    }

    private suspend fun initializeAndRegistryTarget(target: DescribeUserInterface) {
        target.bind(eventBus)
        this.target = target
    }

    private fun com.kcibald.services.user.proto.User.assertEqualsTo(user: SafeUser) {
        assertEquals(this.userId, user.userId)
        assertEquals(this.userName, user.userName)
        assertEquals(this.signature, user.signature)
        assertEquals(this.avatarKey, user.avatarKey)
        assertEquals(this.urlKey, user.urlKey)
    }

}