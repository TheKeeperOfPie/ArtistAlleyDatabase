package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption

fun SeriesEntry.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT,
    AniListLanguageOption.ROMAJI -> titleRomaji
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
}
