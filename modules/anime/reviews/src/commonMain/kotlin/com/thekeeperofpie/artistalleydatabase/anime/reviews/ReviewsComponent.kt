package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.MediaDetailsQuery
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.reviews.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsViewModel
import kotlinx.coroutines.flow.Flow

interface ReviewsComponent {
    val animeMediaDetailsReviewsViewModel: (Flow<MediaDetailsQuery.Data.Media.Reviews?>) -> AnimeMediaDetailsReviewsViewModel
    val reviewDetailsViewModel: (SavedStateHandle) -> ReviewDetailsViewModel
    val reviewsSortFilterViewModel: (SavedStateHandle, MediaDetailsRoute, MediaType) -> ReviewsSortFilterViewModel
    val reviewsViewModelFactory: (anime: ReviewsSortFilterViewModel, manga: ReviewsSortFilterViewModel) -> ReviewsViewModel.Factory
    val mediaReviewsSortFilterViewModel: (SavedStateHandle) -> MediaReviewsSortFilterViewModel
    val mediaReviewsViewModel: (SavedStateHandle, MediaReviewsSortFilterViewModel) -> MediaReviewsViewModel
}
