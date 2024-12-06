package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.reviews.details.ReviewDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.media.MediaReviewsViewModel
import kotlinx.coroutines.flow.Flow

interface ReviewsComponent {
    val animeMediaDetailsReviewsViewModel: (Flow<MediaDetailsQuery.Data.Media.Reviews?>) -> AnimeMediaDetailsReviewsViewModel
    val reviewDetailsViewModel: (SavedStateHandle) -> ReviewDetailsViewModel
    val reviewsViewModelFactory: () -> ReviewsViewModel.Factory
    val mediaReviewsViewModel: (SavedStateHandle) -> MediaReviewsViewModel
}
