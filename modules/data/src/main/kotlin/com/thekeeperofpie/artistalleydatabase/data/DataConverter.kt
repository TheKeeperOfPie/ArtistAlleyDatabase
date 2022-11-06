package com.thekeeperofpie.artistalleydatabase.data

import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import javax.inject.Inject

class DataConverter @Inject constructor(
    private val aniListDataConverter: AniListDataConverter
) {

    fun seriesEntries(series: List<Series>) = series.map {
        when (it) {
            is Series.AniList -> aniListDataConverter.seriesEntry(it.entry)
            is Series.Custom -> EntrySection.MultiText.Entry.Custom(it.text)
        }
    }

    fun characterEntries(characters: List<Character>) = characters.map {
        when (it) {
            is Character.AniList -> aniListDataConverter.characterEntry(it.entry)
            is Character.Custom -> EntrySection.MultiText.Entry.Custom(it.text)
        }
    }
}