@file:Suppress("RemoveRedundantQualifierName")

package com.kcibald.services.user.proto

data class AuthenticationRequest(
    val userEmail: String = "",
    val plainPassword: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<AuthenticationRequest> {
    override operator fun plus(other: AuthenticationRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<AuthenticationRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = AuthenticationRequest.protoUnmarshalImpl(u)
    }
}

data class AuthenticationResponse(
    val result: Result? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<AuthenticationResponse> {
    sealed class Result {
        data class SuccessUser(val successUser: com.kcibald.services.user.proto.User) : Result()
        data class CommonAuthenticationError(val commonAuthenticationError: com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType = com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.fromValue(0)) : Result()
        data class BannedInfo(val bannedInfo: com.kcibald.services.user.proto.AuthenticationResponse.BannedInfo) : Result()
        data class SystemErrorMessage(val systemErrorMessage: String = "") : Result()
    }

    override operator fun plus(other: AuthenticationResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<AuthenticationResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = AuthenticationResponse.protoUnmarshalImpl(u)
    }

    data class AuthenticationErrorType(override val value: Int) : pbandk.Message.Enum {
        companion object : pbandk.Message.Enum.Companion<AuthenticationErrorType> {
            val USER_NOT_FOUND = AuthenticationErrorType(0)
            val INVALID_CREDENTIAL = AuthenticationErrorType(1)

            override fun fromValue(value: Int) = when (value) {
                0 -> USER_NOT_FOUND
                1 -> INVALID_CREDENTIAL
                else -> AuthenticationErrorType(value)
            }
        }
    }

    data class BannedInfo(
        val timeBanned: Int = 0,
        val duration: Int = 0,
        val message: String = "",
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
    ) : pbandk.Message<BannedInfo> {
        override operator fun plus(other: BannedInfo?) = protoMergeImpl(other)
        override val protoSize by lazy { protoSizeImpl() }
        override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
        companion object : pbandk.Message.Companion<BannedInfo> {
            override fun protoUnmarshal(u: pbandk.Unmarshaller) = BannedInfo.protoUnmarshalImpl(u)
        }
    }
}

private fun AuthenticationRequest.protoMergeImpl(plus: AuthenticationRequest?): AuthenticationRequest = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun AuthenticationRequest.protoSizeImpl(): Int {
    var protoSize = 0
    if (userEmail.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(userEmail)
    if (plainPassword.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(plainPassword)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun AuthenticationRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (userEmail.isNotEmpty()) protoMarshal.writeTag(10).writeString(userEmail)
    if (plainPassword.isNotEmpty()) protoMarshal.writeTag(18).writeString(plainPassword)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun AuthenticationRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): AuthenticationRequest {
    var userEmail = ""
    var plainPassword = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return AuthenticationRequest(userEmail, plainPassword, protoUnmarshal.unknownFields())
        10 -> userEmail = protoUnmarshal.readString()
        18 -> plainPassword = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun AuthenticationResponse.protoMergeImpl(plus: AuthenticationResponse?): AuthenticationResponse = plus?.copy(
    result = when {
        result is AuthenticationResponse.Result.SuccessUser && plus.result is AuthenticationResponse.Result.SuccessUser ->
            AuthenticationResponse.Result.SuccessUser(result.successUser + plus.result.successUser)
        result is AuthenticationResponse.Result.BannedInfo && plus.result is AuthenticationResponse.Result.BannedInfo ->
            AuthenticationResponse.Result.BannedInfo(result.bannedInfo + plus.result.bannedInfo)
        else ->
            plus.result ?: result
    },
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun AuthenticationResponse.protoSizeImpl(): Int {
    var protoSize = 0
    when (result) {
        is AuthenticationResponse.Result.SuccessUser -> protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(result.successUser)
        is AuthenticationResponse.Result.CommonAuthenticationError -> protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.enumSize(result.commonAuthenticationError)
        is AuthenticationResponse.Result.BannedInfo -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.messageSize(result.bannedInfo)
        is AuthenticationResponse.Result.SystemErrorMessage -> protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.stringSize(result.systemErrorMessage)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun AuthenticationResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (result is AuthenticationResponse.Result.SuccessUser) protoMarshal.writeTag(10).writeMessage(result.successUser)
    if (result is AuthenticationResponse.Result.CommonAuthenticationError) protoMarshal.writeTag(16).writeEnum(result.commonAuthenticationError)
    if (result is AuthenticationResponse.Result.BannedInfo) protoMarshal.writeTag(26).writeMessage(result.bannedInfo)
    if (result is AuthenticationResponse.Result.SystemErrorMessage) protoMarshal.writeTag(34).writeString(result.systemErrorMessage)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun AuthenticationResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): AuthenticationResponse {
    var result: AuthenticationResponse.Result? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return AuthenticationResponse(result, protoUnmarshal.unknownFields())
        10 -> result = AuthenticationResponse.Result.SuccessUser(protoUnmarshal.readMessage(com.kcibald.services.user.proto.User.Companion))
        16 -> result = AuthenticationResponse.Result.CommonAuthenticationError(protoUnmarshal.readEnum(com.kcibald.services.user.proto.AuthenticationResponse.AuthenticationErrorType.Companion))
        26 -> result = AuthenticationResponse.Result.BannedInfo(protoUnmarshal.readMessage(com.kcibald.services.user.proto.AuthenticationResponse.BannedInfo.Companion))
        34 -> result = AuthenticationResponse.Result.SystemErrorMessage(protoUnmarshal.readString())
        else -> protoUnmarshal.unknownField()
    }
}

private fun AuthenticationResponse.BannedInfo.protoMergeImpl(plus: AuthenticationResponse.BannedInfo?): AuthenticationResponse.BannedInfo = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun AuthenticationResponse.BannedInfo.protoSizeImpl(): Int {
    var protoSize = 0
    if (timeBanned != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.int32Size(timeBanned)
    if (duration != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(duration)
    if (message.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(message)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun AuthenticationResponse.BannedInfo.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (timeBanned != 0) protoMarshal.writeTag(8).writeInt32(timeBanned)
    if (duration != 0) protoMarshal.writeTag(16).writeInt32(duration)
    if (message.isNotEmpty()) protoMarshal.writeTag(26).writeString(message)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun AuthenticationResponse.BannedInfo.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): AuthenticationResponse.BannedInfo {
    var timeBanned = 0
    var duration = 0
    var message = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return AuthenticationResponse.BannedInfo(timeBanned, duration, message, protoUnmarshal.unknownFields())
        8 -> timeBanned = protoUnmarshal.readInt32()
        16 -> duration = protoUnmarshal.readInt32()
        26 -> message = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}
