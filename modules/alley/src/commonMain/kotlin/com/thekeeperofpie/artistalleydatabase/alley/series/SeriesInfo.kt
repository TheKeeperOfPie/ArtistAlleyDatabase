package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption

fun SeriesInfo.toImageInfo() = SeriesImageInfo(
    id = id,
    aniListId = aniListId,
    wikipediaId = wikipediaId,
    tmdbId = tmdbId,
    tmdbType = tmdbType,
    steamId = steamId,
    steamImagePath = steamImagePath,
    openLibraryId = openLibraryId,
)

fun SeriesInfo.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}

fun SeriesInfo.otherTitles(languageOption: AniListLanguageOption): List<String> = listOf(
    titlePreferred,
    titleEnglish,
    titleRomaji,
    titleNative,
).distinct() - name(languageOption)

fun GetSeriesTitles.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}
