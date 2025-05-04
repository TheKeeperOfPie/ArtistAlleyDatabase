package com.thekeeperofpie.artistalleydatabase.alley.settings

expect object ImportExportUtils {
    suspend fun download(text: String)
    fun getImportUrl(exportPartial: String): String
}
