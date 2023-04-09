package com.thekeeperofpie.artistalleydatabase.anime.utils

import com.anilist.type.MediaFormat
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anime.R

fun MediaStatus?.toTextRes() = when (this) {
    MediaStatus.FINISHED -> R.string.anime_media_status_finished
    MediaStatus.RELEASING -> R.string.anime_media_status_releasing
    MediaStatus.NOT_YET_RELEASED -> R.string.anime_media_status_not_yet_released
    MediaStatus.CANCELLED -> R.string.anime_media_status_cancelled
    MediaStatus.HIATUS -> R.string.anime_media_status_hiatus
    else -> R.string.anime_media_status_unknown
}

fun MediaFormat?.toTextRes() = when (this) {
    MediaFormat.TV -> R.string.anime_media_format_tv
    MediaFormat.TV_SHORT -> R.string.anime_media_format_tv_short
    MediaFormat.MOVIE -> R.string.anime_media_format_movie
    MediaFormat.SPECIAL -> R.string.anime_media_format_special
    MediaFormat.OVA -> R.string.anime_media_format_ova
    MediaFormat.ONA -> R.string.anime_media_format_ona
    MediaFormat.MUSIC -> R.string.anime_media_format_music
    // MANGA, NOVEL, and ONE_SHOT excluded since not anime
    else -> R.string.anime_media_format_unknown
}