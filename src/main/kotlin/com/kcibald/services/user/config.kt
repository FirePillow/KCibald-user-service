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
const val custom_config_file_key = "config_file"

internal fun load(additional: JsonObject): Config {
    var config = Config { addSpec(MasterConfigSpec) }
        .from.json.resource(default_resource_position)

    if (additional.containsKey(custom_config_file_key)) {
        config = config.from.json.file(additional.getString(custom_config_file_key))
    }

    config = config
        .from.map.hierarchical(additional.map)
        .from.env()
        .from.systemProperties()

    return config
}
