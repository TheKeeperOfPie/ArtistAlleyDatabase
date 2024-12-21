package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone._18UpRating
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_default
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_fall
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_spring
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_summer
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_winter
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_manga
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_movie
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_music
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_novel
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_ona
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_one_shot
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_ova
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_special
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_tv
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_tv_short
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_format_unknown
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_completed
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_current_anime
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_current_not_anime
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_dropped
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_none
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_paused
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_planning
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_repeating
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_list_status_unknown
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_cancelled
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_finished
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_hiatus
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_not_yet_released
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_releasing
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_unknown
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_tag_is_adult
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_tag_is_spoiler
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_anime_icon_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_manga_icon_content_description
import com.anilist.data.fragment.AniListDate
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaTitleFragment
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.ImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import org.jetbrains.compose.resources.stringResource

object MediaDataUtils {

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

    @Composable
    fun formatMonthYearStartDate(startDate: AniListDate) =
        LocalDateTimeFormatter.current.formatSubtitleMonthYear(
            year = startDate.year,
            month = startDate.month,
        )

    @Composable
    fun formatSubtitle(
        format: MediaFormat?,
        status: MediaStatus?,
        season: MediaSeason?,
        seasonYear: Int?,
        startDate: AniListDate?,
    ): String? {
        format ?: status ?: season ?: seasonYear ?: return null
        return listOfNotNull(
            format?.toTextRes()?.let { stringResource(it) },
            status?.toTextRes()?.let { stringResource(it) },
            formatSeasonYear(season, seasonYear, withSeparator = true)
                ?: startDate?.let { formatMonthYearStartDate(it) },
        ).joinToString(separator = " - ")
    }

    fun ratingColor(rating: Int) = when {
        rating > 80 -> Color.Green
        rating > 70 -> Color.Yellow
        rating > 50 -> Color(0xFFFF9000) // Orange
        else -> Color.Red
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

    fun tagLeadingIcon(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null,
    ) = when {
        isAdult == true -> Icons.TwoTone._18UpRating
        (isGeneralSpoiler == true) || (isMediaSpoiler == true) ->
            Icons.Filled.Warning
        else -> null
    }

    fun tagLeadingIconContentDescription(
        isAdult: Boolean? = false,
        isGeneralSpoiler: Boolean? = false,
        isMediaSpoiler: Boolean? = null,
    ) = when {
        isAdult == true -> Res.string.anime_media_tag_is_adult
        (isGeneralSpoiler == true) || (isMediaSpoiler == true) ->
            Res.string.anime_media_tag_is_spoiler
        else -> null
    }
}

@Composable
fun MediaTitleFragment.primaryTitle() = primaryTitle(LocalLanguageOptionMedia.current)

fun MediaTitleFragment.primaryTitle(languageOption: AniListLanguageOption) =
    when (languageOption) {
        AniListLanguageOption.DEFAULT -> userPreferred
        AniListLanguageOption.ENGLISH -> english
        AniListLanguageOption.NATIVE -> native
        AniListLanguageOption.ROMAJI -> romaji
    } ?: userPreferred ?: romaji ?: english ?: native

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

fun MediaType?.toFavoriteType() = if (this == MediaType.ANIME) {
    FavoriteType.ANIME
} else {
    FavoriteType.MANGA
}

fun MediaSeason.toTextRes() = when (this) {
    MediaSeason.UNKNOWN__ -> Res.string.anime_media_filter_airing_date_season_default
    MediaSeason.WINTER -> Res.string.anime_media_filter_airing_date_season_winter
    MediaSeason.SPRING -> Res.string.anime_media_filter_airing_date_season_spring
    MediaSeason.SUMMER -> Res.string.anime_media_filter_airing_date_season_summer
    MediaSeason.FALL -> Res.string.anime_media_filter_airing_date_season_fall
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
    null, -> Res.string.anime_media_format_unknown
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

fun MediaListStatus?.toColor() = when (this) {
    MediaListStatus.CURRENT -> Color(146, 86, 243)
    MediaListStatus.PLANNING -> Color(104, 214, 57)
    MediaListStatus.COMPLETED -> Color(2, 169, 255)
    MediaListStatus.DROPPED -> Color(0xFF810831)
    MediaListStatus.PAUSED -> Color(247, 121, 164)
    MediaListStatus.REPEATING -> Color(0xFFFF9000)
    MediaListStatus.UNKNOWN__, null -> Color.White
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

/** Decouples MediaDetails from this module */
typealias MediaDetailsRoute = (
    mediaNavigationData: MediaNavigationData,
    coverImage: ImageState?,
    languageOptionMedia: AniListLanguageOption,
    sharedTransitionKey: SharedTransitionKey?,
) -> NavDestination

typealias MediaDetailsByIdRoute = (
    mediaId: String,
    sharedTransitionKey: SharedTransitionKey?,
) -> NavDestination

/** Decouples MediaDetails from this module */
typealias MediaEditBottomSheetScaffoldComposable = @Composable (
    @Composable (
        PaddingValues,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) -> Unit,
) -> Unit
