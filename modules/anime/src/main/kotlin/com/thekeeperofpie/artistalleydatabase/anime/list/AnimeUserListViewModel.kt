package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.UserMediaListQuery
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
    val ignoreList: AnimeMediaIgnoreList,
    private val mediaTagsController: MediaTagsController,
    private val mediaLicensorsController: MediaLicensorsController,
    private val userMediaListController: UserMediaListController,
    private val featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content by mutableStateOf<LoadingResult<List<ListEntry>>>(LoadingResult.loading())
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    var userName by mutableStateOf<String?>(null)
        private set

    private var userId: String? = null
    private var initialized = false
    private lateinit var mediaType: MediaType

    lateinit var sortFilterController: MediaSortFilterController<*, *>

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?, userName: String?, mediaType: MediaType) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.mediaType = mediaType
        this.userName = userName
        sortFilterController = if (mediaType == MediaType.ANIME) {
            AnimeSortFilterController(
                sortTypeEnumClass = MediaListSortOption::class,
                aniListApi = aniListApi,
                settings = settings,
                featureOverrideProvider = featureOverrideProvider,
                mediaTagsController = mediaTagsController,
                mediaLicensorsController = mediaLicensorsController,
            ).apply {
                initialize(
                    viewModel = this@AnimeUserListViewModel,
                    refreshUptimeMillis = refreshUptimeMillis,
                    initialParams = AnimeSortFilterController.InitialParams(
                        onListEnabled = false,
                        defaultSort = MediaListSortOption.UPDATED_TIME,
                        lockSort = false,
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
                mediaLicensorsController = mediaLicensorsController,
            ).apply {
                initialize(
                    viewModel = this@AnimeUserListViewModel,
                    refreshUptimeMillis = refreshUptimeMillis,
                    initialParams = MangaSortFilterController.InitialParams(
                        onListEnabled = false,
                        defaultSort = MediaListSortOption.UPDATED_TIME,
                        lockSort = false,
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

        viewModelScope.launch(CustomDispatchers.Main) {
            val response = if (userId == null) {
                when (mediaType) {
                    MediaType.MANGA -> userMediaListController.manga
                    MediaType.ANIME,
                    MediaType.UNKNOWN__,
                    -> userMediaListController.anime
                }
            } else {
                refreshUptimeMillis.flatMapLatest {
                    aniListApi.userMediaList(userId = userId.toInt(), type = mediaType)
                        .mapLatest {
                            it.transformResult {
                                it.lists?.filterNotNull()
                                    ?.map(UserMediaListController::ListEntry)
                                    .orEmpty()
                            }
                        }
                }
            }

            @Suppress("UNCHECKED_CAST")
            combine(
                response,
                snapshotFlow { query }.debounce(500.milliseconds),
                (sortFilterController as? AnimeSortFilterController<MediaListSortOption>)?.filterParams()
                    ?: (sortFilterController as MangaSortFilterController<MediaListSortOption>).filterParams(),
                ::FilterParams
            ).map { (lists, query, filterParams) ->
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
                        .map { toFilteredEntries(query, filterParams, it) }
                        .distinctBy { it.name }
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

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
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
            entries = filteredEntries.map { MediaEntry(it.media) }
                .distinctBy { it.media.id },
        )
    }

    private data class FilterParams(
        val response: LoadingResult<List<UserMediaListController.ListEntry>>,
        val query: String,
        val filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
    )

    data class ListEntry(
        val name: String,
        val entries: List<MediaEntry>,
    )

    class MediaEntry(
        media: UserMediaListQuery.Data.MediaListCollection.List.Entry.Media,
        mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        progress: Int? = null,
        progressVolumes: Int? = null,
        ignored: Boolean = false,
        showLessImportantTags: Boolean = false,
        showSpoilerTags: Boolean = false,
    ) : AnimeMediaListRow.Entry<MediaPreviewWithDescription>(
        media = media,
        mediaListStatus = mediaListStatus,
        progress = progress,
        progressVolumes = progressVolumes,
        ignored = ignored,
        showLessImportantTags = showLessImportantTags,
        showSpoilerTags = showSpoilerTags,
    ), MediaStatusAware, AnimeMediaLargeCard.Entry, MediaGridCard.Entry {
        override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)

        // So that enough meaningful text is shown, strip any double newlines
        override val description = media.description?.replace("<br><br />\n<br><br />\n", "\n")

        val compactEntry = AnimeMediaCompactListRow.Entry(
            media = media,
            ignored = ignored,
            showLessImportantTags = showLessImportantTags,
            showSpoilerTags = showSpoilerTags,
        )
    }
}
