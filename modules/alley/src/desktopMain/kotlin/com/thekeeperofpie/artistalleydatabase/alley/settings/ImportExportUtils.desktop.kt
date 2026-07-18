package com.thekeeperofpie.artistalleydatabase.alley.settings

actual object ImportExportUtils {
    actual suspend fun download(fullExport: Boolean, text: String) {
        println(if (fullExport) "Full export: $text" else "Partial export: $text")
    }

    actual fun getImportUrl(exportPartial: String): String =
        "https://localhost/import/$exportPartial"
}
