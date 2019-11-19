package com.kcibald.services.user.handlers

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.dao.DBAccess
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.SafeUpdateOperation
import com.kcibald.services.user.proto.UpdateUserInfoRequest
import com.kcibald.services.user.proto.UpdateUserInfoRequest.QueryBy
import com.kcibald.services.user.proto.UpdateUserInfoRequest.Target
import com.kcibald.services.user.proto.UpdateUserInfoResponse
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.junit5.VertxExtension
import io.vertx.kotlin.core.eventbus.requestAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class UpdateUserInfoInterfaceTest {

    abstract class TestDBAccess : DBAccess {
        override suspend fun initialize() {}

        override suspend fun getUserWithId(id: String): SafeUser? = failInternal()

        override suspend fun getUserWithName(name: String): List<SafeUser> = failInternal()

        override suspend fun getUserWithUrlKey(urlKey: String): SafeUser? = failInternal()

        override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? = failInternal()

        override suspend fun updateUserName(
            to: String,
            tolerateUrlKeySpin: Boolean,
            userId: String?,
            urlKey: String?
        ): Boolean = failInternal()

        override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean = failInternal()

        override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean = failInternal()

        override suspend fun updatePassword(before: String, after: String, userId: String?, urlKey: String?): Boolean =
            failInternal()

        override suspend fun close() {}

        private fun failInternal(): Nothing {
            fail<Unit>()
            throw AssertionError()
        }
    }

    @BeforeEach
    fun setup(vertx: Vertx) {
        this.vertx = vertx
        this.eventBus = vertx.eventBus()
    }

    private lateinit var vertx: Vertx
    private lateinit var eventBus: EventBus
    private val eventBusAddress = "_update_user_info_interface_test"

    @Test
    fun updateUserName_by_id_normal() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedNameTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateUserName(
                to: String,
                tolerateUrlKeySpin: Boolean,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                assertEquals(expectedNameTo, to)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.UserName(expectedNameTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateUserName_by_id_fail() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedNameTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateUserName(
                to: String,
                tolerateUrlKeySpin: Boolean,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                assertEquals(expectedNameTo, to)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.UserName(expectedNameTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updateUserName_by_urlKey_normal() = runBlocking {
        val expectedUserUrlKey = "kkkkkk"
        val expectedNameTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateUserName(
                to: String,
                tolerateUrlKeySpin: Boolean,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedUserUrlKey, urlKey)
                assertNull(userId)
                assertEquals(expectedNameTo, to)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlKey), Target.UserName(expectedNameTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateUserName_by_urlKey_fail() = runBlocking {
        val expectedUserUrlKey = "kkkkkk"
        val expectedNameTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateUserName(
                to: String,
                tolerateUrlKeySpin: Boolean,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedUserUrlKey, urlKey)
                assertNull(userId)
                assertEquals(expectedNameTo, to)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlKey), Target.UserName(expectedNameTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updateAvatar_by_id_normal() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedAvatarTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedAvatarTo, to)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.AvatarKey(expectedAvatarTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateAvatar_by_id_fail() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedAvatarTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedAvatarTo, to)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.AvatarKey(expectedAvatarTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updateAvatar_by_urlKey_normal() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedAvatarTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedAvatarTo, to)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlkey), Target.AvatarKey(expectedAvatarTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateAvatar_by_urlKey_fail() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedAvatarTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedAvatarTo, to)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlkey), Target.AvatarKey(expectedAvatarTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updateUserName_error_fail() = runBlocking {
        val expectedUserUrlKey = "kkkkkk"
        val expectedNameTo = "newName"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateUserName(
                to: String,
                tolerateUrlKeySpin: Boolean,
                userId: String?,
                urlKey: String?
            ): Boolean {
                throw AssertionError("OMG I MESSED UP")
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlKey), Target.UserName(expectedNameTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.INTERNAL_ERROR, response.responseType)
    }

    @Test
    fun updateSig_by_id_normal() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedSigTo = "newSig"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedSigTo, to)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return true
            }

        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.Signature(expectedSigTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateSig_by_id_fail() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedSigTo = "newSig"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedSigTo, to)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return false
            }

        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UserId(expectedUserId), Target.Signature(expectedSigTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updateSig_by_urlKey_normal() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedSigTo = "newSig"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedSigTo, to)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return true
            }

        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlkey), Target.Signature(expectedSigTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updateSig_by_urlKey_fail() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedSigTo = "newSig"

        val dbAccess = object : TestDBAccess() {
            override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean {
                assertEquals(expectedSigTo, to)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return false
            }

        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(QueryBy.UrlKey(expectedUserUrlkey), Target.Signature(expectedSigTo))
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updatePassword_by_urlKey_normal() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedPasswordFrom = "oldPass"
        val expectedPasswordTo = "newPass"

        val dbAccess = object : TestDBAccess() {

            override suspend fun updatePassword(
                before: String,
                after: String,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedPasswordFrom, before)
                assertEquals(expectedPasswordTo, after)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(
            QueryBy.UrlKey(expectedUserUrlkey),
            Target.Password(SafeUpdateOperation(expectedPasswordFrom, expectedPasswordTo))
        )
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updatePassword_by_urlKey_fail() = runBlocking {
        val expectedUserUrlkey = "kkkkkk"
        val expectedPasswordFrom = "oldPass"
        val expectedPasswordTo = "newPass"

        val dbAccess = object : TestDBAccess() {

            override suspend fun updatePassword(
                before: String,
                after: String,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedPasswordFrom, before)
                assertEquals(expectedPasswordTo, after)
                assertEquals(expectedUserUrlkey, urlKey)
                assertNull(userId)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(
            QueryBy.UrlKey(expectedUserUrlkey),
            Target.Password(SafeUpdateOperation(expectedPasswordFrom, expectedPasswordTo))
        )
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    @Test
    fun updatePassword_by_id_normal() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedPasswordFrom = "oldPass"
        val expectedPasswordTo = "newPass"

        val dbAccess = object : TestDBAccess() {

            override suspend fun updatePassword(
                before: String,
                after: String,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedPasswordFrom, before)
                assertEquals(expectedPasswordTo, after)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return true
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(
            QueryBy.UserId(expectedUserId),
            Target.Password(SafeUpdateOperation(expectedPasswordFrom, expectedPasswordTo))
        )
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.SUCCESS, response.responseType)
    }

    @Test
    fun updatePassword_by_id_fail() = runBlocking {
        val expectedUserId = "kkkkkk"
        val expectedPasswordFrom = "oldPass"
        val expectedPasswordTo = "newPass"

        val dbAccess = object : TestDBAccess() {

            override suspend fun updatePassword(
                before: String,
                after: String,
                userId: String?,
                urlKey: String?
            ): Boolean {
                assertEquals(expectedPasswordFrom, before)
                assertEquals(expectedPasswordTo, after)
                assertEquals(expectedUserId, userId)
                assertNull(urlKey)
                return false
            }
        }

        prepareAndRegistryInterface(dbAccess)

        val request = UpdateUserInfoRequest(
            QueryBy.UserId(expectedUserId),
            Target.Password(SafeUpdateOperation(expectedPasswordFrom, expectedPasswordTo))
        )
        val response = sendRequest(request)

        assertEquals(UpdateUserInfoResponse.GeneralResponseTypes.FAILURE_UNSAFE_UPDATE, response.responseType)
    }

    suspend fun prepareAndRegistryInterface(dbAccess: DBAccess): UpdateUserInfoInterface {
        val config = Config { addSpec(MasterConfigSpec.UpdateUserInfoConfig) }
            .from().map.flat(mapOf("update_user.event_bus_name" to eventBusAddress))
        val runtimeData = SharedRuntimeData(vertx, config, dbAccess)
        val target = UpdateUserInfoInterface(runtimeData)
        initializeAndSetTarget(target)
        return target
    }

    private var target: UpdateUserInfoInterface? = null

    suspend fun initializeAndSetTarget(target: UpdateUserInfoInterface) {
        this.target = target
        target.bind(eventBus)
    }

    suspend fun sendRequest(updateUserInfoRequest: UpdateUserInfoRequest): UpdateUserInfoResponse {
        val bytes = updateUserInfoRequest.protoMarshal()
        val response = eventBus.requestAwait<ByteArray>(eventBusAddress, bytes)
        return UpdateUserInfoResponse.protoUnmarshal(response.body())
    }

}