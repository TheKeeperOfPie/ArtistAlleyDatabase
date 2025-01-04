package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.io.Source
import kotlinx.io.readLine

actual object CsvReader {
    // TODO: Real implementation
    actual fun read(source: Source): Sequence<Map<String, String>> {
        val header = source.readLine()!!
        val columnNames = header.split(",")
        return sequence {
            while (!source.exhausted()) {
                val line = source.readLine() ?: continue
                val values = line.split(",").take(columnNames.size)
                yield(values.withIndex().associate { columnNames[it.index] to it.value })
            }
        }
    }
}
