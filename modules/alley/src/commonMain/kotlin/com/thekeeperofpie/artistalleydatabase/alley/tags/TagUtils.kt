package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_default
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_english
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_native
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_romaji
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
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

@Composable
fun previewSeriesWithUserData(id: String): SeriesWithUserData {
    if (!LocalInspectionMode.current) throw IllegalStateException("Must be in preview")
    return SeriesWithUserData(
        series = SeriesEntry(
            id = id,
            uuid = id,
            notes = null,
            aniListId = null,
            aniListType = null,
            wikipediaId = null,
            source = null,
            titlePreferred = id,
            titleEnglish = id,
            titleRomaji = id,
            titleNative = id,
            link = null,
            has2024 = false,
            has2025 = false,
            counter = 1,
        ),
        userEntry = SeriesUserEntry(
            seriesId = id,
            favorite = false,
        ),
    )
}
