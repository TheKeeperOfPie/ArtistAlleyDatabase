package com.thekeeperofpie.artistalleydatabase.alley.settings

import com.thekeeperofpie.artistalleydatabase.utils.DateTimeUtils
import io.ktor.util.toJsArray
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.dom.createElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.File

actual object ImportExportUtils {
    actual suspend fun download(fullExport: Boolean, text: String) {
        val dateSuffix = DateTimeUtils.fileDateFormat.format(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
        val fileName = "ArtistAlley-$dateSuffix.${if (fullExport) "json" else "txt"}"
        val array = JsArray<JsAny?>()
        array[0] = text.encodeToByteArray().toJsArray()

        val file = File(fileBits = array, fileName = fileName)
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
