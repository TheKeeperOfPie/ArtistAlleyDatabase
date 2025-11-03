package com.thekeeperofpie.artistalleydatabase.data

import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
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
