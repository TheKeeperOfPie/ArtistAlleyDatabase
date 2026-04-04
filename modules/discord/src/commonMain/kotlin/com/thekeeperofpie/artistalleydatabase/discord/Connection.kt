package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class Connection(
    val type: Type? = null,
    val name: String,
    val revoked: Boolean? = null,
    val verified: Boolean,
) {
    @Serializable(with = Type.Serializer::class)
    enum class Type(val value: String) {
        BLUESKY("bluesky"),
        FACEBOOK("facebook"),
        INSTAGRAM("instagram"),
        TIK_TOK("tiktok"),
        TWITCH("twitch"),
        YOU_TUBE("youtube"),
        X("twitter"),
        ;

        object Serializer : NullableStringEnumSerializer<Type>(
            entries = Type.entries,
            serialName = "Connection.Type",
            value = { it.value },
        )
    }
}
