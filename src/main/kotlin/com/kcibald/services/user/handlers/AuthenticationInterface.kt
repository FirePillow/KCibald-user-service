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

    override suspend fun bind(eventBus: EventBus) {
        val consumer = eventBus.consumer<JsonObject>("kcibald.user.authentication")
        consumer.coroutineHandler(verticle.vertx) {
            logger.d { "authentication inbound" }
            val request = it.body()
            val email: String = request["email"]
            val password: String = request["password"]
            logger.d { "accessing db for user with email $email" }
            val dbResult = this@AuthenticationInterface.verticle.dbaccess.getUserAndPasswordWithEmail(email)
            if (dbResult != null) {
                logger.d { "user with email $email exists, checking password and authority" }
                val (user, hash) = dbResult
                try {
                    val result = verticle.vertx.executeBlockingAwait<BCrypt.Result> { future ->
                        try {
                            future.complete(BCrypt.verifyer().verify(password.toByteArray(), hash))
                        } catch (e: Throwable) {
                            future.fail(e)
                        }
                    }!!
                    if (result.verified) {
                        logger.d { "user with email $email have input correct password, continue" }
                        it.reply(
                            jsonObjectOf(
                                "verified" to true,
                                "user" to user.jsonObject
                            )
                        )
                    }
                } catch (e: Exception) {
                    logger.info("authentication failed for user ${user.user_id}, exception $e", e)
                    it.reply(jsonObjectOf("error" to true))
                }
            }
            logger.d { "user $email failed to authenticate" }
            it.reply("verified" to false)
        }

    }

}