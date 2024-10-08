package com.thekeeperofpie.artistalleydatabase.anime.user.follow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.PaginationInfo
import com.anilist.fragment.UserWithFavorites
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortOption
import com.thekeeperofpie.artistalleydatabase.anime.user.UserUtils
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class UserListViewModel(
    protected val aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    private val apiCall: suspend (
        userId: String,
        filterParams: UserFollowSortFilterController.FilterParams,
        page: Int,
    ) -> Pair<PaginationInfo?, List<UserWithFavorites>>,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val sortFilterController =
        UserFollowSortFilterController(viewModelScope, settings, featureOverrideProvider)
    val users = MutableStateFlow(PagingData.empty<UserListRow.Entry>())

    private val userId = savedStateHandle.get<String?>("userId")

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                aniListApi.authedUser,
                sortFilterController.filterParams,
                ::Pair
            ).flatMapLatest { (viewer, filterParams) ->
                val userId = userId ?: viewer?.id
                if (userId == null) {
                    flowOf(PagingData.empty())
                } else {
                    AniListPager { apiCall(userId, filterParams, it) }
                }
            }
                .mapLatest {
                    it.mapOnIO {
                        UserListRow.Entry(
                            user = it,
                            media = UserUtils.buildInitialMediaEntries(it),
                        )
                    }
                }
                .enforceUniqueIntIds { it.user.id }
                .cachedIn(viewModelScope)
                .flatMapLatest {
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(media = it.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            })
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(users::emit)
        }
    }

    @Inject
    class Following(
        aniListApi: AuthedAniListApi,
        mediaListStatusController: MediaListStatusController,
        ignoreController: IgnoreController,
        settings: AnimeSettings,
        featureOverrideProvider: FeatureOverrideProvider,
        @Assisted savedStateHandle: SavedStateHandle,
    ) : UserListViewModel(
        aniListApi = aniListApi,
        mediaListStatusController = mediaListStatusController,
        ignoreController = ignoreController,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        savedStateHandle = savedStateHandle,
        apiCall = { userId, filterParams, page ->
            val result = aniListApi.userSocialFollowingWithFavorites(
                userId = userId,
                sort = filterParams.sort.selectedOption(UserSortOption.ID)
                    .toApiValue(filterParams.sortAscending),
                page = page,
            )
            result.page?.pageInfo to result.page?.following?.filterNotNull()
                .orEmpty()
        }
    )

    @Inject
    class Followers(
        aniListApi: AuthedAniListApi,
        mediaListStatusController: MediaListStatusController,
        ignoreController: IgnoreController,
        settings: AnimeSettings,
        featureOverrideProvider: FeatureOverrideProvider,
        @Assisted savedStateHandle: SavedStateHandle,
    ) : UserListViewModel(
        aniListApi = aniListApi,
        mediaListStatusController = mediaListStatusController,
        ignoreController = ignoreController,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        savedStateHandle = savedStateHandle,
        apiCall = { userId, filterParams, page ->
            val result = aniListApi.userSocialFollowersWithFavorites(
                userId = userId,
                sort = filterParams.sort.selectedOption(UserSortOption.ID)
                    .toApiValue(filterParams.sortAscending),
                page = page,
            )
            result.page?.pageInfo to result.page?.followers?.filterNotNull()
                .orEmpty()
        }
    )
}
