package com.kcibald.services.user.dao

import com.kcibald.services.user.JHelper
import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.utils.ImmutableJsonObject
import io.vertx.core.json.JsonObject

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