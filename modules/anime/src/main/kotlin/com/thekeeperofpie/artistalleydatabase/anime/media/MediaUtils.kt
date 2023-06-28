package com.thekeeperofpie.artistalleydatabase.anime.media

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.twotone._18UpRating
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.anilist.MediaDetailsQuery
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRelation
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anime.R
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

object MediaUtils {

    // No better alternative to FORMAT_UTC
    // TODO: Find an alternative
    @Suppress("DEPRECATION")
    const val BASE_DATE_FORMAT_FLAGS = DateUtils.FORMAT_ABBREV_ALL

    val scoreDistributionColors = listOf(
        Color(210, 72, 45),
        Color(210, 100, 45),
        Color(210, 128, 45),
        Color(210, 155, 45),
        Color(210, 183, 45),
        Color(210, 210, 45),
        Color(183, 210, 45),
        Color(155, 210, 45),
        Color(128, 210, 45),
        Color(100, 210, 45),
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

    fun MediaListStatus?.toTextRes(anime: Boolean) = when (this) {
        MediaListStatus.CURRENT -> if (anime) {
            R.string.anime_media_list_status_current_anime
        } else {
            R.string.anime_media_list_status_current_not_anime
        }
        MediaListStatus.PLANNING -> R.string.anime_media_list_status_planning
        MediaListStatus.COMPLETED -> R.string.anime_media_list_status_completed
        MediaListStatus.DROPPED -> R.string.anime_media_list_status_dropped
        MediaListStatus.PAUSED -> R.string.anime_media_list_status_paused
        MediaListStatus.REPEATING -> R.string.anime_media_list_status_repeating
        MediaListStatus.UNKNOWN__ -> R.string.anime_media_list_status_unknown
        null -> R.string.anime_media_list_status_none
    }

    fun scoreFormatToText(score: Double, format: ScoreFormat) = when (format) {
        ScoreFormat.POINT_10_DECIMAL -> String.format("%.1f", score / 10f)
        ScoreFormat.POINT_10 -> (score.roundToInt() / 10).toString()
        ScoreFormat.POINT_100,
        ScoreFormat.POINT_5,
        ScoreFormat.POINT_3,
        ScoreFormat.UNKNOWN__ -> score.roundToInt().toString()
    }

    @Composable
    fun MediaListStatus?.toStatusText(
        mediaType: MediaType?,
        progress: Int,
        progressMax: Int,
        score: Double?,
        scoreFormat: ScoreFormat?,
    ) = when (this) {
        MediaListStatus.CURRENT -> {
            if (mediaType == MediaType.ANIME) {
                stringResource(
                    R.string.anime_media_details_fab_user_status_current_anime,
                    progress,
                    progressMax,
                )
            } else {
                stringResource(
                    R.string.anime_media_details_fab_user_status_current_not_anime,
                    progress,
                    progressMax,
                )
            }
        }
        MediaListStatus.PLANNING -> stringResource(
            R.string.anime_media_details_fab_user_status_planning
        )
        MediaListStatus.COMPLETED -> if (score == null || scoreFormat == null) {
            stringResource(R.string.anime_media_details_fab_user_status_completed)
        } else {
            // TODO: Show smileys instead for that score format
            stringResource(
                R.string.anime_media_details_fab_user_status_completed_with_score,
                scoreFormatToText(score, scoreFormat),
            )
        }
        MediaListStatus.DROPPED -> stringResource(
            R.string.anime_media_details_fab_user_status_dropped,
            progress,
            progressMax,
        )
        MediaListStatus.PAUSED -> stringResource(
            R.string.anime_media_details_fab_user_status_paused,
            progress,
            progressMax,
        )
        MediaListStatus.REPEATING -> stringResource(
            R.string.anime_media_details_fab_user_status_repeating,
            progress,
            progressMax,
        )
        MediaListStatus.UNKNOWN__, null -> stringResource(
            R.string.anime_media_details_fab_user_status_unknown
        )
    }

    @Composable
    fun MediaListStatus?.toStatusIcon(mediaType: MediaType?) = when (this) {
        MediaListStatus.CURRENT -> if (mediaType == MediaType.ANIME) {
            Icons.Filled.Monitor to R.string.anime_media_details_fab_user_status_current_anime_icon_content_description
        } else {
            Icons.Filled.MenuBook to R.string.anime_media_details_fab_user_status_current_not_anime_icon_content_description
        }
        MediaListStatus.PLANNING -> if (mediaType == MediaType.ANIME) {
            Icons.Filled.WatchLater
        } else {
            Icons.Filled.Bookmark
        } to R.string.anime_media_details_fab_user_status_planning_icon_content_description
        MediaListStatus.COMPLETED -> Icons.Filled.CheckBox to
                R.string.anime_media_details_fab_user_status_completed_icon_content_description
        MediaListStatus.DROPPED -> Icons.Filled.Delete to
                R.string.anime_media_details_fab_user_status_dropped_icon_content_description
        MediaListStatus.PAUSED -> Icons.Filled.PauseCircle to
                R.string.anime_media_details_fab_user_status_paused_icon_content_description
        MediaListStatus.REPEATING -> Icons.Filled.Repeat to
                R.string.anime_media_details_fab_user_status_repeating_icon_content_description
        MediaListStatus.UNKNOWN__, null -> Icons.Filled.Edit to
                R.string.anime_media_details_fab_user_status_edit_icon_content_description
    }

    fun MediaListStatus?.toColor() = when (this) {
        MediaListStatus.CURRENT -> Color(146, 86, 243)
        MediaListStatus.PLANNING -> Color(104, 214, 57)
        MediaListStatus.COMPLETED -> Color(2, 169, 255)
        MediaListStatus.DROPPED -> Color(0xFF810831)
        MediaListStatus.PAUSED -> Color(247, 121, 164)
        MediaListStatus.REPEATING -> Color(0xFFFF9000)
        MediaListStatus.UNKNOWN__, null -> Color.White
    }

    fun MediaFormat?.toTextRes() = when (this) {
        MediaFormat.TV -> R.string.anime_media_format_tv
        MediaFormat.TV_SHORT -> R.string.anime_media_format_tv_short
        MediaFormat.MOVIE -> R.string.anime_media_format_movie
        MediaFormat.SPECIAL -> R.string.anime_media_format_special
        MediaFormat.OVA -> R.string.anime_media_format_ova
        MediaFormat.ONA -> R.string.anime_media_format_ona
        MediaFormat.MUSIC -> R.string.anime_media_format_music
        MediaFormat.MANGA -> R.string.anime_media_format_manga
        MediaFormat.NOVEL -> R.string.anime_media_format_novel
        MediaFormat.ONE_SHOT -> R.string.anime_media_format_one_shot
        MediaFormat.UNKNOWN__,
        null -> R.string.anime_media_format_unknown
    }

    fun MediaFormat?.toColor() = when (this) {
        MediaFormat.TV -> Color(146, 86, 243)
        MediaFormat.TV_SHORT -> Color(104, 214, 57)
        MediaFormat.MOVIE -> Color(2, 169, 255)
        MediaFormat.SPECIAL -> Color(0xFF810831)
        MediaFormat.OVA -> Color(247, 121, 164)
        MediaFormat.ONA -> Color(0xFFFF9000)
        MediaFormat.MUSIC -> Color.Blue
        MediaFormat.MANGA -> Color.Yellow
        MediaFormat.NOVEL -> Color(0xFFFFE4C4)
        MediaFormat.ONE_SHOT -> Color(0xFF778899)
        MediaFormat.UNKNOWN__, null -> Color.White
    }

    fun MediaSeason.toTextRes() = when (this) {
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

    fun MediaRelation?.toTextRes() = when (this) {
        MediaRelation.ADAPTATION -> R.string.anime_media_relation_adaptation
        MediaRelation.PREQUEL -> R.string.anime_media_relation_prequel
        MediaRelation.SEQUEL -> R.string.anime_media_relation_sequel
        MediaRelation.PARENT -> R.string.anime_media_relation_parent
        MediaRelation.SIDE_STORY -> R.string.anime_media_relation_side_story
        MediaRelation.CHARACTER -> R.string.anime_media_relation_character
        MediaRelation.SUMMARY -> R.string.anime_media_relation_summary
        MediaRelation.ALTERNATIVE -> R.string.anime_media_relation_alternative
        MediaRelation.SPIN_OFF -> R.string.anime_media_relation_spin_off
        MediaRelation.OTHER -> R.string.anime_media_relation_other
        MediaRelation.SOURCE -> R.string.anime_media_relation_source
        MediaRelation.COMPILATION -> R.string.anime_media_relation_compilation
        MediaRelation.CONTAINS -> R.string.anime_media_relation_contains
        MediaRelation.UNKNOWN__,
        null -> R.string.anime_media_relation_unknown
    }

    @Composable
    fun formatSeasonYear(season: MediaSeason?, seasonYear: Int?, withSeparator: Boolean = false) =
        when {
            season != null && seasonYear != null -> {
                val seasonText = stringResource(season.toTextRes())
                if (withSeparator) {
                    "$seasonText - $seasonYear"
                } else {
                    "$seasonText $seasonYear"
                }
            }
            season != null -> stringResource(season.toTextRes())
            seasonYear != null -> seasonYear.toString()
            else -> null
        }

    fun formatDateTime(
        context: Context,
        year: Int?,
        month: Int?,
        dayOfMonth: Int?
    ): String? = when {
        year != null && month != null && dayOfMonth != null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, month, dayOfMonth)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        )
        year != null && month != null && dayOfMonth == null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, month, 1)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE
        )
        year != null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, Month.JANUARY, 1)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_YEAR
        )
        else -> null
    }

    fun formatEntryDateTime(context: Context, timeInMillis: Long): String =
        DateUtils.formatDateTime(
            context,
            timeInMillis,
            DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR or
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or
                    DateUtils.FORMAT_SHOW_TIME
        )

    fun formatAiringAt(context: Context, timeInMillis: Long): String = DateUtils.formatDateTime(
        context,
        timeInMillis,
        BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_TIME
    )

    fun formatRemainingTime(timeInMillis: Long): CharSequence = DateUtils.getRelativeTimeSpanString(
        timeInMillis,
        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
        0,
        BASE_DATE_FORMAT_FLAGS,
    )

    fun formatShortDay(context: Context, localDate: LocalDate) =
        DateUtils.formatDateTime(
            context,
            localDate.atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE
        )!!

    fun formatShortWeekday(context: Context, localDate: LocalDate) =
        DateUtils.formatDateTime(
            context,
            localDate.atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        )!!

    fun twitterHashtagsLink(hashtags: List<String>) =
        "https://twitter.com/search?q=${hashtags.joinToString(separator = "+OR ")}&src=typd"

    @Composable
    fun formatRanking(
        ranking: MediaDetailsQuery.Data.Media.Ranking,
        @StringRes seasonYearTextRes: Int,
        @StringRes yearTextRes: Int,
        @StringRes allTimeTextRes: Int,
    ) = if (ranking.season != null && ranking.year != null) {
        stringResource(
            seasonYearTextRes,
            ranking.rank,
            ranking.season?.toTextRes()?.let { stringResource(it) }.orEmpty(),
            ranking.year!!
        )
    } else if (ranking.year != null) {
        stringResource(
            yearTextRes,
            ranking.rank,
            ranking.year!!
        )
    } else if (ranking.allTime == true) {
        stringResource(allTimeTextRes, ranking.rank)
    } else {
        stringResource(R.string.anime_media_details_ranking_unknown, ranking.rank, ranking.context)
    }

    fun dailymotionUrl(videoId: String) = "https://www.dailymotion.com/video/$videoId"

    fun parseLocalDate(startedAt: MediaDetailsListEntry.StartedAt?) =
        startedAt?.run { parseLocalDate(year, month, day) }

    fun parseLocalDate(completedAt: MediaDetailsListEntry.CompletedAt?) =
        completedAt?.run { parseLocalDate(year, month, day) }

    private fun parseLocalDate(year: Int?, month: Int?, dayOfMonth: Int?): LocalDate? {
        return if (year != null && month != null && dayOfMonth != null) {
            LocalDate.of(year, month, dayOfMonth)
        } else null
    }
}
