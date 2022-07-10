package com.thekeeperofpie.artistalleydatabase.art.details

import com.thekeeperofpie.artistalleydatabase.art.ArtEntry

class ArtEntryModel(
    private val value: ArtEntry,
    val artists: List<ArtEntrySection.MultiText.Entry.Custom>,
    val series: List<ArtEntrySection.MultiText.Entry>,
    val characters: List<ArtEntrySection.MultiText.Entry>,
    val tags: List<ArtEntrySection.MultiText.Entry.Custom>,
) {

    val sourceType
        get() = value.sourceType

    val sourceValue
        get() = value.sourceValue

    val printWidth
        get() = value.printWidth

    val printHeight
        get() = value.printHeight

    val notes
        get() = value.notes

    val locks
        get() = value.locks
}