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
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ForumSearchViewModel(
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<ForumDestinations.ForumSearch>(navigationTypeMap)

    val sortFilterController = ForumSubsectionSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaDetailsRoute = mediaDetailsRoute,
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
