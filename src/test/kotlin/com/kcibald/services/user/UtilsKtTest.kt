package com.kcibald.services.user

import com.kcibald.services.user.handlers.EmptyEventResult
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

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
        val target = object : MessageConsumer<String> {
            override fun bodyStream() = throw IllegalAccessError()

            override fun pause() = throw IllegalAccessError()

            override fun completionHandler(completionHandler: Handler<AsyncResult<Void>>?) = throw IllegalAccessError()

            override fun handler(handler: Handler<Message<String>>): MessageConsumer<String> {
                handler.handle(object : Message<String> {
                    override fun replyAddress(): String = throw IllegalAccessError()

                    override fun isSend(): Boolean = throw IllegalAccessError()

                    override fun body(): String = answer

                    override fun address(): String = throw IllegalAccessError()

                    override fun fail(failureCode: Int, message: String?) = throw IllegalAccessError()

                    override fun reply(message: Any?) = throw IllegalAccessError()

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
        target.coroutineHandler(vertx) k@{
            context.verify {
                assertEquals(answer, it.body())
                context.completeNow()
            }
            return@k EmptyEventResult
        }
        context.awaitCompletion(5, TimeUnit.SECONDS)
    }

}