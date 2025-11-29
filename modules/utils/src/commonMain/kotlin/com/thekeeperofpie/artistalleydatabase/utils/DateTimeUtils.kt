package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeComponents
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
    val yearMonthFormat = LocalDate.Format {
        year()
        chars(", ")
        monthName(MonthNames.ENGLISH_ABBREVIATED)
    }
    val subtitleMonthYearFormat = LocalDate.Format {
        monthName(MonthNames.ENGLISH_ABBREVIATED)
        chars(" - ")
        year()
    }
    val fileDateFormat = LocalDate.Format {
        year()
        chars("-")
        monthNumber()
        char('-')
        dayOfMonth()
    }
    val dateTimeFormat = DateTimeComponents.Format {
        year()
        chars("-")
        monthNumber()
        char('-')
        day()
        char(' ')
        hour()
        char(':')
        minute()
        char(':')
        second()
        char(' ')
        offsetHours()
        char(':')
        offsetMinutesOfHour()
    }
}
