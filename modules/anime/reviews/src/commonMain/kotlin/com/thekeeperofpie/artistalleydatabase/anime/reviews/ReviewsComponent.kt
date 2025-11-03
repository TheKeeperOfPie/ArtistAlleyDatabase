package com.thekeeperofpie.artistalleydatabase.anime.reviews

import com.thekeeperofpie.artistalleydatabase.anime.reviews.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsViewModel

interface ReviewsComponent {
    val animeMediaDetailsReviewsViewModelFactory: AnimeMediaDetailsReviewsViewModel.Factory
    val reviewDetailsViewModelFactory: ReviewDetailsViewModel.Factory
    val reviewsSortFilterViewModelFactory: ReviewsSortFilterViewModel.Factory
    val reviewsViewModelFactoryFactory: ReviewsViewModel.TypedFactory.Factory
    val mediaReviewsSortFilterViewModelFactory: MediaReviewsSortFilterViewModel.Factory
    val mediaReviewsViewModelFactory: MediaReviewsViewModel.Factory
}
