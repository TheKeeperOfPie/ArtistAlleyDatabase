package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.data.toCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTitle
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

    override val mediaId: String get() = media.id.toString()
    override val image get() = media.coverImage?.extraLarge

    override val rating get() = media.averageScore
    override val popularity get() = media.popularity

    override val nextAiringEpisode = media.nextAiringEpisode?.let {
        NextAiringEpisode(
            episode = it.episode,
            airingAt = Instant.fromEpochSeconds(it.airingAt.toLong()),
        )
    }

    // TODO: Favorite local overrides
    override val isFavourite get() = media.isFavourite

    override val isAdult get() = media.isAdult
    override val bannerImageUrl get() = media.bannerImage
    override val coverImageUrl get() = coverImage?.url

    override val titleRomaji get() = media.title?.romaji
    override val titleEnglish get() = media.title?.english
    override val titleNative get() = media.title?.native
    override val mediaType = media.type?.toMediaType()

    override val format get() = media.format
    override val status get() = media.status
    override val season get() = media.season
    override val seasonYear get() = media.seasonYear
    override val startDate get() = media.startDate
    override val episodes get() = media.episodes
    override val chapters: Int? get() = media.chapters
    override val volumes: Int? get() = media.volumes
    override val title = media.title?.toTitle()
    override val coverImage = media.coverImage?.toCoverImage()
    override val bannerImage get() = media.bannerImage

    override val mediaListStatus get() = mediaFilterable.mediaListStatus
    override val progress get() = mediaFilterable.progress
    override val progressVolumes get() = mediaFilterable.progressVolumes
    override val scoreRaw get() = mediaFilterable.scoreRaw
    override val ignored get() = mediaFilterable.ignored

    @Composable
    override fun primaryTitle() = media.title?.primaryTitle()

    object Provider : MediaEntryProvider<MediaPreviewWithDescription, MediaPreviewWithDescriptionEntry> {
        override fun mediaEntry(media: MediaPreviewWithDescription) = MediaPreviewWithDescriptionEntry(media)
        override fun mediaFilterable(entry: MediaPreviewWithDescriptionEntry) = entry.mediaFilterable
        override fun media(entry: MediaPreviewWithDescriptionEntry) = entry.media
        override fun copyMediaEntry(entry: MediaPreviewWithDescriptionEntry, data: MediaFilterableData) =
            entry.copy(mediaFilterable = data)
        override fun id(entry: MediaPreviewWithDescriptionEntry) = entry.mediaFilterable.mediaId
    }
}
