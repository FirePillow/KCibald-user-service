package com.kcibald.services.user.dao

import com.kcibald.services.user.genRandomString
import com.kcibald.services.user.load
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.ext.mongo.dropCollectionAwait
import io.vertx.kotlin.ext.mongo.getCollectionsAwait
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*
import kotlin.collections.LinkedHashSet

@ExtendWith(VertxExtension::class)
internal class DBAccessTest {

    @BeforeEach
    fun setUpConfig(vertx: Vertx) = runBlocking {
        config = load(JsonObject())
        dbAccess = DBAccess(vertx, config)
        dbClient = dbAccess.dbClient
        dbClient.dropCollectionAwait(dbAccess.userCollectionName)
    }

    @AfterEach
    fun tearDown() = runBlocking {
        dbClient.dropCollectionAwait(dbAccess.userCollectionName)
        dbAccess.close()
    }

    lateinit var config: Config
    lateinit var dbAccess: DBAccess
    lateinit var dbClient: MongoClient
    val random = Random()

    @Test
    fun userCollectionName(vertx: Vertx, context: VertxTestContext) {
        val dbAccess = DBAccess(vertx, config)
        assertEquals("users_collection", dbAccess.userCollectionName)
        context.completeNow()
    }

    @Test
    fun initialize(vertx: Vertx, context: VertxTestContext) = runBlocking {
        dbAccess.initialize()
        val collections = dbClient.getCollectionsAwait()
        assertTrue(collections.contains(dbAccess.userCollectionName))
        context.completeNow()

        Unit
    }

    @Test
    fun getUserWithId() = runBlocking {
        dbAccess.initialize()

        val userNameAns = "username#${random.nextInt()}"

        createNoiseDocument()

        val userId = dbAccess.insertNewUser(
            userName = userNameAns,
            urlKey = genRandomString(5),
            avatarKey = "avatar",
            signature = "signature",
            schoolEmail = "school@example.com",
            rawPassword = ByteArray(0)
        ).userId

        createNoiseDocument()

        val user = dbAccess.getUserWithId(userId)!!

        assertEquals(userNameAns, user.userName)

        Unit
    }

    @Test
    fun getUserWithName_single() = runBlocking {
        dbAccess.initialize()

        val userName = "username#${random.nextInt()}"
        val urlKeyAns = "urlKey#${random.nextInt()}"

        createNoiseDocument()

        dbAccess.insertNewUser(
            userName = userName,
            urlKey = urlKeyAns,
            avatarKey = "avatar",
            signature = "signature",
            schoolEmail = "school@example.com",
            rawPassword = ByteArray(0)
        )

        createNoiseDocument()

        val user = dbAccess.getUserWithName(userName).first()
        assertEquals(urlKeyAns, user.urlKey)

        Unit
    }

    @Test
    fun getUserWithName_multi() = runBlocking {
        dbAccess.initialize()

        val random = Random()
        val userName = "username#${random.nextInt()}"
        val urlKeyAns = "urlKey#${random.nextInt()}"

        val signature1 = genRandomString(5)
        val signature2 = genRandomString(5)
        val signature3 = genRandomString(5)
        val signature4 = genRandomString(5)
        val signature5 = genRandomString(5)

        val signatureSet = LinkedHashSet(
            listOf(
                signature1,
                signature2,
                signature3,
                signature4,
                signature5
            )
        )

        createNoiseDocument()

        signatureSet.forEach {
            dbAccess.insertNewUser(
                userName = userName,
                urlKey = urlKeyAns + genRandomString(5),
                avatarKey = "avatar",
                signature = it,
                schoolEmail = "school@example.com",
                rawPassword = ByteArray(0)
            )
        }

        createNoiseDocument()

        val users = dbAccess.getUserWithName(userName)

        assertEquals(signatureSet.size, users.size)

        users.forEach {
            assertEquals(userName, it.userName)
            assertTrue(signatureSet.contains(it.signature))
            signatureSet.remove(it.signature)
        }

        Unit
    }

    @Test
    fun getUserWithUrlKey() = runBlocking {
        dbAccess.initialize()
        val urlKey = "url-key"

        createNoiseDocument()

        val id = dbAccess.insertNewUser(
            userName = "url key",
            urlKey = urlKey,
            signature = "sig",
            avatarKey = "avtar",
            schoolEmail = "email",
            rawPassword = ByteArray(0)
        ).userId

        createNoiseDocument()

        val dbResult = dbAccess.getUserWithUrlKey(urlKey) ?: fail()
        assertEquals(id, dbResult.userId)

        Unit
    }

    @Test
    fun getUserAndPasswordWithEmail() = runBlocking {
        dbAccess.initialize()

        val passwordBytes = ByteArray(10)
        random.nextBytes(passwordBytes)

        val userName = "target_user"
        val email = "target_user@school.com"

        createNoiseDocument()

        val id = dbAccess.insertNewUser(
            userName = userName,
            urlKey = "target_user",
            signature = "I'm the target",
            avatarKey = "",
            schoolEmail = email,
            rawPassword = passwordBytes
        ).userId

        createNoiseDocument()

        val (userInternal, receivedBytes) = dbAccess.getUserAndPasswordWithEmail(email) ?: fail()

        assertTrue(passwordBytes contentEquals receivedBytes)
        assertEquals(userName, userInternal.userName)
        assertEquals(id, userInternal.userId)

        Unit
    }

    @Test
    fun insertUser_conflict_tolerateUrlKeySpin() = runBlocking {
        dbAccess.initialize()

        val userName = "user name"
        val urlKey = "user-name"

        dbAccess.insertNewUser(
            userName,
            urlKey,
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        val after = dbAccess.insertNewUser(
            userName,
            urlKey,
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        assertNotEquals(urlKey, after.urlKey)
        assertTrue(after.urlKey.startsWith(urlKey))

        Unit
    }

    @Test
    fun insertUser_conflict_non_tolerateUrlKeySpin() = runBlocking {
        dbAccess.initialize()

        val userName = "user name"
        val urlKey = "user-name"

        dbAccess.insertNewUser(
            userName,
            urlKey,
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        assertThrows(DBAccess.URLKeyDuplicationException::class.java) {
            runBlocking {
                dbAccess.insertNewUser(
                    userName,
                    urlKey,
                    avatarKey = "",
                    schoolEmail = "",
                    rawPassword = ByteArray(0),
                    tolerateUrlKeySpin = false
                )
            }
        }

        Unit
    }

    private suspend fun createNoiseDocument() {
        repeat(10) {
            val userName = "user#noise#$it#${random.nextInt()}"
            dbAccess.insertNewUser(
                userName = userName,
                urlKey = userName,
                avatarKey = genRandomString(10),
                signature = genRandomString(10),
                schoolEmail = "${genRandomString(10)}@example.com",
                rawPassword = ByteArray(0)
            )

        }
    }
}