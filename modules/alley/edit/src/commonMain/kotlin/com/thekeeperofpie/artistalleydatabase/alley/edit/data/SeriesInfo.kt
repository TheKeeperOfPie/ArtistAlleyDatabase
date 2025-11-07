package com.thekeeperofpie.artistalleydatabase.alley.edit.data

import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import kotlinx.serialization.Serializable

@Serializable
data class SeriesInfo(
    val id: String,
    val uuid: String,
    val notes: String?,
    val aniListId: Long?,
    val aniListType: String?,
    val wikipediaId: Long?,
    val titlePreferred: String,
    val titleEnglish: String,
    val titleRomaji: String,
    val titleNative: String,
) {
    fun toImageInfo() = SeriesImageInfo(
        id = id,
        uuid = uuid,
        aniListId = aniListId,
        aniListType = aniListType,
        wikipediaId = wikipediaId,
    )
}

fun SeriesInfo.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}
