package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

fun StampRallyEntryAnimeExpo2026.toStampRallyDatabaseEntry() =
    StampRallyDatabaseEntry(
        year = DataYear.ANIME_EXPO_2026,
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        series = series,
        merch = merch,
        notes = notes,
        images = images,
        counter = counter,
        confirmed = confirmed,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
    )

fun StampRallyDatabaseEntry.toStampRallyEntryAnimeExpo2026() =
    StampRallyEntryAnimeExpo2026(
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prize = prize,
        prizeLimit = prizeLimit,
        series = series,
        merch = merch,
        notes = notes,
        images = images,
        counter = counter,
        confirmed = confirmed,
        editorNotes = editorNotes,
        lastEditor = lastEditor,
        lastEditTime = lastEditTime,
    )
