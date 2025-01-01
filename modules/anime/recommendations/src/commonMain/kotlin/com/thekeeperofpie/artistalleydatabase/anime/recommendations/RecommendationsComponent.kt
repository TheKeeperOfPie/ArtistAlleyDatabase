package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute

interface RecommendationsComponent {
    val animeMediaDetailsRecommendationsViewModelFactory: (mediaId: String) -> AnimeMediaDetailsRecommendationsViewModel.Factory
    val recommendationsSortFilterViewModel: (SavedStateHandle, MediaDetailsRoute) -> RecommendationsSortFilterViewModel
    val recommendationsViewModelFactory: (RecommendationsSortFilterViewModel) -> RecommendationsViewModel.Factory
}
