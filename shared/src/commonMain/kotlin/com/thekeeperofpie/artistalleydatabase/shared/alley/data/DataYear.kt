package com.thekeeperofpie.artistalleydatabase.shared.alley.data

import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DataYear(
    val serializedName: String,
    val year: Int,
    val artistTableName: String,
    val folderName: String,
    val dates: ClosedRange<LocalDate>,
    val timeZone: TimeZone,
) {
    @SerialName("AX2023")
    ANIME_EXPO_2023(
        serializedName = "AX2023",
        year = 2023,
        artistTableName = "artistEntry2023",
        folderName = "2023",
        dates = LocalDate(year = 2023, Month.JULY, 1)..LocalDate(year = 2023, Month.JULY, 4),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    @SerialName("AX2024")
    ANIME_EXPO_2024(
        serializedName = "AX2024",
        year = 2024,
        artistTableName = "artistEntry2024",
        folderName = "2024",
        dates = LocalDate(year = 2024, Month.JULY, 4)..LocalDate(year = 2024, Month.JULY, 7),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    @SerialName("AX2025")
    ANIME_EXPO_2025(
        serializedName = "AX2025",
        year = 2025,
        artistTableName = "artistEntry2025",
        folderName = "2025",
        dates = LocalDate(year = 2025, Month.JULY, 3)..LocalDate(year = 2025, Month.JULY, 6),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    @SerialName("ANYC2024")
    ANIME_NYC_2024(
        serializedName = "ANYC2024",
        year = 2024,
        artistTableName = "artistEntryAnimeNyc2024",
        folderName = "animeNyc2024",
        dates = LocalDate(year = 2024, Month.AUGUST, 23)..LocalDate(year = 2024, Month.AUGUST, 25),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),
    @SerialName("ANYC2025")
    ANIME_NYC_2025(
        serializedName = "ANYC2025",
        year = 2025,
        artistTableName = "artistEntryAnimeNyc2025",
        folderName = "animeNyc2025",
        dates = LocalDate(year = 2025, Month.AUGUST, 21)..LocalDate(year = 2025, Month.AUGUST, 24),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -4)),
    ),
    ;
    companion object {
        val LATEST = ANIME_NYC_2025
    }
}
