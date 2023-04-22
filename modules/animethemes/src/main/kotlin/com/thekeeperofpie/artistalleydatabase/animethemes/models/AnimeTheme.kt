package com.thekeeperofpie.artistalleydatabase.animethemes.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnimeTheme(
    val id: String,
    val slug: String,
    val type: Type? = null,
    val sequence: Int? = null,
    val song: Song? = null,
    @SerialName("animethemeentries")
    val animeThemeEntries: List<AnimeThemeEntry> = emptyList(),
) {

    enum class Type {
        @SerialName("OP")
        Opening,

        @SerialName("ED")
        Ending,
    }

    @Serializable
    data class Song(
        val id: String,
        val title: String = "",
        val artists: List<Artist> = emptyList(),
    ) {
        @Serializable
        data class Artist(
            val id: String,
            val name: String = "",
            @SerialName("as")
            val character: String? = null,
            val images: List<Images> = emptyList(),
        ) {
            @Serializable
            data class Images(
                val facet: Facet? = null,
                val link: String? = null,
            ) {
                @Serializable
                enum class Facet {
                    @SerialName("Large Cover")
                    LargeCover,
                    @SerialName("Small Cover")
                    SmallCover,
                }
            }
        }
    }
}
