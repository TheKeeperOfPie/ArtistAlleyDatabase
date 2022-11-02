package com.thekeeperofpie.artistalleydatabase.android_utils.persistence

import java.io.InputStream

interface Importer {

    val zipEntryName: String

    suspend fun readEntries(input: InputStream, dryRun: Boolean, replaceAll: Boolean): Int

    suspend fun readInnerFile(input: InputStream, fileName: String, dryRun: Boolean)
}