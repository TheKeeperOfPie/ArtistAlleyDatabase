package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object AlleyUtils {
    fun isCurrentYear(year: DataYear) =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year == year.year
}
