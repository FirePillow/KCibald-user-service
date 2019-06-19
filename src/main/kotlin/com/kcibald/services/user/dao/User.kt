package com.kcibald.services.user.dao

import com.kcibald.services.user.JHelper
import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.utils.ImmutableJsonObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.*

internal interface SafeUser {
    val userId: String
    val userName: String
    val urlKey: String
    val signature: String
    val avatarKey: String

    companion object {
        internal operator fun invoke(
            userId: String,
            userName: String,
            signature: String,
            avatarKey: String,
            urlKey: String = userNameToURLKey(userName)
        ): SafeUser {
            return object : SafeUser {
                override val userId: String = userId

                override val userName: String = userName

                override val urlKey: String = urlKey

                override val signature: String = signature

                override val avatarKey: String = avatarKey
            }
        }

        internal fun fromDBJson(jsonObject: JsonObject): SafeUser = SafeUserInternal(jsonObject)
    }
}

private val notNeedURLEncodingBitSet = JHelper.makeUrlDontNeedEncodingBitSet()

internal fun userNameToURLKey(userName: String): String {
    val out = StringBuilder(userName.length)
    for (c in userName) {
        if (notNeedURLEncodingBitSet.get(c.toInt())) {
            out.append(c)
        } else {
            out.append("-")
        }
    }
    return out.toString()
}


private inline class SafeUserInternal(
    val jsonObject: ImmutableJsonObject
) : SafeUser {
    override val userId: String
        get() = encodeUserIdFromDBID(jsonObject.getString("_id"))

    override val userName: String
        get() = jsonObject.getString(userNameKey)

    override val urlKey: String
        get() = jsonObject.getString(urlKeyKey)

    override val signature: String
        get() =
            jsonObject.getString(signatureKey)

    override val avatarKey: String
        get() =
            jsonObject.getString(avatarFileKey)

}

internal inline class UserInternal(
    val jsonObject: ImmutableJsonObject
) : SafeUser {
    override val userId: String
        get() = encodeUserIdFromDBID(jsonObject.getString("_id"))

    override val userName: String
        get() = jsonObject.getString(userNameKey)

    override val urlKey: String
        get() = jsonObject.getString(urlKeyKey)

    override val signature: String
        get() =
            jsonObject.getString(signatureKey)

    override val avatarKey: String
        get() =
            jsonObject.getString(avatarFileKey)

    val password: ByteArray
        get() =
            Base64
                .getDecoder()
                .decode(jsonObject.getString(passwordHashKey))

    val emails: Emails
        get() = Emails(jsonObject.getJsonArray(emailKey))

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

internal const val userIdKey = "user_id"
internal const val userNameKey = "user_name"
internal const val urlKeyKey = "url_key"
internal const val signatureKey = "signature"
internal const val avatarFileKey = "avatar_file"
internal const val passwordHashKey = "password"

internal const val emailKey = "email"
internal const val typeKey = "type"
internal const val schoolEmailKey = "school"
internal const val personalEmailKey = "personal"

internal const val emailAddressKey = "address"
internal const val emailVerifiedKey = "verified"