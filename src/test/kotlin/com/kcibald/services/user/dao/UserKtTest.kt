package com.kcibald.services.user.dao

import com.kcibald.services.user.encodeUserIdFromDBID
import io.vertx.kotlin.core.json.jsonObjectOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UserKtTest {

    @Test
    fun userNameToURLKey() {
        assertEquals("abcdefg", userNameToURLKey("abcdefg"))
        assertEquals("12345", userNameToURLKey("12345"))
        assertEquals("abd-e", userNameToURLKey("abd e"))
        assertEquals("---d", userNameToURLKey("///d"))
    }

    @Test
    fun create() {
        val userId = "userId"
        val userName = "user Name"
        val signature = "sig"
        val avatarKey = "avatar"
        val user = SafeUser(userId, userName, signature, avatarKey)
        assertEquals(userId, user.userId)
        assertEquals(userName, user.userName)
        assertEquals("user-Name", user.urlKey)
        assertEquals(signature, user.signature)
        assertEquals(avatarKey, user.avatarKey)
    }

    @Test
    fun fromJson() {
        val dbId = "5d09f3048b5aca67cc2a3c80"
        val userId = encodeUserIdFromDBID(dbId)
        val userName = "user name"
        val urlKey = "user-name"
        val signature = "sig"
        val avatar = "avatar"
        val json = jsonObjectOf(
            "_id" to dbId,
            userNameKey to userName,
            urlKeyKey to urlKey,
            signatureKey to signature,
            avatarFileKey to avatar
        )
        val target = SafeUser.fromDBJson(json)
        assertEquals(userId, target.userId)
        assertEquals(userName, target.userName)
        assertEquals(urlKey, target.urlKey)
        assertEquals(signature, target.signature)
        assertEquals(avatar, target.avatarKey)
    }
}