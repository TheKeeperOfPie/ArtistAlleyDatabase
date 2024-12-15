package com.thekeeperofpie.artistalleydatabase.anime.users.follow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.PaginationInfo
import com.anilist.data.fragment.UserWithFavorites
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.users.UserDestinations
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.users.UserSortOption
import com.thekeeperofpie.artistalleydatabase.anime.users.UserUtils
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
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
abstract class UserListViewModel<MediaEntry>(
    protected val aniListApi: AuthedAniListApi,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val apiCall: suspend (
        userId: String,
        filterParams: UserFollowSortFilterController.FilterParams,
        page: Int,
    ) -> Pair<PaginationInfo?, List<UserWithFavorites>>,
    mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    val sortFilterController =
        UserFollowSortFilterController(viewModelScope, settings, featureOverrideProvider)
    val users = MutableStateFlow(PagingData.empty<UserListRow.Entry<MediaEntry>>())

    abstract val userId: String?

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
                            media = UserUtils.buildInitialMediaEntries(
                                user = it,
                                mediaEntryProvider = mediaEntryProvider,
                            ),
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
                                    filterableData = mediaEntryProvider.mediaFilterable(it),
                                    copy = { mediaEntryProvider.copyMediaEntry(this, it) },
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
    class Following<MediaEntry>(
        aniListApi: AuthedAniListApi,
        mediaListStatusController: MediaListStatusController,
        ignoreController: IgnoreController,
        settings: MediaDataSettings,
        featureOverrideProvider: FeatureOverrideProvider,
        navigationTypeMap: NavigationTypeMap,
        @Assisted savedStateHandle: SavedStateHandle,
        @Assisted mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
    ) : UserListViewModel<MediaEntry>(
        aniListApi = aniListApi,
        mediaListStatusController = mediaListStatusController,
        ignoreController = ignoreController,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        apiCall = { userId, filterParams, page ->
            val result = aniListApi.userSocialFollowingWithFavorites(
                userId = userId,
                sort = filterParams.sort.selectedOption(UserSortOption.ID)
                    .toApiValue(filterParams.sortAscending),
                page = page,
            )
            result.page?.pageInfo to result.page?.following?.filterNotNull()
                .orEmpty()
        },
        mediaEntryProvider = mediaEntryProvider,
    ) {
        override val userId =
            savedStateHandle.toDestination<UserDestinations.UserFollowing>(navigationTypeMap).userId

        @Inject
        class Factory(
            private val aniListApi: AuthedAniListApi,
            private val mediaListStatusController: MediaListStatusController,
            private val ignoreController: IgnoreController,
            private val settings: MediaDataSettings,
            private val featureOverrideProvider: FeatureOverrideProvider,
            private val navigationTypeMap: NavigationTypeMap,
            @Assisted private val savedStateHandle: SavedStateHandle,
        ) {
            fun <MediaEntry> create(
                mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
            ) = Following(
                aniListApi = aniListApi,
                mediaListStatusController = mediaListStatusController,
                ignoreController = ignoreController,
                settings = settings,
                featureOverrideProvider = featureOverrideProvider,
                navigationTypeMap = navigationTypeMap,
                savedStateHandle = savedStateHandle,
                mediaEntryProvider = mediaEntryProvider,
            )
        }
    }

    @Inject
    class Followers<MediaEntry>(
        aniListApi: AuthedAniListApi,
        mediaListStatusController: MediaListStatusController,
        ignoreController: IgnoreController,
        settings: MediaDataSettings,
        featureOverrideProvider: FeatureOverrideProvider,
        navigationTypeMap: NavigationTypeMap,
        @Assisted savedStateHandle: SavedStateHandle,
        @Assisted mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
    ) : UserListViewModel<MediaEntry>(
        aniListApi = aniListApi,
        mediaListStatusController = mediaListStatusController,
        ignoreController = ignoreController,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        apiCall = { userId, filterParams, page ->
            val result = aniListApi.userSocialFollowersWithFavorites(
                userId = userId,
                sort = filterParams.sort.selectedOption(UserSortOption.ID)
                    .toApiValue(filterParams.sortAscending),
                page = page,
            )
            result.page?.pageInfo to result.page?.followers?.filterNotNull()
                .orEmpty()
        },
        mediaEntryProvider = mediaEntryProvider,
    ) {
        override val userId =
            savedStateHandle.toDestination<UserDestinations.UserFollowers>(navigationTypeMap).userId

        @Inject
        class Factory(
            private val aniListApi: AuthedAniListApi,
            private val mediaListStatusController: MediaListStatusController,
            private val ignoreController: IgnoreController,
            private val settings: MediaDataSettings,
            private val featureOverrideProvider: FeatureOverrideProvider,
            private val navigationTypeMap: NavigationTypeMap,
            @Assisted private val savedStateHandle: SavedStateHandle,
        ) {
            fun <MediaEntry> create(
                mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
            ) = Followers(
                aniListApi = aniListApi,
                mediaListStatusController = mediaListStatusController,
                ignoreController = ignoreController,
                settings = settings,
                featureOverrideProvider = featureOverrideProvider,
                navigationTypeMap = navigationTypeMap,
                savedStateHandle = savedStateHandle,
                mediaEntryProvider = mediaEntryProvider,
            )
        }
    }
}
