package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.Serializable

@Serializable
internal data class Connection(
    val type: Type? = null,
    val revoked: Boolean? = null,
    val verified: Boolean,
) {
    @Serializable(with = Type.Serializer::class)
    enum class Type(val value: String) {
        BLUESKY("bluesky"),
        INSTAGRAM("instagram"),
        TWITCH("twitch"),
        YOUTUBE("youtube"),
        X("twitter"),
        ;

        object Serializer : NullableStringEnumSerializer<Type>(
            entries = Type.entries,
            serialName = "Connection.Type",
            value = { it.value },
        )
    }
}
