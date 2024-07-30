package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.ForumThreadSearchQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumSearchViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = savedStateHandle.toDestination<AnimeDestination.ForumSearch>(navigationTypeMap)

    val sortFilterController = ForumSubsectionSortFilterController(
        screenKey = AnimeNavDestinations.FORUM_SEARCH.id,
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
    )

    var query by mutableStateOf("")

    val content =
        MutableStateFlow(PagingData.empty<ForumThreadSearchQuery.Data.Page.Thread>())

    init {
        sortFilterController.initialize(
            ForumSubsectionSortFilterController.InitialParams(
                defaultSort = destination.sort,
                categoryId = destination.categoryId,
                mediaCategoryId = destination.mediaCategoryId,
            )
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { query }.flowOn(CustomDispatchers.Main),
                sortFilterController.filterParams,
                ::Pair
            )
                .flatMapLatest { (query, filterParams) ->
                    AniListPager {
                        val result = aniListApi.forumThreadSearch(
                            search = query,
                            subscribed = filterParams.subscribed,
                            categoryId = filterParams.categories
                                .firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
                                ?.value
                                ?.categoryId
                                ?.toString(),
                            mediaCategoryId = filterParams.mediaCategoryId,
                            sort = filterParams.sortOptions.firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }
                                ?.value
                                ?.toApiValue(filterParams.sortAscending),
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
}
