package com.kcibald.services.user

import com.kcibald.services.user.dao.DBAccess
import com.kcibald.services.user.handlers.AuthenticationInterface
import com.kcibald.services.user.handlers.DescribeUserInterface
import com.kcibald.utils.i
import com.uchuhimo.konf.Config
import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserServiceVerticle : CoroutineVerticle() {

    private val logger = LoggerFactory.getLogger(UserServiceVerticle::class.java)

    override suspend fun start() {
        logger.i { "user service launching, deployment Id: $deploymentID" }

        logger.i { "loading config" }
        val config = withContext(Dispatchers.IO) {
            load(super.config)
        }
        logger.i { "config loaded, ${config.toMap()}" }

        logger.i { "initializing database" }
        dbaccess = DBAccess(this.vertx, config)
        dbaccess.initialize()
        logger.i { "database initialization complete" }

        sharedRuntimeData = SharedRuntimeData(this, config, dbaccess, this.deploymentID)

        logger.i { "binding services" }
        AuthenticationInterface(sharedRuntimeData).bind(vertx.eventBus())
        DescribeUserInterface(sharedRuntimeData).bind(vertx.eventBus())
    }

    internal lateinit var dbaccess: DBAccess

    internal lateinit var sharedRuntimeData: SharedRuntimeData

    override suspend fun stop() {
    }
}

internal class SharedRuntimeData(
    val verticle: UserServiceVerticle,
    val config: Config,
    val dbAccess: DBAccess,
    val deploymentID: String
) {
    val vertx: Vertx
        get() = verticle.vertx
}