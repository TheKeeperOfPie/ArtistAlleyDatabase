package com.thekeeperofpie.artistalleydatabase.alley.series

import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.SeriesSource
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TmdbType
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SeriesImageInfo(
    val id: String,
    val uuid: Uuid,
    val aniListId: Long?,
    val wikipediaId: Long?,
    val tmdbId: String?,
    val tmdbType: TmdbType?,
    val steamId: String?,
    val openLibraryId: String?,
) {
    fun toSeriesInfo() = SeriesInfo(
        id = id,
        uuid = uuid,
        notes = null,
        aniListId = aniListId,
        aniListType = AniListType.ANIME,
        wikipediaId = wikipediaId,
        tmdbId = tmdbId,
        tmdbType = tmdbType,
        steamId = steamId,
        openLibraryId = openLibraryId,
        source = SeriesSource.NONE,
        titlePreferred = id,
        titleEnglish = id,
        titleRomaji = id,
        titleNative = id,
        synonyms = emptyList(),
        link = null,
        faked = true,
    )
}
