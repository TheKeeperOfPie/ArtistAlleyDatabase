package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_default
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_english
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_native
import artistalleydatabase.modules.alley.generated.resources.alley_language_option_romaji
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import org.jetbrains.compose.resources.StringResource
import kotlin.uuid.Uuid

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
        series = SeriesInfo(
            id = id,
            uuid = Uuid.parse(id),
            notes = null,
            aniListId = null,
            aniListType = null,
            wikipediaId = null,
            source = SeriesSource.NONE,
            titlePreferred = id,
            titleEnglish = id,
            titleRomaji = id,
            titleNative = id,
            link = null,
        ),
        userEntry = SeriesUserEntry(
            seriesId = id,
            favorite = false,
        ),
    )
}
