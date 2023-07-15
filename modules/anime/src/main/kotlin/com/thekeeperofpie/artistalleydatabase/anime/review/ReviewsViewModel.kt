package com.thekeeperofpie.artistalleydatabase.anime.review

import com.anilist.fragment.MediaAndReviewsReview
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    aniListApi: AuthedAniListApi
) : HeaderAndListViewModel<ReviewsScreen.Entry, MediaAndReviewsReview, MediaAndReviewsReview, ReviewsSortOption>(
    aniListApi = aniListApi,
    sortOptionEnum = ReviewsSortOption::class,
    sortOptionEnumDefault = ReviewsSortOption.RATING,
    loadingErrorTextRes = R.string.anime_reviews_error_loading,
) {
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
