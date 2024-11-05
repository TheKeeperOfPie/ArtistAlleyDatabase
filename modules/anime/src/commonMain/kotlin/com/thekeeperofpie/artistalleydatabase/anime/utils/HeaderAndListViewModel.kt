package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
abstract class HeaderAndListViewModel<EntryType, ListItemType : Any, ListEntryType : Any, SortType : SortOption, FilterParams : Any>(
    protected val aniListApi: AuthedAniListApi,
    private val loadingErrorTextRes: StringResource,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    var entry by mutableStateOf<LoadingResult<EntryType>>(LoadingResult.loading())

    val items = MutableStateFlow(PagingData.empty<ListEntryType>())

    abstract val sortFilterController: AnimeSettingsSortFilterController<FilterParams>

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh.updates, loadingErrorTextRes) {
                sortFilterController.filterParams
                    .mapLatest(::initialRequest)
            }
                .catch { emit(LoadingResult.error(loadingErrorTextRes, it)) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            refresh.updates
                .flatMapLatest { sortFilterController.filterParams }
                .flatMapLatest { filterParams ->
                    AniListPager { pagedRequest(it, filterParams) }
                }
                .map { it.mapOnIO { makeEntry(it) } }
                .enforceUniqueIds { entryId(it) }
                .cachedIn(viewModelScope)
                .transformFlow()
                .cachedIn(viewModelScope)
                .collectLatest(items::emit)
        }
    }

    protected abstract suspend fun initialRequest(filterParams: FilterParams?): EntryType

    protected abstract suspend fun pagedRequest(
        page: Int,
        filterParams: FilterParams?,
    ): Pair<PaginationInfo?, List<ListItemType?>?>

    protected abstract fun entryId(entry: ListEntryType): String

    protected abstract fun makeEntry(item: ListItemType): ListEntryType

    protected open fun Flow<PagingData<ListEntryType>>.transformFlow():
            Flow<PagingData<ListEntryType>> = this

    fun refresh() = refresh.refresh()

    fun clearError() {
        entry = entry.copy(error = null)
    }
}
