package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

fun StampRallyEntry.toStampRallyDatabaseEntry() =
    StampRallyDatabaseEntry(
        year = dataYear,
        id = id,
        fandom = fandom,
        hostTable = tables.firstOrNull().orEmpty(),
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        prizeMerch = prizeMerch.orEmpty(),
        startTables = startTables.orEmpty(),
        endTables = endTables.orEmpty(),
        series = series,
        merch = merch,
        notes = notes,
        images = images,
        confirmed = links.isNotEmpty() || images.isNotEmpty(),
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
    )

fun StampRallyDatabaseEntry.toStampRallyEntry(dataYear: DataYear) =
    StampRallyEntry(
        id = id,
        dataYear = dataYear,
        fandom = fandom,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        prizeMerch = prizeMerch,
        startTables = startTables,
        endTables = endTables,
        series = series,
        merch = merch,
        notes = notes,
        images = images,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
    )
