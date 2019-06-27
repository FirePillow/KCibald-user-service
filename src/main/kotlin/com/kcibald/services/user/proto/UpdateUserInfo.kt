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
        data class UserName(val userName: com.kcibald.services.user.proto.SafeUpdateOperation) : Target()
        data class Signature(val signature: com.kcibald.services.user.proto.SafeUpdateOperation) : Target()
        data class AvatarKey(val avatarKey: com.kcibald.services.user.proto.SafeUpdateOperation) : Target()
        data class Password(val password: com.kcibald.services.user.proto.SafeUpdateOperation) : Target()
    }

    override operator fun plus(other: UpdateUserInfoRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<UpdateUserInfoRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UpdateUserInfoRequest.protoUnmarshalImpl(u)
    }
}

data class UpdateUserInfoResponse(
    val responseType: com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes = com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.fromValue(0),
    val errorMessage: ErrorMessage? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<UpdateUserInfoResponse> {
    sealed class ErrorMessage {
        data class Content(val content: String = "") : ErrorMessage()
        data class Non(val non: com.kcibald.services.user.proto.Empty) : ErrorMessage()
    }

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
        target is UpdateUserInfoRequest.Target.UserName && plus.target is UpdateUserInfoRequest.Target.UserName ->
            UpdateUserInfoRequest.Target.UserName(target.userName + plus.target.userName)
        target is UpdateUserInfoRequest.Target.Signature && plus.target is UpdateUserInfoRequest.Target.Signature ->
            UpdateUserInfoRequest.Target.Signature(target.signature + plus.target.signature)
        target is UpdateUserInfoRequest.Target.AvatarKey && plus.target is UpdateUserInfoRequest.Target.AvatarKey ->
            UpdateUserInfoRequest.Target.AvatarKey(target.avatarKey + plus.target.avatarKey)
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
        is UpdateUserInfoRequest.Target.UserName -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.messageSize(target.userName)
        is UpdateUserInfoRequest.Target.Signature -> protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.messageSize(target.signature)
        is UpdateUserInfoRequest.Target.AvatarKey -> protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.messageSize(target.avatarKey)
        is UpdateUserInfoRequest.Target.Password -> protoSize += pbandk.Sizer.tagSize(6) + pbandk.Sizer.messageSize(target.password)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UpdateUserInfoRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (queryBy is UpdateUserInfoRequest.QueryBy.UserId) protoMarshal.writeTag(10).writeString(queryBy.userId)
    if (queryBy is UpdateUserInfoRequest.QueryBy.UrlKey) protoMarshal.writeTag(18).writeString(queryBy.urlKey)
    if (target is UpdateUserInfoRequest.Target.UserName) protoMarshal.writeTag(26).writeMessage(target.userName)
    if (target is UpdateUserInfoRequest.Target.Signature) protoMarshal.writeTag(34).writeMessage(target.signature)
    if (target is UpdateUserInfoRequest.Target.AvatarKey) protoMarshal.writeTag(42).writeMessage(target.avatarKey)
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
        26 -> target = UpdateUserInfoRequest.Target.UserName(protoUnmarshal.readMessage(com.kcibald.services.user.proto.SafeUpdateOperation.Companion))
        34 -> target = UpdateUserInfoRequest.Target.Signature(protoUnmarshal.readMessage(com.kcibald.services.user.proto.SafeUpdateOperation.Companion))
        42 -> target = UpdateUserInfoRequest.Target.AvatarKey(protoUnmarshal.readMessage(com.kcibald.services.user.proto.SafeUpdateOperation.Companion))
        50 -> target = UpdateUserInfoRequest.Target.Password(protoUnmarshal.readMessage(com.kcibald.services.user.proto.SafeUpdateOperation.Companion))
        else -> protoUnmarshal.unknownField()
    }
}

private fun UpdateUserInfoResponse.protoMergeImpl(plus: UpdateUserInfoResponse?): UpdateUserInfoResponse = plus?.copy(
    errorMessage = when {
        errorMessage is UpdateUserInfoResponse.ErrorMessage.Non && plus.errorMessage is UpdateUserInfoResponse.ErrorMessage.Non ->
            UpdateUserInfoResponse.ErrorMessage.Non(errorMessage.non + plus.errorMessage.non)
        else ->
            plus.errorMessage ?: errorMessage
    },
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun UpdateUserInfoResponse.protoSizeImpl(): Int {
    var protoSize = 0
    if (responseType.value != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.enumSize(responseType)
    when (errorMessage) {
        is UpdateUserInfoResponse.ErrorMessage.Content -> protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(errorMessage.content)
        is UpdateUserInfoResponse.ErrorMessage.Non -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.messageSize(errorMessage.non)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UpdateUserInfoResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (responseType.value != 0) protoMarshal.writeTag(8).writeEnum(responseType)
    if (errorMessage is UpdateUserInfoResponse.ErrorMessage.Content) protoMarshal.writeTag(18).writeString(errorMessage.content)
    if (errorMessage is UpdateUserInfoResponse.ErrorMessage.Non) protoMarshal.writeTag(26).writeMessage(errorMessage.non)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun UpdateUserInfoResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UpdateUserInfoResponse {
    var responseType: com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes = com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.fromValue(0)
    var errorMessage: UpdateUserInfoResponse.ErrorMessage? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return UpdateUserInfoResponse(responseType, errorMessage, protoUnmarshal.unknownFields())
        8 -> responseType = protoUnmarshal.readEnum(com.kcibald.services.user.proto.UpdateUserInfoResponse.GeneralResponseTypes.Companion)
        18 -> errorMessage = UpdateUserInfoResponse.ErrorMessage.Content(protoUnmarshal.readString())
        26 -> errorMessage = UpdateUserInfoResponse.ErrorMessage.Non(protoUnmarshal.readMessage(com.kcibald.services.user.proto.Empty.Companion))
        else -> protoUnmarshal.unknownField()
    }
}
