package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeUserListViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    val ignoreController: IgnoreController,
    private val mediaTagsController: MediaTagsController,
    private val mediaGenresController: MediaGenresController,
    private val mediaLicensorsController: MediaLicensorsController,
    private val userMediaListController: UserMediaListController,
    private val featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
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
    var userName by mutableStateOf<String?>(null)
        private set

    private var userId: String? = null
    private var initialized = false
    var mediaListStatus: MediaListStatus? = null
        private set
    lateinit var mediaType: MediaType
        private set

    lateinit var sortFilterController: MediaSortFilterController<*, *>

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(
        userId: String?,
        userName: String?,
        mediaType: MediaType,
        status: MediaListStatus? = null,
    ) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.mediaType = mediaType
        this.userName = userName
        this.mediaListStatus = status
        sortFilterController = if (mediaType == MediaType.ANIME) {
            AnimeSortFilterController(
                sortTypeEnumClass = MediaListSortOption::class,
                aniListApi = aniListApi,
                settings = settings,
                featureOverrideProvider = featureOverrideProvider,
                mediaTagsController = mediaTagsController,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
            ).apply {
                initialize(
                    viewModel = this@AnimeUserListViewModel,
                    refreshUptimeMillis = refreshUptimeMillis,
                    initialParams = AnimeSortFilterController.InitialParams(
                        onListEnabled = false,
                        defaultSort = MediaListSortOption.UPDATED_TIME,
                        lockSort = false,
                        mediaListStatus = status,
                        lockMediaListStatus = status != null,
                    )
                )
            }
        } else {
            MangaSortFilterController(
                sortTypeEnumClass = MediaListSortOption::class,
                aniListApi = aniListApi,
                settings = settings,
                featureOverrideProvider = featureOverrideProvider,
                mediaTagsController = mediaTagsController,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
            ).apply {
                initialize(
                    viewModel = this@AnimeUserListViewModel,
                    refreshUptimeMillis = refreshUptimeMillis,
                    initialParams = MangaSortFilterController.InitialParams(
                        onListEnabled = false,
                        defaultSort = MediaListSortOption.UPDATED_TIME,
                        lockSort = false,
                        mediaListStatus = status,
                        lockMediaListStatus = status != null,
                    )
                )
            }
        }

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

        val includeDescriptionFlow = MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption }

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
                    refreshUptimeMillis,
                    R.string.anime_media_list_error_loading
                ) {
                    includeDescriptionFlow.mapLatest {
                        val result = aniListApi.userMediaList(
                            userId = userId,
                            type = mediaType,
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
            }

            val mediaUpdates = response.mapLatest {
                it.result?.flatMap { it.entries.map { it.media.id.toString() } }?.toSet().orEmpty()
            }
                .distinctUntilChanged()
                .flatMapLatest(mediaListStatusController::allChanges)

            @Suppress("UNCHECKED_CAST")
            combine(
                response,
                mediaUpdates,
                snapshotFlow { query }.debounce(500.milliseconds),
                (sortFilterController as? AnimeSortFilterController<MediaListSortOption>)?.filterParams()
                    ?: (sortFilterController as MangaSortFilterController<MediaListSortOption>).filterParams(),
                ::FilterParams
            ).flatMapLatest { (lists, mediaUpdates, query, filterParams) ->
                combine(
                    ignoreController.updates(),
                    settings.showAdult,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { _, showAdult, showLessImportantTags, showSpoilerTags ->
                    suspend fun List<MediaEntry>.mapEntries() = mapNotNull {
                        applyMediaFiltering(
                            statuses = mediaUpdates,
                            ignoreController = ignoreController,
                            showAdult = showAdult,
                            showIgnored = true,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                            entry = it,
                            transform = { it.entry },
                            media = it.entry.media,
                            copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                copy(
                                    entry = entry.copy(
                                        mediaListStatus = mediaListStatus,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        scoreRaw = scoreRaw,
                                        ignored = ignored,
                                        showLessImportantTags = showLessImportantTags,
                                        showSpoilerTags = showSpoilerTags,
                                    )
                                )
                            }
                        )
                    }

                    lists.transformResult { allLists ->
                        Entry(
                            all = allLists
                                .filter {
                                    val listStatuses = filterParams.listStatuses
                                        .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                                        .map { it.value }
                                    if (listStatuses.isEmpty()) {
                                        true
                                    } else {
                                        listStatuses.contains(it.status)
                                    }
                                }
                                .flatMap { it.entries }
                                .toFilteredEntries(query, filterParams)
                                .mapEntries(),
                            lists = allLists
                                .sortedBy { SORT_ORDER.indexOf(it.status) }
                                .map {
                                    ListEntry(
                                        name = it.name,
                                        scoreFormat = it.scoreFormat,
                                        entries = it.entries
                                            .toFilteredEntries(query, filterParams)
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
            refreshUptimeMillis.value = SystemClock.uptimeMillis()
        }
    }

    private fun List<UserMediaListController.MediaEntry>.toFilteredEntries(
        query: String,
        filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
    ): List<MediaEntry> {
        var filteredEntries = MediaUtils.filterEntries(
            filterParams = filterParams,
            entries = this,
            media = { it.media },
        )

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

        val sortOption = filterParams.sort
            .find { it.state == FilterIncludeExcludeState.INCLUDE }?.value
        if (sortOption != null) {
            val baseComparator: Comparator<UserMediaListController.MediaEntry> =
                when (sortOption) {
                    MediaListSortOption.SCORE -> compareBy { it.media.averageScore }
                    MediaListSortOption.STATUS -> compareBy { it.media.status }
                    MediaListSortOption.PROGRESS -> if (mediaType == MediaType.ANIME) {
                        compareBy { it.media.mediaListEntry?.progress }
                    } else {
                        compareBy { it.media.mediaListEntry?.progressVolumes }
                    }
                    MediaListSortOption.PRIORITY -> compareBy { it.media.mediaListEntry?.priority }
                    MediaListSortOption.STARTED_ON ->
                        compareBy<UserMediaListController.MediaEntry, Int?>(nullsLast()) {
                            it.media.startDate?.year
                        }
                            .thenComparing(compareBy(nullsLast()) { it.media.startDate?.month })
                            .thenComparing(compareBy(nullsLast()) { it.media.startDate?.day })
                    MediaListSortOption.FINISHED_ON ->
                        compareBy<UserMediaListController.MediaEntry, Int?>(nullsLast()) {
                            it.media.endDate?.year
                        }
                            .thenComparing(compareBy(nullsLast()) { it.media.endDate?.month })
                            .thenComparing(compareBy(nullsLast()) { it.media.endDate?.day })
                    MediaListSortOption.ADDED_TIME -> compareBy { it.media.mediaListEntry?.createdAt }
                    MediaListSortOption.UPDATED_TIME -> compareBy { it.media.mediaListEntry?.updatedAt }
                    MediaListSortOption.TITLE_ROMAJI -> compareBy { it.media.title?.romaji }
                    MediaListSortOption.TITLE_ENGLISH -> compareBy { it.media.title?.english }
                    MediaListSortOption.TITLE_NATIVE -> compareBy { it.media.title?.native }
                    MediaListSortOption.POPULARITY -> compareBy { it.media.popularity }
                }

            val comparator = nullsFirst(baseComparator).let {
                if (filterParams.sortAscending) it else it.reversed()
            }

            filteredEntries = filteredEntries.sortedWith(comparator)
        }

        return filteredEntries.map {
            MediaEntry(
                entry = MediaPreviewWithDescriptionEntry(
                    it.media,
                    mediaListStatus = it.mediaListStatus,
                    progress = it.progress,
                    progressVolumes = it.progressVolumes,
                    scoreRaw = it.scoreRaw,
                    ignored = it.ignored,
                    showLessImportantTags = it.showLessImportantTags,
                    showSpoilerTags = it.showSpoilerTags,
                ),
                authorData = it.authorData,
            )
        }
            .distinctBy { it.entry.media.id }
    }

    private data class FilterParams(
        val response: LoadingResult<List<UserMediaListController.ListEntry>>,
        val mediaUpdates: Map<String, MediaListStatusController.Update>,
        val query: String,
        val filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
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
}
