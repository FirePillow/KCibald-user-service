package com.kcibald.services.user

import at.favre.lib.crypto.bcrypt.BCrypt
import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.handlers.EventResult
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.executeBlockingAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Hex
import java.util.*
import kotlin.collections.ArrayList

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

@Suppress("NOTHING_TO_INLINE")
internal inline fun SafeUser.transform(): com.kcibald.services.user.proto.User =
    com.kcibald.services.user.proto.User(
        userId = this.userId,
        userName = this.userName,
        urlKey = this.urlKey,
        signature = this.signature,
        avatarKey = this.avatarKey
    )

private val base64Encoder = Base64.getUrlEncoder()

@Suppress("NOTHING_TO_INLINE")
internal inline fun encodeUserIdFromDBID(dbId: String): String {
    val bytes = Hex.decode(dbId)
    return base64Encoder.encodeToString(bytes)
}

private val base64Decoder = Base64.getUrlDecoder()

@Suppress("NOTHING_TO_INLINE")
internal inline fun encodeDBIDFromUserId(userId: String): String {
    val bytes = base64Decoder.decode(userId)
    return Hex.toHexString(bytes)
}

internal val JsonObject.flattenedMap: Map<String, *>
    get() = processMap(this.map)

private fun processMap(reference: Map<String, *>): Map<String, *> {
    if (reference.isEmpty())
        return emptyMap<String, Any>()

    val target = HashMap<String, Any?>()
    for ((key, value) in reference) {
        target[key] = process(value)
    }

    return target
}

private fun processCollection(collection: Collection<*>): ArrayList<*> {
    val list = ArrayList<Any?>(collection.size)

    for (any in collection) {
        list.add(process(any))
    }

    return list
}

private fun process(value: Any?): Any? {
    if (value == null) return null

    @Suppress("UNCHECKED_CAST")
    return when (value) {

        is String -> value
        is Number -> value
        is Boolean -> value

        is Map<*, *> -> processMap(value as Map<String, Any?>)
        is Collection<*> -> processCollection(value)
        is Array<*> -> processCollection(value.asList())

        is JsonObject -> processMap(value.map)
        is JsonArray -> processCollection(value.list)

        else -> value.toString()
    }
}

private val bcryptVerifier = BCrypt.verifyer()!!

internal suspend fun passwordMatches(vertx: Vertx, hash: ByteArray, given: String): Boolean {
    val result = vertx.executeBlockingAwait<BCrypt.Result> { future ->
        future.complete(
            bcryptVerifier.verify(given.toByteArray(), hash)
        )
    }!!

    if (!result.validFormat)
        throw AssertionError("invalid bcrypt format, format error message: ${result.formatErrorMessage}")
    else
        return result.verified
}

private val bcryptMaker = BCrypt.withDefaults()!!

internal suspend fun hashPassword(vertx: Vertx, password: String): ByteArray =
    vertx.executeBlockingAwait<ByteArray> { future ->
        future.complete(
            bcryptMaker.hash(8, password.toByteArray())
        )
    }!!