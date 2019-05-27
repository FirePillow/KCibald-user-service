package com.kcibald.services.user

import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

private val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()
private val random = Random()
internal fun genRandomString(length: Int): String {
    val sb = StringBuilder(length)
    repeat(length) {
        sb.append(chars[random.nextInt(chars.size)])
    }
    return sb.toString()
}

internal inline fun <T> MessageConsumer<T>.coroutineHandler(
    vertx: Vertx,
    crossinline block: suspend (Message<T>) -> Unit
) {
    this.handler {
        GlobalScope.launch(vertx.dispatcher()) {
            block(it)
        }
    }
}
