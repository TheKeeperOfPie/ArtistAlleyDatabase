package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.Composable
import com.anilist.fragment.HomeMedia
import com.anilist.fragment.MediaHeaderData
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils

data class AnimeHomeDataEntry(
    val lists: List<RowData>?,
) {
    data class MediaEntry(
        val media: HomeMedia,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = media.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : AnimeMediaLargeCard.Entry, MediaHeaderData by media {
        override val mediaId = media.id.toString()
        override val image
            get() = media.coverImage?.extraLarge
        override val imageBanner
            get() = media.bannerImage
        override val color
            get() = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)

        override val rating
            get() = media.averageScore
        override val popularity
            get() = media.popularity

        override val nextAiringEpisode
            get() = media.nextAiringEpisode
        override val type
            get() = media.type

        override val isAdult
            get() = media.isAdult

        override val titleRomaji
            get() = media.title?.romaji
        override val titleEnglish
            get() = media.title?.english
        override val titleNative
            get() = media.title?.native

        override val format
            get() = media.format
        override val status
            get() = media.status
        override val season
            get() = media.season
        override val seasonYear
            get() = media.seasonYear
        override val episodes
            get() = media.episodes
        override val chapters
            get() = media.chapters
        override val volumes
            get() = media.volumes

        @Composable
        override fun primaryTitle() = MediaUtils.userPreferredTitle(
            userPreferred = media.title?.userPreferred,
            romaji = titleRomaji,
            english = titleEnglish,
            native = titleNative,
        )

        // So that enough meaningful text is shown, strip any double newlines
        override val description = media.description?.replace("<br><br />\n<br><br />\n", "\n")
        override val tags = media.tags?.asSequence()
            ?.filterNotNull()
            ?.filter {
                showLessImportantTags
                        || it.category !in MediaUtils.LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
            }
            ?.filter {
                showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
            }
            ?.map(::AnimeMediaTagEntry)
            ?.distinctBy { it.id }?.toList()
            .orEmpty()
    }

    data class RowData(
        val id: String,
        val titleRes: Int,
        val entries: List<MediaEntry>?,
        val viewAllRoute: AnimeDestination,
    )
}
