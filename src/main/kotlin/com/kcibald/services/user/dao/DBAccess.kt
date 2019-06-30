package com.kcibald.services.user.dao

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.encodeDBIDFromUserId
import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.services.user.genRandomString
import com.kcibald.utils.d
import com.kcibald.utils.i
import com.kcibald.utils.immutable
import com.kcibald.utils.w
import com.mongodb.MongoWriteException
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.mongo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

internal class DBAccess(private val vertx: Vertx, private val config: Config) {

    internal val dbClient =
        MongoClient.createShared(
            vertx,
            JsonObject(config[MasterConfigSpec.mongo_config])
        )

    private val logger = LoggerFactory.getLogger(DBAccess::class.java)

    internal val userCollectionName = config[MasterConfigSpec.UserCollection.collection_name]

    private val userFieldProjection = json {
        obj(
            userIdKey to 1,
            userNameKey to 1,
            urlKeyKey to 1,
            signatureKey to 1,
            avatarFileKey to 1
        )
    }.immutable()

    suspend fun initialize() {
        logger.info("DBAccess initializing, verifying database integrity")
        val existsNames = dbClient.getCollectionsAwait()
        if (existsNames.contains(userCollectionName)) {
            logger.i { "user collection (name: $userCollectionName) exists, continue" }
        } else {
            logger.w { "user collection (name: $userCollectionName) do not exist, creating collection" }
            dbClient.createCollectionAwait(userCollectionName)
            logger.i { "user collection (name: $userCollectionName) created" }
        }
        logger.i { "creating index on user collection (name: $userCollectionName)" }
        val indexes = config[MasterConfigSpec.UserCollection.indexes]
        if (indexes.isNotEmpty()) {
            val indexQuery = JsonObject(indexes)
            dbClient.createIndexAwait(userCollectionName, indexQuery)
            logger.i { "index creation success for user collection (name: $userCollectionName), fields ${indexes.keys}" }
        } else {
            logger.i { "indexes empty, not creating indexes" }
        }
        logger.i { "creating unique index on user collection (name: $userCollectionName)" }
        val indexQuery = jsonObjectOf(
            urlKeyKey to 1
        )
        val indexOption = indexOptionsOf().unique(true)
        dbClient.createIndexWithOptionsAwait(userCollectionName, indexQuery, indexOption)
        logger.i { "unique index empty, not creating unique indexes" }
        logger.i { "DBAccess initialization complete" }
    }

    suspend fun getUserWithId(id: String): SafeUser? {
        val dbId = encodeDBIDFromUserId(id)
        val query = JsonObject(Collections.singletonMap("_id", dbId) as Map<String, Any>)
        val jsonObject = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return jsonObject?.let { SafeUser.fromDBJson(it) }
    }

    suspend fun getUserWithName(name: String): List<SafeUser> {
        val query = JsonObject(Collections.singletonMap(userNameKey, name) as Map<String, Any>)
        val jsonObject = dbClient.findAwait(userCollectionName, query)
        return jsonObject.map { SafeUser.fromDBJson(it) }
    }

    suspend fun getUserWithUrlKey(urlKey: String): SafeUser? {
        val query = JsonObject(Collections.singletonMap(urlKeyKey, urlKey) as Map<String, Any>)
        val dbResult = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return dbResult?.let { SafeUser.fromDBJson(it) }
    }

    suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
        val query = JsonObject(Collections.singletonMap("$emailKey.$emailAddressKey", email) as Map<String, Any>)
        val field = (json {
            obj(
                passwordHashKey to 1
            )
        }).mergeIn(userFieldProjection)
        return dbClient
            .findOneAwait(userCollectionName, query, field)
            ?.let {
                SafeUser.fromDBJson(it) to Base64.getDecoder().decode(it.getString(passwordHashKey))
            }
    }

    private val base64Encoder = Base64.getEncoder()!!

    suspend fun insertNewUser(
        userName: String,
        urlKey: String,
        signature: String = "",
        avatarKey: String,
        schoolEmail: String,
        rawPassword: ByteArray,
        tolerateUrlKeySpin: Boolean = true,
        schoolEmailVerified: Boolean = false,
        personalEmail: String? = null,
        personalEmailVerified: Boolean = false
    ): SafeUser {

        val emailJson = jsonArrayOf(
            jsonObjectOf(
                typeKey to schoolEmailKey,
                emailAddressKey to schoolEmail,
                emailVerifiedKey to schoolEmailVerified
            )
        )

        if (personalEmail != null) {
            emailJson.add(
                jsonObjectOf(
                    typeKey to personalEmailKey,
                    emailAddressKey to personalEmail,
                    emailVerifiedKey to personalEmailVerified
                )
            )
        }

//        as the document do not have an _id field, this method WILL NOT return null

        val passwordValue = base64Encoder.encodeToString(rawPassword)

        var documentId: String? = null
        var currentUrlKey = urlKey
        for (i in 1..1000) {
            try {
                documentId = dbClient.insertAwait(
                    userCollectionName,
                    jsonObjectOf(
                        userNameKey to userName,
                        urlKeyKey to currentUrlKey,
                        signatureKey to signature,
                        avatarFileKey to avatarKey,
                        emailKey to emailJson,
                        passwordHashKey to passwordValue
                    )
                )!!
                break
            } catch (e: MongoWriteException) {
                if (e.error.code == 11000) {
                    if (tolerateUrlKeySpin) {
                        logger.d { "url key collision for user with name $userName, collide url key: $currentUrlKey" }
//                    potential flood attack !
//                    if the attacker have drain all the possible combination,
//                    this will be spin for 1000 times
                        val previous = currentUrlKey
                        currentUrlKey = urlKey + "-" + genRandomString(3)
                        logger.d { "spinning from $previous -> $currentUrlKey" }
                    } else {
                        throw URLKeyDuplicationException
                    }
                } else {
                    throw e
                }
            }
        }

        if (documentId == null) {
            val message =
                "url key spin failed for user name $userName, target url key $urlKey, current iteration $currentUrlKey"
            logger.warn(message)
            throw RuntimeException(message)
        }

        val userId = encodeUserIdFromDBID(documentId)
        return SafeUser(
            userId,
            userName,
            signature,
            avatarKey,
            currentUrlKey
        )
    }

    internal object URLKeyDuplicationException : RuntimeException("key duplication occurs", null, false, false)

    suspend fun updateUserName(
        before: String,
        after: String,
        tolerateUrlKeySpin: Boolean = true,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean {
        val query = makeUrlKeyOrUserIdQuery(urlKey, userId)

        query.put(userNameKey, before)

        val originalUrlKey = userNameToURLKey(after)
        var currentUrlKey = originalUrlKey
        for (i in 1..1000) {
            try {
                val result = dbClient.updateCollectionAwait(
                    userCollectionName,
                    query,
                    jsonObjectOf(
                        "\$set" to jsonObjectOf(
                            userNameKey to after,
                            urlKeyKey to currentUrlKey
                        )
                    )
                )

                return result.docModified == 1.toLong()
            } catch (e: MongoWriteException) {
                if (e.error.code == 11000) {
                    if (tolerateUrlKeySpin) {
                        logger.d { "url key collision for user with name $before, collide url key: $currentUrlKey" }
                        val previous = currentUrlKey
                        currentUrlKey = originalUrlKey + "-" + genRandomString(3)
                        logger.d { "spinning from $previous -> $currentUrlKey" }
                    } else {
                        throw URLKeyDuplicationException
                    }
                } else {
                    throw e
                }
            }
        }

        val message =
            "url key spin failed for user name $before when updating user name," +
                    " target url key $urlKey, current iteration $currentUrlKey"
        logger.warn(message)
        throw RuntimeException(message)
    }

    suspend fun updateSignature(
        before: String,
        after: String,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean {
        val query = makeUrlKeyOrUserIdQuery(urlKey, userId)

        query.put(signatureKey, before)

        val result = dbClient.updateCollectionAwait(
            userCollectionName,
            query,
            jsonObjectOf("\$set" to jsonObjectOf(signatureKey to after))
        )

        return result.docModified == 1.toLong()
    }

    private fun makeUrlKeyOrUserIdQuery(urlKey: String?, userId: String?): JsonObject {
        if (urlKey == null && userId == null)
            throw IllegalArgumentException("at least urlKey or userId should be non-null")

        return if (urlKey != null)
            jsonObjectOf(urlKeyKey to urlKey)
        else
            jsonObjectOf("_id" to encodeDBIDFromUserId(userId!!))
    }

    suspend fun close() {
        withContext(Dispatchers.IO) {
            dbClient.close()
        }
    }

}