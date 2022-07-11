package com.thekeeperofpie.artistalleydatabase.art.details

import com.thekeeperofpie.artistalleydatabase.art.ArtEntry

class ArtEntryModel(
    val artists: List<ArtEntrySection.MultiText.Entry.Custom>,
    val series: List<ArtEntrySection.MultiText.Entry>,
    val characters: List<ArtEntrySection.MultiText.Entry>,
    val tags: List<ArtEntrySection.MultiText.Entry.Custom>,
    val sourceType: String?,
    val sourceValue: String?,
    val printWidth: Int?,
    val printHeight: Int?,
    val notes: String?,
    val locks: ArtEntry.Locks,
) {
    constructor(
        entry: ArtEntry,
        artists: List<ArtEntrySection.MultiText.Entry.Custom>,
        series: List<ArtEntrySection.MultiText.Entry>,
        characters: List<ArtEntrySection.MultiText.Entry>,
        tags: List<ArtEntrySection.MultiText.Entry.Custom>,
    ) : this(
        artists = artists,
        series = series,
        characters = characters,
        tags = tags,
        sourceType = entry.sourceType,
        sourceValue = entry.sourceValue,
        printWidth = entry.printWidth,
        printHeight = entry.printHeight,
        notes = entry.notes,
        locks = entry.locks,
    )
}