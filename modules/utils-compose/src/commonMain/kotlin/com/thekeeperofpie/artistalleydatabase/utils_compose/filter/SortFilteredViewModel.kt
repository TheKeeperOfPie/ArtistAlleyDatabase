package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
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
abstract class SortFilteredViewModel<EntryType, ListItemType : Any, ListEntryType : Any, FilterParams : Any>(
    private val loadingErrorTextRes: StringResource,
) : ViewModel() {

    var entry by mutableStateOf<LoadingResult<EntryType>>(LoadingResult.loading())

    val items = MutableStateFlow(PagingData.empty<ListEntryType>())

    abstract val filterParams: Flow<FilterParams>

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh.updates, loadingErrorTextRes) {
                filterParams.mapLatest(::initialRequest)
            }
                .catch { emit(LoadingResult.error(loadingErrorTextRes, it)) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            refresh.updates
                .flatMapLatest { filterParams }
                .flatMapLatest(::request)
                .map { it.mapOnIO(::makeEntry) }
                .enforceUniqueIds(::entryId)
                .cachedIn(viewModelScope)
                .transformFlow()
                .cachedIn(viewModelScope)
                .collectLatest(items::emit)
        }
    }

    protected abstract suspend fun initialRequest(filterParams: FilterParams?): EntryType

    protected abstract suspend fun request(
        filterParams: FilterParams?,
    ): Flow<PagingData<ListItemType>>

    protected abstract fun entryId(entry: ListEntryType): String

    protected abstract fun makeEntry(item: ListItemType): ListEntryType

    protected open fun Flow<PagingData<ListEntryType>>.transformFlow():
            Flow<PagingData<ListEntryType>> = this

    fun refresh() = refresh.refresh()
}
