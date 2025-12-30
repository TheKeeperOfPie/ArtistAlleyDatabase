package com.thekeeperofpie.artistalleydatabase.alley.form

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.utils.start
import com.thekeeperofpie.artistalleydatabase.alley.utils.timeZone
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.Padding
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days

expect object FormUtils {

    suspend fun generateEncryptedFormLink(): String
}

@Suppress("UnusedReceiverParameter")
fun FormUtils.generateAddToCalendarLink(dataYear: DataYear, conventionName: String, encryptedFormLink: String,): String {
    val dates = dataYear.dates
    val timeZone = dates.timeZone
    val targetStartDate = (dates.start.atStartOfDayIn(timeZone) - 30.days)
        .toLocalDateTime(timeZone)
        .date
    val targetEndDate = targetStartDate.plus(1, DateTimeUnit.DAY)
    val formatter = LocalDate.Format {
        year(Padding.ZERO)
        monthNumber(Padding.ZERO)
        day(Padding.ZERO)
    }
    return Uri.parse("https://calendar.google.com/calendar/render?action=TEMPLATE")
            .buildUpon()
            .appendQueryParameter(
                "text",
                "[$conventionName] Update artist info"
            )
            .appendQueryParameter(
                "dates",
                "${formatter.format(targetStartDate)}/" +
                        formatter.format(targetEndDate)
            )
            .appendQueryParameter(
                "details",
                "Access your link here; this will only work from the exact same " +
                        "browser you submitted the form. Otherwise use your " +
                        "private access link again.\n\n" + encryptedFormLink
            )
            .appendQueryParameter("trp", "true")
            .appendQueryParameter("sprop", AlleyUtils.siteUrl)
            .appendQueryParameter("sprop", "name:Artist Alley Directory")
            .build()
            .toString()
}
