package com.thekeeperofpie.artistalleydatabase.anime.media

import android.view.animation.PathInterpolator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone._18UpRating
import androidx.compose.ui.graphics.Color
import com.anilist.type.MediaFormat
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anime.R
import kotlin.math.absoluteValue

object MediaUtils {

    // Uses a cubic bezier to interpolate tag IDs to more distinct colors,
    // as the tag IDs are not uniformly distributed
    private val tagColorInterpolator = PathInterpolator(0.35f, 0.9f, 0.39f, 0.39f)

    // TODO: More distinct colors
    fun calculateTagColor(tagId: Int) = Color.hsl(
        hue = tagColorInterpolator.getInterpolation((tagId % 2000) / 2000f) * 360,
        lightness = 0.25f,
        saturation = 0.25f,
    )

    fun genreColor(name: String) = Color.hsl(
        hue = (name.hashCode() % 360f).absoluteValue,
        lightness = 0.25f,
        saturation = 0.25f,
    )

    fun tagLeadingIcon(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null
    ) = when {
        isAdult ?: false -> Icons.TwoTone._18UpRating
        (isGeneralSpoiler ?: false) || (isMediaSpoiler ?: false) ->
            Icons.Filled.Warning
        else -> null
    }

    fun tagLeadingIconContentDescription(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null
    ) = when {
        isAdult ?: false -> R.string.anime_media_tag_is_adult
        (isGeneralSpoiler ?: false) || (isMediaSpoiler
            ?: false) -> R.string.anime_media_tag_is_spoiler
        else -> null
    }
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

    fun MediaSeason?.toTextRes() = when (this) {
        null,
        MediaSeason.UNKNOWN__ -> R.string.anime_media_filter_airing_date_season_default
        MediaSeason.WINTER -> R.string.anime_media_filter_airing_date_season_winter
        MediaSeason.SPRING -> R.string.anime_media_filter_airing_date_season_spring
        MediaSeason.SUMMER -> R.string.anime_media_filter_airing_date_season_summer
        MediaSeason.FALL -> R.string.anime_media_filter_airing_date_season_fall
    }

    fun MediaSource?.toTextRes() = when (this) {
        MediaSource.ORIGINAL -> R.string.anime_media_filter_source_original
        MediaSource.ANIME -> R.string.anime_media_filter_source_anime
        MediaSource.COMIC -> R.string.anime_media_filter_source_comic
        MediaSource.DOUJINSHI -> R.string.anime_media_filter_source_doujinshi
        MediaSource.GAME -> R.string.anime_media_filter_source_game
        MediaSource.LIGHT_NOVEL -> R.string.anime_media_filter_source_light_novel
        MediaSource.LIVE_ACTION -> R.string.anime_media_filter_source_live_action
        MediaSource.MANGA -> R.string.anime_media_filter_source_manga
        MediaSource.MULTIMEDIA_PROJECT -> R.string.anime_media_filter_source_multimedia_project
        MediaSource.NOVEL -> R.string.anime_media_filter_source_novel
        MediaSource.OTHER -> R.string.anime_media_filter_source_other
        MediaSource.PICTURE_BOOK -> R.string.anime_media_filter_source_picture_book
        MediaSource.VIDEO_GAME -> R.string.anime_media_filter_source_video_game
        MediaSource.VISUAL_NOVEL -> R.string.anime_media_filter_source_visual_novel
        MediaSource.WEB_NOVEL -> R.string.anime_media_filter_source_web_novel
        MediaSource.UNKNOWN__,
        null -> R.string.anime_media_filter_source_unknown
    }

}