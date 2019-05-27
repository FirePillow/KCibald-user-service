package com.kcibald.services.user.handlers

import at.favre.lib.crypto.bcrypt.BCrypt
import com.kcibald.services.user.UserServiceVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class AuthenticationInterface(verticle: UserServiceVerticle): ServiceInterface(verticle){
    private val logger = LoggerFactory.getLogger(AuthenticationInterface::class.java)

    override suspend fun bind(eventBus: EventBus) {
        val consumer = eventBus.consumer<JsonObject>("kcibald.user.authentication")
        consumer.handler {
            GlobalScope.launch(verticle.vertx.dispatcher()) {
                logger.debug("message inbound")
                val request = it.body()
                val email: String = request["email"]
                val password: String = request["password"]
                val dbResult = this@AuthenticationInterface.verticle.dbaccess.getUserAndPasswordWithEmail(email)
                if (dbResult != null) {
                    val (user, hash) = dbResult
                    try {
                        val result = verticle.vertx.executeBlockingAwait<BCrypt.Result> {
                            try {
                                it.complete(BCrypt.verifyer().verify(password.toByteArray(), hash))
                            } catch (e: Throwable) {
                                it.fail(e)
                            }
                        }!!
                        it.reply(
                            jsonObjectOf(
                                "verified" to result.verified,
                                "user" to user.jsonObject
                            )
                        )
                    } catch (e: Exception) {
                        it.reply(jsonObjectOf("error" to true))
                    }

                } else {

                }
            }
        }

    }

}