package com.thekeeperofpie.artistalleydatabase.export

import java.text.DateFormat
import java.time.Instant
import java.util.Date

object ExportUtils {

    fun currentDateTimeFileName(): String =
        DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
            .format(Date.from(Instant.now()))
}