package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.io.Source
import kotlinx.io.asInputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

internal actual object CsvReader {
    actual fun read(source: Source) = csvReader(source)
        .asSequence()
        .map(CSVRecord::toMap)

    private fun csvReader(source: Source) =
        CSVFormat.RFC4180.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()
            .parse(source.asInputStream().reader())
            .asSequence()
}
