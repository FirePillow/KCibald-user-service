package com.kcibald.services.user.handlers

import com.kcibald.services.user.SharedRuntimeData
import com.kcibald.services.user.UserServiceVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject

internal abstract class ServiceInterface(
    protected val runtimeData: SharedRuntimeData
) {
    abstract suspend fun bind(eventBus: EventBus)
}

internal interface EventResult {
    fun reply(message: Message<*>)
}

internal inline class RawEventResult(
    private val buffer: Buffer
) : EventResult {
    override fun reply(message: Message<*>) = message.reply(buffer)
}

internal object EmptyEventResult : EventResult {
    override fun reply(message: Message<*>) {}
}

internal class FailureEventResult(
    private val statusCode: Int,
    private val failureMessage: String
): EventResult {
    override fun reply(message: Message<*>) = message.fail(
        statusCode,
        failureMessage
    )
}

internal inline class JsonEventResult(
    private val jsonObject: JsonObject
): EventResult {
    override fun reply(message: Message<*>) {
        message.reply(jsonObject)
    }
}