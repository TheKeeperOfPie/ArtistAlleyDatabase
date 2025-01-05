package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readLine
import kotlinx.io.readString

actual object CsvReader {
    actual fun read(source: Source): Sequence<Map<String, String>> {
        val header = source.readLine()!!
        val columnNames = header.split(",")
        val columnCount = columnNames.size
        return sequence {
            val buffer = Buffer()
            while (!source.exhausted()) {
                var fieldIndex = 0
                val map = mutableMapOf<String, String>()
                buffer.clear()
                val commaByte = ','.code.toByte()
                val quoteByte = '"'.code.toByte()
                val newLineByte = '\n'.code.toByte()
                var insideQuote = false
                while (fieldIndex < columnCount && !source.exhausted()) {
                    when (val byte = source.readByte()) {
                        quoteByte -> insideQuote = !insideQuote
                        commaByte,
                        newLineByte,
                            -> {
                            if (insideQuote) {
                                buffer.writeByte(byte)
                            } else {
                                map[columnNames[fieldIndex]] = buffer.readString()
                                fieldIndex++
                            }
                        }
                        else -> buffer.writeByte(byte)
                    }
                }

                yield(map)
            }

            buffer.close()
        }
    }
}
