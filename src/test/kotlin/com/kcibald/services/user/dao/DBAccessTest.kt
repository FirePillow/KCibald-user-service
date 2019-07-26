package com.kcibald.services.user.dao

import com.kcibald.services.user.genRandomString
import com.kcibald.services.user.hashPassword
import com.kcibald.services.user.load
import com.kcibald.services.user.passwordMatches
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
    fun initialize() = runBlocking {
        dbAccess.initialize()
        val collections = dbClient.getCollectionsAwait()
        assertTrue(collections.contains(dbAccess.userCollectionName))
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

    @Test
    fun updateUserName() = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val originalUserName = "original"

        val id = dbAccess.insertNewUser(
            userName = originalUserName,
            signature = "",
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0),
            urlKey = userNameToURLKey(originalUserName)
        )

        createNoiseDocument()

        val newUserName = "new"

        dbAccess.updateUserName(newUserName, userId = id.userId)

        val newUser = dbAccess.getUserWithId(id.userId) ?: fail()

        assertEquals(newUserName, newUser.userName)
        assertEquals(userNameToURLKey(newUserName), newUser.urlKey)

        Unit
    }

    @Test
    fun updateUserName_overlap() = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val overLay = "overlay"

        val overLayOriUrlKey = userNameToURLKey(overLay)

        dbAccess.insertNewUser(
            userName = overLay,
            schoolEmail = "",
            rawPassword = ByteArray(0),
            urlKey = overLayOriUrlKey,
            avatarKey = ""
        )

        val originalUserName = "original"

        val id = dbAccess.insertNewUser(
            userName = originalUserName,
            signature = "",
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0),
            urlKey = userNameToURLKey(originalUserName)
        )

        createNoiseDocument()

        assertTrue(dbAccess.updateUserName(overLay, userId = id.userId))

        val newUser = dbAccess.getUserWithId(id.userId) ?: fail()

        assertEquals(overLay, newUser.userName)
        assertNotEquals(overLayOriUrlKey, newUser.urlKey)
        assertTrue(
            newUser.urlKey.startsWith("$overLayOriUrlKey-"),
            "incorrect url key spin replacement, get ${newUser.urlKey}"
        )

        Unit
    }

    @Test
    fun updateSignature() = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val original = dbAccess.insertNewUser(
            userName = "user-name",
            urlKey = "user-name",
            signature = "before",
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        createNoiseDocument()

        val answer = "answer"
        assertTrue(dbAccess.updateSignature(answer, userId = original.userId))

        val after = dbAccess.getUserWithId(original.userId) ?: fail()

        assertEquals(answer, after.signature)

        Unit
    }

    @Test
    fun updateAvatar() = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val original = dbAccess.insertNewUser(
            userName = "user-name",
            urlKey = "user-name",
            avatarKey = "before",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        createNoiseDocument()

        val answer = "answer"
        assertTrue(dbAccess.updateAvatar(answer, userId = original.userId))

        val after = dbAccess.getUserWithId(original.userId) ?: fail()

        assertEquals(answer, after.avatarKey)

        Unit
    }

    @Test
    fun updateAvatar_unsafe() = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val original = dbAccess.insertNewUser(
            userName = "",
            urlKey = "",
            avatarKey = "",
            schoolEmail = "",
            rawPassword = ByteArray(0)
        )

        createNoiseDocument()

        assertFalse(dbAccess.updateAvatar("before", "after", original.userId))

        Unit
    }

    @Test
    fun updatePassword(vertx: Vertx, vertxTestContext: VertxTestContext) = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val rawOriginalPassword = "password*@^!&^&^&^@&#^|"
        val rawOriginal = hashPassword(vertx, rawOriginalPassword)

        val schoolEmail = "school@email"

        val user = dbAccess.insertNewUser(
            userName = "",
            urlKey = "",
            avatarKey = "",
            schoolEmail = schoolEmail,
            rawPassword = rawOriginal
        )

        createNoiseDocument()

        val newPassword = "newPassword*&*^#@"

        assertTrue(
            dbAccess.updatePassword(
                rawOriginalPassword,
                newPassword,
                userId = user.userId
            )
        )

        val (_, updatedPassword) = dbAccess.getUserAndPasswordWithEmail(schoolEmail) ?: fail()

        assertTrue(passwordMatches(vertx, updatedPassword, newPassword))

        vertxTestContext.completeNow()

        Unit
    }

    @Test
    fun updatePassword_unsafe(vertx: Vertx, vertxTestContext: VertxTestContext) = runBlocking {
        dbAccess.initialize()

        createNoiseDocument()

        val rawOriginalPassword = "password*@^!&^&^&^@&#^|"

        val user = dbAccess.insertNewUser(
            userName = "",
            urlKey = "",
            avatarKey = "",
            schoolEmail = "",
            rawPassword = hashPassword(vertx, rawOriginalPassword)
        )

        createNoiseDocument()

        val incorrect = "incorrect"

        val newPassword = "newPassword*&*^#@"

        assertFalse(
            dbAccess.updatePassword(
                incorrect,
                newPassword,
                userId = user.userId
            )
        )

        vertxTestContext.completeNow()

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