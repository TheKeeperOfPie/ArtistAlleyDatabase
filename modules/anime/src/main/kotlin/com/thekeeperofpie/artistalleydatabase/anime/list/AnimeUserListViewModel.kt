package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.AuthedUserQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.combine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaFilterEntry
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeUserListViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList
) : ViewModel() {

    val query = MutableStateFlow("")
    var content by mutableStateOf<ContentState>(ContentState.LoadingEmpty)
    var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)

    private var initialized = false
    private var userId: String? = null
    private lateinit var mediaType: MediaType

    private val filterController =
        AnimeMediaFilterController(MediaListSortOption::class, aniListApi, settings)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?, mediaType: MediaType) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.mediaType = mediaType
        filterController.initialize(
            this, refreshUptimeMillis, AnimeMediaFilterController.InitialParams(
                // Disable "On list" filter, everything in this screen is on the user's list
                onListEnabled = false,
                isAnime = mediaType == MediaType.ANIME,
            )
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                aniListApi.authedUser.run {
                    if (userId == null) filterNotNull() else this
                },
                refreshUptimeMillis,
                filterController.sortOptions,
                filterController.sortAscending,
                ::RefreshParams
            )
                .debounce(100.milliseconds)
                .flatMapLatest { refreshParams ->
                    withContext(CustomDispatchers.IO) {
                        val baseResponse = flowOf(refreshParams)
                            .map {
                                aniListApi.userMediaList(
                                    userId = userId?.toIntOrNull() ?: refreshParams.authedUser!!.id,
                                    type = mediaType,
                                    sort = refreshParams.sortApiValue()
                                )
                            }
                        combine(
                            baseResponse,
                            query.debounce(500.milliseconds),
                            filterController.genres,
                            filterController.tagsByCategory,
                            filterController.tagRank(),
                            filterController.statuses,
                            filterController.listStatuses,
                            filterController.formats,
                            filterController.averageScoreRange,
                            filterController.episodesRange,
                            filterController.showAdult,
                            filterController.showIgnored,
                            filterController.airingDate(),
                            filterController.sources,
                            ::FilterParams,
                        )
                            .map { filterParams ->
                                filterParams.mediaListCollection
                                    ?.lists
                                    ?.filterNotNull()
                                    ?.filter {
                                        val listStatuses = filterParams.listStatuses
                                            .filter { it.state == IncludeExcludeState.INCLUDE }
                                            .map { it.value }
                                        if (listStatuses.isEmpty()) {
                                            true
                                        } else {
                                            listStatuses.contains(it.status)
                                        }
                                    }
                                    ?.let {
                                        if (refreshParams.sortOptions.none { it.state == IncludeExcludeState.INCLUDE }) {
                                            // If default sort, force COMPLETED list to top
                                            val index =
                                                it.indexOfFirst { it.status == MediaListStatus.COMPLETED }
                                            if (index >= 0) {
                                                val mutableList = it.toMutableList()
                                                val completedList = mutableList.removeAt(index)
                                                mutableList.add(0, completedList)
                                                mutableList
                                            } else it
                                        } else it
                                    }
                                    ?.map { toFilteredEntries(filterParams, it) }
                                    ?.flatten()
                                    ?.let(ContentState::Success)
                                    ?: ContentState.Error()
                            }
                            .startWith(ContentState.LoadingEmpty)
                            .catch { emit(ContentState.Error(exception = it)) }
                    }
                }
                .collectLatest { content = it }
        }
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onQuery(query: String) = this.query.update { query }

    fun onTagDismiss() {
        tagShown = null
    }

    fun onTagLongClick(tagId: String) {
        tagShown = filterController.tagsByCategory.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    private fun toFilteredEntries(
        filterParams: FilterParams,
        list: UserMediaListQuery.Data.MediaListCollection.List
    ) = mutableListOf<AnimeUserListScreen.Entry>().apply {
        var filteredEntries = list.entries
            ?.mapNotNull { it?.media }
            ?.map(AnimeUserListScreen.Entry::Item)
            .orEmpty()

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.statuses,
            filteredEntries,
            state = { it.state },
            key = { it.value },
            transform = { listOfNotNull(it.media.status) }
        )

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.formats,
            filteredEntries,
            state = { it.state },
            key = { it.value },
            transform = { listOfNotNull(it.media.format) }
        )

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.genres,
            filteredEntries,
            state = { it.state },
            key = { it.value },
            transform = { it.media.genres?.filterNotNull().orEmpty() }
        )

        val tagRank = filterParams.tagRank
        val transformIncludes: ((AnimeUserListScreen.Entry.Item) -> List<String>)? =
            if (tagRank == null) null else {
                {
                    it.media.tags
                        ?.filterNotNull()
                        ?.filter { it.rank?.let { it >= tagRank } == true }
                        ?.map { it.id.toString() }
                        .orEmpty()
                }
            }

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.tagsByCategory.values.flatMap {
                when (it) {
                    is AnimeMediaFilterController.TagSection.Category -> it.flatten()
                    is AnimeMediaFilterController.TagSection.Tag -> listOf(it)
                }
            },
            filteredEntries,
            state = { it.state },
            key = { it.value.id.toString() },
            transform = { it.media.tags?.filterNotNull()?.map { it.id.toString() }.orEmpty() },
            transformIncludes = transformIncludes,
        )

        if (!filterParams.showAdult) {
            filteredEntries = filteredEntries.filterNot { it.media.isAdult ?: false }
        }

        if (!filterParams.showIgnored) {
            filteredEntries = filteredEntries.filterNot { ignoreList.get(it.media.id) }
        }

        filteredEntries = when (val airingDate = filterParams.airingDate) {
            is AnimeMediaFilterController.AiringDate.Basic -> {
                filteredEntries.filter {
                    val season = airingDate.season
                    val seasonYear = airingDate.seasonYear.toIntOrNull()
                    (seasonYear == null || it.media.seasonYear == seasonYear)
                            && (season == null || it.media.season == season)
                }
            }
            is AnimeMediaFilterController.AiringDate.Advanced -> {
                val startDate = airingDate.startDate
                val endDate = airingDate.endDate

                if (startDate == null && endDate == null) {
                    filteredEntries
                } else {
                    fun List<AnimeUserListScreen.Entry.Item>.filterStartDate(
                        startDate: LocalDate
                    ) = filter {
                        val mediaStartDate = it.media.startDate
                        val mediaYear = mediaStartDate?.year
                        if (mediaYear == null) {
                            return@filter false
                        } else if (mediaYear > startDate.year) {
                            return@filter true
                        } else if (mediaYear < startDate.year) {
                            return@filter false
                        }

                        val mediaMonth = mediaStartDate.month
                        val mediaDayOfMonth = mediaStartDate.day

                        // TODO: Is this the correct behavior?
                        // If there's no month, match the media to avoid stripping expected result
                        if (mediaMonth == null) {
                            return@filter true
                        }

                        if (mediaMonth < startDate.monthValue) {
                            return@filter false
                        }

                        if (mediaMonth > startDate.monthValue) {
                            return@filter true
                        }

                        mediaDayOfMonth == null || mediaDayOfMonth >= startDate.dayOfMonth
                    }

                    fun List<AnimeUserListScreen.Entry.Item>.filterEndDate(
                        endDate: LocalDate
                    ) = filter {
                        val mediaStartDate = it.media.startDate
                        val mediaYear = mediaStartDate?.year
                        if (mediaYear == null) {
                            return@filter false
                        } else if (mediaYear > endDate.year) {
                            return@filter false
                        } else if (mediaYear < endDate.year) {
                            return@filter true
                        }

                        val mediaMonth = mediaStartDate.month
                        val mediaDayOfMonth = mediaStartDate.day

                        // TODO: Is this the correct behavior?
                        // If there's no month, match the media to avoid stripping expected result
                        if (mediaMonth == null) {
                            return@filter true
                        }

                        if (mediaMonth < endDate.monthValue) {
                            return@filter true
                        }

                        if (mediaMonth > endDate.monthValue) {
                            return@filter false
                        }

                        mediaDayOfMonth == null || mediaDayOfMonth <= endDate.dayOfMonth
                    }

                    if (startDate != null && endDate != null) {
                        filteredEntries.filterStartDate(startDate)
                            .filterEndDate(endDate)
                    } else if (startDate != null) {
                        filteredEntries.filterStartDate(startDate)
                    } else if (endDate != null) {
                        filteredEntries.filterEndDate(endDate)
                    } else {
                        filteredEntries
                    }
                }
            }
        }

        val averageScore = filterParams.averageScoreRange
        val averageScoreStart = averageScore.startInt ?: 0
        val averageScoreEnd = averageScore.endInt
        if (averageScoreStart > 0) {
            filteredEntries = filteredEntries.filter {
                it.media.averageScore.let { it != null && it >= averageScoreStart }
            }
        }
        if (averageScoreEnd != null) {
            filteredEntries = filteredEntries.filter {
                it.media.averageScore.let { it != null && it <= averageScoreEnd }
            }
        }

        val episodes = filterParams.episodesRange
        val episodesStart = episodes.startInt ?: 0
        val episodesEnd = episodes.endInt
        if (episodesStart > 0) {
            filteredEntries = filteredEntries.filter {
                it.media.episodes.let { it != null && it >= episodesStart }
            }
        }
        if (episodesEnd != null) {
            filteredEntries = filteredEntries.filter {
                it.media.episodes.let { it != null && it <= episodesEnd }
            }
        }

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.sources,
            filteredEntries,
            state = { it.state },
            key = { it.value },
            transform = { listOfNotNull(it.media.source) }
        )

        val query = filterParams.query
        if (query.isNotBlank()) {
            filteredEntries = filteredEntries.filter {
                listOfNotNull(
                    it.media.title?.romaji,
                    it.media.title?.english,
                    it.media.title?.native,
                ).plus(it.media.synonyms?.filterNotNull().orEmpty())
                    .any { it.contains(query, ignoreCase = true) }
            }
        }

        if (filteredEntries.isNotEmpty()) {
            this += AnimeUserListScreen.Entry.Header(
                list.name.orEmpty(),
                list.status
            )
            this += filteredEntries
        }
    }

    private data class RefreshParams(
        val authedUser: AuthedUserQuery.Data.Viewer?,
        val requestMillis: Long = SystemClock.uptimeMillis(),
        val sortOptions: List<AnimeMediaFilterController.SortEntry<MediaListSortOption>>,
        val sortAscending: Boolean,
    ) {
        fun sortApiValue() = sortOptions.filter { it.state == IncludeExcludeState.INCLUDE }
            .map { it.value.toApiValue(sortAscending) }
    }

    private data class FilterParams(
        val mediaListCollection: UserMediaListQuery.Data.MediaListCollection?,
        val query: String,
        val genres: List<MediaFilterEntry<String>>,
        val tagsByCategory: Map<String, AnimeMediaFilterController.TagSection>,
        val tagRank: Int?,
        val statuses: List<AnimeMediaFilterController.StatusEntry>,
        val listStatuses: List<AnimeMediaFilterController.ListStatusEntry>,
        val formats: List<AnimeMediaFilterController.FormatEntry>,
        val averageScoreRange: AnimeMediaFilterController.RangeData,
        val episodesRange: AnimeMediaFilterController.RangeData,
        val showAdult: Boolean,
        val showIgnored: Boolean,
        val airingDate: AnimeMediaFilterController.AiringDate,
        val sources: List<AnimeMediaFilterController.SourceEntry>,
    )

    sealed interface ContentState {
        object LoadingEmpty : ContentState

        data class Success(
            val entries: List<AnimeUserListScreen.Entry>,
            val loading: Boolean = false,
        ) : ContentState

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Throwable? = null
        ) : ContentState
    }
}
