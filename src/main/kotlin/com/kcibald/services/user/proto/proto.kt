package com.kcibald.services.user.proto

data class AuthenticationRequest(
    val userName: String = "",
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
    val error: Error? = null,
    val user: com.kcibald.services.user.proto.UserWithRole? = null,
    val bannedInfo: BannedInfo? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<AuthenticationResponse> {
    sealed class Error {
        data class ErrorMessage(val errorMessage: String = "") : Error()
    }

    sealed class BannedInfo {
        data class Message(val message: String = "") : BannedInfo()
    }

    override operator fun plus(other: AuthenticationResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<AuthenticationResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = AuthenticationResponse.protoUnmarshalImpl(u)
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

data class UserWithRole(
    val user: com.kcibald.services.user.proto.User? = null,
    val role: com.kcibald.services.user.proto.Roles? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<UserWithRole> {
    override operator fun plus(other: UserWithRole?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<UserWithRole> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UserWithRole.protoUnmarshalImpl(u)
    }
}

data class Roles(
    val role: List<String> = emptyList(),
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<Roles> {
    override operator fun plus(other: Roles?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<Roles> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = Roles.protoUnmarshalImpl(u)
    }
}

private fun AuthenticationRequest.protoMergeImpl(plus: AuthenticationRequest?): AuthenticationRequest = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun AuthenticationRequest.protoSizeImpl(): Int {
    var protoSize = 0
    if (userName.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(userName)
    if (plainPassword.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(plainPassword)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun AuthenticationRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (userName.isNotEmpty()) protoMarshal.writeTag(10).writeString(userName)
    if (plainPassword.isNotEmpty()) protoMarshal.writeTag(18).writeString(plainPassword)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun AuthenticationRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): AuthenticationRequest {
    var userName = ""
    var plainPassword = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return AuthenticationRequest(userName, plainPassword, protoUnmarshal.unknownFields())
        10 -> userName = protoUnmarshal.readString()
        18 -> plainPassword = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun AuthenticationResponse.protoMergeImpl(plus: AuthenticationResponse?): AuthenticationResponse = plus?.copy(
    error = plus.error ?: error,
    user = user?.plus(plus.user) ?: plus.user,
    bannedInfo = plus.bannedInfo ?: bannedInfo,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun AuthenticationResponse.protoSizeImpl(): Int {
    var protoSize = 0
    when (error) {
        is AuthenticationResponse.Error.ErrorMessage -> protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(error.errorMessage)
    }
    if (user != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(user)
    when (bannedInfo) {
        is AuthenticationResponse.BannedInfo.Message -> protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(bannedInfo.message)
    }
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun AuthenticationResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (error is AuthenticationResponse.Error.ErrorMessage) protoMarshal.writeTag(10).writeString(error.errorMessage)
    if (user != null) protoMarshal.writeTag(18).writeMessage(user)
    if (bannedInfo is AuthenticationResponse.BannedInfo.Message) protoMarshal.writeTag(26).writeString(bannedInfo.message)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun AuthenticationResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): AuthenticationResponse {
    var error: AuthenticationResponse.Error? = null
    var user: com.kcibald.services.user.proto.UserWithRole? = null
    var bannedInfo: AuthenticationResponse.BannedInfo? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return AuthenticationResponse(error, user, bannedInfo, protoUnmarshal.unknownFields())
        10 -> error = AuthenticationResponse.Error.ErrorMessage(protoUnmarshal.readString())
        18 -> user = protoUnmarshal.readMessage(com.kcibald.services.user.proto.UserWithRole.Companion)
        26 -> bannedInfo = AuthenticationResponse.BannedInfo.Message(protoUnmarshal.readString())
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

private fun UserWithRole.protoMergeImpl(plus: UserWithRole?): UserWithRole = plus?.copy(
    user = user?.plus(plus.user) ?: plus.user,
    role = role?.plus(plus.role) ?: plus.role,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun UserWithRole.protoSizeImpl(): Int {
    var protoSize = 0
    if (user != null) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(user)
    if (role != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(role)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UserWithRole.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (user != null) protoMarshal.writeTag(10).writeMessage(user)
    if (role != null) protoMarshal.writeTag(18).writeMessage(role)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun UserWithRole.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UserWithRole {
    var user: com.kcibald.services.user.proto.User? = null
    var role: com.kcibald.services.user.proto.Roles? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return UserWithRole(user, role, protoUnmarshal.unknownFields())
        10 -> user = protoUnmarshal.readMessage(com.kcibald.services.user.proto.User.Companion)
        18 -> role = protoUnmarshal.readMessage(com.kcibald.services.user.proto.Roles.Companion)
        else -> protoUnmarshal.unknownField()
    }
}

private fun Roles.protoMergeImpl(plus: Roles?): Roles = plus?.copy(
    role = role + plus.role,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun Roles.protoSizeImpl(): Int {
    var protoSize = 0
    if (role.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(1) * role.size) + role.sumBy(pbandk.Sizer::stringSize)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun Roles.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (role.isNotEmpty()) role.forEach { protoMarshal.writeTag(10).writeString(it) }
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun Roles.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): Roles {
    var role: pbandk.ListWithSize.Builder<String>? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return Roles(pbandk.ListWithSize.Builder.fixed(role), protoUnmarshal.unknownFields())
        10 -> role = protoUnmarshal.readRepeated(role, protoUnmarshal::readString, true)
        else -> protoUnmarshal.unknownField()
    }
}
