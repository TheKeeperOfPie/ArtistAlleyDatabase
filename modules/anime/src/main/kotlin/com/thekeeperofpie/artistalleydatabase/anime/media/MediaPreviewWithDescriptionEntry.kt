package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils

@Immutable
data class MediaPreviewWithDescriptionEntry(
    override val media: MediaPreviewWithDescription,
    override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
    override val progress: Int? = media.mediaListEntry?.progress,
    override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
    override val scoreRaw: Double? = media.mediaListEntry?.score,
    override val ignored: Boolean = false,
    override val showLessImportantTags: Boolean = false,
    override val showSpoilerTags: Boolean = false,
) : AnimeMediaListRow.Entry, MediaStatusAware, AnimeMediaLargeCard.Entry, MediaGridCard.Entry,
    AnimeMediaCompactListRow.Entry {
    override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    override val type = media.type
    override val maxProgress = MediaUtils.maxProgress(media)
    override val maxProgressVolumes = media.volumes
    override val averageScore = media.averageScore

    // So that enough meaningful text is shown, strip any double newlines
    override val description = media.description?.replace("<br><br />\n<br><br />\n", "\n")
    override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)

    override val mediaId: String
        get() = media.id.toString()
    override val image
        get() = media.coverImage?.extraLarge
    override val imageBanner
        get() = media.bannerImage

    override val rating
        get() = media.averageScore
    override val popularity
        get() = media.popularity

    override val nextAiringEpisode
        get() = media.nextAiringEpisode?.episode
    override val nextAiringAiringAt: Int?
        get() = media.nextAiringEpisode?.airingAt

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
    override val chapters: Int?
        get() = media.chapters
    override val volumes: Int?
        get() = media.volumes

    @Composable
    override fun primaryTitle() = media.title?.primaryTitle()
}
