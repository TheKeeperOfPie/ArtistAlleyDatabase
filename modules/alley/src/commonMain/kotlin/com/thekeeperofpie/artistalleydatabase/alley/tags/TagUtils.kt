package com.thekeeperofpie.artistalleydatabase.alley.tags

import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_default
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_english
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_native
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_romaji
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import org.jetbrains.compose.resources.StringResource

fun SeriesEntry.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}

val AniListLanguageOption.textWithExplanation: StringResource
    get() = when (this) {
        AniListLanguageOption.DEFAULT -> Res.string.alley_language_option_default
        AniListLanguageOption.ENGLISH -> Res.string.alley_language_option_english
        AniListLanguageOption.NATIVE -> Res.string.alley_language_option_native
        AniListLanguageOption.ROMAJI -> Res.string.alley_language_option_romaji
    }
