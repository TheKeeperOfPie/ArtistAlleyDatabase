package com.thekeeperofpie.artistalleydatabase.shared.alley.data

enum class TagYearFlag(year: DataYear) {
    ANIME_EXPO_2024_INFERRED(DataYear.ANIME_EXPO_2024),
    ANIME_EXPO_2024_CONFIRMED(DataYear.ANIME_EXPO_2024),
    ANIME_EXPO_2025_INFERRED(DataYear.ANIME_EXPO_2025),
    ANIME_EXPO_2025_CONFIRMED(DataYear.ANIME_EXPO_2025),
    ANIME_NYC_2024_INFERRED(DataYear.ANIME_NYC_2024),
    ANIME_NYC_2024_CONFIRMED(DataYear.ANIME_NYC_2024),
    ANIME_NYC_2025_INFERRED(DataYear.ANIME_NYC_2025),
    ANIME_NYC_2025_CONFIRMED(DataYear.ANIME_NYC_2025),
    ;

    companion object {
        fun getFlags(
            hasAnimeExpo2024Inferred: Boolean = false,
            hasAnimeExpo2024Confirmed: Boolean = false,
            hasAnimeExpo2025Inferred: Boolean = false,
            hasAnimeExpo2025Confirmed: Boolean = false,
            hasAnimeNyc2024Inferred: Boolean = false,
            hasAnimeNyc2024Confirmed: Boolean = false,
            hasAnimeNyc2025Inferred: Boolean = false,
            hasAnimeNyc2025Confirmed: Boolean = false,
        ): Long {
            val years = listOfNotNull(
                ANIME_EXPO_2024_INFERRED.takeIf { hasAnimeExpo2024Inferred || hasAnimeExpo2024Confirmed },
                ANIME_EXPO_2024_CONFIRMED.takeIf { hasAnimeExpo2024Confirmed },
                ANIME_EXPO_2025_INFERRED.takeIf { hasAnimeExpo2025Inferred || hasAnimeExpo2025Confirmed },
                ANIME_EXPO_2025_CONFIRMED.takeIf { hasAnimeExpo2025Confirmed },
                ANIME_NYC_2024_INFERRED.takeIf { hasAnimeNyc2024Inferred || hasAnimeNyc2024Confirmed },
                ANIME_NYC_2024_CONFIRMED.takeIf { hasAnimeNyc2024Confirmed },
                ANIME_NYC_2025_INFERRED.takeIf { hasAnimeNyc2025Inferred || hasAnimeNyc2025Confirmed },
                ANIME_NYC_2025_CONFIRMED.takeIf { hasAnimeNyc2025Confirmed },
            )
            val entries = TagYearFlag.entries
            return years.fold(0L) { flags, type ->
                val index = entries.indexOf(type)
                flags or (1L shl index)
            }
        }
        
        fun hasFlag(input: Long, flag: TagYearFlag) =
            (input and (1L shl TagYearFlag.entries.indexOf(flag))) != 0L

        fun getFlag(year: DataYear, confirmed: Boolean): Long {
            val flag = when (year) {
                DataYear.ANIME_EXPO_2023 -> return 0L
                DataYear.ANIME_EXPO_2024 -> if (confirmed) ANIME_EXPO_2024_CONFIRMED else ANIME_EXPO_2024_INFERRED
                DataYear.ANIME_EXPO_2025 -> if (confirmed) ANIME_EXPO_2025_CONFIRMED else ANIME_EXPO_2025_INFERRED
                DataYear.ANIME_NYC_2024 -> if (confirmed) ANIME_NYC_2024_CONFIRMED else ANIME_NYC_2024_INFERRED
                DataYear.ANIME_NYC_2025 -> if (confirmed) ANIME_NYC_2025_CONFIRMED else ANIME_NYC_2025_INFERRED
            }
            return 1L shl TagYearFlag.entries.indexOf(flag)
        }
    }
}
