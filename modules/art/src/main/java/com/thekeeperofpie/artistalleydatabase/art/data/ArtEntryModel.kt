package com.thekeeperofpie.artistalleydatabase.art.data

import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.form.EntrySection

data class ArtEntryModel(
    val artists: List<EntrySection.MultiText.Entry>,
    val series: List<EntrySection.MultiText.Entry>,
    val characters: List<EntrySection.MultiText.Entry>,
    val tags: List<EntrySection.MultiText.Entry>,
    val source: SourceType?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String?,
    val artistsLocked: EntrySection.LockState?,
    val sourceLocked: EntrySection.LockState?,
    val seriesLocked: EntrySection.LockState?,
    val charactersLocked: EntrySection.LockState?,
    val tagsLocked: EntrySection.LockState?,
    val notesLocked: EntrySection.LockState?,
    val printSizeLocked: EntrySection.LockState?,
) {
    constructor(
        entry: ArtEntry,
        artists: List<EntrySection.MultiText.Entry.Custom>,
        series: List<EntrySection.MultiText.Entry>,
        characters: List<EntrySection.MultiText.Entry>,
        tags: List<EntrySection.MultiText.Entry.Custom>,
        source: SourceType?,
    ) : this(
        artists = artists,
        series = series,
        characters = characters,
        tags = tags,
        source = source,
        printWidth = entry.printWidth,
        printHeight = entry.printHeight,
        notes = entry.notes,
        artistsLocked = EntrySection.LockState.from(entry.locks.artistsLocked),
        sourceLocked = EntrySection.LockState.from(entry.locks.sourceLocked),
        seriesLocked = EntrySection.LockState.from(entry.locks.seriesLocked),
        charactersLocked = EntrySection.LockState.from(entry.locks.charactersLocked),
        tagsLocked = EntrySection.LockState.from(entry.locks.tagsLocked),
        notesLocked = EntrySection.LockState.from(entry.locks.notesLocked),
        printSizeLocked = EntrySection.LockState.from(entry.locks.printSizeLocked),
    )
}