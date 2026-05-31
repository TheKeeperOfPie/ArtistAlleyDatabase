package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import kotlinx.serialization.Serializable

@Serializable
data class SeriesImageInfo(
    val id: String,
    val aniListId: Long?,
    val wikipediaId: Long?,
    val tmdbId: String?,
    val tmdbType: TmdbType?,
    val steamId: String?,
    val openLibraryId: String?,
)
