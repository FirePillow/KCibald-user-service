@file:Suppress("RemoveRedundantQualifierName")

package com.kcibald.services.user.proto

data class DescribeUserRequest(
    val by: By? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<DescribeUserRequest> {
    sealed class By {
        data class UrlKey(val urlKey: String = "") : By()
        data class ID(val iD: String = "") : By()
        data class UserName(val userName: String = "") : By()
    }

    override operator fun plus(other: DescribeUserRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<DescribeUserRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = DescribeUserRequest.protoUnmarshalImpl(u)
    }
}

data class DescribeUserResponse(
    val result: Result? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<DescribeUserResponse> {
    sealed class Result {
        data class SingleUserResult(val singleUserResult: com.kcibald.services.user.proto.DescribeUserResponse.SuccessSingleUserResult) : Result()
        data class MultiUserResult(val multiUserResult: com.kcibald.services.user.proto.DescribeUserResponse.SuccessMultiUserResult) : Result()
        data class UserNotFound(val userNotFound: com.kcibald.services.user.proto.Empty) : Result()
        data class SystemErrorMessage(val systemErrorMessage: String = "") : Result()
    }

    override operator fun plus(other: DescribeUserResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<DescribeUserResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = DescribeUserResponse.protoUnmarshalImpl(u)
    }

    data class SuccessSingleUserResult(
        val result: com.kcibald.services.user.proto.User? = null,
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
    ) : pbandk.Message<SuccessSingleUserResult> {
        override operator fun plus(other: SuccessSingleUserResult?) = protoMergeImpl(other)
        override val protoSize by lazy { protoSizeImpl() }
        override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
        companion object : pbandk.Message.Companion<SuccessSingleUserResult> {
            override fun protoUnmarshal(u: pbandk.Unmarshaller) = SuccessSingleUserResult.protoUnmarshalImpl(u)
        }
    }

    data class SuccessMultiUserResult(
        val result: List<com.kcibald.services.user.proto.User> = emptyList(),
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
    ) : pbandk.Message<SuccessMultiUserResult> {
        override operator fun plus(other: SuccessMultiUserResult?) = protoMergeImpl(other)
        override val protoSize by lazy { protoSizeImpl() }
        override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
        companion object : pbandk.Message.Companion<SuccessMultiUserResult> {
            override fun protoUnmarshal(u: pbandk.Unmarshaller) = SuccessMultiUserResult.protoUnmarshalImpl(u)
        }
    }
}

private fun DescribeUserRequest.protoMergeImpl(plus: DescribeUserRequest?): DescribeUserRequest = plus?.copy(
    by = plus.by ?: by,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun DescribeUserRequest.protoSizeImpl(): Int {
    var protoSize = 0
    when (by) {
        is DescribeUserRequest.By.UrlKey -> protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(by.urlKey)
        is DescribeUserRequest.By.ID -> protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(by.iD)
        is DescribeUserRequest.By.UserName -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(by.userName)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun DescribeUserRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (by is DescribeUserRequest.By.UrlKey) protoMarshal.writeTag(10).writeString(by.urlKey)
    if (by is DescribeUserRequest.By.ID) protoMarshal.writeTag(18).writeString(by.iD)
    if (by is DescribeUserRequest.By.UserName) protoMarshal.writeTag(26).writeString(by.userName)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun DescribeUserRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): DescribeUserRequest {
    var by: DescribeUserRequest.By? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return DescribeUserRequest(by, protoUnmarshal.unknownFields())
        10 -> by = DescribeUserRequest.By.UrlKey(protoUnmarshal.readString())
        18 -> by = DescribeUserRequest.By.ID(protoUnmarshal.readString())
        26 -> by = DescribeUserRequest.By.UserName(protoUnmarshal.readString())
        else -> protoUnmarshal.unknownField()
    }
}

private fun DescribeUserResponse.protoMergeImpl(plus: DescribeUserResponse?): DescribeUserResponse = plus?.copy(
    result = when {
        result is DescribeUserResponse.Result.SingleUserResult && plus.result is DescribeUserResponse.Result.SingleUserResult ->
            DescribeUserResponse.Result.SingleUserResult(result.singleUserResult + plus.result.singleUserResult)
        result is DescribeUserResponse.Result.MultiUserResult && plus.result is DescribeUserResponse.Result.MultiUserResult ->
            DescribeUserResponse.Result.MultiUserResult(result.multiUserResult + plus.result.multiUserResult)
        result is DescribeUserResponse.Result.UserNotFound && plus.result is DescribeUserResponse.Result.UserNotFound ->
            DescribeUserResponse.Result.UserNotFound(result.userNotFound + plus.result.userNotFound)
        else ->
            plus.result ?: result
    },
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun DescribeUserResponse.protoSizeImpl(): Int {
    var protoSize = 0
    when (result) {
        is DescribeUserResponse.Result.SingleUserResult -> protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(result.singleUserResult)
        is DescribeUserResponse.Result.MultiUserResult -> protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(result.multiUserResult)
        is DescribeUserResponse.Result.UserNotFound -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.messageSize(result.userNotFound)
        is DescribeUserResponse.Result.SystemErrorMessage -> protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.stringSize(result.systemErrorMessage)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun DescribeUserResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (result is DescribeUserResponse.Result.SingleUserResult) protoMarshal.writeTag(10).writeMessage(result.singleUserResult)
    if (result is DescribeUserResponse.Result.MultiUserResult) protoMarshal.writeTag(18).writeMessage(result.multiUserResult)
    if (result is DescribeUserResponse.Result.UserNotFound) protoMarshal.writeTag(26).writeMessage(result.userNotFound)
    if (result is DescribeUserResponse.Result.SystemErrorMessage) protoMarshal.writeTag(34).writeString(result.systemErrorMessage)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun DescribeUserResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): DescribeUserResponse {
    var result: DescribeUserResponse.Result? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return DescribeUserResponse(result, protoUnmarshal.unknownFields())
        10 -> result = DescribeUserResponse.Result.SingleUserResult(protoUnmarshal.readMessage(com.kcibald.services.user.proto.DescribeUserResponse.SuccessSingleUserResult.Companion))
        18 -> result = DescribeUserResponse.Result.MultiUserResult(protoUnmarshal.readMessage(com.kcibald.services.user.proto.DescribeUserResponse.SuccessMultiUserResult.Companion))
        26 -> result = DescribeUserResponse.Result.UserNotFound(protoUnmarshal.readMessage(com.kcibald.services.user.proto.Empty.Companion))
        34 -> result = DescribeUserResponse.Result.SystemErrorMessage(protoUnmarshal.readString())
        else -> protoUnmarshal.unknownField()
    }
}

private fun DescribeUserResponse.SuccessSingleUserResult.protoMergeImpl(plus: DescribeUserResponse.SuccessSingleUserResult?): DescribeUserResponse.SuccessSingleUserResult = plus?.copy(
    result = result?.plus(plus.result) ?: plus.result,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun DescribeUserResponse.SuccessSingleUserResult.protoSizeImpl(): Int {
    var protoSize = 0
    if (result != null) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(result)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun DescribeUserResponse.SuccessSingleUserResult.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (result != null) protoMarshal.writeTag(10).writeMessage(result)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun DescribeUserResponse.SuccessSingleUserResult.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): DescribeUserResponse.SuccessSingleUserResult {
    var result: com.kcibald.services.user.proto.User? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return DescribeUserResponse.SuccessSingleUserResult(result, protoUnmarshal.unknownFields())
        10 -> result = protoUnmarshal.readMessage(com.kcibald.services.user.proto.User.Companion)
        else -> protoUnmarshal.unknownField()
    }
}

private fun DescribeUserResponse.SuccessMultiUserResult.protoMergeImpl(plus: DescribeUserResponse.SuccessMultiUserResult?): DescribeUserResponse.SuccessMultiUserResult = plus?.copy(
    result = result + plus.result,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun DescribeUserResponse.SuccessMultiUserResult.protoSizeImpl(): Int {
    var protoSize = 0
    if (result.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(1) * result.size) + result.sumBy(pbandk.Sizer::messageSize)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun DescribeUserResponse.SuccessMultiUserResult.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (result.isNotEmpty()) result.forEach { protoMarshal.writeTag(10).writeMessage(it) }
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun DescribeUserResponse.SuccessMultiUserResult.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): DescribeUserResponse.SuccessMultiUserResult {
    var result: pbandk.ListWithSize.Builder<com.kcibald.services.user.proto.User>? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return DescribeUserResponse.SuccessMultiUserResult(pbandk.ListWithSize.Builder.fixed(result), protoUnmarshal.unknownFields())
        10 -> result = protoUnmarshal.readRepeatedMessage(result, com.kcibald.services.user.proto.User.Companion, true)
        else -> protoUnmarshal.unknownField()
    }
}
