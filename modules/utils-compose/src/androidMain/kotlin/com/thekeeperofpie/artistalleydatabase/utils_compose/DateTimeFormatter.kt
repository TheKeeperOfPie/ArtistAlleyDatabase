package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalContext
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset

actual val LocalDateTimeFormatter: CompositionLocal<DateTimeFormatter> =
    compositionLocalWithComputedDefaultOf {
        DateTimeFormatter(LocalContext.currentValue)
    }

actual class DateTimeFormatter(private val context: Context) {

    // No better alternative to FORMAT_UTC
    // TODO: Find an alternative
    @Suppress("DEPRECATION")
    private val baseDateFormatFlags = DateUtils.FORMAT_ABBREV_ALL

    actual fun formatDateTime(
        year: Int?,
        month: Int?,
        dayOfMonth: Int?,
    ) = when {
        year != null && month != null && dayOfMonth != null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, month, dayOfMonth)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            baseDateFormatFlags or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        )
        year != null && month != null && dayOfMonth == null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, month, 1)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            baseDateFormatFlags or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE
        )
        year != null -> DateUtils.formatDateTime(
            context,
            LocalDate.of(year, Month.JANUARY, 1)
                .atTime(0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli(),
            baseDateFormatFlags or DateUtils.FORMAT_SHOW_YEAR
        )
        else -> null
    }

    actual fun formatEntryDateTime(timeInMillis: Long): String = DateUtils.formatDateTime(
        context,
        timeInMillis,
        DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_SHOW_TIME
    )

    actual fun formatAiringAt(timeInMillis: Long, showDate: Boolean): String = DateUtils.formatDateTime(
        context,
        timeInMillis,
        (baseDateFormatFlags or DateUtils.FORMAT_SHOW_TIME).transformIf(showDate) {
            this or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        }
    )

    actual fun formatRemainingTime(timeInMillis: Long): CharSequence = DateUtils.getRelativeTimeSpanString(
        timeInMillis,
        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
        0,
        baseDateFormatFlags,
    )

    actual fun formatShortDay(localDate: LocalDate): String = DateUtils.formatDateTime(
        context,
        localDate.atStartOfDay(ZoneOffset.UTC)
            .toEpochSecond() * 1000,
        baseDateFormatFlags or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_UTC
    )

    actual fun formatShortWeekday(localDate: LocalDate): String = DateUtils.formatDateTime(
        context,
        localDate.atStartOfDay(ZoneOffset.UTC)
            .toEpochSecond() * 1000,
        baseDateFormatFlags or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_UTC
    )
}
