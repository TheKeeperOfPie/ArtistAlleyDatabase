package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import kotlin.uuid.Uuid

fun SeriesInfo.toImageInfo() = SeriesImageInfo(
    id = id,
    uuid = uuid,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
)

fun SeriesEntry.toSeriesInfo() = SeriesInfo(
    id = id,
    uuid = Uuid.parse(uuid),
    notes = notes,
    aniListId = aniListId,
    aniListType = aniListType,
    wikipediaId = wikipediaId,
    source = source ?: SeriesSource.NONE,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    link = link,
)

fun SeriesInfo.name(languageOption: AniListLanguageOption) = when (languageOption) {
    AniListLanguageOption.DEFAULT -> titlePreferred
    AniListLanguageOption.ENGLISH -> titleEnglish
    AniListLanguageOption.NATIVE -> titleNative
    AniListLanguageOption.ROMAJI -> titleRomaji
}
