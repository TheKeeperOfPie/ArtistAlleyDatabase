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
        return DateTimeUtils.shortDateFormat.format(
            LocalDate(
                year = year ?: today.year,
                month = Month(month ?: today.month.value),
                dayOfMonth = dayOfMonth ?: today.dayOfMonth,
            )
        )
    }

    // TODO
    actual fun formatEntryDateTime(timeInMillis: Long) =
        DateTimeUtils.shortDateFormat.format(
            Instant.fromEpochMilliseconds(timeInMillis)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
        )

    actual fun formatAiringAt(timeInMillis: Long, showDate: Boolean) =
        HumanReadable.timeAgo(Instant.fromEpochMilliseconds(timeInMillis))

    actual fun formatRemainingTime(timeInMillis: Long): CharSequence {
        return HumanReadable.timeAgo(Instant.fromEpochMilliseconds(timeInMillis))
    }

    // TODO
    actual fun formatShortDay(localDate: LocalDate) =
        DateTimeUtils.shortDateFormat.format(localDate)

    // TODO
    actual fun formatShortWeekday(localDate: LocalDate) =
        DateTimeUtils.shortDateFormat.format(localDate)
}
