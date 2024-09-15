package com.thekeeperofpie.artistalleydatabase.anime.media

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_completed
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_completed_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_completed_with_score
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_anime_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_anime_unknown_max
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_not_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_not_anime_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_current_not_anime_unknown_max
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_dropped
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_dropped_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_dropped_unknown_max
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_edit_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_paused
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_paused_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_planning
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_planning_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_repeating
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_repeating_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_fab_user_status_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_details_ranking_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_airing_date_season_default
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_airing_date_season_fall
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_airing_date_season_spring
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_airing_date_season_summer
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_airing_date_season_winter
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_comic
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_doujinshi
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_game
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_light_novel
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_live_action
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_manga
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_multimedia_project
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_novel
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_original
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_other
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_picture_book
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_video_game
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_visual_novel
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_web_novel
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_manga
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_movie
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_music
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_novel
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_ona
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_one_shot
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_ova
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_special
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_tv
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_tv_short
import artistalleydatabase.modules.anime.generated.resources.anime_media_format_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_completed
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_current_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_current_not_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_dropped
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_none
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_paused
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_planning
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_repeating
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_status_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_next_airing_episode_with_episode
import artistalleydatabase.modules.anime.generated.resources.anime_media_next_airing_episode_with_episode_with_relative
import artistalleydatabase.modules.anime.generated.resources.anime_media_next_airing_episode_without_episode
import artistalleydatabase.modules.anime.generated.resources.anime_media_next_airing_episode_without_episode_with_relative
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_adaptation
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_alternative
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_character
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_compilation
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_contains
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_other
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_parent
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_prequel
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_sequel
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_side_story
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_source
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_spin_off
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_summary
import artistalleydatabase.modules.anime.generated.resources.anime_media_relation_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_cancelled
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_finished
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_hiatus
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_not_yet_released
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_releasing
import artistalleydatabase.modules.anime.generated.resources.anime_media_status_unknown
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_is_adult
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_is_spoiler
import artistalleydatabase.modules.anime.generated.resources.anime_media_type_anime_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_type_manga_icon_content_description
import com.anilist.MediaDetails2Query
import com.anilist.MediaListEntryQuery
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.fragment.MediaPreview
import com.anilist.fragment.MediaTitleFragment
import com.anilist.fragment.MediaWithListStatus
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaRelation
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.anilist.type.UserTitleLanguage
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterable
import com.thekeeperofpie.artistalleydatabase.anime.data.NextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
object MediaUtils {

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
        isAdult ?: false -> Res.string.anime_media_tag_is_adult
        (isGeneralSpoiler ?: false) || (isMediaSpoiler
            ?: false) -> Res.string.anime_media_tag_is_spoiler
        else -> null
    }

    fun MediaStatus?.toTextRes() = when (this) {
        MediaStatus.FINISHED -> Res.string.anime_media_status_finished
        MediaStatus.RELEASING -> Res.string.anime_media_status_releasing
        MediaStatus.NOT_YET_RELEASED -> Res.string.anime_media_status_not_yet_released
        MediaStatus.CANCELLED -> Res.string.anime_media_status_cancelled
        MediaStatus.HIATUS -> Res.string.anime_media_status_hiatus
        else -> Res.string.anime_media_status_unknown
    }

    fun MediaListStatus?.toTextRes(mediaType: MediaType?) = toTextRes(mediaType != MediaType.MANGA)

    fun MediaListStatus?.toTextRes(anime: Boolean) = when (this) {
        MediaListStatus.CURRENT -> if (anime) {
            Res.string.anime_media_list_status_current_anime
        } else {
            Res.string.anime_media_list_status_current_not_anime
        }
        MediaListStatus.PLANNING -> Res.string.anime_media_list_status_planning
        MediaListStatus.COMPLETED -> Res.string.anime_media_list_status_completed
        MediaListStatus.DROPPED -> Res.string.anime_media_list_status_dropped
        MediaListStatus.PAUSED -> Res.string.anime_media_list_status_paused
        MediaListStatus.REPEATING -> Res.string.anime_media_list_status_repeating
        MediaListStatus.UNKNOWN__ -> Res.string.anime_media_list_status_unknown
        null -> Res.string.anime_media_list_status_none
    }

    fun scoreFormatToText(score: Double, format: ScoreFormat): String {
        return if (score == 0.0) "" else when (format) {
            // TODO: Locale aware decimal format
            ScoreFormat.POINT_10_DECIMAL -> BigDecimal.fromDouble(score)
                .div(10)
                .roundToDigitPositionAfterDecimalPoint(1, RoundingMode.FLOOR)
                .toStringExpanded()
            ScoreFormat.POINT_10 -> (score.roundToInt() / 10).toString()
            ScoreFormat.POINT_100,
            ScoreFormat.POINT_5,
            ScoreFormat.POINT_3,
            ScoreFormat.UNKNOWN__,
                -> score.roundToInt().toString()
        }
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
                        Res.string.anime_media_details_fab_user_status_current_anime_unknown_max,
                        progress,
                    )
                } else {
                    stringResource(
                        Res.string.anime_media_details_fab_user_status_current_anime,
                        progress,
                        progressMax,
                    )
                }
            } else {
                if (progressMax == null) {
                    stringResource(
                        Res.string.anime_media_details_fab_user_status_current_not_anime_unknown_max,
                        progress,
                    )
                } else {
                    stringResource(
                        Res.string.anime_media_details_fab_user_status_current_not_anime,
                        progress,
                        progressMax,
                    )
                }
            }
        }
        MediaListStatus.PLANNING -> stringResource(
            Res.string.anime_media_details_fab_user_status_planning
        )
        MediaListStatus.COMPLETED -> {
            val scoreText = if (score != null && scoreFormat != null) {
                scoreFormatToText(score, scoreFormat)
            } else null

            if (scoreText.isNullOrEmpty()) {
                stringResource(Res.string.anime_media_details_fab_user_status_completed)
            } else {
                // TODO: Show smileys instead for that score format
                stringResource(
                    Res.string.anime_media_details_fab_user_status_completed_with_score,
                    scoreText,
                )
            }
        }
        MediaListStatus.DROPPED -> if (progressMax == null) {
            stringResource(
                Res.string.anime_media_details_fab_user_status_dropped_unknown_max,
                progress,
            )
        } else {
            stringResource(
                Res.string.anime_media_details_fab_user_status_dropped,
                progress,
                progressMax,
            )
        }
        MediaListStatus.PAUSED -> stringResource(
            Res.string.anime_media_details_fab_user_status_paused,
        )
        MediaListStatus.REPEATING -> stringResource(
            Res.string.anime_media_details_fab_user_status_repeating,
        )
        MediaListStatus.UNKNOWN__, null -> stringResource(
            Res.string.anime_media_details_fab_user_status_unknown
        )
    }

    @Composable
    fun MediaListStatus?.toStatusIcon(mediaType: MediaType?) = when (this) {
        MediaListStatus.CURRENT -> if (mediaType == MediaType.ANIME) {
            Icons.Filled.Monitor to Res.string.anime_media_details_fab_user_status_current_anime_icon_content_description
        } else {
            Icons.Filled.MenuBook to Res.string.anime_media_details_fab_user_status_current_not_anime_icon_content_description
        }
        MediaListStatus.PLANNING -> if (mediaType == MediaType.ANIME) {
            Icons.Filled.WatchLater
        } else {
            Icons.Filled.Bookmark
        } to Res.string.anime_media_details_fab_user_status_planning_icon_content_description
        MediaListStatus.COMPLETED -> Icons.Filled.CheckBox to
                Res.string.anime_media_details_fab_user_status_completed_icon_content_description
        MediaListStatus.DROPPED -> Icons.Filled.Delete to
                Res.string.anime_media_details_fab_user_status_dropped_icon_content_description
        MediaListStatus.PAUSED -> Icons.Filled.PauseCircle to
                Res.string.anime_media_details_fab_user_status_paused_icon_content_description
        MediaListStatus.REPEATING -> Icons.Filled.Repeat to
                Res.string.anime_media_details_fab_user_status_repeating_icon_content_description
        MediaListStatus.UNKNOWN__, null -> Icons.Filled.Add to
                Res.string.anime_media_details_fab_user_status_edit_icon_content_description
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
        MediaFormat.TV -> Res.string.anime_media_format_tv
        MediaFormat.TV_SHORT -> Res.string.anime_media_format_tv_short
        MediaFormat.MOVIE -> Res.string.anime_media_format_movie
        MediaFormat.SPECIAL -> Res.string.anime_media_format_special
        MediaFormat.OVA -> Res.string.anime_media_format_ova
        MediaFormat.ONA -> Res.string.anime_media_format_ona
        MediaFormat.MUSIC -> Res.string.anime_media_format_music
        MediaFormat.MANGA -> Res.string.anime_media_format_manga
        MediaFormat.NOVEL -> Res.string.anime_media_format_novel
        MediaFormat.ONE_SHOT -> Res.string.anime_media_format_one_shot
        MediaFormat.UNKNOWN__,
        null,
            -> Res.string.anime_media_format_unknown
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
        MediaSeason.UNKNOWN__ -> Res.string.anime_media_filter_airing_date_season_default
        MediaSeason.WINTER -> Res.string.anime_media_filter_airing_date_season_winter
        MediaSeason.SPRING -> Res.string.anime_media_filter_airing_date_season_spring
        MediaSeason.SUMMER -> Res.string.anime_media_filter_airing_date_season_summer
        MediaSeason.FALL -> Res.string.anime_media_filter_airing_date_season_fall
    }

    fun MediaSource?.toTextRes() = when (this) {
        MediaSource.ORIGINAL -> Res.string.anime_media_filter_source_original
        MediaSource.ANIME -> Res.string.anime_media_filter_source_anime
        MediaSource.COMIC -> Res.string.anime_media_filter_source_comic
        MediaSource.DOUJINSHI -> Res.string.anime_media_filter_source_doujinshi
        MediaSource.GAME -> Res.string.anime_media_filter_source_game
        MediaSource.LIGHT_NOVEL -> Res.string.anime_media_filter_source_light_novel
        MediaSource.LIVE_ACTION -> Res.string.anime_media_filter_source_live_action
        MediaSource.MANGA -> Res.string.anime_media_filter_source_manga
        MediaSource.MULTIMEDIA_PROJECT -> Res.string.anime_media_filter_source_multimedia_project
        MediaSource.NOVEL -> Res.string.anime_media_filter_source_novel
        MediaSource.OTHER -> Res.string.anime_media_filter_source_other
        MediaSource.PICTURE_BOOK -> Res.string.anime_media_filter_source_picture_book
        MediaSource.VIDEO_GAME -> Res.string.anime_media_filter_source_video_game
        MediaSource.VISUAL_NOVEL -> Res.string.anime_media_filter_source_visual_novel
        MediaSource.WEB_NOVEL -> Res.string.anime_media_filter_source_web_novel
        MediaSource.UNKNOWN__,
        null,
            -> Res.string.anime_media_filter_source_unknown
    }

    fun MediaRelation?.toTextRes() = when (this) {
        MediaRelation.ADAPTATION -> Res.string.anime_media_relation_adaptation
        MediaRelation.PREQUEL -> Res.string.anime_media_relation_prequel
        MediaRelation.SEQUEL -> Res.string.anime_media_relation_sequel
        MediaRelation.PARENT -> Res.string.anime_media_relation_parent
        MediaRelation.SIDE_STORY -> Res.string.anime_media_relation_side_story
        MediaRelation.CHARACTER -> Res.string.anime_media_relation_character
        MediaRelation.SUMMARY -> Res.string.anime_media_relation_summary
        MediaRelation.ALTERNATIVE -> Res.string.anime_media_relation_alternative
        MediaRelation.SPIN_OFF -> Res.string.anime_media_relation_spin_off
        MediaRelation.OTHER -> Res.string.anime_media_relation_other
        MediaRelation.SOURCE -> Res.string.anime_media_relation_source
        MediaRelation.COMPILATION -> Res.string.anime_media_relation_compilation
        MediaRelation.CONTAINS -> Res.string.anime_media_relation_contains
        MediaRelation.UNKNOWN__,
        null,
            -> Res.string.anime_media_relation_unknown
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

    fun twitterHashtagsLink(hashtags: List<String>) =
        "https://twitter.com/search?q=${hashtags.joinToString(separator = "+OR ")}&src=typd"

    @Composable
    fun formatRanking(
        ranking: MediaDetails2Query.Data.Media.Ranking,
        seasonYearTextRes: StringResource,
        yearTextRes: StringResource,
        allTimeTextRes: StringResource,
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
        stringResource(
            Res.string.anime_media_details_ranking_unknown,
            ranking.rank,
            ranking.context
        )
    }

    fun dailymotionUrl(videoId: String) = "https://www.dailymotion.com/video/$videoId"

    fun parseLocalDate(startedAt: MediaDetailsListEntry.StartedAt?) =
        startedAt?.run { parseLocalDate(year, month, day) }

    fun parseLocalDate(completedAt: MediaDetailsListEntry.CompletedAt?) =
        completedAt?.run { parseLocalDate(year, month, day) }

    fun parseLocalDate(year: Int?, month: Int?, dayOfMonth: Int?): LocalDate? {
        return if (year != null && month != null && dayOfMonth != null) {
            LocalDate(year, month, dayOfMonth)
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
            format?.toTextRes()?.let { stringResource(it) },
            status?.toTextRes()?.let { stringResource(it) },
            formatSeasonYear(season, seasonYear, withSeparator = true),
        ).joinToString(separator = " - ")
    }

    fun ratingColor(rating: Int) = when {
        rating > 80 -> Color.Green
        rating > 70 -> Color.Yellow
        rating > 50 -> Color(0xFFFF9000) // Orange
        else -> Color.Red
    }

    fun <SortType : SortOption, MediaEntryType> filterEntries(
        filterParams: MediaSortFilterController.FilterParams<SortType>,
        showTagWhenSpoiler: Boolean,
        entries: List<MediaEntryType>,
        media: (MediaEntryType) -> MediaPreview,
        mediaFilterable: (MediaEntryType) -> MediaFilterable,
        forceShowIgnored: Boolean = false,
    ): List<MediaEntryType> {
        var filteredEntries = entries

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.statuses,
            filteredEntries,
            transform = { listOfNotNull(media(it).status) },
            mustContainAll = false,
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.formats,
            filteredEntries,
            transform = { listOfNotNull(media(it).format) },
            mustContainAll = false,
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filterParams.genres,
            filteredEntries,
            transform = { media(it).genres?.filterNotNull().orEmpty() }
        )

        val tagRank = filterParams.tagRank
        val tagTransformIncludes: ((MediaEntryType) -> List<String>)? =
            if (tagRank == null) null else {
                {
                    media(it).tags
                        ?.filterNotNull()
                        ?.filter { it.rank?.let { it >= tagRank } == true }
                        ?.map { it.id.toString() }
                        .orEmpty()
                }
            }

        val allTags = filterParams.tagsByCategory.values.flatMap {
            when (it) {
                is TagSection.Category -> it.flatten()
                is TagSection.Tag -> listOf(it)
            }
        }
        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filters = allTags,
            list = filteredEntries,
            state = { it.state },
            key = { it.value.id.toString() },
            transform = { media(it).tags?.filterNotNull()?.map { it.id.toString() }.orEmpty() },
            transformIncludes = tagTransformIncludes,
        )

        val tagsIncluded = allTags.filter { it.state == FilterIncludeExcludeState.INCLUDE }
        if (!showTagWhenSpoiler && tagsIncluded.isNotEmpty()) {
            filteredEntries = filteredEntries.filter {
                tagsIncluded.all { tag ->
                    media(it).tags?.find { it?.id.toString() == tag.id }
                        ?.isMediaSpoiler != true
                }
            }
        }

        if (!filterParams.showAdult) {
            filteredEntries = filteredEntries.filterNot { media(it).isAdult ?: false }
        }

        if (!filterParams.showIgnored && !forceShowIgnored) {
            filteredEntries = filteredEntries.filterNot { mediaFilterable(it).ignored }
        }

        filteredEntries = when (val airingDate = filterParams.airingDate) {
            is AiringDate.Basic -> {
                filteredEntries.filter {
                    val season = airingDate.season
                    val seasonYear = airingDate.seasonYear.toIntOrNull()
                    (seasonYear == null || media(it).seasonYear == seasonYear)
                            && (season == null || media(it).season == season.toAniListSeason())
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

                        if (mediaMonth < startDate.monthNumber) {
                            return@filter false
                        }

                        if (mediaMonth > startDate.monthNumber) {
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

                        if (mediaMonth < endDate.monthNumber) {
                            return@filter true
                        }

                        if (mediaMonth > endDate.monthNumber) {
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

        val myScore = filterParams.myScore
        if (myScore != null) {
            val myScoreStart = myScore.startInt ?: 0
            val myScoreEnd = myScore.endInt
            if (myScoreStart > 0) {
                filteredEntries = filteredEntries.filter {
                    mediaFilterable(it).scoreRaw.let { it != null && it >= myScoreStart }
                }
            }
            if (myScoreEnd != null) {
                // TODO: How should this handle null?
                filteredEntries = filteredEntries.filter {
                    mediaFilterable(it).scoreRaw.let { it == null || it <= myScoreEnd }
                }
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
            transform = { listOfNotNull(media(it).source) },
            mustContainAll = false,
        )

        filteredEntries = FilterIncludeExcludeState.applyFiltering(
            filters = filterParams.licensedBy,
            list = filteredEntries,
            state = { it.state },
            key = { it.value.siteId },
            transform = { media(it).externalLinks?.mapNotNull { it?.siteId }.orEmpty() },
            mustContainAll = false,
        )

        return filteredEntries
    }

    fun maxProgress(media: MediaWithListStatus) = if (media.type == MediaType.MANGA) {
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

    fun maxProgress(type: MediaType?, chapters: Int?, episodes: Int?, nextAiringEpisode: Int?) =
        if (type == MediaType.MANGA) {
            chapters
        } else {
            episodes ?: nextAiringEpisode?.let { (it - 1).coerceAtLeast(1) }
        }

    fun maxProgress(
        type: MediaType?,
        chapters: Int?,
        episodes: Int?,
        nextAiringEpisode: NextAiringEpisode?,
    ) =
        if (type == MediaType.MANGA) {
            chapters
        } else {
            episodes ?: nextAiringEpisode?.episode?.let { (it - 1).coerceAtLeast(1) }
        }

    fun MediaType?.toFavoriteType() = if (this == MediaType.ANIME) {
        FavoriteType.ANIME
    } else {
        FavoriteType.MANGA
    }

    fun userPreferredTitle(
        titleRomaji: String?,
        titleEnglish: String?,
        titleNative: String?,
        titleLanguage: UserTitleLanguage?,
        languageOption: AniListLanguageOption?,
    ) = when (languageOption) {
        AniListLanguageOption.DEFAULT -> when (titleLanguage) {
            UserTitleLanguage.ROMAJI -> titleRomaji
            UserTitleLanguage.ENGLISH -> titleEnglish
            UserTitleLanguage.NATIVE -> titleNative
            UserTitleLanguage.ROMAJI_STYLISED -> titleRomaji
            UserTitleLanguage.ENGLISH_STYLISED -> titleEnglish
            UserTitleLanguage.NATIVE_STYLISED -> titleNative
            UserTitleLanguage.UNKNOWN__,
            null,
                -> null
        }
        AniListLanguageOption.ENGLISH -> titleEnglish
        AniListLanguageOption.NATIVE -> titleNative
        AniListLanguageOption.ROMAJI -> titleRomaji
        null -> null
    } ?: titleRomaji ?: titleEnglish ?: titleNative

    @Composable
    fun userPreferredTitle(
        userPreferred: String?,
        romaji: String?,
        english: String?,
        native: String?,
        languageOption: AniListLanguageOption? = LocalLanguageOptionMedia.current,
    ) = when (languageOption) {
        AniListLanguageOption.DEFAULT -> userPreferred
        AniListLanguageOption.ENGLISH -> english
        AniListLanguageOption.NATIVE -> native
        AniListLanguageOption.ROMAJI -> romaji
        null -> null
    } ?: userPreferred ?: romaji ?: english ?: native

    @Composable
    fun MediaTitleFragment.primaryTitle() = primaryTitle(LocalLanguageOptionMedia.current)

    fun MediaTitleFragment.primaryTitle(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> userPreferred
            AniListLanguageOption.ENGLISH -> english
            AniListLanguageOption.NATIVE -> native
            AniListLanguageOption.ROMAJI -> romaji
        } ?: userPreferred ?: romaji ?: english ?: native

    fun buildTags(
        media: MediaCompactWithTags,
        showLessImportantTags: Boolean,
        showSpoilerTags: Boolean,
    ) = media.tags?.asSequence()
        ?.filterNotNull()
        ?.filter {
            showLessImportantTags || it.category !in LESS_IMPORTANT_MEDIA_TAG_CATEGORIES
        }
        ?.filter {
            showSpoilerTags || (it.isGeneralSpoiler != true && it.isMediaSpoiler != true)
        }
        ?.map { AnimeMediaTagEntry(it, isMediaSpoiler = it.isMediaSpoiler) }
        ?.distinctBy { it.id }
        ?.toList()
        .orEmpty()

    fun MediaType?.toIcon() = if (this == MediaType.ANIME) {
        Icons.Filled.Monitor
    } else {
        Icons.Filled.MenuBook
    }

    @Composable
    fun MediaType?.toIconContentDescription() = stringResource(
        if (this == MediaType.ANIME) {
            Res.string.anime_media_type_anime_icon_content_description
        } else {
            Res.string.anime_media_type_manga_icon_content_description
        }
    )

    @Composable
    fun nextAiringSectionText(
        nextAiringEpisode: NextAiringEpisode,
        episodes: Int?,
        format: MediaFormat?,
        showDate: Boolean = true,
    ): String {
        val dateTimeFormatter = LocalDateTimeFormatter.current
        val airingAtText =
            remember { dateTimeFormatter.formatAiringAt(nextAiringEpisode.airingAt, showDate) }

        // TODO: De-dupe airingAt and remainingTime if both show a specific date
        //  (airing > 7 days away)
        val remainingTime =
            remember { dateTimeFormatter.formatRemainingTime(nextAiringEpisode.airingAt) }

        return if (episodes == 1 || (episodes == null && format == MediaFormat.MOVIE)) {
            if (airingAtText.contains(remainingTime)) {
                stringResource(
                    Res.string.anime_media_next_airing_episode_without_episode,
                    airingAtText,
                )
            } else {
                stringResource(
                    Res.string.anime_media_next_airing_episode_without_episode_with_relative,
                    airingAtText,
                    remainingTime,
                )
            }
        } else {
            if (airingAtText.contains(remainingTime)) {
                stringResource(
                    Res.string.anime_media_next_airing_episode_with_episode,
                    nextAiringEpisode.episode,
                    airingAtText,
                )
            } else {
                stringResource(
                    Res.string.anime_media_next_airing_episode_with_episode_with_relative,
                    nextAiringEpisode.episode,
                    airingAtText,
                    remainingTime,
                )
            }
        }
    }

    fun mediaViewOptionIncludeDescriptionFlow(mediaViewOption: () -> MediaViewOption) =
        snapshotFlow { mediaViewOption() }
            .map { it == MediaViewOption.LARGE_CARD }
            .transformWhile {
                // Take until description is ever requested,
                // then always request to prevent unnecessary refreshes
                emit(it)
                !it
            }
            .distinctUntilChanged()
}
