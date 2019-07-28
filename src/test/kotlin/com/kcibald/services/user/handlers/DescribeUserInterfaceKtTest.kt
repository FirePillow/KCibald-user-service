package com.kcibald.services.user.handlers

import com.kcibald.services.user.dao.SafeUser
import com.kcibald.services.user.proto.DescribeUserResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DescribeUserInterfaceKtTest {

    @Test
    fun unexpectedErrorEventResult() {
        val message = unexpectedErrorEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result.SystemErrorMessage)
        result as DescribeUserResponse.Result.SystemErrorMessage

        assertEquals("unexpected error", result.systemErrorMessage)
    }

    @Test
    fun databaseErrorEventResult() {
        val message = databaseErrorEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result.SystemErrorMessage)
        result as DescribeUserResponse.Result.SystemErrorMessage

        assertEquals("database error", result.systemErrorMessage)
    }

    @Test
    fun userNotFoundEventResult() {
        val message = userNotFoundEventResult.message
        assertTrue(message is DescribeUserResponse)
        message as DescribeUserResponse

        val result = message.result
        assertTrue(result is DescribeUserResponse.Result.UserNotFound)
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
        assertTrue(result is DescribeUserResponse.Result.SingleUserResult)
        result as DescribeUserResponse.Result.SingleUserResult

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
        assertTrue(result is DescribeUserResponse.Result.MultiUserResult)

        result as DescribeUserResponse.Result.MultiUserResult
        result.multiUserResult.result.forEach {
            assertTrue(list.remove(it))
        }
    }

    @Test
    @Disabled("NOT Implemented")
    fun process_urlKey() {
        fail<Unit>("NOT Implemented")
    }

    @Test
    @Disabled("NOT Implemented")
    fun process_id() {
        fail<Unit>("NOT Implemented")
    }

    @Test
    @Disabled("NOT Implemented")
    fun process_username_single() {
        fail<Unit>("NOT Implemented")
    }

    @Test
    @Disabled("NOT Implemented")
    fun process_username_muti() {
        fail<Unit>("NOT Implemented")
    }

    private fun com.kcibald.services.user.proto.User.assertEqualsTo(user: SafeUser) {
        assertEquals(this.userId, user.userId)
        assertEquals(this.userName, user.userName)
        assertEquals(this.signature, user.signature)
        assertEquals(this.avatarKey, user.avatarKey)
        assertEquals(this.urlKey, user.urlKey)
    }

}