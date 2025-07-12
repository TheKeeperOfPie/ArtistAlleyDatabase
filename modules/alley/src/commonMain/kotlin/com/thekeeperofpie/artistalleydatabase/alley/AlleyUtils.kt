package com.thekeeperofpie.artistalleydatabase.alley

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2023_full
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2023_short
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2024_full
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2024_short
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2025_full
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_2025_short
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_anime_nyc_2025_full
import artistalleydatabase.modules.alley.generated.resources.alley_data_year_anime_nyc_2025_short
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object AlleyUtils {
    fun isCurrentYear(year: DataYear) =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year == year.year
}

val DataYear.fullName
    get() = when (this) {
        DataYear.ANIME_EXPO_2023 -> Res.string.alley_data_year_2023_full
        DataYear.ANIME_EXPO_2024 -> Res.string.alley_data_year_2024_full
        DataYear.ANIME_EXPO_2025 -> Res.string.alley_data_year_2025_full
        DataYear.ANIME_NYC_2025 -> Res.string.alley_data_year_anime_nyc_2025_full
    }

val DataYear.shortName
    get() = when (this) {
        DataYear.ANIME_EXPO_2023 -> Res.string.alley_data_year_2023_short
        DataYear.ANIME_EXPO_2024 -> Res.string.alley_data_year_2024_short
        DataYear.ANIME_EXPO_2025 -> Res.string.alley_data_year_2025_short
        DataYear.ANIME_NYC_2025 -> Res.string.alley_data_year_anime_nyc_2025_short
    }
