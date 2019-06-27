package com.kcibald.services.user.proto

data class Empty(
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<Empty> {
    override operator fun plus(other: Empty?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<Empty> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = Empty.protoUnmarshalImpl(u)
    }
}

data class User(
    val userId: String = "",
    val userName: String = "",
    val urlKey: String = "",
    val signature: String = "",
    val avatarKey: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<User> {
    override operator fun plus(other: User?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<User> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = User.protoUnmarshalImpl(u)
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

private fun Empty.protoMergeImpl(plus: Empty?): Empty = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun Empty.protoSizeImpl(): Int {
    var protoSize = 0
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun Empty.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun Empty.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): Empty {
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return Empty(protoUnmarshal.unknownFields())
        else -> protoUnmarshal.unknownField()
    }
}

private fun User.protoMergeImpl(plus: User?): User = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun User.protoSizeImpl(): Int {
    var protoSize = 0
    if (userId.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(userId)
    if (userName.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(userName)
    if (urlKey.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(urlKey)
    if (signature.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.stringSize(signature)
    if (avatarKey.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.stringSize(avatarKey)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun User.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (userId.isNotEmpty()) protoMarshal.writeTag(10).writeString(userId)
    if (userName.isNotEmpty()) protoMarshal.writeTag(18).writeString(userName)
    if (urlKey.isNotEmpty()) protoMarshal.writeTag(26).writeString(urlKey)
    if (signature.isNotEmpty()) protoMarshal.writeTag(34).writeString(signature)
    if (avatarKey.isNotEmpty()) protoMarshal.writeTag(42).writeString(avatarKey)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun User.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): User {
    var userId = ""
    var userName = ""
    var urlKey = ""
    var signature = ""
    var avatarKey = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return User(userId, userName, urlKey, signature,
            avatarKey, protoUnmarshal.unknownFields())
        10 -> userId = protoUnmarshal.readString()
        18 -> userName = protoUnmarshal.readString()
        26 -> urlKey = protoUnmarshal.readString()
        34 -> signature = protoUnmarshal.readString()
        42 -> avatarKey = protoUnmarshal.readString()
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
