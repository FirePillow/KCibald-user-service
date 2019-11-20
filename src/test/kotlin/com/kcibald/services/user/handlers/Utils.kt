package com.kcibald.services.user.handlers

import com.kcibald.services.user.dao.DBAccess
import com.kcibald.services.user.dao.SafeUser
import org.junit.jupiter.api.Assertions

internal abstract class TestDBAccess : DBAccess {
    override suspend fun initialize() {}

    override suspend fun getUserWithId(id: String): SafeUser? = failInternal()

    override suspend fun getUserWithName(name: String): List<SafeUser> = failInternal()

    override suspend fun getUserWithUrlKey(urlKey: String): SafeUser? = failInternal()

    override suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>? = failInternal()

    override suspend fun updateUserName(
        to: String,
        tolerateUrlKeySpin: Boolean,
        userId: String?,
        urlKey: String?
    ): Boolean = failInternal()

    override suspend fun updateSignature(to: String, userId: String?, urlKey: String?): Boolean = failInternal()

    override suspend fun updateAvatar(to: String, userId: String?, urlKey: String?): Boolean = failInternal()

    override suspend fun updatePassword(before: String, after: String, userId: String?, urlKey: String?): Boolean =
        failInternal()

    override suspend fun close() {}

    private fun failInternal(): Nothing {
        Assertions.fail<Unit>()
        throw AssertionError()
    }
}