package com.thekeeperofpie.artistalleydatabase.anime.data

import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.StaffDetailsStaffMediaPage
import com.anilist.data.fragment.UserFavoriteMediaNode
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import kotlinx.datetime.Instant

// Temporary utils until all direct AniList GraphQL references are removed from the module

fun MediaPreviewWithDescription.CoverImage.toCoverImage() = CoverImage(
    url = extraLarge,
    color = color?.let(ComposeColorUtils::hexToColor),
)

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
