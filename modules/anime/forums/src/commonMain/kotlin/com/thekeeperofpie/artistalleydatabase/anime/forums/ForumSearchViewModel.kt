package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.ForumThreadSearchQuery
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class ForumSearchViewModel(
    aniListApi: AuthedAniListApi,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted forumSubsectionSortFilterViewModel: ForumSubsectionSortFilterViewModel,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<ForumDestinations.ForumSearch>(navigationTypeMap)

    var query by mutableStateOf("")

    val content =
        MutableStateFlow(PagingData.empty<ForumThreadSearchQuery.Data.Page.Thread>())

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { query }.flowOn(CustomDispatchers.Main),
                forumSubsectionSortFilterViewModel.state.filterParams,
                ::Pair
            )
                .flatMapLatest { (query, filterParams) ->
                    AniListPager {
                        val result = aniListApi.forumThreadSearch(
                            search = query,
                            subscribed = filterParams.subscribed,
                            categoryId = filterParams.categoryId,
                            mediaCategoryId = filterParams.mediaCategoryId,
                            sort = filterParams.sort.toApiValue(filterParams.sortAscending),
                            page = it,
                        )
                        result.page.pageInfo to result.page.threads?.filterNotNull().orEmpty()
                    }
                }
                .enforceUniqueIntIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(content::emit)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            savedStateHandle: SavedStateHandle,
            forumSubsectionSortFilterViewModel: ForumSubsectionSortFilterViewModel,
        ): ForumSearchViewModel
    }
}
