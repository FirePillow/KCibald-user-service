package com.kcibald.services.user.handlers

import com.kcibald.services.user.UserServiceVerticle
import io.vertx.core.eventbus.EventBus

internal abstract class ServiceInterface(
    protected val verticle: UserServiceVerticle
) {
    abstract suspend fun bind(eventBus: EventBus)
}
