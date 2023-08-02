package com.thekeeperofpie.artistalleydatabase.anime.media

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
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
import com.anilist.MediaListEntryQuery
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.MediaTitleFragment
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRelation
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
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

    val LESS_IMPORTANT_MEDIA_TAG_CATEGORIES = setOf(
        "Cast-Main Cast",
        "Demographic",
        "Technical",
    )

    fun genreColor(name: String) = Color.hsl(
        hue = (name.hashCode() % 360f).absoluteValue,
        lightness = 0.25f,
        saturation = 0.25f,
    )

    fun tagLeadingIcon(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null,
    ) = when {
        isAdult ?: false -> Icons.TwoTone._18UpRating
        (isGeneralSpoiler ?: false) || (isMediaSpoiler ?: false) ->
            Icons.Filled.Warning
        else -> null
    }

    fun tagLeadingIconContentDescription(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null,
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

    fun MediaListStatus?.toTextRes(mediaType: MediaType?) = toTextRes(mediaType != MediaType.MANGA)

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

    fun scoreFormatToText(score: Double, format: ScoreFormat) =
        if (score == 0.0) "" else when (format) {
            ScoreFormat.POINT_10_DECIMAL -> String.format("%.1f", score / 10f)
            ScoreFormat.POINT_10 -> (score.roundToInt() / 10).toString()
            ScoreFormat.POINT_100,
            ScoreFormat.POINT_5,
            ScoreFormat.POINT_3,
            ScoreFormat.UNKNOWN__,
            -> score.roundToInt().toString()
        }

    @Composable
    fun MediaListStatus?.toStatusText(
        mediaType: MediaType?,
        progress: Int,
        progressMax: Int?,
        score: Double?,
        scoreFormat: ScoreFormat?,
    ) = when (this) {
        MediaListStatus.CURRENT -> {
            if (mediaType == MediaType.ANIME) {
                if (progressMax == null) {
                    stringResource(
                        R.string.anime_media_details_fab_user_status_current_anime_unknown_max,
                        progress,
                    )
                } else {
                    stringResource(
                        R.string.anime_media_details_fab_user_status_current_anime,
                        progress,
                        progressMax,
                    )
                }
            } else {
                if (progressMax == null) {
                    stringResource(
                        R.string.anime_media_details_fab_user_status_current_not_anime_unknown_max,
                        progress,
                    )
                } else {
                    stringResource(
                        R.string.anime_media_details_fab_user_status_current_not_anime,
                        progress,
                        progressMax,
                    )
                }
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
        MediaListStatus.DROPPED -> if (progressMax == null) {
            stringResource(
                R.string.anime_media_details_fab_user_status_dropped_unknown_max,
                progress,
            )
        } else {
            stringResource(
                R.string.anime_media_details_fab_user_status_dropped,
                progress,
                progressMax,
            )
        }
        MediaListStatus.PAUSED -> stringResource(
            R.string.anime_media_details_fab_user_status_paused,
        )
        MediaListStatus.REPEATING -> stringResource(
            R.string.anime_media_details_fab_user_status_repeating,
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
        MediaListStatus.UNKNOWN__, null -> Icons.Filled.Add to
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
        null,
        -> R.string.anime_media_format_unknown
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
        null,
        -> R.string.anime_media_filter_source_unknown
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
        null,
        -> R.string.anime_media_relation_unknown
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
        dayOfMonth: Int?,
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
            localDate.atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond() * 1000,
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_UTC
        )!!

    fun formatShortWeekday(context: Context, localDate: LocalDate) =
        DateUtils.formatDateTime(
            context,
            localDate.atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond() * 1000,
            BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_UTC
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

    fun parseLocalDate(year: Int?, month: Int?, dayOfMonth: Int?): LocalDate? {
        return if (year != null && month != null && dayOfMonth != null) {
            LocalDate.of(year, month, dayOfMonth)
        } else null
    }

    @Composable
    fun formatSubtitle(
        format: MediaFormat?,
        status: MediaStatus?,
        season: MediaSeason?,
        seasonYear: Int?,
    ) = if (format == null && status == null && season == null && seasonYear == null) {
        ""
    } else {
        listOfNotNull(
            stringResource(format.toTextRes()),
            stringResource(status.toTextRes()),
            formatSeasonYear(season, seasonYear, withSeparator = true),
        ).joinToString(separator = " - ")
    }

    fun ratingColor(rating: Int) = when {
        rating > 80 -> Color.Green
        rating > 70 -> Color.Yellow
        rating > 50 -> Color(0xFFFF9000) // Orange
        else -> Color.Red
    }

    fun <SortType : SortOption, MediaEntryType : MediaStatusAware> filterEntries(
        filterParams: MediaSortFilterController.FilterParams<SortType>,
        entries: List<MediaEntryType>,
        media: (MediaEntryType) -> MediaPreview,
        forceShowIgnored: Boolean = false,
    ): List<MediaEntryType> {
        var filteredEntries = entries

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.statuses,
            filteredEntries,
            transform = { listOfNotNull(media(it).status) }
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.formats,
            filteredEntries,
            transform = { listOfNotNull(media(it).format) }
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.genres,
            filteredEntries,
            transform = { media(it).genres?.filterNotNull().orEmpty() }
        )

        val tagRank = filterParams.tagRank
        val transformIncludes: ((MediaEntryType) -> List<String>)? =
            if (tagRank == null) null else {
                {
                    media(it).tags
                        ?.filterNotNull()
                        ?.filter { it.rank?.let { it >= tagRank } == true }
                        ?.map { it.id.toString() }
                        .orEmpty()
                }
            }

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filters = filterParams.tagsByCategory.values.flatMap {
                when (it) {
                    is TagSection.Category -> it.flatten()
                    is TagSection.Tag -> listOf(it)
                }
            },
            list = filteredEntries,
            state = { it.state },
            key = { it.value.id.toString() },
            transform = { media(it).tags?.filterNotNull()?.map { it.id.toString() }.orEmpty() },
            transformIncludes = transformIncludes,
        )

        if (!filterParams.showAdult) {
            filteredEntries = filteredEntries.filterNot { media(it).isAdult ?: false }
        }

        if (!filterParams.showIgnored && !forceShowIgnored) {
            filteredEntries = filteredEntries.filterNot { it.ignored }
        }

        filteredEntries = when (val airingDate = filterParams.airingDate) {
            is AiringDate.Basic -> {
                filteredEntries.filter {
                    val season = airingDate.season
                    val seasonYear = airingDate.seasonYear.toIntOrNull()
                    (seasonYear == null || media(it).seasonYear == seasonYear)
                            && (season == null || media(it).season == season)
                }
            }
            is AiringDate.Advanced -> {
                val startDate = airingDate.startDate
                val endDate = airingDate.endDate

                if (startDate == null && endDate == null) {
                    filteredEntries
                } else {
                    fun List<MediaEntryType>.filterStartDate(
                        startDate: LocalDate,
                    ) = filter {
                        val mediaStartDate = media(it).startDate
                        val mediaYear = mediaStartDate?.year
                        if (mediaYear == null) {
                            return@filter false
                        } else if (mediaYear > startDate.year) {
                            return@filter true
                        } else if (mediaYear < startDate.year) {
                            return@filter false
                        }

                        val mediaMonth = mediaStartDate.month
                        val mediaDayOfMonth = mediaStartDate.day

                        // TODO: Is this the correct behavior?
                        // If there's no month, match the media to avoid stripping expected result
                        if (mediaMonth == null) {
                            return@filter true
                        }

                        if (mediaMonth < startDate.monthValue) {
                            return@filter false
                        }

                        if (mediaMonth > startDate.monthValue) {
                            return@filter true
                        }

                        mediaDayOfMonth == null || mediaDayOfMonth >= startDate.dayOfMonth
                    }

                    fun List<MediaEntryType>.filterEndDate(
                        endDate: LocalDate,
                    ) = filter {
                        val mediaStartDate = media(it).startDate
                        val mediaYear = mediaStartDate?.year
                        if (mediaYear == null) {
                            return@filter false
                        } else if (mediaYear > endDate.year) {
                            return@filter false
                        } else if (mediaYear < endDate.year) {
                            return@filter true
                        }

                        val mediaMonth = mediaStartDate.month
                        val mediaDayOfMonth = mediaStartDate.day

                        // TODO: Is this the correct behavior?
                        // If there's no month, match the media to avoid stripping expected result
                        if (mediaMonth == null) {
                            return@filter true
                        }

                        if (mediaMonth < endDate.monthValue) {
                            return@filter true
                        }

                        if (mediaMonth > endDate.monthValue) {
                            return@filter false
                        }

                        mediaDayOfMonth == null || mediaDayOfMonth <= endDate.dayOfMonth
                    }

                    if (startDate != null && endDate != null) {
                        filteredEntries.filterStartDate(startDate)
                            .filterEndDate(endDate)
                    } else if (startDate != null) {
                        filteredEntries.filterStartDate(startDate)
                    } else if (endDate != null) {
                        filteredEntries.filterEndDate(endDate)
                    } else {
                        filteredEntries
                    }
                }
            }
        }

        val averageScore = filterParams.averageScoreRange
        val averageScoreStart = averageScore.startInt ?: 0
        val averageScoreEnd = averageScore.endInt
        if (averageScoreStart > 0) {
            filteredEntries = filteredEntries.filter {
                media(it).averageScore.let { it != null && it >= averageScoreStart }
            }
        }
        if (averageScoreEnd != null) {
            filteredEntries = filteredEntries.filter {
                media(it).averageScore.let { it != null && it <= averageScoreEnd }
            }
        }

        val episodes = filterParams.episodesRange
        val episodesStart = episodes?.startInt ?: 0
        val episodesEnd = episodes?.endInt
        if (episodesStart > 0) {
            filteredEntries = filteredEntries.filter {
                media(it).episodes.let { it != null && it >= episodesStart }
            }
        }
        if (episodesEnd != null) {
            filteredEntries = filteredEntries.filter {
                media(it).episodes.let { it != null && it <= episodesEnd }
            }
        }

        val volumes = filterParams.volumesRange
        val volumesStart = volumes?.startInt ?: 0
        val volumesEnd = volumes?.endInt
        if (volumesStart > 0) {
            filteredEntries = filteredEntries.filter {
                media(it).volumes.let { it != null && it >= volumesStart }
            }
        }
        if (volumesEnd != null) {
            filteredEntries = filteredEntries.filter {
                media(it).volumes.let { it != null && it <= volumesEnd }
            }
        }

        val chapters = filterParams.chaptersRange
        val chaptersStart = chapters?.startInt ?: 0
        val chaptersEnd = chapters?.endInt
        if (chaptersStart > 0) {
            filteredEntries = filteredEntries.filter {
                media(it).chapters.let { it != null && it >= chaptersStart }
            }
        }
        if (chaptersEnd != null) {
            filteredEntries = filteredEntries.filter {
                media(it).chapters.let { it != null && it <= chaptersEnd }
            }
        }

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.sources,
            filteredEntries,
            transform = { listOfNotNull(media(it).source) }
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filters = filterParams.licensedBy,
            list = filteredEntries,
            state = { it.state },
            key = { it.value.siteId },
            transform = { media(it).externalLinks?.mapNotNull { it?.siteId }.orEmpty() },
        )

        return filteredEntries
    }

    fun maxProgress(media: MediaPreview) = if (media.type == MediaType.MANGA) {
        media.chapters
    } else {
        media.episodes ?: media.nextAiringEpisode
            ?.episode?.let { (it - 1).coerceAtLeast(1) }
    }

    fun maxProgress(media: MediaListEntryQuery.Data.Media) = if (media.type == MediaType.MANGA) {
        media.chapters
    } else {
        media.episodes ?: media.nextAiringEpisode
            ?.episode?.let { (it - 1).coerceAtLeast(1) }
    }

    fun MediaType?.toFavoriteType() = if (this == MediaType.ANIME) {
        FavoriteType.ANIME
    } else {
        FavoriteType.MANGA
    }

    @Composable
    fun MediaTitleFragment.primaryTitle() = primaryTitle(LocalLanguageOptionMedia.current)

    fun MediaTitleFragment.primaryTitle(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> userPreferred
            AniListLanguageOption.ENGLISH -> english
            AniListLanguageOption.NATIVE -> native
            AniListLanguageOption.ROMAJI -> romaji
        }
}
