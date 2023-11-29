package com.thekeeperofpie.artistalleydatabase.anime.utils

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
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

@OptIn(ExperimentalCoroutinesApi::class)
abstract class HeaderAndListViewModel<EntryType, ListItemType : Any, ListEntryType : Any, SortType : SortOption, FilterParams>(
    protected val aniListApi: AuthedAniListApi,
    @StringRes private val loadingErrorTextRes: Int,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    var entry by mutableStateOf<LoadingResult<EntryType>>(LoadingResult.loading())

    val items = MutableStateFlow(PagingData.empty<ListEntryType>())

    abstract val sortFilterController: SortFilterController<FilterParams>

    private val refresh = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, loadingErrorTextRes) {
                sortFilterController.filterParams
                    .mapLatest(::initialRequest)
            }
                .catch { emit(LoadingResult.error(loadingErrorTextRes, it)) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            refresh.flatMapLatest { sortFilterController.filterParams }
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

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

    fun clearError() {
        entry = entry.copy(error = null)
    }
}
