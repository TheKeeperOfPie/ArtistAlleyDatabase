package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

actual object ExportFileDownloader {
    actual suspend fun download(text: String) {
        val dateSuffix = DateTimeUtils.fileDateFormat.format(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
        val fileName = "ArtistAlley-$dateSuffix.json"
        FileKit.download(text.encodeToByteArray(), fileName)
    }
}
