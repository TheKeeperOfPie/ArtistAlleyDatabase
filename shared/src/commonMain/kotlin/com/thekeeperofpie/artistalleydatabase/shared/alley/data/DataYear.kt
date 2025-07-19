package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.serialization.Serializable

@Serializable
enum class DataYear(
    val serializedName: String,
    val year: Int,
    val tableName: String,
    val folderName: String,
    val dates: ClosedRange<LocalDate>,
    val timeZone: TimeZone,
) {
    ANIME_EXPO_2023(
        serializedName = "AX2023",
        year = 2023,
        tableName = "artistEntry2023",
        folderName = "2023",
        dates = LocalDate(year = 2023, Month.JULY, 1)..LocalDate(year = 2023, Month.JULY, 4),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    ANIME_EXPO_2024(
        serializedName = "AX2024",
        year = 2024,
        tableName = "artistEntry2024",
        folderName = "2024",
        dates = LocalDate(year = 2024, Month.JULY, 4)..LocalDate(year = 2024, Month.JULY, 7),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    ANIME_EXPO_2025(
        serializedName = "AX2025",
        year = 2025,
        tableName = "artistEntry2025",
        folderName = "2025",
        dates = LocalDate(year = 2025, Month.JULY, 3)..LocalDate(year = 2025, Month.JULY, 6),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    ANIME_NYC_2024(
        serializedName = "ANYC2024",
        year = 2024,
        tableName = "artistEntryAnimeNyc2024",
        folderName = "animeNyc2024",
        dates = LocalDate(year = 2024, Month.AUGUST, 23)..LocalDate(year = 2024, Month.AUGUST, 25),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),
    ANIME_NYC_2025(
        serializedName = "ANYC2025",
        year = 2025,
        tableName = "artistEntryAnimeNyc2025",
        folderName = "animeNyc2025",
        dates = LocalDate(year = 2025, Month.AUGUST, 21)..LocalDate(year = 2025, Month.AUGUST, 24),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),
    ;
    companion object {
        val LATEST = ANIME_NYC_2025
    }
}
