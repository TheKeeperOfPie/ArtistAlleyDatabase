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
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
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
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeUserListViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    val ignoreList: AnimeMediaIgnoreList,
    private val mediaTagsController: MediaTagsController,
    private val mediaGenresController: MediaGenresController,
    private val mediaLicensorsController: MediaLicensorsController,
    private val userMediaListController: UserMediaListController,
    private val featureOverrideProvider: FeatureOverrideProvider,
    private val mediaListStatusController: MediaListStatusController,
) : ViewModel() {

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content by mutableStateOf<LoadingResult<List<ListEntry>>>(LoadingResult.loading())
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

        val includeDescriptionFlow = snapshotFlow { mediaViewOption }
            .map { it == MediaViewOption.LARGE_CARD }
            .transformWhile {
                // Take until description is ever requested,
                // then always request to prevent unnecessary refreshes
                emit(it)
                !it
            }
            .distinctUntilChanged()

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
                combine(includeDescriptionFlow, refreshUptimeMillis, ::Pair)
                    .flatMapLatest { (includeDescription) ->
                        aniListApi.userMediaList(
                            userId = userId,
                            type = mediaType,
                            includeDescription = includeDescription,
                        )
                    }
                    .mapLatest {
                        it.transformResult {
                            it.lists?.filterNotNull()
                                ?.map(UserMediaListController::ListEntry)
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
                    ignoreList.updates,
                    settings.showAdult,
                    settings.showLessImportantTags,
                    settings.showSpoilerTags,
                ) { ignoredIds, showAdult, showLessImportantTags, showSpoilerTags ->
                    lists.transformResult {
                        it.filter {
                            val listStatuses = filterParams.listStatuses
                                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                                .map { it.value }
                            if (listStatuses.isEmpty()) {
                                true
                            } else {
                                listStatuses.contains(it.status)
                            }
                        }
                            .map {
                                it.copy(
                                    entries = it.entries.mapNotNull {
                                        applyMediaFiltering(
                                            statuses = mediaUpdates,
                                            ignoredIds = ignoredIds,
                                            showAdult = showAdult,
                                            showIgnored = true,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                            entry = it,
                                            transform = { it },
                                            media = it.media,
                                            copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                                copy(
                                                    mediaListStatus = mediaListStatus,
                                                    progress = progress,
                                                    progressVolumes = progressVolumes,
                                                    scoreRaw = scoreRaw,
                                                    ignored = ignored,
                                                    showLessImportantTags = showLessImportantTags,
                                                    showSpoilerTags = showSpoilerTags,
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                            .map { toFilteredEntries(query, filterParams, it) }
                            .distinctBy { it.name }
                    }
                }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { content = it }
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

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry) =
        ignoreList.toggle(entry.media.id.toString())

    private fun toFilteredEntries(
        query: String,
        filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
        list: UserMediaListController.ListEntry,
    ): ListEntry {
        val entries = list.entries

        var filteredEntries = MediaUtils.filterEntries(
            filterParams = filterParams,
            entries = entries,
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

        return ListEntry(
            name = list.name,
            entries = filteredEntries.map {
                MediaPreviewWithDescriptionEntry(
                    it.media,
                    mediaListStatus = it.mediaListStatus,
                    progress = it.progress,
                    progressVolumes = it.progressVolumes,
                    scoreRaw = it.scoreRaw,
                    ignored = it.ignored,
                    showLessImportantTags = it.showLessImportantTags,
                    showSpoilerTags = it.showSpoilerTags,
                )
            }
                .distinctBy { it.media.id },
        )
    }

    private data class FilterParams(
        val response: LoadingResult<List<UserMediaListController.ListEntry>>,
        val mediaUpdates: Map<String, MediaListStatusController.Update>,
        val query: String,
        val filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
    )

    data class ListEntry(
        val name: String,
        val entries: List<MediaPreviewWithDescriptionEntry>,
    )
}
