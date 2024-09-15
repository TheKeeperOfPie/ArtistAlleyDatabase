package com.thekeeperofpie.artistalleydatabase.anime.data

import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.StaffDetailsStaffMediaPage
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import kotlinx.datetime.Instant

// Temporary utils until all direct AniList GraphQL references are removed from the module

fun MediaType.toMediaType() = when (this) {
    MediaType.ANIME -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.ANIME
    MediaType.MANGA -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.MANGA
    MediaType.UNKNOWN__ -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaType.UNKNOWN
}

fun MediaPreviewWithDescription.Title.toTitle() =
    Title(
        userPreferred = userPreferred,
        romaji = romaji,
        english = english,
        native = native,
    )

fun MediaPreviewWithDescription.CoverImage.toCoverImage() = CoverImage(
    url = extraLarge,
    color = color?.let(ComposeColorUtils::hexToColor),
)

fun com.anilist.type.MediaListStatus.toMediaListStatus() = when (this) {
    com.anilist.type.MediaListStatus.CURRENT -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.CURRENT
    com.anilist.type.MediaListStatus.PLANNING -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PLANNING
    com.anilist.type.MediaListStatus.COMPLETED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.COMPLETED
    com.anilist.type.MediaListStatus.DROPPED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.DROPPED
    com.anilist.type.MediaListStatus.PAUSED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PAUSED
    com.anilist.type.MediaListStatus.REPEATING -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.REPEATING
    com.anilist.type.MediaListStatus.UNKNOWN__ -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN
}

fun com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.toMediaListStatus() = when (this) {
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.CURRENT -> com.anilist.type.MediaListStatus.CURRENT
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PLANNING -> com.anilist.type.MediaListStatus.PLANNING
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.COMPLETED -> com.anilist.type.MediaListStatus.COMPLETED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.DROPPED -> com.anilist.type.MediaListStatus.DROPPED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PAUSED -> com.anilist.type.MediaListStatus.PAUSED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.REPEATING -> com.anilist.type.MediaListStatus.REPEATING
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN -> com.anilist.type.MediaListStatus.UNKNOWN__
}

fun MediaCompactWithTags.NextAiringEpisode.toNextAiringEpisode() = NextAiringEpisode(
    episode = episode,
    airingAt = Instant.fromEpochSeconds(airingAt.toLong()),
)

fun UserFavoriteMediaNode.NextAiringEpisode.toNextAiringEpisode() = NextAiringEpisode(
    episode = episode,
    airingAt = Instant.fromEpochSeconds(airingAt.toLong()),
)

fun StaffDetailsStaffMediaPage.Edge.Node.NextAiringEpisode.toNextAiringEpisode() = NextAiringEpisode(
    episode = episode,
    airingAt = Instant.fromEpochSeconds(airingAt.toLong()),
)
