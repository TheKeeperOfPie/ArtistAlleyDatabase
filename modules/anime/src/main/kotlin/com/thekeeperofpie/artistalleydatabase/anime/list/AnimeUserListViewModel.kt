package com.thekeeperofpie.artistalleydatabase.anime.list

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.AuthedUserQuery
import com.anilist.MediaTagsQuery
import com.anilist.UserMediaListQuery
import com.anilist.type.MediaFormat
import com.anilist.type.MediaStatus
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaFilterEntry
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class AnimeUserListViewModel @Inject constructor(aniListApi: AuthedAniListApi) : ViewModel() {

    var content by mutableStateOf<ContentState>(ContentState.LoadingEmpty)

    private val filterController =
        AnimeMediaFilterController(MediaListSortOption::class, aniListApi)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        filterController.initialize(this, refreshUptimeMillis)
        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
            combine(
                aniListApi.authedUser.filterNotNull(),
                refreshUptimeMillis,
                filterController.sort,
                filterController.sortAscending,
                ::RefreshParams
            )
                .debounce(100.milliseconds)
                .flatMapLatest { refreshParams ->
                    withContext(CustomDispatchers.IO) {
                        val baseResponse = flowOf(refreshParams)
                            .map {
                                aniListApi.userMediaList(
                                    userId = refreshParams.authedUser.id,
                                    sort = refreshParams.sortApiValue()
                                )
                            }
                        combine(
                            baseResponse,
                            filterController.genres,
                            filterController.tagsByCategory,
                            filterController.statuses,
                            filterController.formats,
                            ::FilterParams,
                        )
                            .map { filterParams ->
                                filterParams.mediaListCollection
                                    ?.lists
                                    ?.filterNotNull()
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
            key = { it.value.first },
            transform = { listOfNotNull(it.media.status) }
        )

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.formats,
            filteredEntries,
            state = { it.state },
            key = { it.value.first },
            transform = { listOfNotNull(it.media.format) }
        )

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.genres,
            filteredEntries,
            state = { it.state },
            key = { it.value },
            transform = { it.media.genres?.filterNotNull().orEmpty() }
        )

        filteredEntries = IncludeExcludeState.applyFiltering(
            filterParams.tagsByCategory.flatMap { it.value },
            filteredEntries,
            state = { it.state },
            key = { it.value.id },
            transform = { it.media.tags?.filterNotNull()?.map { it.id }.orEmpty() }
        )

        if (filteredEntries.isNotEmpty()) {
            this += AnimeUserListScreen.Entry.Header(
                list.name.orEmpty(),
                list.status
            )
            this += filteredEntries
        }
    }

    private data class RefreshParams(
        val authedUser: AuthedUserQuery.Data.Viewer,
        val requestMillis: Long = SystemClock.uptimeMillis(),
        val sort: MediaListSortOption,
        val sortAscending: Boolean,
    ) {
        fun sortApiValue() = if (sort == MediaListSortOption.DEFAULT) {
            emptyArray()
        } else {
            arrayOf(sort.toApiValue(sortAscending)!!)
        }
    }

    private data class FilterParams(
        val mediaListCollection: UserMediaListQuery.Data.MediaListCollection?,
        val genres: List<MediaFilterEntry<String>>,
        val tagsByCategory: Map<String?,
                List<MediaFilterEntry<MediaTagsQuery.Data.MediaTagCollection>>>,
        val statuses: List<MediaFilterEntry<Pair<MediaStatus, Int>>>,
        val formats: List<MediaFilterEntry<Pair<MediaFormat, Int>>>,
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