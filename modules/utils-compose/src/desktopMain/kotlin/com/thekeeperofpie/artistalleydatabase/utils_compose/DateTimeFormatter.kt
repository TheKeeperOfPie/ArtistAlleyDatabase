package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.jacobras.humanreadable.HumanReadable

actual val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter> =
    compositionLocalWithComputedDefaultOf { DateTimeFormatter() }

actual class DateTimeFormatter {
    // TODO
    actual fun formatDateTime(
        year: Int?,
        month: Int?,
        dayOfMonth: Int?,
    ): String? {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        if (year == null) return null
        if (month == null) return year.toString()
        return if (dayOfMonth == null) {
            DateTimeUtils.yearMonthFormat.format(
                LocalDate(
                    year = year,
                    month = Month(month),
                    dayOfMonth = today.dayOfMonth,
                )
            )
        } else {
            DateTimeUtils.shortDateFormat.format(
                LocalDate(
                    year = year,
                    month = Month(month),
                    dayOfMonth = dayOfMonth,
                )
            )
        }
    }

    // TODO
    actual fun formatEntryDateTime(timeInMillis: Long) =
        DateTimeUtils.shortDateFormat.format(
            Instant.fromEpochMilliseconds(timeInMillis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        )

    actual fun formatAiringAt(instant: Instant, showDate: Boolean) = HumanReadable.timeAgo(instant)

    actual fun formatRemainingTime(instant: Instant): CharSequence = HumanReadable.timeAgo(instant)

    // TODO
    actual fun formatShortDay(localDate: LocalDate) =
        DateTimeUtils.shortDateFormat.format(localDate)

    // TODO
    actual fun formatShortWeekday(localDate: LocalDate) =
        DateTimeUtils.shortDateFormat.format(localDate)

    actual fun formatSubtitleMonthYear(year: Int?, month: Int?): String? {
        year ?: return null
        if (month == null) return year.toString()
        return DateTimeUtils.subtitleMonthYearFormat.format(LocalDate(year, month, 1))
    }
}
