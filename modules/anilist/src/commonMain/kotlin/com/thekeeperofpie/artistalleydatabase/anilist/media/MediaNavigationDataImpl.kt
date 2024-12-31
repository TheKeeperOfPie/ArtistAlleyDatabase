package com.thekeeperofpie.artistalleydatabase.anilist.media

import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class MediaNavigationDataImpl(
    override val id: Int,
    override val title: TitleImpl?,
    override val coverImage: CoverImageImpl?,
    override val type: MediaType?,
    override val isAdult: Boolean?
) : MediaNavigationData {
    constructor(mediaNavigationData: MediaNavigationData) : this(
        mediaNavigationData.id,
        mediaNavigationData.title?.let(::TitleImpl),
        mediaNavigationData.coverImage?.let(::CoverImageImpl),
        mediaNavigationData.type,
        mediaNavigationData.isAdult,
    )

    @Serializable
    data class TitleImpl(
        override val __typename: String,
        override val userPreferred: String?,
        override val romaji: String?,
        override val english: String?,
        override val native: String?
    ) : MediaNavigationData.Title {
        constructor(title: MediaNavigationData.Title) : this(
            title.__typename,
            title.userPreferred,
            title.romaji,
            title.english,
            title.native,
        )
    }

    @Serializable
    data class CoverImageImpl(
        override val extraLarge: String?
    ) : MediaNavigationData.CoverImage {
        constructor(image: MediaNavigationData.CoverImage) : this(image.extraLarge)
    }
}
