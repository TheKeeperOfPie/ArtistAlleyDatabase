package com.thekeeperofpie.artistalleydatabase.alley.utils

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock

val DataYear.Dates.start
    get() = LocalDate(year = year, month = month, day = startDay)

val DataYear.Dates.end
    get() = LocalDate(year = year, month = month, day = endDay)

val DataYear.Dates.timeZone
    get() = FixedOffsetTimeZone(UtcOffset(hours = timeZoneOffsetHours))

val DataYear.Dates.isOver
    get() = end.atStartOfDayIn(timeZone) < Clock.System.now()
