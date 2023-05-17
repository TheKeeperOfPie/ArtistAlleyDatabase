package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.AuthedUserQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
open class AnimeUserListViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList
) : ViewModel(), AnimeUserListScreen.ViewModel<MediaListSortOption> {

    override var query by mutableStateOf("")
    override var content by mutableStateOf<AnimeUserListScreen.ContentState>(
        AnimeUserListScreen.ContentState.LoadingEmpty
    )
    override var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)

    private var initialized = false
    private var userId: String? = null
    private lateinit var mediaType: MediaType

    private val filterController =
        AnimeMediaFilterController(MediaListSortOption::class, aniListApi, settings, ignoreList)

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
                        val baseResponse = aniListApi.userMediaList(
                            userId = userId?.toIntOrNull() ?: refreshParams.authedUser!!.id,
                            type = mediaType,
                            sort = refreshParams.sortApiValue()
                        )
                        combine(
                            snapshotFlow { query }.debounce(500.milliseconds),
                            filterController.filterParams(),
                            ::Pair
                        ).map { (query, filterParams) ->
                            baseResponse?.lists
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
                                ?.map { toFilteredEntries(query, filterParams, it) }
                                ?.flatten()
                                ?.let(AnimeUserListScreen.ContentState::Success)
                                ?: AnimeUserListScreen.ContentState.Error()
                        }
                            .startWith(AnimeUserListScreen.ContentState.LoadingEmpty)
                            .catch { emit(AnimeUserListScreen.ContentState.Error(exception = it)) }
                    }
                }
                .collectLatest { content = it }
        }
    }

    override fun filterData() = filterController.data()

    override fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    override fun onTagLongClick(tagId: String) {
        tagShown = filterController.tagsByCategory.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    private fun toFilteredEntries(
        query: String,
        filterParams: AnimeMediaFilterController.FilterParams,
        list: UserMediaListQuery.Data.MediaListCollection.List
    ): List<AnimeUserListScreen.Entry> {
        val entries = list.entries?.filterNotNull()
            ?.mapNotNull { it.media }
            ?.map { AnimeUserListScreen.Entry.Item(it) }
            .orEmpty()

        var filteredEntries = filterController.filterEntries(filterParams, entries)

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

        return if (filteredEntries.isEmpty()) {
            filteredEntries
        } else {
            mutableListOf(AnimeUserListScreen.Entry.Header(
                list.name.orEmpty(),
                list.status
            )) + filteredEntries
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
}
