package com.thekeeperofpie.artistalleydatabase.data

import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class DataConverter(
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
