package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute

interface RecommendationsComponent {
    val animeMediaDetailsRecommendationsViewModelFactory: (mediaId: String) -> AnimeMediaDetailsRecommendationsViewModel.Factory
    // TODO: Can MediaDetailsRoute be removed from here?
    val recommendationsViewModelFactory: (MediaDetailsRoute) -> RecommendationsViewModel.Factory
}
