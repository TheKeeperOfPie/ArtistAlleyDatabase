package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.media.data.generated.resources.Res
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_current
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_fall
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_next
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_previous
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_spring
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_summer
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_filter_airing_date_season_winter
import com.anilist.data.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
sealed interface AiringDate {

    @Serializable
    data class Basic(
        val season: SeasonOption? = null,
        val seasonYear: String = "",
    ) : AiringDate {

        fun previousSeason(): Pair<SeasonOption, Int> {
            val currentSeasonYear by lazy { AniListUtils.getCurrentSeasonYear() }
            val (previousSeason, previousSeasonYear) = AniListUtils.getPreviousSeasonYear(
                (season?.toAniListSeason()
                    ?: currentSeasonYear.first) to (seasonYear.toIntOrNull()
                    ?: currentSeasonYear.second)
            )
            return SeasonOption.fromAniListSeason(previousSeason, previousSeasonYear) to
                    previousSeasonYear
        }

        fun nextSeason(): Pair<SeasonOption, Int> {
            val currentSeasonYear by lazy { AniListUtils.getCurrentSeasonYear() }
            val (nextSeason, nextSeasonYear) = AniListUtils.getNextSeasonYear(
                (season?.toAniListSeason()
                    ?: currentSeasonYear.first) to (seasonYear.toIntOrNull()
                    ?: currentSeasonYear.second)
            )
            return SeasonOption.fromAniListSeason(nextSeason, nextSeasonYear) to
                    nextSeasonYear
        }
    }

    @Serializable
    data class Advanced(
        val startDate: LocalDate? = null,
        val endDate: LocalDate? = null,
    ) : AiringDate {

        fun summaryText(): String? {
            val startDateString = startDate?.format(DateTimeUtils.shortDateFormat)
            val endDateString = endDate?.format(DateTimeUtils.shortDateFormat)

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
                Res.string.anime_media_filter_airing_date_season_previous,
                stringResource(AniListUtils.getPreviousSeasonYear().first.toTextRes()),
            )
            CURRENT -> stringResource(
                Res.string.anime_media_filter_airing_date_season_current,
                stringResource(AniListUtils.getCurrentSeasonYear().first.toTextRes()),
            )
            NEXT -> stringResource(
                Res.string.anime_media_filter_airing_date_season_next,
                stringResource(AniListUtils.getNextSeasonYear().first.toTextRes()),
            )
            WINTER -> stringResource(Res.string.anime_media_filter_airing_date_season_winter)
            SPRING -> stringResource(Res.string.anime_media_filter_airing_date_season_spring)
            SUMMER -> stringResource(Res.string.anime_media_filter_airing_date_season_summer)
            FALL -> stringResource(Res.string.anime_media_filter_airing_date_season_fall)
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

        fun makeAbsolute(seasonYear: Int?): SeasonOption {
            if (seasonYear == null) return this
            val currentSeason by lazy { AniListUtils.getCurrentSeasonYear() }
            return when (this) {
                PREVIOUS -> {
                    val (previousSeason, previousSeasonYear) =
                        AniListUtils.getPreviousSeasonYear(currentSeason)
                    return if (seasonYear == previousSeasonYear) {
                        this
                    } else {
                        fromAniListSeasonAbsolute(toAniListSeason())
                    }
                }
                CURRENT -> {
                    return if (seasonYear == currentSeason.second) {
                        this
                    } else {
                        fromAniListSeasonAbsolute(toAniListSeason())
                    }
                }
                NEXT -> {
                    val (nextSeason, nextSeasonYear) =
                        AniListUtils.getNextSeasonYear(currentSeason)
                    return if (seasonYear == nextSeasonYear) {
                        this
                    } else {
                        fromAniListSeasonAbsolute(toAniListSeason())
                    }
                }
                WINTER,
                SPRING,
                SUMMER,
                FALL -> this
            }
        }

        companion object {
            fun fromAniListSeason(
                mediaSeason: MediaSeason,
                seasonYear: Int,
                currentSeasonYear: Pair<MediaSeason, Int> = AniListUtils.getCurrentSeasonYear(),
            ) = when (mediaSeason to seasonYear) {
                currentSeasonYear -> CURRENT
                AniListUtils.getPreviousSeasonYear(currentSeasonYear) -> PREVIOUS
                AniListUtils.getNextSeasonYear(currentSeasonYear) -> NEXT
                else -> fromAniListSeasonAbsolute(mediaSeason)
            }

            fun fromAniListSeasonAbsolute(
                mediaSeason: MediaSeason,
            ) = when (mediaSeason) {
                MediaSeason.WINTER -> WINTER
                MediaSeason.SPRING -> SPRING
                MediaSeason.SUMMER -> SUMMER
                MediaSeason.FALL -> FALL
                MediaSeason.UNKNOWN__ -> CURRENT
            }
        }
    }
}
