package com.thekeeperofpie.artistalleydatabase.anime.media.data

import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.data.Title

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

fun MediaListStatus.toMediaListStatus() = when (this) {
    MediaListStatus.CURRENT -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.CURRENT
    MediaListStatus.PLANNING -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PLANNING
    MediaListStatus.COMPLETED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.COMPLETED
    MediaListStatus.DROPPED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.DROPPED
    MediaListStatus.PAUSED -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PAUSED
    MediaListStatus.REPEATING -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.REPEATING
    MediaListStatus.UNKNOWN__ -> com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN
}

fun com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.toMediaListStatus() = when (this) {
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.CURRENT -> MediaListStatus.CURRENT
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PLANNING -> MediaListStatus.PLANNING
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.COMPLETED -> MediaListStatus.COMPLETED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.DROPPED -> MediaListStatus.DROPPED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.PAUSED -> MediaListStatus.PAUSED
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.REPEATING -> MediaListStatus.REPEATING
    com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN -> MediaListStatus.UNKNOWN__
}
