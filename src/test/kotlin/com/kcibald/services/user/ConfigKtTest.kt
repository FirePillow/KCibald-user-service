package com.kcibald.services.user

import com.kcibald.services.user.dao.urlKeyKey
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
            "mongo_config": {
              "a": "b"
            },
            "user_collection": {
              "collection_name": "collection_name",
              "indexes": {}
            },
            "auth": {
              "event_bus_name": "event_bus"
            }
        }
    """.trimIndent()

    val config = Config { addSpec(MasterConfigSpec) }
        .from.json.string(passJson)

    @Test
    fun user_collection_name() {
        assertEquals(config[MasterConfigSpec.UserCollection.collection_name], "collection_name")
    }

    @Test
    fun mongo_config() {
        assertEquals(config[MasterConfigSpec.mongo_config], mapOf("a" to "b"))
    }

    @Test
    fun mongo_config_to_json() {
        JsonObject(config[MasterConfigSpec.mongo_config])
    }

    @Test
    fun auth_event_bus_name() {
        assertEquals("event_bus", config[MasterConfigSpec.AuthenticationConfig.event_bus_name])
    }

    @Test
    fun user_collection_unique_index() {
        assertEquals(mapOf(urlKeyKey to 1), config[MasterConfigSpec.UserCollection.unique_indexes])
    }

    @Test
    fun user_collection_index() {
        assertEquals(mapOf(urlKeyKey to 1), config[MasterConfigSpec.UserCollection.unique_indexes])
    }

    /**
     * see config file: config.json
     */
    @Test
    fun load_config() {
        val load = load(JsonObject())
        assertEquals("test", load[MasterConfigSpec.mongo_config]["db_name"])
        assertEquals("users_collection", load[MasterConfigSpec.UserCollection.collection_name])
        assertEquals("event_bus", load[MasterConfigSpec.AuthenticationConfig.event_bus_name])
    }

    @Test
    fun overload_custom_config_file() {
        val tempFile = Files.createTempFile("temp-config", ".json")
        try {
            @Language("JSON")
            val configPayload = """
            {
                "user_collection": {
                  "collection_name": "new_collection_name",
                  "indexes": {}
                }
            }
            """.trimIndent()
            Files.write(tempFile, configPayload.toByteArray())
            val address = tempFile.toAbsolutePath().toString()
            val load = load(jsonObjectOf(custom_config_file_key to address))
            assertEquals("new_collection_name", load[MasterConfigSpec.UserCollection.collection_name])
        } finally {
            Files.delete(tempFile)
        }
    }

    @Test
    fun overload_JsonConfig() {
        val answer = "new_collection_name"
        val json = JsonObject("""
            {
                "user_collection": {
                  "collection_name": "new_collection_name",
                  "indexes": {}
                }
            }
        """.trimIndent())
        val load = load(json)
        assertEquals(answer, load[MasterConfigSpec.UserCollection.collection_name])
    }

    @Test
    fun overload_SystemProperty() {
        val key = "user_collection.collection_name"
        val answer = "better_collection_name"
        System.setProperty(key, answer)
        val load = load(jsonObjectOf())
        assertEquals(answer, load[MasterConfigSpec.UserCollection.collection_name])
        System.clearProperty(key)
    }

}