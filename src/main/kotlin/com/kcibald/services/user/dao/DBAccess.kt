package com.kcibald.services.user.dao

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.encodeDBIDFromUserId
import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.utils.i
import com.kcibald.utils.immutable
import com.kcibald.utils.w
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

internal class DBAccess(vertx: Vertx, private val config: Config) {

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
        logger.i { "DBAccess initialization complete" }
    }

    suspend fun getUserWithId(id: String): SafeUserInternal? {
        val dbId = encodeDBIDFromUserId(id)
        val query = JsonObject(Collections.singletonMap("_id", dbId) as Map<String, Any>)
        val jsonObject = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return jsonObject?.let(::SafeUserInternal)
    }

    suspend fun getUserWithName(name: String): List<SafeUserInternal> {
        val query = JsonObject(Collections.singletonMap(userNameKey, name) as Map<String, Any>)
        val jsonObject = dbClient.findAwait(userCollectionName, query)
        return jsonObject.map(::SafeUserInternal)
    }

    suspend fun getUserWithUrlKey(urlKey: String): SafeUserInternal? {
        val query = JsonObject(Collections.singletonMap(urlKeyKey, urlKey) as Map<String, Any>)
        val dbResult = dbClient.findOneAwait(userCollectionName, query, userFieldProjection)
        return dbResult?.let(::SafeUserInternal)
    }

    suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUserInternal, ByteArray>? {
        val query = JsonObject(Collections.singletonMap("$emailKey.$emailAddressKey", email) as Map<String, Any>)
        val field = (json {
            obj(
                passwordHashKey to 1
            )
        }).mergeIn(userFieldProjection)
        return dbClient
            .findOneAwait(userCollectionName, query, field)
            ?.let {
                SafeUserInternal(it) to Base64.getDecoder().decode(it.getString(passwordHashKey))
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
        schoolEmailVerified: Boolean = false,
        personalEmail: String? = null,
        personalEmailVerified: Boolean = false
    ): String {

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

        val documentId = dbClient.insertAwait(
            userCollectionName,
            jsonObjectOf(
                userNameKey to userName,
                urlKeyKey to urlKey,
                signatureKey to signature,
                avatarFileKey to avatarKey,
                emailKey to emailJson,
                passwordHashKey to passwordValue
            )
        )!!
        return encodeUserIdFromDBID(documentId)
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun close() {
        withContext(Dispatchers.IO) {
            dbClient.close()
        }
    }

}