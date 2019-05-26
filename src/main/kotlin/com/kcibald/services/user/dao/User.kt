package com.kcibald.services.user.dao

import com.kcibald.utils.ImmutableJsonObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

internal class SafeUser(
    val jsonObject: ImmutableJsonObject
) {
    val user_id: String
        get() = jsonObject.getString(userIdKey)

    val user_name: String
        get() = jsonObject.getString(userNameKey)

    val url_key: String
        get() = jsonObject.getString(urlKeyKey)

    val signature: String
        get() =
            jsonObject.getString(signatureKey)

    val avatar_key: String
        get() =
            jsonObject.getString(avatarFileKey)

}

internal inline class UserInternal(
    val jsonObject: ImmutableJsonObject
) {
    val user_id: String
        get() = jsonObject.getString(userIdKey)

    val user_name: String
        get() = jsonObject.getString(userNameKey)

    val url_key: String
        get() = jsonObject.getString(urlKeyKey)

    val signature: String
        get() =
            jsonObject.getString(signatureKey)

    val avatar_key: String
        get() =
            jsonObject.getString(avatarFileKey)

    val password: ByteArray
        get() =
            Base64
                .getDecoder()
                .decode(jsonObject.getString(passwordHashKey))

    val emails: Emails
        get() = Emails(jsonObject.getJsonArray(emailKey))

    val nativeRole: List<String>
        get() {
            return jsonObject
                .getJsonObject(authoritiesKey)
                .getJsonArray(nativeRoleKey)
                .map {
                    it as String
                }
        }

    val roleModels: List<String>
        get() {
            return jsonObject
                .getJsonObject(authoritiesKey)
                .getJsonArray(roleModelsKey)
                .map { it as String }
        }
}

internal inline class Emails(
    val jsonArray: JsonArray
) {
    val school: EmailConfig
        get() {
            for (email in jsonArray) {
                email as JsonObject
                val type = email.getString(typeKey)
                if (type == schoolEmailKey) {
                    return EmailConfig(email)
                }
            }
            throw AssertionError("school email must be present")
        }

    val personal: EmailConfig?
        get() {
            for (email in jsonArray) {
                email as JsonObject
                val type = email.getString(typeKey)
                if (type == personalEmailKey) {
                    return EmailConfig(email)
                }
            }
            return null
        }
}

internal inline class EmailConfig(
    val jsonObject: JsonObject
) {
    val type: String
        get() = jsonObject.getString(typeKey)

    val address: String
        get() = jsonObject.getString(emailAddressKey)

    val verified: Boolean
        get() = jsonObject.getBoolean(emailVerifiedKey)
}

internal inline class Authorities(
    val jsonObject: ImmutableJsonObject
) {
    val native_roles: List<String>
        get() =
             jsonObject
                .getJsonArray(nativeRoleKey)
                .map { it as String }

    val roleModels: List<String>
        get() =
                jsonObject
                    .getJsonArray(roleModelsKey)
                    .map { it as String }
}

internal const val userIdKey = "user_id"
internal const val userNameKey = "user_name"
internal const val urlKeyKey = "url_key"
internal const val signatureKey = "signature"
internal const val avatarFileKey = "avatar_file"
internal const val passwordHashKey = "password"

internal const val authoritiesKey = "authorities"
internal const val nativeRoleKey = "native_role"
internal const val roleModelsKey = "role_models"

internal const val emailKey = "email"
internal const val typeKey = "type"
internal const val schoolEmailKey = "school"
internal const val personalEmailKey = "personal"

internal const val emailAddressKey = "address"
internal const val emailVerifiedKey = "verified"