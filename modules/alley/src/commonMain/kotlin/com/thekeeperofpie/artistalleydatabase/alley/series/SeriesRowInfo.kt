package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption

interface SeriesRowInfo {
    val id: String
    val uuid: String
    val notes: String?
    val aniListId: Long?
    val aniListType: String?
    val wikipediaId: Long?
    val titlePreferred: String
    val titleEnglish: String
    val titleRomaji: String
    val titleNative: String
    val link: String?

    fun toImageInfo() = SeriesImageInfo(
        id = id,
        uuid = uuid,
        aniListId = aniListId,
        aniListType = aniListType,
        wikipediaId = wikipediaId,
    )

    class Impl(
        override val id: String,
        override val uuid: String,
        override val notes: String?,
        override val aniListId: Long?,
        override val aniListType: String?,
        override val wikipediaId: Long?,
        override val titlePreferred: String,
        override val titleEnglish: String,
        override val titleRomaji: String,
        override val titleNative: String,
        override val link: String?,
    ) : SeriesRowInfo
}

fun SeriesEntry.toRowInfo() = SeriesRowInfo.Impl(
    id = id,
    uuid = uuid,
    notes = notes,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    link = link,
)

fun SeriesRowInfo.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}
