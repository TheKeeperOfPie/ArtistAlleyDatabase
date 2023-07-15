package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.fragment.PaginationInfo
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
abstract class HeaderAndListViewModel<EntryType, ListItemType : Any, ListEntryType : Any, SortType : SortOption>(
    protected val aniListApi: AuthedAniListApi,
    sortOptionEnum: KClass<SortType>,
    private val sortOptionEnumDefault: SortType,
    @StringRes private val loadingErrorTextRes: Int,
    defaultSortAscending: Boolean = false,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    var headerId by mutableStateOf("")
        private set

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    var entry by mutableStateOf<EntryType?>(null)
        private set

    val items = MutableStateFlow(PagingData.empty<ListEntryType>())

    var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    // TODO: Actually expose sort options?
    var sortOptions by mutableStateOf(
        SortEntry.options(sortOptionEnum, sortOptionEnumDefault)
    )
        private set

    var sortAscending by mutableStateOf(defaultSortAscending)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow {
                headerId.takeIf { it.isNotEmpty() }?.let {
                    Triple(
                        it,
                        sortOptions.selectedOption(sortOptionEnumDefault),
                        sortAscending,
                    )
                }
            }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .mapLatest { (headerId, sortOption, sortAscending) ->
                    Result.success(initialRequest(headerId, sortOption, sortAscending))
                }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isFailure) {
                            error = loadingErrorTextRes to it.exceptionOrNull()
                        } else {
                            entry = it.getOrThrow()
                        }
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flatMapLatest {
                    snapshotFlow {
                        Triple(
                            it,
                            sortOptions.selectedOption(sortOptionEnumDefault),
                            sortAscending,
                        )
                    }
                }
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (entry, sortOption, sortAscending) ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource { pagedRequest(entry, it, sortOption, sortAscending) }
                    }.flow
                }
                .map { it.map { makeEntry(it) } }
                .enforceUniqueIds { entryId(it) }
                .cachedIn(viewModelScope)
                .transformFlow()
                .collectLatest(items::emit)
        }
    }

    fun initialize(headerId: String) {
        this.headerId = headerId
    }

    protected abstract suspend fun initialRequest(
        headerId: String,
        sortOption: SortType,
        sortAscending: Boolean
    ): EntryType

    protected abstract suspend fun pagedRequest(
        entry: EntryType,
        page: Int,
        sortOption: SortType,
        sortAscending: Boolean
    ): Pair<PaginationInfo?, List<ListItemType>>

    protected abstract fun entryId(entry: ListEntryType): String

    protected abstract fun makeEntry(item: ListItemType): ListEntryType

    protected open fun Flow<PagingData<ListEntryType>>.transformFlow():
            Flow<PagingData<ListEntryType>> = this
}
