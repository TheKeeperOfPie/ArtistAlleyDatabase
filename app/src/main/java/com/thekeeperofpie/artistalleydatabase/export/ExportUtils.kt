package com.thekeeperofpie.artistalleydatabase.export

import java.text.DateFormat
import java.time.Instant
import java.util.Date

object ExportUtils {

    const val UNIQUE_WORK_NAME = "export_all_entries"
    const val KEY_OUTPUT_CONTENT_URI = "output_content_uri"

    fun currentDateTimeFileName(): String =
        DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL)
            .format(Date.from(Instant.now()))
}