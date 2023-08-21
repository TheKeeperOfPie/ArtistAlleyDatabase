package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaReviewsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<MediaReviewsScreen.Entry, MediaAndReviewsReview, MediaAndReviewsReview, ReviewSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = ReviewSortOption::class,
    sortOptionEnumDefault = ReviewSortOption.RATING,
    loadingErrorTextRes = R.string.anime_reviews_error_loading,
) {
    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    override fun initialize(headerId: String) {
        super.initialize(headerId)
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry } },
            entryToId = { it.media.id.toString() },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )
    }

    override fun makeEntry(item: MediaAndReviewsReview) = item

    override fun entryId(entry: MediaAndReviewsReview) = entry.id.toString()

    override suspend fun initialRequest(
        headerId: String,
        sortOption: ReviewSortOption,
        sortAscending: Boolean
    ) = MediaReviewsScreen.Entry(
        aniListApi.mediaAndReviews(
            mediaId = headerId,
            sort = sortOption.toApiValue(sortAscending)
        )
    )

    override suspend fun pagedRequest(
        entry: MediaReviewsScreen.Entry,
        page: Int,
        sortOption: ReviewSortOption,
        sortAscending: Boolean
    ) = if (page == 1) {
        val result = entry.media.reviews
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    } else {
        val result = aniListApi.mediaAndReviewsPage(
            mediaId = entry.media.id.toString(),
            sort = sortOption.toApiValue(sortAscending),
            page = page,
        ).media.reviews
        result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
    }
}
