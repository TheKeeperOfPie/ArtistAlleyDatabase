package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.annotation.StringRes
import androidx.compose.runtime.compositionLocalOf

enum class AniListLanguageOption(@StringRes val textRes: Int) {
    DEFAULT(R.string.aniList_language_option_default),
    ENGLISH(R.string.aniList_language_option_english),
    NATIVE(R.string.aniList_language_option_native),
    ROMAJI(R.string.aniList_language_option_romaji),
}

val LocalLanguageOptionMedia = compositionLocalOf { AniListLanguageOption.DEFAULT }
val LocalLanguageOptionCharacters = compositionLocalOf { AniListLanguageOption.DEFAULT }
val LocalLanguageOptionStaff = compositionLocalOf { AniListLanguageOption.DEFAULT }
