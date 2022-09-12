package com.thekeeperofpie.artistalleydatabase.cds

import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import java.math.BigDecimal
import java.util.Date

data class CdEntryModel(
    val id: String,
    val catalogId: EntrySection.MultiText.Entry,
    val titles: List<EntrySection.MultiText.Entry>,
    val artists: List<EntrySection.MultiText.Entry>,
    val series: List<EntrySection.MultiText.Entry>,
    val characters: List<EntrySection.MultiText.Entry>,
    val tags: List<EntrySection.MultiText.Entry>,
    val price: BigDecimal?,
    val date: Date?,
    val lastEditTime: Date?,
    val imageWidth: Int?,
    val imageHeight: Int?,
    val notes: String?,
    val catalogIdLocked: EntrySection.LockState?,
    val titlesLocked: EntrySection.LockState?,
    val artistsLocked: EntrySection.LockState?,
    val seriesLocked: EntrySection.LockState?,
    val charactersLocked: EntrySection.LockState?,
    val tagsLocked: EntrySection.LockState?,
    val priceLocked: EntrySection.LockState?,
    val notesLocked: EntrySection.LockState?,
) {
    constructor(
        entry: CdEntry,
        catalogId: EntrySection.MultiText.Entry,
        titles: List<EntrySection.MultiText.Entry>,
        artists: List<EntrySection.MultiText.Entry>,
        series: List<EntrySection.MultiText.Entry>,
        characters: List<EntrySection.MultiText.Entry>,
        tags: List<EntrySection.MultiText.Entry>,
    ) : this(
        id = entry.id,
        catalogId = catalogId,
        titles = titles,
        artists = artists,
        series = series,
        characters = characters,
        tags = tags,
        price = entry.price,
        date = entry.date,
        lastEditTime = entry.lastEditTime,
        imageWidth = entry.imageWidth,
        imageHeight = entry.imageHeight,
        notes = entry.notes,
        catalogIdLocked = EntrySection.LockState.from(entry.locks.catalogIdLocked),
        titlesLocked = EntrySection.LockState.from(entry.locks.titlesLocked),
        artistsLocked = EntrySection.LockState.from(entry.locks.artistsLocked),
        seriesLocked = EntrySection.LockState.from(entry.locks.seriesLocked),
        charactersLocked = EntrySection.LockState.from(entry.locks.charactersLocked),
        tagsLocked = EntrySection.LockState.from(entry.locks.tagsLocked),
        priceLocked = EntrySection.LockState.from(entry.locks.priceLocked),
        notesLocked = EntrySection.LockState.from(entry.locks.notesLocked),
    )
}