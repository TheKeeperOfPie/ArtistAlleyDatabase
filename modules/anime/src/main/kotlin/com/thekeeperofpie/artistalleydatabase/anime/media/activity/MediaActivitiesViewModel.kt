package com.thekeeperofpie.artistalleydatabase.anime.media.activity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.ListActivityWithoutMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.compose.navigation.toDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MediaActivitiesViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    private val activityStatusController: ActivityStatusController,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)
    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)
    val activitySortFilterController = ActivitySortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
    )

    private val destination = savedStateHandle.toDestination<AnimeDestination.MediaActivities>(navigationTypeMap)
    val mediaId = destination.mediaId
    var entry by mutableStateOf(LoadingResult.empty<MediaActivitiesScreen.Entry>())
        private set

    val following = MutableStateFlow(PagingData.empty<ActivityEntry>())
    val global = MutableStateFlow(PagingData.empty<ActivityEntry>())

    private val initialIsFollowing = destination.showFollowing
    var selectedIsFollowing by mutableStateOf(initialIsFollowing)

    private val refresh = MutableStateFlow(-1L)

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result }.flowOn(CustomDispatchers.Main) },
            entryToId = { it.data.media.id.toString() },
            entryToType = { it.data.media.type.toFavoriteType() },
            entryToFavorite = { it.data.media.isFavourite },
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_media_activities_error_loading) {
                combine(activitySortFilterController.filterParams, refresh, ::Pair)
                    .mapLatest { (filterParams) ->
                        MediaActivitiesScreen.Entry(
                            aniListApi.mediaActivities(
                                id = mediaId,
                                sort = filterParams.sort.selectedOption(ActivitySortOption.NEWEST)
                                    .toApiValue(),
                                following = initialIsFollowing,
                            )
                        )
                    }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { entry.result }.flowOn(CustomDispatchers.Main).filterNotNull(),
                aniListApi.authedUser.filterNotNull(),
                ::Pair
            )
                .filterNotNull()
                .flatMapLatest { (entry) ->
                    combine(activitySortFilterController.filterParams, refresh, ::Pair)
                        .flatMapLatest { (filterParams) ->
                            AniListPager { page ->
                                if (page == 1 && initialIsFollowing) {
                                    val result = entry.data.page
                                    return@AniListPager result.pageInfo to
                                            result.activities
                                                .filterIsInstance<ListActivityWithoutMedia>()
                                }

                                val result = aniListApi.mediaActivitiesPage(
                                    id = entry.data.media.id.toString(),
                                    sort = filterParams.sort
                                        .selectedOption(ActivitySortOption.NEWEST)
                                        .toApiValue(),
                                    following = true,
                                    page = page,
                                ).page
                                result.pageInfo to result.activities
                                    .filterIsInstance<ListActivityWithoutMedia>()
                            }
                        }
                }
                .mapLatest {
                    it.mapOnIO {
                        ActivityEntry(
                            activity = it,
                            liked = it.isLiked ?: false,
                            subscribed = it.isSubscribed ?: false,
                        )
                    }
                }
                .enforceUniqueIds { it.activityId }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    activityStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.mapOnIO {
                                val liked = updates[it.activityId]?.liked ?: it.liked
                                val subscribed = updates[it.activityId]?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(following::emit)
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry.result }
                .flowOn(CustomDispatchers.Main)
                .filterNotNull()
                .flatMapLatest { entry ->
                    combine(activitySortFilterController.filterParams, refresh, ::Pair)
                        .flatMapLatest { (filterParams) ->
                            AniListPager { page ->
                                if (page == 1 && !initialIsFollowing) {
                                    val result = entry.data.page
                                    return@AniListPager result.pageInfo to
                                            result.activities
                                                .filterIsInstance<ListActivityWithoutMedia>()
                                }

                                val result = aniListApi.mediaActivitiesPage(
                                    id = entry.data.media.id.toString(),
                                    sort = filterParams.sort
                                        .selectedOption(ActivitySortOption.NEWEST)
                                        .toApiValue(),
                                    following = false,
                                    page = page,
                                ).page
                                result.pageInfo to result.activities
                                    .filterIsInstance<ListActivityWithoutMedia>()
                            }
                        }
                }
                .mapLatest {
                    it.mapOnIO {
                        ActivityEntry(
                            activity = it,
                            liked = it.isLiked ?: false,
                            subscribed = it.isSubscribed ?: false,
                        )
                    }
                }
                .enforceUniqueIds { it.activityId }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    activityStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.mapOnIO {
                                val liked = updates[it.activityId]?.liked ?: it.liked
                                val subscribed = updates[it.activityId]?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(global::emit)
        }
    }

    data class ActivityEntry(
        val activity: ListActivityWithoutMedia,
        val activityId: String = activity.id.toString(),
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware
}
