package com.kcibald.services.user.dao

import com.kcibald.services.user.MasterConfigSpec
import com.kcibald.services.user.UserServiceVerticle
import com.kcibald.utils.immutable
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.ext.mongo.createCollectionAwait
import io.vertx.kotlin.ext.mongo.findOneAwait
import io.vertx.kotlin.ext.mongo.getCollectionsAwait
import java.util.*

internal class DBAccess(verticle: UserServiceVerticle) {

    private val dbClient =
        MongoClient.createShared(
            verticle.vertx,
            JsonObject(verticle.parsedConfig[MasterConfigSpec.db_config])
        )

    private val logger = LoggerFactory.getLogger(DBAccess::class.java)

    private val userCollectionName = "kcibald-user"

    private val userFieldJson = json {
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
        checkCollectionExists(setOf(userCollectionName))
        logger.info("DBAccess initialization complete")
    }

    suspend fun getUserWithId(id: String): SafeUserInternal? {
        assert(id.length == 5)
        val query = JsonObject(Collections.singletonMap(userIdKey, id) as Map<String, Any>)
        val jsonObject = dbClient.findOneAwait(userCollectionName, query, JsonObject())
        return jsonObject?.let(::SafeUserInternal)
    }

    suspend fun getUserWithName(id: String): SafeUserInternal? {
        assert(id.length == 5)
        val query = JsonObject(Collections.singletonMap(userIdKey, id) as Map<String, Any>)
        val jsonObject = dbClient.findOneAwait(userCollectionName, query, JsonObject())
        return jsonObject?.let(::SafeUserInternal)
    }

    suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUserInternal, ByteArray>? {
        val query = json {
            obj(
                emailKey to obj(
                    emailAddressKey to email
                )
            )
        }
        val field = (json {
            obj(
                passwordHashKey to 1
            )
        }).mergeIn(userFieldJson)
        return dbClient
            .findOneAwait(userCollectionName, query, field)
            ?.let {
                SafeUserInternal(it) to Base64.getDecoder().decode(it.getString(passwordHashKey))
            }
    }

    private suspend fun checkCollectionExists(collectionNames: Set<String>) {
        val existsNames = dbClient.getCollectionsAwait()
        for (collectionName in collectionNames) {
            if (existsNames.contains(collectionName)) {
                logger.info("collection with name $collectionName exists, continue")
            } else {
                logger.warn("collection with name $collectionName do not exist, creating collection")
                dbClient.createCollectionAwait(collectionName)
                logger.info("collection $collectionName created")
            }
        }
    }

}