package com.kcibald.services.user

import com.kcibald.services.user.dao.*
import com.kcibald.services.user.handlers.EmptyEventResult
import com.kcibald.services.user.handlers.EventResult
import io.vertx.core.*
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.jsonObjectOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@ExtendWith(VertxExtension::class)
internal class UtilsKtTest {

    @Test
    fun genRandomString() {
        val string = genRandomString(5)
        println("is this random? $string")
        assertEquals(string.length, 5)
    }

    @Test
    fun coroutineHandler(vertx: Vertx, context: VertxTestContext) {
        val answer = "works!"
        val target = MockMessageConsumer(answer)
        target.coroutineHandler(vertx) k@{
            context.verify {
                assertEquals(answer, it.body())
                context.completeNow()
            }
            return@k EmptyEventResult
        }
        context.awaitCompletion(5, TimeUnit.SECONDS)
    }

    @Test
    fun coroutineHandlerUnexpectErrorOverride(vertx: Vertx, context: VertxTestContext)  {
        val answer = "oh yeah"
        val target = MockMessageConsumer()

        target.coroutineHandler(vertx, object : EventResult {
            override fun reply(message: Message<*>) {
                message.reply(answer)
            }
        }) {
            throw IllegalStateException("oh no!")
        }

        target.completionHandler {
            context.verify{
                assertEquals(answer, target.handlerResult.get())
                context.completeNow()
            }
        }

        context.awaitCompletion(5, TimeUnit.SECONDS)
    }

    class MockMessageConsumer(
        val body: String = ""
    ) : MessageConsumer<String> {
        override fun bodyStream() = throw IllegalAccessError()

        override fun pause() = throw IllegalAccessError()

        private var completionHandlerF: Handler<AsyncResult<Void>>? = null

        override fun completionHandler(completionHandler: Handler<AsyncResult<Void>>?) {
            this.completionHandlerF = completionHandler
        }

        var handlerResult: AtomicReference<Any?> = AtomicReference()

        override fun handler(handler: Handler<Message<String>>): MessageConsumer<String> {
            handler.handle(object : Message<String> {
                override fun replyAddress(): String = throw IllegalAccessError()

                override fun isSend(): Boolean = throw IllegalAccessError()

                override fun body(): String = body

                override fun address(): String = throw IllegalAccessError()

                override fun fail(failureCode: Int, message: String?) = throw IllegalAccessError()

                override fun reply(message: Any?) {
                    this@MockMessageConsumer.handlerResult.compareAndSet(null, message)
                    completionHandlerF?.handle(Future.succeededFuture())
                }

                override fun <R : Any?> reply(
                    message: Any?,
                    replyHandler: Handler<AsyncResult<Message<R>>>?
                ) = throw IllegalAccessError()

                override fun reply(message: Any?, options: DeliveryOptions?) = throw IllegalAccessError()

                override fun <R : Any?> reply(
                    message: Any?,
                    options: DeliveryOptions?,
                    replyHandler: Handler<AsyncResult<Message<R>>>?
                ) = throw IllegalAccessError()

                override fun headers(): MultiMap = throw IllegalAccessError()

            })
            return this
        }

        override fun endHandler(endHandler: Handler<Void>?) = throw IllegalAccessError()

        override fun fetch(amount: Long) = throw IllegalAccessError()

        override fun address() = throw IllegalAccessError()

        override fun resume() = throw IllegalAccessError()

        override fun isRegistered() = throw IllegalAccessError()

        override fun unregister() = throw IllegalAccessError()

        override fun unregister(completionHandler: Handler<AsyncResult<Void>>?) = throw IllegalAccessError()

        override fun exceptionHandler(handler: Handler<Throwable>?) = throw IllegalAccessError()

        override fun getMaxBufferedMessages() = throw IllegalAccessError()

        override fun setMaxBufferedMessages(maxBufferedMessages: Int) = throw IllegalAccessError()

    }

    @Test
    fun protoUserTransform() {
        val userName = "user_name"
        val avatarKey = "avatar"
        val userId = "XQnlUfIU1EA3-50f"
        val signature = "signature"
        val urlKey = "url"

        val userInternal = SafeUserInternal(
            jsonObjectOf(
                "_id" to "5d09e551f214d44037fb9d1f",
                userNameKey to userName,
                avatarFileKey to avatarKey,
                signatureKey to signature,
                urlKeyKey to urlKey
            )
        )

        val transformed = userInternal.transform()
        assertEquals(userName, transformed.userName)
        assertEquals(userId, transformed.userId)
        assertEquals(avatarKey, transformed.avatarKey)
        assertEquals(signature, transformed.signature)
        assertEquals(urlKey, transformed.urlKey)
    }


}