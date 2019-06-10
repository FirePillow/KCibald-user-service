package com.kcibald.services.user

import com.uchuhimo.konf.Config
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

internal class ConfigKtTest {

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
        assertEquals(JsonObject(config[MasterConfigSpec.db_config]), config[MasterConfigSpec.db_config_json])
    }

    @Test
    fun auth_event_bus_name() {
        assertEquals("event_bus", config[MasterConfigSpec.AuthenticationConfig.event_bus_name])
    }

    /**
     * see config file: config.json
     */
    @Test
    fun load_config() {
        val load = load(JsonObject())
        assertEquals("d", load[MasterConfigSpec.db_config]["c"])
        assertEquals("collection_name", load[MasterConfigSpec.user_collection_name])
        assertEquals("event_bus", load[MasterConfigSpec.AuthenticationConfig.event_bus_name])
    }

    @Test
    fun overload_custom_config_file() {
        val tempFile = Files.createTempFile("temp-config", ".json")
        try {
            @Language("JSON")
            val configPayload = """
            {
                "user_collection_name": "new_collection_name"
            }
            """.trimIndent()
            Files.write(tempFile, configPayload.toByteArray())
            val address = tempFile.toAbsolutePath().toString()
            println(address)
            val load = load(jsonObjectOf(custom_config_file_key to address))
            assertEquals("new_collection_name", load[MasterConfigSpec.user_collection_name])
        } finally {
            Files.delete(tempFile)
        }
    }

    @Test
    fun overload_JsonConfig() {
        val answer = "new_collection_name"
        val load = load(jsonObjectOf("user_collection_name" to answer))
        assertEquals(answer, load[MasterConfigSpec.user_collection_name])
    }

    @Test
    fun overload_SystemProperty() {
        val answer = "better_collection_name"
        val key = "user_collection_name"
        System.setProperty(key, answer)
        val load = load(jsonObjectOf())
        assertEquals(answer, load[MasterConfigSpec.user_collection_name])
    }

}