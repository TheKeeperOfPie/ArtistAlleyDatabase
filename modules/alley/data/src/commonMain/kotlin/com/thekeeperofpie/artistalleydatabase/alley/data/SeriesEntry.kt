package com.thekeeperofpie.artistalleydatabase.alley.data

import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo

fun SeriesInfo.toSeriesEntry() = SeriesEntry(
    id = id,
    uuid = uuid.toString(),
    notes = notes,
    aniListId = aniListId,
    aniListType = aniListType.takeUnless { it == AniListType.NONE }?.name,
    wikipediaId = wikipediaId,
    tmdbId = tmdbId,
    tmdbType = tmdbType,
    steamId = steamId,
    steamImagePath = steamImagePath,
    openLibraryId = openLibraryId,
    source = source,
    titlePreferred = titlePreferred,
    titleEnglish = titleEnglish,
    titleRomaji = titleRomaji,
    titleNative = titleNative,
    synonyms = synonyms,
    link = link,
    inferred2024 = 0, // TODO: Recalculate counts when adding to local database
    inferred2025 = 0,
    inferredAnimeExpo2026 = 0,
    inferredAnimeNyc2024 = 0,
    inferredAnimeNyc2025 = 0,
    inferredAnimeNyc2026 = 0,
    confirmed2024 = 0,
    confirmed2025 = 0,
    confirmedAnimeExpo2026 = 0,
    confirmedAnimeNyc2024 = 0,
    confirmedAnimeNyc2025 = 0,
    confirmedAnimeNyc2026 = 0,
)
