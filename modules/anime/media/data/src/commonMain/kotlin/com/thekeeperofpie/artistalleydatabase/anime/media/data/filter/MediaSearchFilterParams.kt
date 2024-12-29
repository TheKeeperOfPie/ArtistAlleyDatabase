package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

data class MediaSearchFilterParams<SortType : SortOption>(
    val sort: List<SortType> = emptyList(),
    val sortAscending: Boolean = false,
    val genreIn: List<String> = emptyList(),
    val genreNotIn: List<String> = emptyList(),
    val tagNameIn: List<String> = emptyList(),
    val tagNameNotIn: List<String> = emptyList(),
    val tagRank: Int? = null,
    val statusIn: List<MediaStatus> = emptyList(),
    val statusNotIn: List<MediaStatus> = emptyList(),
    val myListStatusIn: List<MediaListStatus> = emptyList(),
    val myListStatusNotIn: List<MediaListStatus> = emptyList(),
    val theirListStatusIn: List<MediaListStatus>? = null,
    val theirListStatusNotIn: List<MediaListStatus>? = null,
    val onList: Boolean? = null,
    val myScore: RangeData? = null,
    val theirScore: RangeData? = null,
    val formatIn: List<MediaFormat> = emptyList(),
    val formatNotIn: List<MediaFormat> = emptyList(),
    val averageScoreRange: RangeData = RangeData(100, hardMax = true),
    val episodesRange: RangeData? = null,
    val volumesRange: RangeData? = null,
    val chaptersRange: RangeData? = null,
    val airingDate: AiringDate = AiringDate.Basic(),
    val sourceIn: List<MediaSource> = emptyList(),
    val licensedByIdIn: List<Int>? = null,
)
