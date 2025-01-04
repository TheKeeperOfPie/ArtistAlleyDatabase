package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.io.Source

internal expect object CsvReader {
    fun read(source: Source): Sequence<Map<String, String>>
}
