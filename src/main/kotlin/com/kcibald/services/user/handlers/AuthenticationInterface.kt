package com.kcibald.services.user.handlers

import at.favre.lib.crypto.bcrypt.BCrypt
import com.kcibald.services.user.UserServiceVerticle
import com.kcibald.services.user.coroutineHandler
import com.kcibald.utils.d
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf

internal class AuthenticationInterface(verticle: UserServiceVerticle) : ServiceInterface(verticle) {
    private val logger = LoggerFactory.getLogger(AuthenticationInterface::class.java)
    private val DBFailureEventResult = FailureEventResult(503, "database failure")

    override suspend fun bind(eventBus: EventBus) {
        val consumer = eventBus.consumer<JsonObject>("kcibald.user.authentication")
        consumer.coroutineHandler(verticle.vertx) {
            logger.d { "authentication inbound" }
            val request = it.body()
            val email: String = request["email"]
            val password: String = request["password"]

            logger.d { "accessing db for user with email $email" }
            val dbResult =
                try {
                    this@AuthenticationInterface.verticle.dbaccess.getUserAndPasswordWithEmail(email)
                } catch (e: Exception) {
                    logger.warn("dbaccess failure exception: $e", e)
                    return@coroutineHandler DBFailureEventResult
                }

            if (dbResult != null) {
                logger.d { "user with email $email exists, checking password and authority" }
                val (user, hash) = dbResult
                try {
                    val result = verticle.vertx.executeBlockingAwait<BCrypt.Result> { future ->
                        //                        exception will be catch from vertx
                        future.complete(
                            BCrypt.verifyer().verify(password.toByteArray(), hash)
                        )
                    }!!
                    if (result.verified) {
                        logger.d { "user with email $email have input correct password, continue" }
                        return@coroutineHandler JsonEventResult(
                            jsonObjectOf(
                                "verified" to true,
                                "user" to user.jsonObject
                            )
                        )
                    }
                } catch (e: Exception) {
                    logger.info("authentication failed for user ${user.user_id}, exception $e", e)
                    return@coroutineHandler JsonEventResult(jsonObjectOf("error" to true))
                }
            }

            logger.d { "user $email failed to authenticate" }
            return@coroutineHandler JsonEventResult(jsonObjectOf("verified" to false))
        }
    }

}