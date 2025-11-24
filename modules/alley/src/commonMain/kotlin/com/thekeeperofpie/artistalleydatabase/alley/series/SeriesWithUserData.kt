package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import kotlin.uuid.Uuid

data class SeriesWithUserData(
    val series: SeriesEntry,
    val userEntry: SeriesUserEntry,
): SeriesRowInfo {
    override val uuid = Uuid.parse(series.uuid)

    override val id get() = series.id
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
