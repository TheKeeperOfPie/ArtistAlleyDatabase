package com.thekeeperofpie.artistalleydatabase.anime.media.activity

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.anilist.fragment.ListActivityWithoutMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MediaActivitiesViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    private val activityStatusController: ActivityStatusController,
) : HeaderAndListViewModel<MediaActivitiesScreen.Entry, ListActivityWithoutMedia,
        MediaActivitiesViewModel.Entry, ActivitySortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = ActivitySortOption::class,
    sortOptionEnumDefault = ActivitySortOption.PINNED,
    loadingErrorTextRes = R.string.anime_media_activities_error_loading
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)
    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.data.media.id.toString() },
            entryToType = { it.data.media.type.toFavoriteType() },
            entryToFavorite = { it.data.media.isFavourite },
        )
    }

    override fun makeEntry(item: ListActivityWithoutMedia) = Entry(
        activity = item,
        liked = item.isLiked ?: false,
        subscribed = item.isSubscribed ?: false,
    )

    override fun entryId(entry: Entry) = entry.activityId

    override suspend fun initialRequest(
        headerId: String,
        sortOption: ActivitySortOption,
        sortAscending: Boolean,
    ) = MediaActivitiesScreen.Entry(
        aniListApi.mediaActivities(
            id = headerId,
            sort = sortOption.toApiValue(),
        )
    )

    override suspend fun pagedRequest(
        entry: MediaActivitiesScreen.Entry,
        page: Int,
        sortOption: ActivitySortOption,
        sortAscending: Boolean,
    ) = if (page == 1) {
        val result = entry.data.page
        result.pageInfo to result.activities
            .filterIsInstance<ListActivityWithoutMedia>()
    } else {
        val result = aniListApi.mediaActivitiesPage(
            id = entry.data.media.id.toString(),
            sort = sortOption.toApiValue(),
            page = page,
        ).page
        result.pageInfo to result.activities
            .filterIsInstance<ListActivityWithoutMedia>()
    }

    override fun Flow<PagingData<Entry>>.transformFlow() = flatMapLatest { pagingData ->
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

    data class Entry(
        val activity: ListActivityWithoutMedia,
        val activityId: String = activity.id.toString(),
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware
}
