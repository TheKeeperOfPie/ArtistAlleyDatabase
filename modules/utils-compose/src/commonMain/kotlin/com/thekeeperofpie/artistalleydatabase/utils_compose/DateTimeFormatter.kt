package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.CompositionLocal
import java.time.LocalDate

expect val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter>

expect class DateTimeFormatter {
    fun formatDateTime(year: Int?, month: Int?, dayOfMonth: Int?): String?
    fun formatEntryDateTime(timeInMillis: Long): String
    fun formatAiringAt(timeInMillis: Long, showDate: Boolean = true): String
    fun formatRemainingTime(timeInMillis: Long): CharSequence
    fun formatShortDay(localDate: LocalDate): String
    fun formatShortWeekday(localDate: LocalDate): String
}
