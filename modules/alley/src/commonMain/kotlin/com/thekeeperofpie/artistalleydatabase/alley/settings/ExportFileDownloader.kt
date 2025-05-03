package com.thekeeperofpie.artistalleydatabase.alley.settings

expect object ExportFileDownloader {
    suspend fun download(text: String)
}
