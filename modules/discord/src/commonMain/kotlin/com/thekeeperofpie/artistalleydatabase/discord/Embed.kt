package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.Serializable

@Serializable
data class Embed(
    val title: String? = null,
    val type: Type? = null,
    val description: String? = null,
    val url: String? = null,
    val fields: List<Field>? = null,
    val image: Image? = null,
    val thumbnail: Image? = null,
    val color: Int? = null,
) {
    @Serializable(with = Type.Serializer::class)
    enum class Type(val value: String) {
        RICH("rich"),
        IMAGE("image"),
        VIDEO("video"),
        GIFV("gifv"),
        ARTICLE("article"),
        LINK("link"),
        POLL_RESULT("poll_result"),
        ;

        object Serializer :
            NullableStringEnumSerializer<Type>(
                entries = Type.entries,
                serialName = "Embed.Type",
                value = { it.value },
            )
    }

    @Serializable
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean? = null,
    )

    @Serializable
    data class Image constructor(
        val url: String,
        val flags: Int? = null,
    )
}
