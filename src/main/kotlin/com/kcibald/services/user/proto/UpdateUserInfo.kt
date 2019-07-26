package com.kcibald.services.user.proto

data class UpdateUserInfoRequest(
    val queryBy: QueryBy? = null,
    val target: Target? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<UpdateUserInfoRequest> {
    sealed class QueryBy {
        data class UserId(val userId: String = "") : QueryBy()
        data class UrlKey(val urlKey: String = "") : QueryBy()
    }

    sealed class Target {
        data class UserName(val userName: String = "") : Target()
        data class Signature(val signature: String = "") : Target()
        data class AvatarKey(val avatarKey: String = "") : Target()
        data class Password(val password: com.kcibald.services.user.proto.SafeUpdateOperation) : Target()
    }

    override operator fun plus(other: UpdateUserInfoRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<UpdateUserInfoRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UpdateUserInfoRequest.protoUnmarshalImpl(u)
    }
}

data class SafeUpdateOperation(
    val previous: String = "",
    val after: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<SafeUpdateOperation> {
    override operator fun plus(other: SafeUpdateOperation?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<SafeUpdateOperation> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = SafeUpdateOperation.protoUnmarshalImpl(u)
    }
}

data class UpdateUserInfoResponse(
    val responseType: com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes = com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.fromValue(0),
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<UpdateUserInfoResponse> {
    override operator fun plus(other: UpdateUserInfoResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<UpdateUserInfoResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UpdateUserInfoResponse.protoUnmarshalImpl(u)
    }

    data class GeneralResponseTypes(override val value: Int) : pbandk.Message.Enum {
        companion object : pbandk.Message.Enum.Companion<GeneralResponseTypes> {
            val SUCCESS = GeneralResponseTypes(0)
            val FAILURE_UNSAFE_UPDATE = GeneralResponseTypes(1)
            val DB_ERROR = GeneralResponseTypes(2)
            val INTERNAL_ERROR = GeneralResponseTypes(3)

            override fun fromValue(value: Int) = when (value) {
                0 -> SUCCESS
                1 -> FAILURE_UNSAFE_UPDATE
                2 -> DB_ERROR
                3 -> INTERNAL_ERROR
                else -> GeneralResponseTypes(value)
            }
        }
    }
}

private fun UpdateUserInfoRequest.protoMergeImpl(plus: UpdateUserInfoRequest?): UpdateUserInfoRequest = plus?.copy(
    queryBy = plus.queryBy ?: queryBy,
    target = when {
        target is UpdateUserInfoRequest.Target.Password && plus.target is UpdateUserInfoRequest.Target.Password ->
            UpdateUserInfoRequest.Target.Password(target.password + plus.target.password)
        else ->
            plus.target ?: target
    },
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun UpdateUserInfoRequest.protoSizeImpl(): Int {
    var protoSize = 0
    when (queryBy) {
        is UpdateUserInfoRequest.QueryBy.UserId -> protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(queryBy.userId)
        is UpdateUserInfoRequest.QueryBy.UrlKey -> protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(queryBy.urlKey)
    }
    when (target) {
        is UpdateUserInfoRequest.Target.UserName -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(target.userName)
        is UpdateUserInfoRequest.Target.Signature -> protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.stringSize(target.signature)
        is UpdateUserInfoRequest.Target.AvatarKey -> protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.stringSize(target.avatarKey)
        is UpdateUserInfoRequest.Target.Password -> protoSize += pbandk.Sizer.tagSize(6) + pbandk.Sizer.messageSize(target.password)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UpdateUserInfoRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (queryBy is UpdateUserInfoRequest.QueryBy.UserId) protoMarshal.writeTag(10).writeString(queryBy.userId)
    if (queryBy is UpdateUserInfoRequest.QueryBy.UrlKey) protoMarshal.writeTag(18).writeString(queryBy.urlKey)
    if (target is UpdateUserInfoRequest.Target.UserName) protoMarshal.writeTag(26).writeString(target.userName)
    if (target is UpdateUserInfoRequest.Target.Signature) protoMarshal.writeTag(34).writeString(target.signature)
    if (target is UpdateUserInfoRequest.Target.AvatarKey) protoMarshal.writeTag(42).writeString(target.avatarKey)
    if (target is UpdateUserInfoRequest.Target.Password) protoMarshal.writeTag(50).writeMessage(target.password)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun UpdateUserInfoRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UpdateUserInfoRequest {
    var queryBy: UpdateUserInfoRequest.QueryBy? = null
    var target: UpdateUserInfoRequest.Target? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return UpdateUserInfoRequest(queryBy, target, protoUnmarshal.unknownFields())
        10 -> queryBy = UpdateUserInfoRequest.QueryBy.UserId(protoUnmarshal.readString())
        18 -> queryBy = UpdateUserInfoRequest.QueryBy.UrlKey(protoUnmarshal.readString())
        26 -> target = UpdateUserInfoRequest.Target.UserName(protoUnmarshal.readString())
        34 -> target = UpdateUserInfoRequest.Target.Signature(protoUnmarshal.readString())
        42 -> target = UpdateUserInfoRequest.Target.AvatarKey(protoUnmarshal.readString())
        50 -> target = UpdateUserInfoRequest.Target.Password(protoUnmarshal.readMessage(com.kcibald.services.user.proto.SafeUpdateOperation.Companion))
        else -> protoUnmarshal.unknownField()
    }
}

private fun SafeUpdateOperation.protoMergeImpl(plus: SafeUpdateOperation?): SafeUpdateOperation = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun SafeUpdateOperation.protoSizeImpl(): Int {
    var protoSize = 0
    if (previous.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(previous)
    if (after.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(after)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun SafeUpdateOperation.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (previous.isNotEmpty()) protoMarshal.writeTag(10).writeString(previous)
    if (after.isNotEmpty()) protoMarshal.writeTag(18).writeString(after)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun SafeUpdateOperation.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): SafeUpdateOperation {
    var previous = ""
    var after = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return SafeUpdateOperation(previous, after, protoUnmarshal.unknownFields())
        10 -> previous = protoUnmarshal.readString()
        18 -> after = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun UpdateUserInfoResponse.protoMergeImpl(plus: UpdateUserInfoResponse?): UpdateUserInfoResponse = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun UpdateUserInfoResponse.protoSizeImpl(): Int {
    var protoSize = 0
    if (responseType.value != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.enumSize(responseType)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UpdateUserInfoResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (responseType.value != 0) protoMarshal.writeTag(8).writeEnum(responseType)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun UpdateUserInfoResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UpdateUserInfoResponse {
    var responseType: com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes = com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.fromValue(0)
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return UpdateUserInfoResponse(responseType, protoUnmarshal.unknownFields())
        8 -> responseType = protoUnmarshal.readEnum(com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.Companion)
        else -> protoUnmarshal.unknownField()
    }
}
