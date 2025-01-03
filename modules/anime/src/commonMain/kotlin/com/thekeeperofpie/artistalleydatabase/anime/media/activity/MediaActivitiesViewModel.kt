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
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_error_loading
import com.anilist.data.fragment.ListActivityWithoutMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class MediaActivitiesViewModel(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    private val activityStatusController: ActivityStatusController,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted activitySortFilterViewModel: ActivitySortFilterViewModel,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)
    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.MediaActivities>(navigationTypeMap)
    val mediaId = destination.mediaId
    var entry by mutableStateOf(LoadingResult.empty<MediaActivitiesScreen.Entry>())
        private set

    val following = MutableStateFlow(PagingData.empty<ActivityEntry>())
    val global = MutableStateFlow(PagingData.empty<ActivityEntry>())

    private val initialIsFollowing = destination.showFollowing
    var selectedIsFollowing by mutableStateOf(initialIsFollowing)

    // TODO: Refresh not exposed to user
    private val refresh = RefreshFlow()

    // TODO: Should this be accessed from inside a composable?
    private val timeZone = TimeZone.currentSystemDefault()

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result }.flowOn(CustomDispatchers.Main) },
            entryToId = { it.data.media.id.toString() },
            entryToType = { it.data.media.type.toFavoriteType() },
            entryToFavorite = { it.data.media.isFavourite },
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh.updates,
                Res.string.anime_media_activities_error_loading,
            ) {
                activitySortFilterViewModel.state.filterParams
                    .mapLatest {
                        MediaActivitiesScreen.Entry(
                            aniListApi.mediaActivities(
                                id = mediaId,
                                sort = it.sort.toApiValue(),
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
                    combine(activitySortFilterViewModel.state.filterParams, refresh.updates, ::Pair)
                        .flatMapLatest { (filterParams) ->
                            AniListPager { page ->
                                if (page == 1 && initialIsFollowing && filterParams.isDefault) {
                                    val result = entry.data.page
                                    return@AniListPager result.pageInfo to
                                            result.activities
                                                .filterIsInstance<ListActivityWithoutMedia>()
                                }

                                val result = aniListApi.mediaActivitiesPage(
                                    id = entry.data.media.id.toString(),
                                    sort = filterParams.sort.toApiValue(),
                                    following = true,
                                    hasReplies = if (filterParams.hasReplies) true else null,
                                    createdAtGreater = filterParams.date.startDate
                                        ?.atStartOfDayIn(timeZone)
                                        ?.epochSeconds
                                        ?.toInt(),
                                    createdAtLesser = filterParams.date.endDate
                                        ?.plus(1, DateTimeUnit.DAY)
                                        ?.atStartOfDayIn(timeZone)
                                        ?.epochSeconds
                                        ?.toInt(),
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
                    combine(activitySortFilterViewModel.state.filterParams, refresh.updates, ::Pair)
                        .flatMapLatest { (filterParams) ->
                            AniListPager { page ->
                                if (page == 1 && !initialIsFollowing && filterParams.isDefault) {
                                    val result = entry.data.page
                                    return@AniListPager result.pageInfo to
                                            result.activities
                                                .filterIsInstance<ListActivityWithoutMedia>()
                                }

                                val result = aniListApi.mediaActivitiesPage(
                                    id = entry.data.media.id.toString(),
                                    sort = filterParams.sort.toApiValue(),
                                    following = false,
                                    hasReplies = if (filterParams.hasReplies) true else null,
                                    createdAtGreater = filterParams.date.startDate
                                        ?.atStartOfDayIn(timeZone)
                                        ?.epochSeconds
                                        ?.toInt(),
                                    createdAtLesser = filterParams.date.endDate
                                        ?.plus(1, DateTimeUnit.DAY)
                                        ?.atStartOfDayIn(timeZone)
                                        ?.epochSeconds
                                        ?.toInt(),
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
