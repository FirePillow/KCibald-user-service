package com.kcibald.services.user.dao

import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.services.user.genRandomString
import com.mongodb.MongoWriteException
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.mongo.insertAwait
import java.util.*

private val base64Encoder = Base64.getEncoder()!!

internal suspend fun DefaultDBAccess.insertNewUser(
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
//                    logger.d { "url key collision for user with name $userName, collide url key: $currentUrlKey" }
//                    potential flood attack !
//                    if the attacker have drain all the possible combination,
//                    this will be spin for 1000 times
//                    val previous = currentUrlKey
                    currentUrlKey = urlKey + "-" + genRandomString(3)
//                    logger.d { "spinning from $previous -> $currentUrlKey" }
                } else {
                    throw DBAccess.URLKeyDuplicationException
                }
            } else {
                throw e
            }
        }
    }

    if (documentId == null) {
        val message =
            "url key spin failed for user name $userName, target url key $urlKey, current iteration $currentUrlKey"
//        logger.warn(message)
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
