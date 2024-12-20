package com.thekeeperofpie.artistalleydatabase.anime.media.data.filter

import com.anilist.data.LicensorsQuery
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaTagSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

data class MediaSearchFilterParams<SortType : SortOption>(
    val sort: List<SortEntry<SortType>>,
    val sortAscending: Boolean,
    val genres: List<FilterEntry<String>>,
    val tagsByCategory: Map<String, MediaTagSection>,
    val tagRank: Int?,
    val statuses: List<FilterEntry<MediaStatus>>,
    val myListStatuses: List<FilterEntry<MediaListStatus>>,
    val theirListStatuses: List<FilterEntry<MediaListStatus>>?,
    val onList: Boolean?,
    val myScore: RangeData?,
    val theirScore: RangeData?,
    val formats: List<FilterEntry<MediaFormat>>,
    val averageScoreRange: RangeData,
    val episodesRange: RangeData?,
    val volumesRange: RangeData?,
    val chaptersRange: RangeData?,
    val showAdult: Boolean,
    val showIgnored: Boolean,
    val airingDate: AiringDate,
    val sources: List<FilterEntry<MediaSource>>,
    val licensedBy: List<FilterEntry<LicensorsQuery.Data.ExternalLinkSourceCollection>>,
)
