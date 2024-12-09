package com.thekeeperofpie.artistalleydatabase.anime.media.data

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.runtime.Composable
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
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_cancelled
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_finished
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_hiatus
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_not_yet_released
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_releasing
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_status_unknown
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_anime_icon_content_description
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_type_manga_icon_content_description
import com.anilist.data.fragment.AniListDate
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaTitleFragment
import com.anilist.data.type.MediaFormat
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

/** Decouples MediaDetails from this module */
typealias MediaDetailsRoute = (
    mediaNavigationData: MediaNavigationData,
    coverImage: ImageState?,
    languageOptionMedia: AniListLanguageOption,
    sharedTransitionKey: SharedTransitionKey?,
) -> NavDestination

/** Decouples MediaDetails from this module */
typealias MediaEditBottomSheetScaffoldComposable = @Composable (
    @Composable (
        PaddingValues,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) -> Unit,
) -> Unit
