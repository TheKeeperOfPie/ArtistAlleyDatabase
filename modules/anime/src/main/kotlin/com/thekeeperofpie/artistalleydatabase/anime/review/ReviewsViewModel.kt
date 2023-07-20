package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    favoritesController: FavoritesController,
) : HeaderAndListViewModel<ReviewsScreen.Entry, MediaAndReviewsReview, MediaAndReviewsReview, ReviewsSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = ReviewsSortOption::class,
    sortOptionEnumDefault = ReviewsSortOption.RATING,
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
        sortOption: ReviewsSortOption,
        sortAscending: Boolean
    ) = ReviewsScreen.Entry(
        aniListApi.mediaAndReviews(
            mediaId = headerId,
            sort = sortOption.toApiValue(sortAscending)
        )
    )

    override suspend fun pagedRequest(
        entry: ReviewsScreen.Entry,
        page: Int,
        sortOption: ReviewsSortOption,
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
