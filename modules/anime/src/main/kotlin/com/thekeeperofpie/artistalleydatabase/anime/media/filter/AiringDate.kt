package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.anilist.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed interface AiringDate {

    data class Basic(
        val season: SeasonOption? = null,
        val seasonYear: String = "",
    ) : AiringDate

    data class Advanced(
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
    ) : AiringDate {

        fun summaryText(): String? {
            val startDateString =
                startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            val endDateString =
                endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

            return when {
                startDateString != null && endDateString != null -> {
                    if (startDateString == endDateString) {
                        startDateString
                    } else {
                        "$startDateString - $endDateString"
                    }
                }
                startDateString != null -> "≥ $startDateString"
                endDateString != null -> "≤ $endDateString"
                else -> null
            }
        }
    }

    enum class SeasonOption {
        PREVIOUS,
        CURRENT,
        NEXT,
        WINTER,
        SPRING,
        SUMMER,
        FALL,
        ;

        @Composable
        fun text() = when (this) {
            PREVIOUS -> stringResource(
                R.string.anime_media_filter_airing_date_season_previous,
                stringResource(AniListUtils.getPreviousSeasonYear().first.toTextRes()),
            )
            CURRENT -> stringResource(
                R.string.anime_media_filter_airing_date_season_current,
                stringResource(AniListUtils.getCurrentSeasonYear().first.toTextRes()),
            )
            NEXT -> stringResource(
                R.string.anime_media_filter_airing_date_season_next,
                stringResource(AniListUtils.getNextSeasonYear().first.toTextRes()),
            )
            WINTER -> stringResource(R.string.anime_media_filter_airing_date_season_winter)
            SPRING -> stringResource(R.string.anime_media_filter_airing_date_season_spring)
            SUMMER -> stringResource(R.string.anime_media_filter_airing_date_season_summer)
            FALL -> stringResource(R.string.anime_media_filter_airing_date_season_fall)
        }

        fun toAniListSeason() = when (this) {
            WINTER -> MediaSeason.WINTER
            SPRING -> MediaSeason.SPRING
            SUMMER -> MediaSeason.SUMMER
            FALL -> MediaSeason.FALL
            PREVIOUS -> AniListUtils.getPreviousSeasonYear().first
            CURRENT -> AniListUtils.getCurrentSeasonYear().first
            NEXT -> AniListUtils.getNextSeasonYear().first
        }
    }
}
