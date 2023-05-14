package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anime.list.MediaListSortOption
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class FilterData(
    // TODO: Sort by multiple is technically possible
    // These sort options are mutually exclusive, but it simplifies a lot of code to store both
    val sortOption: MediaSortOption? = null,
    val sortListOption: MediaListSortOption? = null,
    val sortAscending: Boolean = false,
    val tagRank: Int? = null,
    val onList: Boolean? = null,
    val isAnime: Boolean? = null,
    val averageScoreMin: Int? = null,
    val averageScoreMax: Int? = null,
    val episodesMin: Int? = null,
    val episodesMax: Int? = null,
    val sourcesIncluded: List<MediaSource> = emptyList(),
    val sourcesExcluded: List<MediaSource> = emptyList(),
    val statusesIncluded: List<MediaStatus> = emptyList(),
    val statusesExcluded: List<MediaStatus> = emptyList(),
    val listStatusesIncluded: List<MediaListStatus> = emptyList(),
    val listStatusesExcluded: List<MediaListStatus> = emptyList(),
    val formatsIncluded: List<MediaFormat> = emptyList(),
    val formatsExcluded: List<MediaFormat> = emptyList(),

    val airingDateIsAdvanced: Boolean = false,
    val airingDateSeason: MediaSeason? = null,
    val airingDateSeasonYear: Int? = null,

    // These are separated to make serialization easier
    val airingDateStartYear: Int? = null,
    val airingDateStartMonth: Int? = null,
    val airingDateStartDayOfMonth: Int? = null,
    val airingDateEndYear: Int? = null,
    val airingDateEndMonth: Int? = null,
    val airingDateEndDayOfMonth: Int? = null,

    // The following value are dependent on API responses
    val genresIncluded: List<String> = emptyList(),
    val genresExcluded: List<String> = emptyList(),
    val tagsIncluded: List<String> = emptyList(),
    val tagsExcluded: List<String> = emptyList(),
) {
    fun airingDateStart() = if (airingDateStartYear != null
        && airingDateStartMonth != null
        && airingDateStartDayOfMonth != null
    ) {
        LocalDate.of(airingDateStartYear, airingDateStartMonth, airingDateStartDayOfMonth)
    } else null

    fun airingDateEnd() = if (airingDateEndYear != null
        && airingDateEndMonth != null
        && airingDateEndDayOfMonth != null
    ) {
        LocalDate.of(airingDateEndYear, airingDateEndMonth, airingDateEndDayOfMonth)
    } else null
}
