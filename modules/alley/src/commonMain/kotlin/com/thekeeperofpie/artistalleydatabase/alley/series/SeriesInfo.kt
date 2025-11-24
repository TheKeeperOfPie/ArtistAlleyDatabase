package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption

fun SeriesInfo.toImageInfo() = SeriesImageInfo(
    id = id,
    uuid = uuid,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
)

fun SeriesInfo.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}
