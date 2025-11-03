package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import com.anilist.data.fragment.AniListDate
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@AssistedInject
class AnimeUserListViewModel(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    val ignoreController: IgnoreController,
    private val userMediaListController: UserMediaListController,
    private val mediaListStatusController: MediaListStatusController,
    @Assisted("userId") val userId: String?,
    @Assisted("userName") userName: String?,
    @Assisted val mediaType: MediaType,
    @Assisted val mediaListStatus: MediaListStatus?,
    @Assisted val mediaSortFilterViewModel: MediaSortFilterViewModel<MediaListSortOption>,
) : ViewModel() {

    companion object {
        private val SORT_ORDER = listOf(
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.COMPLETED,
            MediaListStatus.PAUSED,
            MediaListStatus.DROPPED,
        )
    }

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var entry by mutableStateOf<LoadingResult<Entry>>(LoadingResult.loading())
    var userName by mutableStateOf(userName)
        private set

    private val refresh = RefreshFlow()

    init {
        if (userId != null) {
            viewModelScope.launch(CustomDispatchers.IO) {
                val name = aniListApi.user(userId)?.name
                if (name != null) {
                    withContext(CustomDispatchers.Main) {
                        this@AnimeUserListViewModel.userName = name
                    }
                }
            }
        }

        val includeDescriptionFlow =
            MediaDataUtils.mediaViewOptionIncludeDescriptionFlow(snapshotFlow { mediaViewOption })

        viewModelScope.launch(CustomDispatchers.Main) {
            val response = if (userId == null) {
                includeDescriptionFlow.flatMapLatest {
                    when (mediaType) {
                        MediaType.MANGA -> userMediaListController.manga(it)
                        MediaType.ANIME,
                        MediaType.UNKNOWN__,
                            -> userMediaListController.anime(it)
                    }
                }
            } else {
                flowForRefreshableContent(
                    refresh.updates,
                    Res.string.anime_media_list_error_loading,
                ) {
                    includeDescriptionFlow.mapLatest {
                        val result = aniListApi.userMediaList(
                            userId = userId,
                            type = mediaType,
                            status = mediaListStatus,
                            includeDescription = it,
                        )
                        result.lists?.filterNotNull()
                            ?.map {
                                UserMediaListController.ListEntry(
                                    scoreFormat = result.user?.mediaListOptions?.scoreFormat,
                                    list = it,
                                )
                            }
                            .orEmpty()
                    }
                }
            }.map {
                it.transformResult {
                    it.filter { mediaListStatus == null || it.status == mediaListStatus }
                }
            }

            val mediaUpdates = response.mapLatest {
                it.result?.flatMap { it.entries.map { it.media.id.toString() } }?.toSet().orEmpty()
            }
                .distinctUntilChanged()
                .flatMapLatest(mediaListStatusController::allChanges)

            combine(
                response,
                mediaUpdates,
                snapshotFlow { query }.debounce(500.milliseconds),
                mediaSortFilterViewModel.filterParams,
                mediaSortFilterViewModel.tagShowWhenSpoiler,
                ::FilterParams
            ).flatMapLatest { (lists, mediaUpdates, query, filterParams, showTagWhenSpoiler) ->
                combine(
                    ignoreController.updates(),
                    settings.mediaFilteringData(forceShowIgnored = true)
                ) { _, filteringData ->
                    suspend fun List<MediaEntry>.mapEntries() = mapNotNull {
                        val newEntry = applyMediaFiltering(
                            statuses = mediaUpdates,
                            ignoreController = ignoreController,
                            filteringData = filteringData,
                            entry = it.entry,
                            filterableData = it.entry.mediaFilterable,
                            copy = { copy(mediaFilterable = it) }
                        ) ?: return@mapNotNull null
                        it.copy(entry = newEntry)
                    }

                    lists.transformResult { allLists ->
                        Entry(
                            all = allLists
                                .flatMap { it.entries }
                                .let {
                                    FilterIncludeExcludeState.applyFiltering(
                                        includes = filterParams.myListStatusIn,
                                        excludes = filterParams.myListStatusNotIn,
                                        it,
                                        {
                                            listOfNotNull(
                                                it.mediaFilterable.mediaListStatus
                                                    ?.toMediaListStatus()
                                            )
                                        },
                                    )
                                }
                                .let {
                                    val theirListStatusesIn = filterParams.theirListStatusIn
                                    if (theirListStatusesIn == null) it else {
                                        FilterIncludeExcludeState.applyFiltering(
                                            includes = theirListStatusesIn,
                                            excludes = filterParams.theirListStatusNotIn
                                                ?: emptyList(),
                                            list = it,
                                            transform = {
                                                listOfNotNull(it.authorData?.status)
                                            },
                                        )
                                    }
                                }
                                .toFilteredEntries(
                                    query = query,
                                    filterParams = filterParams,
                                    showAdult = filteringData.showAdult,
                                    showIgnored = filteringData.showIgnored,
                                    showTagWhenSpoiler = showTagWhenSpoiler,
                                )
                                .mapEntries(),
                            lists = allLists
                                .sortedBy { SORT_ORDER.indexOf(it.status) }
                                .map {
                                    ListEntry(
                                        name = it.name,
                                        scoreFormat = it.scoreFormat,
                                        entries = it.entries
                                            .toFilteredEntries(
                                                query = query,
                                                filterParams = filterParams,
                                                showAdult = filteringData.showAdult,
                                                showIgnored = filteringData.showIgnored,
                                                showTagWhenSpoiler = showTagWhenSpoiler,
                                            )
                                            .mapEntries(),
                                    )
                                }
                        )
                    }
                }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }
    }

    // TODO: Refresh indicator doesn't last duration of refresh
    fun onRefresh() {
        if (userId == null) {
            userMediaListController.refresh(mediaType)
        } else {
            refresh.refresh()
        }
    }

    private fun List<UserMediaListController.MediaEntry>.toFilteredEntries(
        query: String,
        filterParams: MediaSearchFilterParams<MediaListSortOption>,
        showAdult: Boolean,
        showIgnored: Boolean,
        showTagWhenSpoiler: Boolean,
    ): List<MediaEntry> {
        var filteredEntries = MediaUtils.filterEntries(
            filterParams = filterParams,
            showAdult = showAdult,
            showIgnored = showIgnored,
            showTagWhenSpoiler = showTagWhenSpoiler,
            entries = this,
            media = { it.media },
            mediaFilterable = { it.mediaFilterable },
        ).let {
            val theirScore = filterParams.theirScore
            var filteredByTheirScore = it
            if (theirScore != null) {
                val theirScoreStart = theirScore.startInt ?: 0
                val theirScoreEnd = theirScore.endInt
                if (theirScoreStart > 0) {
                    filteredByTheirScore = filteredByTheirScore.filter {
                        it.authorData?.rawScore.let { it != null && it >= theirScoreStart }
                    }
                }
                if (theirScoreEnd != null) {
                    // TODO: How should this handle null?
                    filteredByTheirScore = filteredByTheirScore.filter {
                        it.authorData?.rawScore.let { it == null || it <= theirScoreEnd }
                    }
                }
            }
            filteredByTheirScore
        }.run {
            val onList = filterParams.onList
            if (onList == null) return@run this
            if (onList) {
                filter {
                    it.mediaFilterable.mediaListStatus != null
                            && it.mediaFilterable.mediaListStatus != com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN
                }
            } else {
                filter {
                    it.mediaFilterable.mediaListStatus == null
                            || it.mediaFilterable.mediaListStatus == com.thekeeperofpie.artistalleydatabase.anime.data.MediaListStatus.UNKNOWN
                }
            }
        }

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

        val sortOption = filterParams.sort.firstOrNull()
        if (sortOption != null) {
            val baseComparator: Comparator<UserMediaListController.MediaEntry> =
                when (sortOption) {
                    MediaListSortOption.AVERAGE_SCORE -> compareBy { it.media.averageScore }
                    MediaListSortOption.STATUS -> compareBy { it.media.status }
                    MediaListSortOption.PROGRESS -> if (mediaType == MediaType.ANIME) {
                        compareBy { it.media.mediaListEntry?.progress }
                    } else {
                        compareBy { it.media.mediaListEntry?.progressVolumes }
                    }
                    MediaListSortOption.PRIORITY -> compareBy { it.media.mediaListEntry?.priority }
                    MediaListSortOption.START_DATE -> compareByAniListDate { it.media.startDate }
                    MediaListSortOption.END_DATE -> compareByAniListDate { it.media.endDate }
                    MediaListSortOption.MY_STARTED_ON -> compareByAniListDate { it.media.mediaListEntry?.startedAt }
                    MediaListSortOption.MY_FINISHED_ON -> compareByAniListDate { it.media.mediaListEntry?.completedAt }
                    MediaListSortOption.THEIR_STARTED_ON -> compareByAniListDate { it.authorData?.startedAt }
                    MediaListSortOption.THEIR_FINISHED_ON -> compareByAniListDate { it.authorData?.completedAt }
                    MediaListSortOption.MY_ADDED_TIME -> compareBy { it.media.mediaListEntry?.createdAt }
                    MediaListSortOption.MY_UPDATED_TIME -> compareBy { it.media.mediaListEntry?.updatedAt }
                    MediaListSortOption.THEIR_ADDED_TIME -> compareBy { it.authorData?.createdAt }
                    MediaListSortOption.THEIR_UPDATED_TIME -> compareBy { it.authorData?.updatedAt }
                    MediaListSortOption.TITLE_ROMAJI -> compareBy { it.media.title?.romaji }
                    MediaListSortOption.TITLE_ENGLISH -> compareBy { it.media.title?.english }
                    MediaListSortOption.TITLE_NATIVE -> compareBy { it.media.title?.native }
                    MediaListSortOption.POPULARITY -> compareBy { it.media.popularity }
                    MediaListSortOption.MY_SCORE -> compareBy { it.mediaFilterable.scoreRaw }
                    MediaListSortOption.THEIR_SCORE -> compareBy { it.authorData?.rawScore }
                }

            val comparator = nullsFirst(baseComparator).let {
                if (filterParams.sortAscending) it else it.reversed()
            }

            filteredEntries = filteredEntries.sortedWith(comparator)
        }

        return filteredEntries.map {
            MediaEntry(
                entry = MediaPreviewWithDescriptionEntry(
                    media = it.media,
                    mediaFilterable = it.mediaFilterable,
                ),
                authorData = it.authorData,
            )
        }
            .distinctBy { it.entry.media.id }
    }

    private fun <Input> compareByAniListDate(toDate: (Input) -> AniListDate?): Comparator<Input> =
        compareBy<Input, Int?>(nullsLast()) { toDate(it)?.year }
            .thenBy(nullsLast()) { toDate(it)?.month }
            .thenBy(nullsLast()) { toDate(it)?.day }

    private data class FilterParams(
        val response: LoadingResult<List<UserMediaListController.ListEntry>>,
        val mediaUpdates: Map<String, MediaListStatusController.Update>,
        val query: String,
        val filterParams: MediaSearchFilterParams<MediaListSortOption>,

        // This is broken out to avoid network refreshes,
        // since this doesn't affect the network response
        val showTagWhenSpoiler: Boolean,
    )

    data class ListEntry(
        val name: String,
        val scoreFormat: ScoreFormat?,
        val entries: List<MediaEntry>,
    )

    data class MediaEntry(
        val entry: MediaPreviewWithDescriptionEntry,
        val authorData: UserMediaListController.MediaEntry.AuthorData?,
    )

    data class Entry(
        val all: List<MediaEntry>,
        val lists: List<ListEntry>,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("userId") userId: String?,
            @Assisted("userName") userName: String?,
            mediaType: MediaType,
            mediaListStatus: MediaListStatus?,
            mediaSortFilterViewModel: MediaSortFilterViewModel<MediaListSortOption>,
        ): AnimeUserListViewModel
    }
}
