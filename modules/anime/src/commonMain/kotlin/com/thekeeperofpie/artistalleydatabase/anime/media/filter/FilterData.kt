package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anime.list.MediaListSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

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
        LocalDate(
            year = airingDateStartYear,
            monthNumber = airingDateStartMonth,
            dayOfMonth = airingDateStartDayOfMonth,
        )
    } else null

    fun airingDateEnd() = if (airingDateEndYear != null
        && airingDateEndMonth != null
        && airingDateEndDayOfMonth != null
    ) {
        LocalDate(
            year = airingDateEndYear,
            monthNumber = airingDateEndMonth,
            dayOfMonth = airingDateEndDayOfMonth,
        )
    } else null
}
