package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.Composable
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.anilist.fragment.HomeMedia
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.data.CoverImage
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.data.Title
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.StringResource

data class AnimeHomeDataEntry(
    val lists: List<RowData>?,
) {
    data class MediaEntry(
        val media: HomeMedia,
        val mediaFilterable: MediaFilterableData = MediaFilterableData(
            mediaId = media.id.toString(),
            isAdult = media.isAdult,
            mediaListStatus = media.mediaListEntry?.status?.toMediaListStatus(),
            progress = media.mediaListEntry?.progress,
            progressVolumes = media.mediaListEntry?.progressVolumes,
            scoreRaw = media.mediaListEntry?.score,
            ignored = false,
            showLessImportantTags = false,
            showSpoilerTags = false,
        ),
    ) : AnimeMediaLargeCard.Entry {
        override val mediaId = media.id.toString()
        override val image get() = media.coverImage?.extraLarge
        override val color get() = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)

        override val rating get() = media.averageScore
        override val popularity get() = media.popularity

        override val nextAiringEpisode = media.nextAiringEpisode?.let {
            NextAiringEpisode(
                episode = it.episode,
                airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
            )
        }
        override val isFavourite get() = media.isFavourite
        override val ignored get() = mediaFilterable.ignored
        override val type get() = media.type

        override val isAdult get() = media.isAdult
        override val bannerImageUrl get() = bannerImage
        override val coverImageUrl get() = coverImage?.url

        override val titleRomaji get() = media.title?.romaji
        override val titleEnglish get() = media.title?.english
        override val titleNative get() = media.title?.native
        override val mediaType = when (media.type) {
            com.anilist.type.MediaType.ANIME -> MediaType.ANIME
            com.anilist.type.MediaType.MANGA -> MediaType.MANGA
            com.anilist.type.MediaType.UNKNOWN__ -> MediaType.UNKNOWN
            null -> null
        }
        override val mediaListStatus get() = mediaFilterable.mediaListStatus
        override val progress get() = mediaFilterable.progress
        override val progressVolumes get() = mediaFilterable.progressVolumes
        override val scoreRaw get() = mediaFilterable.scoreRaw

        override val format get() = media.format
        override val status get() = media.status
        override val season get() = media.season
        override val seasonYear get() = media.seasonYear
        override val startDate get() = media.startDate
        override val episodes get() = media.episodes
        override val chapters get() = media.chapters
        override val volumes get() = media.volumes
        override val title = media.title?.let {
            Title(
                userPreferred = it.userPreferred,
                romaji = it.romaji,
                english = it.english,
                native = it.native,
            )
        }
        override val coverImage = media.coverImage?.let {
            CoverImage(
                url = it.extraLarge,
                color = it.color?.let(ComposeColorUtils::hexToColor),
            )
        }
        override val bannerImage
            get() = media.bannerImage

        @Composable
        override fun primaryTitle() = MediaUtils.userPreferredTitle(
            userPreferred = media.title?.userPreferred,
            romaji = titleRomaji,
            english = titleEnglish,
            native = titleNative,
        )

        // So that enough meaningful text is shown, strip any double newlines
        override val description = media.description
            ?.let(MediaUtils::stripDoubleNewlinesFromDescription)
            ?.let(::htmlToAnnotatedString)
        override val tags = media.tags?.asSequence()
            ?.filterNotNull()
            ?.filter {
                mediaFilterable.showLessImportantTags
                        || it.category !in MediaUtils.LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
            }
            ?.filter {
                mediaFilterable.showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
            }
            ?.map(::AnimeMediaTagEntry)
            ?.distinctBy { it.id }?.toList()
            .orEmpty()
    }

    data class RowData(
        val id: String,
        val titleRes: StringResource,
        val entries: List<MediaEntry>?,
        val viewAllRoute: AnimeDestination,
    )
}
