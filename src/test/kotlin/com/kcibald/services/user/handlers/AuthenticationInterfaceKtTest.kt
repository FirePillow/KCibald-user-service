package com.kcibald.services.user.handlers

import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.encodeUserIdFromDBID
import com.kcibald.services.user.proto.AuthenticationResponse
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.INVALID_CREDENTIAL
import com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion.USER_NOT_FOUND
import com.kcibald.services.user.proto.AuthenticationResponse.Result.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AuthenticationInterfaceKtTest {

    @Test
    fun test_createSuccessAuthenticationResponse() {
        val userId = encodeUserIdFromDBID("039b94f38726a43e64312c7aec33d870")

        val user = SafeUser(
            userId = userId,
            userName = "name",
            signature = "signature",
            avatarKey = "avatar"
        )

        val packed = createSuccessAuthenticationResponse(user)
        val result = (packed.message as AuthenticationResponse).result

        assertTrue(result is SuccessUser)

        val targetUser = (result as SuccessUser).successUser

        assertEquals(userId, targetUser.userId)
        assertEquals(user.userName, targetUser.userName)
        assertEquals(user.signature, targetUser.signature)
        assertEquals(user.avatarKey, targetUser.avatarKey)
    }

    @Test
    fun test_invalidCredentialFailureAuthenticationResponse() {
        val packed = invalidCredentialFailureAuthenticationResponse
        val result = (packed.message as AuthenticationResponse).result

        assertTrue(result is CommonAuthenticationError)
        result as CommonAuthenticationError

        assertEquals(INVALID_CREDENTIAL, result.commonAuthenticationError)
    }

    @Test
    fun test_userNotFoundAuthenticationResponse() {
        val packed = userNotFoundAuthenticationResponse
        val result = (packed.message as AuthenticationResponse).result

        assertTrue(result is CommonAuthenticationError)
        result as CommonAuthenticationError

        assertEquals(USER_NOT_FOUND, result.commonAuthenticationError)
    }

    @Test
    fun test_databaseErrorResponse() {
        val packed = databaseErrorResponse
        val result = (packed.message as AuthenticationResponse).result

        assertTrue(result is SystemErrorMessage)
        result as SystemErrorMessage

        assertEquals("database error", result.systemErrorMessage)
    }

}