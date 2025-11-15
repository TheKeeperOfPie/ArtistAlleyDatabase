package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry

data class SeriesWithUserData(
    val series: SeriesEntry,
    val userEntry: SeriesUserEntry,
): SeriesRowInfo {
    override val id get() = series.id
    override val uuid get() = series.uuid
    override val notes get() = series.notes
    override val aniListId get() = series.aniListId
    override val aniListType get() = series.aniListType
    override val wikipediaId get() = series.wikipediaId
    override val titlePreferred get() = series.titlePreferred
    override val titleEnglish get() = series.titleEnglish
    override val titleRomaji get() = series.titleRomaji
    override val titleNative get() = series.titleNative
    override val link get() = series.link
}
