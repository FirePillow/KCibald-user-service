package com.kcibald.services.user.dao

import com.uchuhimo.konf.Config
import io.vertx.core.Vertx

internal interface DBAccess {
    suspend fun initialize()

    suspend fun getUserWithId(id: String): SafeUser?

    suspend fun getUserWithName(name: String): List<SafeUser>

    suspend fun getUserWithUrlKey(urlKey: String): SafeUser?

    suspend fun getUserAndPasswordWithEmail(email: String): Pair<SafeUser, ByteArray>?

    suspend fun updateUserName(
        to: String,
        tolerateUrlKeySpin: Boolean = true,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean

    suspend fun updateSignature(
        to: String,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean

    suspend fun updateAvatar(
        to: String,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean

    suspend fun updatePassword(
        before: String,
        after: String,
        userId: String? = null,
        urlKey: String? = null
    ): Boolean

    suspend fun close()

    object URLKeyDuplicationException : RuntimeException("key duplication occurs", null, false, false)

    companion object {
        fun createDefault(vertx: Vertx, config: Config): DBAccess = DefaultDBAccess(vertx, config)
    }
}
