package com.thekeeperofpie.artistalleydatabase.alley.settings

actual object ImportExportUtils {
    actual suspend fun download(fullExport: Boolean, text: String) = Unit
    actual fun getImportUrl(exportPartial: String): String = TODO()
}
