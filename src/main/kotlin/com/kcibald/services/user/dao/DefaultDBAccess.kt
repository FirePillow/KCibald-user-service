package com.kcibald.services.user.dao

import com.kcibald.services.user.*
import com.kcibald.utils.d
import com.kcibald.utils.i
import com.kcibald.utils.immutable
import com.kcibald.utils.w
import com.mongodb.MongoWriteException
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.mongo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

internal class DefaultDBAccess constructor(private val vertx: Vertx, private val config: Config) :
    DBAccess {

    internal val dbClient =
        MongoClient.createShared(
            vertx,
            JsonObject(config[MasterConfigSpec.mongo_config])
        )

    private val logger =
        LoggerFactory.getLogger(DefaultDBAccess::class.java)

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

    override suspend fun initialize() {
        logger.info("DefaultDBAccess initializing, verifying database integrity")
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
        logger.i { "DefaultDBAccess initialization complete" }
    }

    override suspend fun getUserWithId(id: String): SafeUser? {
        val dbId = encodeDBIDFromUserId(id)
        val query = JsonObject(Collections.singletonMap("_id", dbId) as Map<String, Any>)
        val jsonObject = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return jsonObject?.let { SafeUser.fromDBJson(it) }
    }

    override suspend fun getUserWithName(name: String): List<SafeUser> {
        val query = JsonObject(
            Collections.singletonMap(
                userNameKey,
                name
            ) as Map<String, Any>
        )
        val jsonObject = dbClient.findAwait(userCollectionName, query)
        return jsonObject.map { SafeUser.fromDBJson(it) }
    }

    override suspend fun getUserWithUrlKey(urlKey: String): SafeUser? {
        val query = JsonObject(
            Collections.singletonMap(
                urlKeyKey,
                urlKey
            ) as Map<String, Any>
        )
        val dbResult = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return dbResult?.let { SafeUser.fromDBJson(it) }
    }

    override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? {
        val query = JsonObject(
            Collections.singletonMap(
                "$emailKey.$emailAddressKey",
                email
            ) as Map<String, Any>
        )
        val field = (json {
            obj(
                passwordHashKey to 1
            )
        }).mergeIn(userFieldProjection)
        return dbClient
            .findOneAwait(userCollectionName, query, field)
            ?.let {
                SafeUser.fromDBJson(it) to Base64.getDecoder().decode(
                    it.getString(
                        passwordHashKey
                    )
                )
            }
    }

    override suspend fun updateUserName(
        to: String,
        tolerateUrlKeySpin: Boolean,
        userId: String?,
        urlKey: String?
    ): Boolean {
//        step 1:
//        update user name
        val original = dbClient.findOneAndUpdateAwait(
            userCollectionName,
            makeUrlKeyOrUserIdQuery(urlKey, userId),
            jsonObjectOf(
                "\$set" to jsonObjectOf(
                    userNameKey to to
                )
            )
        )
            ?.let { SafeUser.fromDBJson(it) }
            ?: return false

        if (tolerateUrlKeySpin) {
//        step 2:
//        update url key
            val query = makeUrlKeyOrUserIdQuery(null, original.userId)

            val originalUrlKey = userNameToURLKey(to)
            var currentUrlKey = originalUrlKey
            for (i in 1..1000) {
                try {
                    val result = dbClient.updateCollectionAwait(
                        userCollectionName,
                        query,
//                    if racing, overwrite it
                        jsonObjectOf(
                            "\$set" to jsonObjectOf(
                                userNameKey to to,
                                urlKeyKey to currentUrlKey
                            )
                        )
                    )

                    assert(result.docMatched != 0.toLong())
                    return result.docModified == 1.toLong()
                } catch (e: MongoWriteException) {
                    if (e.error.code == 11000) {
                        if (tolerateUrlKeySpin) {
                            val previous = currentUrlKey
                            currentUrlKey = originalUrlKey + "-" + genRandomString(3)
                            logger.d { "spinning from $previous -> $currentUrlKey" }
                        } else {
                            throw DBAccess.URLKeyDuplicationException
                        }
                    } else {
                        throw e
                    }
                }
            }

            val message =
                "url key spin failed for user ${userId ?: urlKey} when updating user name," +
                        " target url key $urlKey, current iteration $currentUrlKey"
            logger.warn(message)
            throw NoStackTraceThrowable(message)
        } else {
            return true
        }
    }

    override suspend fun updateSignature(
        to: String,
        userId: String?,
        urlKey: String?
    ): Boolean {
        val query = makeUrlKeyOrUserIdQuery(urlKey, userId)

        val result = dbClient.updateCollectionAwait(
            userCollectionName,
            query,
            jsonObjectOf("\$set" to jsonObjectOf(signatureKey to to))
        )

        return result.docModified == 1.toLong()
    }

    override suspend fun updateAvatar(
        to: String,
        userId: String?,
        urlKey: String?
    ): Boolean {
        val query = makeUrlKeyOrUserIdQuery(urlKey, userId)

        val result = dbClient.updateCollectionAwait(
            userCollectionName,
            query,
            jsonObjectOf("\$set" to jsonObjectOf(avatarFileKey to to))
        )

        return result.docMatched == 1.toLong()
    }

    override suspend fun updatePassword(
        before: String,
        after: String,
        userId: String?,
        urlKey: String?
    ): Boolean {
        val query = makeUrlKeyOrUserIdQuery(urlKey, userId)

        val original = dbClient.findOneAwait(
            userCollectionName,
            query,
            jsonObjectOf(passwordHashKey to 1)
        ) ?: return false

        val originalId: String = original["_id"]
        val originalPassword: String = original[passwordHashKey]
        val hash = Base64.getDecoder().decode(originalPassword)

        if (!passwordMatches(vertx, hash, before))
            return false

        val result = dbClient.updateCollectionAwait(
            userCollectionName,
            jsonObjectOf(
                "_id" to originalId,
                passwordHashKey to originalPassword
            ),
            jsonObjectOf(
                "\$set" to jsonObjectOf(
                    passwordHashKey to hashPassword(
                        vertx,
                        after
                    )
                )
            )
        )

        return result.docMatched == 1.toLong()
    }

    private fun makeUrlKeyOrUserIdQuery(urlKey: String?, userId: String?): JsonObject {
        if (urlKey == null && userId == null)
            throw IllegalArgumentException("at least urlKey or userId should be non-null")

        return if (urlKey != null)
            jsonObjectOf(urlKeyKey to urlKey)
        else
            jsonObjectOf("_id" to encodeDBIDFromUserId(userId!!))
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            dbClient.close()
        }
    }

}