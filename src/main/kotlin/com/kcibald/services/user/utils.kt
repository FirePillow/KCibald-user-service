package com.kcibald.services.user

import com.kcibald.services.user.handlers.EventResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

private object Utils
private val utilLogger = LoggerFactory.getLogger(Utils::class.java)!!

private val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
private val random = Random()
internal fun genRandomString(length: Int): String {
    val sb = StringBuilder(length)
    repeat(length) {
        sb.append(chars[random.nextInt(chars.size)])
    }
    return sb.toString()
}

internal inline fun <IN> MessageConsumer<IN>.coroutineHandler(
    vertx: Vertx,
    unexpectedFailureMessage: EventResult? = null,
    crossinline block: suspend (Message<IN>) -> EventResult
) {
    this.handler {
        GlobalScope.launch(vertx.dispatcher()) {
            try {
                val result = block(it)
                result.reply(it)
            } catch (e: Exception) {
                utilLogger.warn("unexpected failure at coroutineHandler, exception: $e", e)
                if (unexpectedFailureMessage != null) {
                    unexpectedFailureMessage.reply(it)
                } else {
                    it.fail(500, "unexpected")
                }
            }
        }
    }
}
