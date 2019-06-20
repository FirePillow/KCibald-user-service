package com.kcibald.services.user

import com.kcibald.services.user.dao.emailAddressKey
import com.kcibald.services.user.dao.emailKey
import com.kcibald.services.user.dao.urlKeyKey
import com.kcibald.services.user.dao.userNameKey
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import io.vertx.core.json.JsonObject

internal object MasterConfigSpec : ConfigSpec("") {
    val mongo_config by required<Map<String, Any>>()

    internal object AuthenticationConfig : ConfigSpec("auth") {
        val event_bus_name by optional("kcibald.user.authentication")
    }

    internal object DescribeUserConfig : ConfigSpec("describe_user") {
        val event_bus_name by optional("kcibald.user.describe")
    }

    internal object UserCollection : ConfigSpec("user_collection") {
        val collection_name by optional("users")
        val indexes by optional(
            mapOf(
                "$emailKey.$emailAddressKey" to 1,
                userNameKey to 1
            )
        )
        val unique_indexes by optional(
            mapOf(
                urlKeyKey to 1
            )
        )
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

    val flattenedMap = additional.flattenedMap

//    just let the null exception throw
    @Suppress("UNCHECKED_CAST")
    flattenedMap as Map<String, Any>

    config = config
        .from.map.hierarchical(flattenedMap)
        .from.env()
        .from.systemProperties()

    return config
}
