package com.thekeeperofpie.artistalleydatabase.art.details

import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.SourceType

class ArtEntryModel(
    val artists: List<ArtEntrySection.MultiText.Entry>,
    val series: List<ArtEntrySection.MultiText.Entry>,
    val characters: List<ArtEntrySection.MultiText.Entry>,
    val tags: List<ArtEntrySection.MultiText.Entry>,
    val sourceType: SourceType?,
    val sourceValue: String?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String?,
    val artistsLocked: ArtEntrySection.LockState?,
    val sourceLocked: ArtEntrySection.LockState?,
    val seriesLocked: ArtEntrySection.LockState?,
    val charactersLocked: ArtEntrySection.LockState?,
    val tagsLocked: ArtEntrySection.LockState?,
    val notesLocked: ArtEntrySection.LockState?,
    val printSizeLocked: ArtEntrySection.LockState?,
) {
    constructor(
        entry: ArtEntry,
        artists: List<ArtEntrySection.MultiText.Entry.Custom>,
        series: List<ArtEntrySection.MultiText.Entry>,
        characters: List<ArtEntrySection.MultiText.Entry>,
        tags: List<ArtEntrySection.MultiText.Entry.Custom>,
        sourceType: SourceType?,
    ) : this(
        artists = artists,
        series = series,
        characters = characters,
        tags = tags,
        sourceType = sourceType,
        sourceValue = entry.sourceValue,
        printWidth = entry.printWidth,
        printHeight = entry.printHeight,
        notes = entry.notes,
        artistsLocked = ArtEntrySection.LockState.from(entry.locks.artistsLocked),
        sourceLocked = ArtEntrySection.LockState.from(entry.locks.sourceLocked),
        seriesLocked = ArtEntrySection.LockState.from(entry.locks.seriesLocked),
        charactersLocked = ArtEntrySection.LockState.from(entry.locks.charactersLocked),
        tagsLocked = ArtEntrySection.LockState.from(entry.locks.tagsLocked),
        notesLocked = ArtEntrySection.LockState.from(entry.locks.notesLocked),
        printSizeLocked = ArtEntrySection.LockState.from(entry.locks.printSizeLocked),
    )
}