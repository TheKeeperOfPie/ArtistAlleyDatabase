package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import io.github.vinceglb.filekit.utils.toBitsArray
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.dom.createElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File

// TODO: Share more code with wasmJs
actual object ImportExportUtils {
    actual suspend fun download(fullExport: Boolean, text: String) {
        val dateSuffix = DateTimeUtils.fileDateFormat.format(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
        val fileName = "ArtistAlley-$dateSuffix.${if (fullExport) "json" else "txt"}"

        val file = File(fileBits = text.encodeToByteArray().toBitsArray(), fileName = fileName)
        val element = document.createElement("a") {
            this as HTMLAnchorElement
            href = URL.createObjectURL(file)
            download = fileName
        } as HTMLAnchorElement

        element.click()
    }

    actual fun getImportUrl(exportPartial: String) =
        "${window.location.origin}#import=$exportPartial"
}
