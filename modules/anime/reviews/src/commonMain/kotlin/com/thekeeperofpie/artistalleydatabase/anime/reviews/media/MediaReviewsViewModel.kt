package com.thekeeperofpie.artistalleydatabase.anime.reviews.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_error_loading
import com.anilist.data.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewDestinations
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilteredViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class MediaReviewsViewModel(
    private val aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : SortFilteredViewModel<MediaReviewsScreen.Entry, MediaAndReviewsReview, MediaAndReviewsReview, MediaReviewsSortFilterController.FilterParams>(
    loadingErrorTextRes = Res.string.anime_reviews_error_loading,
) {
    private val destination =
        savedStateHandle.toDestination<ReviewDestinations.MediaReviews>(navigationTypeMap)
    val mediaId = destination.mediaId
    val viewer = aniListApi.authedUser
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val sortFilterController =
        MediaReviewsSortFilterController(viewModelScope, settings, featureOverrideProvider)

    override val filterParams = sortFilterController.filterParams

    init {
        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: MediaAndReviewsReview) = item

    override fun entryId(entry: MediaAndReviewsReview) = entry.id.toString()

    override suspend fun initialRequest(
        filterParams: MediaReviewsSortFilterController.FilterParams?,
    ) = MediaReviewsScreen.Entry(aniListApi.mediaAndReviews(mediaId = mediaId))

    override suspend fun request(
        filterParams: MediaReviewsSortFilterController.FilterParams?,
    ): Flow<PagingData<MediaAndReviewsReview>> = AniListPager { page ->
        aniListApi.mediaAndReviewsPage(
            mediaId = mediaId,
            sort = filterParams!!.sort.selectedOption(ReviewSortOption.RATING)
                .toApiValue(filterParams.sortAscending),
            page = page,
        ).media.reviews.run { pageInfo to nodes }
    }
}
