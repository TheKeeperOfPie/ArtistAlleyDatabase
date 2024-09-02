package com.thekeeperofpie.artistalleydatabase.anilist

import androidx.compose.runtime.compositionLocalOf
import artistalleydatabase.modules.anilist.generated.resources.Res
import artistalleydatabase.modules.anilist.generated.resources.aniList_language_option_default
import artistalleydatabase.modules.anilist.generated.resources.aniList_language_option_english
import artistalleydatabase.modules.anilist.generated.resources.aniList_language_option_native
import artistalleydatabase.modules.anilist.generated.resources.aniList_language_option_romaji
import org.jetbrains.compose.resources.StringResource

enum class AniListLanguageOption(val textRes: StringResource) {
    DEFAULT(Res.string.aniList_language_option_default),
    ENGLISH(Res.string.aniList_language_option_english),
    NATIVE(Res.string.aniList_language_option_native),
    ROMAJI(Res.string.aniList_language_option_romaji),
}

val LocalLanguageOptionMedia = compositionLocalOf { AniListLanguageOption.DEFAULT }
val LocalLanguageOptionCharacters = compositionLocalOf { AniListLanguageOption.DEFAULT }
val LocalLanguageOptionStaff = compositionLocalOf { AniListLanguageOption.DEFAULT }
