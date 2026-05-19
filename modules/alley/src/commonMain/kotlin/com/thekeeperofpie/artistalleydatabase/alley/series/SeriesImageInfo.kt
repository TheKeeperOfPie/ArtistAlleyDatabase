package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import kotlin.uuid.Uuid

data class SeriesImageInfo(
    val id: String,
    val uuid: Uuid,
    val aniListId: Long?,
    val wikipediaId: Long?,
    val tmdbId: String?,
    val tmdbType: TmdbType?,
    val steamId: String?,
    val openLibraryId: String?,
)
