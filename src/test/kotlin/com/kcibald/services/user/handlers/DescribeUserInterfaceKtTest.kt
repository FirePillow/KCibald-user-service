package com.kcibald.services.user.handlers

import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.DescribeUserResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DescribeUserInterfaceKtTest {

    @Test
    fun unexpectedErrorEventResult() {
        val message = unexpectedErrorEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result_.SystemErrorMessage)
        result as DescribeUserResponse.Result_.SystemErrorMessage

        assertEquals("unexpected error", result.systemErrorMessage)
    }

    @Test
    fun databaseErrorEventResult() {
        val message = databaseErrorEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result_.SystemErrorMessage)
        result as DescribeUserResponse.Result_.SystemErrorMessage

        assertEquals("database error", result.systemErrorMessage)
    }

    @Test
    fun userNotFoundEventResult() {
        val message = userNotFoundEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result_.UserNotFound)
    }

    @Test
    fun packIndividual_not_found() {
        val packed = packIndividual(null)
        assertEquals(userNotFoundEventResult, packed)
    }

    @Test
    fun packIndividual_normal() {
        val user = SafeUser(
            "userId",
            "user_name",
            "sig",
            "avatar"
        )
        val packed = packIndividual(user)

        val message = packed.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result_.SingleUserResult)
        result as DescribeUserResponse.Result_.SingleUserResult

        val resultUser = result.singleUserResult.result!!
        resultUser.assertEqualsTo(user)
    }

    @Test
    fun packMultiple_not_found() {
        val packed = packMultiple(emptyList())
        assertEquals(packed, userNotFoundEventResult)
    }

    @Test
    fun packMultiple_normal() {
        val user01 = com.kcibald.services.user.proto.User(
            "userId",
            "user_name01",
            "sig",
            "avatar"
        )
        val user02 = com.kcibald.services.user.proto.User(
            "userId",
            "user_name",
            "sig",
            "avatar"
        )
        val list = ArrayList<com.kcibald.services.user.proto.User>(2)
        list.add(user01)
        list.add(user02)

        val packed = packMultiple(
            list
        )

        assertTrue(packed is ProtobufEventResult<*>)
        packed as ProtobufEventResult<*>

        val message = packed.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse
        val result = message.result
        assertTrue(result is DescribeUserResponse.Result_.MultiUserResult)

        result as DescribeUserResponse.Result_.MultiUserResult
        result.multiUserResult.result.forEach {
            assertTrue(list.remove(it))
        }
    }

    private fun com.kcibald.services.user.proto.User.assertEqualsTo(user: SafeUser) {
        assertEquals(this.userId, user.userId)
        assertEquals(this.userName, user.userName)
        assertEquals(this.signature, user.signature)
        assertEquals(this.avatarKey, user.avatarKey)
        assertEquals(this.urlKey, user.urlKey)
    }

}