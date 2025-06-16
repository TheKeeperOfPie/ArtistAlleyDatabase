package com.thekeeperofpie.artistalleydatabase.alley.settings

import java.io.File

actual object ImportExportUtils {
    actual suspend fun download(fullExport: Boolean, text: String) = Unit
    actual fun getImportUrl(exportPartial: String): String =
        "https://localhost#import=$exportPartial"
}
