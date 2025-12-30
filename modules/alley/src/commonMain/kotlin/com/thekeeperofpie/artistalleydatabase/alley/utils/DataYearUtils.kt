package com.thekeeperofpie.artistalleydatabase.alley.utils

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.UtcOffset

val DataYear.Dates.start
    get() = LocalDate(year = year, month = month, day = startDay)

val DataYear.Dates.timeZone
    get() = FixedOffsetTimeZone(UtcOffset(hours = timeZoneOffsetHours))
