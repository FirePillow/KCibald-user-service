package com.kcibald.services.user

import com.uchuhimo.konf.Config
import io.vertx.core.json.JsonObject
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ConfigTest {

    @Language("JSON")
    val passJson = """
        {
            "db_config": {
              "a": "b"
            },
            "user_collection_name": "collection_name",
            "auth": {
              "event_bus_name": "event_bus"
            }
        }
    """.trimIndent()

    val config = Config { addSpec(MasterConfigSpec) }
        .from.json.string(passJson)

    @Test
    fun user_collection_name() {
        assertEquals(config[MasterConfigSpec.user_collection_name], "collection_name")
    }

    @Test
    fun db_config() {
        assertEquals(config[MasterConfigSpec.db_config], mapOf("a" to "b"))
    }

    @Test
    fun db_config_to_json() {
        JsonObject(config[MasterConfigSpec.db_config])
    }

    @Test
    fun auth_event_bus_name() {
        assertEquals(config[MasterConfigSpec.AuthenticationConfig.event_bus_name], "event_bus")
    }

}