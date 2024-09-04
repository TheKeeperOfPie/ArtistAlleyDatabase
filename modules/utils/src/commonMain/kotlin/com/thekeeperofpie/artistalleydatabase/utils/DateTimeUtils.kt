package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

object DateTimeUtils {
    // TODO: Localized date format
    val shortDateFormat = LocalDate.Format {
        year()
        chars(", ")
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        char(' ')
        dayOfMonth()
    }
}
