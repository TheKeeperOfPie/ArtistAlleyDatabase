package com.thekeeperofpie.artistalleydatabase.alley.series.search

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesRowId
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import kotlinx.serialization.Serializable

@Serializable
data class SeriesFilterEntry(
    val rowid: SeriesRowId,
    val id: String,
    val titlePreferred: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleNative: String,
) {
    constructor(entry: SeriesInfo) : this(
        rowid = entry.rowid,
        id = entry.id,
        titlePreferred = entry.titlePreferred,
        titleEnglish = entry.titleEnglish,
        titleRomaji = entry.titleRomaji,
        titleNative = entry.titleNative,
    )

    fun name(languageOption: AniListLanguageOption) = when (languageOption) {
        AniListLanguageOption.DEFAULT -> titlePreferred
        AniListLanguageOption.ENGLISH -> titleEnglish
        AniListLanguageOption.NATIVE -> titleNative
        AniListLanguageOption.ROMAJI -> titleRomaji
    }
}
