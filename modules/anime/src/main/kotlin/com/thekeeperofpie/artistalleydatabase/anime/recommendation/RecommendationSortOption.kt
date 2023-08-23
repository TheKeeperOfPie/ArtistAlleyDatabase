package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.annotation.StringRes
import com.anilist.type.RecommendationSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class RecommendationSortOption(@StringRes override val textRes: Int) : SortOption {

    ID(R.string.anime_recommendations_sort_id),
    RATING(R.string.anime_recommendations_sort_rating),
    ;

    fun toApiValue(ascending: Boolean) = listOf(
        when (this) {
            ID -> if (ascending) RecommendationSort.ID else RecommendationSort.ID_DESC
            RATING -> if (ascending) RecommendationSort.RATING else RecommendationSort.RATING_DESC
        }
    )
}
