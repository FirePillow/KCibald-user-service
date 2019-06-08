package com.kcibald.services.user

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import io.vertx.core.json.JsonObject

internal object MasterConfigSpec : ConfigSpec("") {
    val db_config by required<Map<String, Any>>()
    val db_config_json by lazy {
        JsonObject(it[db_config])
    }

    val user_collection_name by optional("user-collection")

    internal object AuthenticationConfig : ConfigSpec("auth") {
        val event_bus_name by optional("kcibald.user.authentication")
    }
}


const val default_resource_position = "config.json"

internal fun load(additional: JsonObject) = Config { addSpec(MasterConfigSpec) }
        .from.json.resource(default_resource_position)
        .from.map.hierarchical(additional.map)
        .from.env()
        .from.systemProperties()
