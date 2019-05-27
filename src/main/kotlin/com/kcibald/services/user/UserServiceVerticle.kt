package com.kcibald.services.user

import com.kcibald.services.user.dao.DBAccess
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val userServiceVerticle = UserServiceVerticle()
    Vertx.vertx().deployVerticleAwait(userServiceVerticle)
    println("hit")
    println(userServiceVerticle.dbaccess.getUserAndPasswordWithEmail("example@example.com"))
    println("hit")
}

class UserServiceVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(UserServiceVerticle::class.java)

    override suspend fun start() {
        logger.info("user service launching, deployment Id: $deploymentID")
        dbaccess = DBAccess(this)
        logger.info("initalizing database access")
        dbaccess.initialize()
    }

    internal lateinit var dbaccess: DBAccess

    internal val dbConfig by lazy {
        val config = this.config
        if (config.isEmpty) {
            jsonObjectOf(
                "mongo_uri" to "mongodb://localhost:27017"
            )
        } else config
    }

    internal val dId
        get() = deploymentID

    override suspend fun stop() {
        super.stop()
    }
}