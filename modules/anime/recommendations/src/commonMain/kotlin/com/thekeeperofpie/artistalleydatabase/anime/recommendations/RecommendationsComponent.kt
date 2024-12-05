package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute

interface RecommendationsComponent {
    val animeMediaDetailsRecommendationsViewModelFactory: (SavedStateHandle) -> AnimeMediaDetailsRecommendationsViewModel.Factory
    // TODO: Can MediaDetailsRoute be removed from here?
    val recommendationsViewModelFactory: (MediaDetailsRoute) -> RecommendationsViewModel.Factory
}
