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
import com.anilist.UserMediaListQuery.Data.MediaListCollection.List.Entry.Media
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
    settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
    private val mediaTagsController: MediaTagsController,
    private val userMediaListController: UserMediaListController,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content by mutableStateOf<AnimeUserListScreen.ContentState>(
        AnimeUserListScreen.ContentState.LoadingEmpty
    )
    var tagShown by mutableStateOf<TagSection.Tag?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    var userName by mutableStateOf<String?>(null)
        private set

    private var userId: String? = null
    private var initialized = false
    private lateinit var mediaType: MediaType

    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaListSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        mediaTagsController = mediaTagsController,
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?, userName: String?, mediaType: MediaType) {
        if (initialized) return
        initialized = true
        this.userId = userId
        this.mediaType = mediaType
        this.userName = userName
        sortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = AnimeSortFilterController.InitialParams(
                // Disable "On list" filter, everything in this screen is on the user's list
                onListEnabled = false,
                defaultSort = MediaListSortOption.UPDATED_TIME
            )
        )

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
                }.mapLatest { it!!.getOrThrow() }
            } else {
                refreshUptimeMillis.mapLatest {
                    aniListApi.userMediaList(userId = userId.toInt(), type = mediaType)
                        .let {
                            it.lists?.filterNotNull()?.map(UserMediaListController::ListEntry)
                                .orEmpty()
                        }
                }
            }

            combine(
                response,
                snapshotFlow { query }.debounce(500.milliseconds),
                sortFilterController.filterParams(),
                ::FilterParams
            ).map { (lists, query, filterParams) ->
                val sortOption = filterParams.sort
                    .find { it.state == FilterIncludeExcludeState.INCLUDE }?.value
                lists.filter {
                    val listStatuses = filterParams.listStatuses
                        .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                        .map { it.value }
                    if (listStatuses.isEmpty()) {
                        true
                    } else {
                        listStatuses.contains(it.status)
                    }
                }
                    .let {
                        if (sortOption == null) {
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
                    .map { toFilteredEntries(query, filterParams, it) }
                    .flatten()
                    .let(AnimeUserListScreen.ContentState::Success)
            }
                .startWith(AnimeUserListScreen.ContentState.LoadingEmpty)
                .catch { emit(AnimeUserListScreen.ContentState.Error(exception = it)) }
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

    fun onTagLongClick(tagId: String) {
        tagShown = mediaTagsController.tags.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<Media>) =
        ignoreList.toggle(entry.media.id.toString())

    private fun toFilteredEntries(
        query: String,
        filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
        list: UserMediaListController.ListEntry,
    ): List<AnimeUserListScreen.Entry> {
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

        return if (filteredEntries.isEmpty()) {
            filteredEntries.map(AnimeUserListScreen.Entry::Item)
        } else {
            mutableListOf(
                AnimeUserListScreen.Entry.Header(
                    list.name,
                    list.status,
                )
            ) + filteredEntries.map(AnimeUserListScreen.Entry::Item)
        }
    }

    private data class FilterParams(
        val response: List<UserMediaListController.ListEntry>,
        val query: String,
        val filterParams: MediaSortFilterController.FilterParams<MediaListSortOption>,
    )
}
