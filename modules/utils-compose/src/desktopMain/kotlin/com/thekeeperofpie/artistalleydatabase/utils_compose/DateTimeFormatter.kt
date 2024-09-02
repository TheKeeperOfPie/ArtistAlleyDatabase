package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import java.time.LocalDate

actual val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter> = compositionLocalWithComputedDefaultOf { DateTimeFormatter() }

actual class DateTimeFormatter {
    actual fun formatDateTime(
        year: Int?,
        month: Int?,
        dayOfMonth: Int?,
    ): String? {
        TODO("Not yet implemented")
    }

    actual fun formatEntryDateTime(timeInMillis: Long): String {
        TODO("Not yet implemented")
    }

    actual fun formatAiringAt(timeInMillis: Long, showDate: Boolean): String {
        TODO("Not yet implemented")
    }

    actual fun formatRemainingTime(timeInMillis: Long): CharSequence {
        TODO("Not yet implemented")
    }

    actual fun formatShortDay(localDate: LocalDate): String {
        TODO("Not yet implemented")
    }

    actual fun formatShortWeekday(localDate: LocalDate): String {
        TODO("Not yet implemented")
    }
}
