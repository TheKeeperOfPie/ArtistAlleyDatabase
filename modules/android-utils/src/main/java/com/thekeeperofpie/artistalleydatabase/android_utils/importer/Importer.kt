package com.thekeeperofpie.artistalleydatabase.android_utils.importer

import java.io.InputStream

interface Importer {

    val zipEntryName: String

    suspend fun readEntries(input: InputStream, dryRun: Boolean, replaceAll: Boolean): Int

    suspend fun readInnerFile(input: InputStream, fileName: String, dryRun: Boolean)

    suspend fun <T> transaction(block: suspend () -> T): T
}