package com.thekeeperofpie.artistalleydatabase.utils_compose

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalContext
import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.offsetIn
import kotlin.time.Clock
import kotlin.time.Instant

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
            LocalDate(year, month, dayOfMonth)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds(),
            baseDateFormatFlags or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
        )
        year != null && month != null && dayOfMonth == null -> DateUtils.formatDateTime(
            context,
            LocalDate(year, month, 1)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds(),
            baseDateFormatFlags or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NO_MONTH_DAY
        )
        year != null -> year.toString()
        else -> null
    }

    actual fun formatEntryDateTime(timeInMillis: Long): String = DateUtils.formatDateTime(
        context,
        timeInMillis,
        DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_SHOW_TIME
    )

    actual fun formatAiringAt(instant: Instant, showDate: Boolean): String =
        DateUtils.formatDateTime(
            context,
            instant.toEpochMilliseconds(),
            (baseDateFormatFlags or DateUtils.FORMAT_SHOW_TIME).transformIf(showDate) {
                this or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
            }
        )

    actual fun formatRemainingTime(instant: Instant): CharSequence =
        DateUtils.getRelativeTimeSpanString(
            instant.toEpochMilliseconds(),
            Clock.System.now().toEpochMilliseconds(),
            0,
            baseDateFormatFlags,
        )

    actual fun formatShortDay(localDate: LocalDate): String = DateUtils.formatDateTime(
        context,
        localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        baseDateFormatFlags or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_UTC
    )

    actual fun formatShortWeekday(localDate: LocalDate): String = DateUtils.formatDateTime(
        context,
        localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        baseDateFormatFlags or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_UTC
    )

    actual fun formatSubtitleMonthYear(year: Int?, month: Int?): String? {
        year ?: return null
        if (month == null) return year.toString()
        return DateTimeUtils.subtitleMonthYearFormat.format(LocalDate(year, month, 1))
    }

    actual fun formatDateTime(instant: Instant): String =
        instant.format(
            format = DateTimeUtils.dateTimeFormat,
            offset = instant.offsetIn(TimeZone.currentSystemDefault()),
        )
}
