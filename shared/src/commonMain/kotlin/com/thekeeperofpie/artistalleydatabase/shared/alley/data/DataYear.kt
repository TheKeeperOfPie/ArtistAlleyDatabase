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
    val dates: ClosedRange<LocalDate>,
    val timeZone: TimeZone,
) {
    YEAR_2023(
        serializedName = "AX2023",
        year = 2023,
        dates = LocalDate(year = 2023, Month.JULY, 1)..LocalDate(year = 2023, Month.JULY, 4),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    YEAR_2024(
        serializedName = "AX2024",
        year = 2024,
        dates = LocalDate(year = 2024, Month.JULY, 4)..LocalDate(year = 2024, Month.JULY, 7),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
    YEAR_2025(
        serializedName = "AX2025",
        year = 2025,
        dates = LocalDate(year = 2025, Month.JULY, 3)..LocalDate(year = 2025, Month.JULY, 6),
        timeZone = FixedOffsetTimeZone(UtcOffset(hours = -7)),
    ),
}
