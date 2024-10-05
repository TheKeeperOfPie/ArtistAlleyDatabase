package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.anilist.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.data.toCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.data.toMediaType
import com.thekeeperofpie.artistalleydatabase.anime.data.toTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import kotlinx.datetime.Instant

@Immutable
data class MediaPreviewWithDescriptionEntry(
    override val media: MediaPreviewWithDescription,
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
) : AnimeMediaListRow.Entry, AnimeMediaLargeCard.Entry, MediaGridCard.Entry,
    AnimeMediaCompactListRow.Entry {
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val type = media.type
    override val averageScore = media.averageScore

    override val description = media.description
        ?.let(MediaUtils::stripDoubleNewlinesFromDescription)
        ?.let(::htmlToAnnotatedString)
    override val tags = MediaUtils.buildTags(
        media = media,
        showLessImportantTags = mediaFilterable.showLessImportantTags,
        showSpoilerTags = mediaFilterable.showSpoilerTags,
    )

    override val mediaId: String
        get() = media.id.toString()
    override val image
        get() = media.coverImage?.extraLarge

    override val rating
        get() = media.averageScore
    override val popularity
        get() = media.popularity

    override val nextAiringEpisode = media.nextAiringEpisode?.let {
        NextAiringEpisode(
            episode = it.episode,
            airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
        )
    }

    // TODO: Favorite local overrides
    override val isFavourite
        get() = media.isFavourite

    override val isAdult
        get() = media.isAdult
    override val bannerImageUrl
        get() = media.bannerImage
    override val coverImageUrl
        get() = coverImage?.url

    override val titleRomaji
        get() = media.title?.romaji
    override val titleEnglish
        get() = media.title?.english
    override val titleNative
        get() = media.title?.native
    override val mediaType = media.type?.toMediaType()

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
    override val chapters: Int?
        get() = media.chapters
    override val volumes: Int?
        get() = media.volumes
    override val title = media.title?.toTitle()
    override val coverImage = media.coverImage?.toCoverImage()
    override val bannerImage
        get() = media.bannerImage

    override val mediaListStatus get() = mediaFilterable.mediaListStatus
    override val progress get() = mediaFilterable.progress
    override val progressVolumes get() = mediaFilterable.progressVolumes
    override val scoreRaw get() = mediaFilterable.scoreRaw
    override val ignored get() = mediaFilterable.ignored

    @Composable
    override fun primaryTitle() = media.title?.primaryTitle()
}
