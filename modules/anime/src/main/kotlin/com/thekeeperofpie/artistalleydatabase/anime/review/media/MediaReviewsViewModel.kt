package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaReviewsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
) : HeaderAndListViewModel<MediaReviewsScreen.Entry, MediaAndReviewsReview, MediaAndReviewsReview, ReviewSortOption, MediaReviewsSortFilterController.FilterParams>(
    aniListApi = aniListApi,
    loadingErrorTextRes = R.string.anime_reviews_error_loading,
) {
    val mediaId = savedStateHandle.get<String>("mediaId")!!
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override val sortFilterController =
        MediaReviewsSortFilterController(settings, featureOverrideProvider)

    init {
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
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

    override suspend fun pagedRequest(
        page: Int,
        filterParams: MediaReviewsSortFilterController.FilterParams?,
    ) = aniListApi.mediaAndReviewsPage(
        mediaId = mediaId,
        sort = filterParams!!.sort.selectedOption(ReviewSortOption.RATING)
            .toApiValue(filterParams.sortAscending),
        page = page,
    ).media.reviews.run { pageInfo to nodes }
}
