package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_sort_id
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_sort_rating
import com.anilist.data.type.RecommendationSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class RecommendationSortOption(override val textRes: StringResource) : SortOption {

    ID(Res.string.anime_recommendations_sort_id),
    RATING(Res.string.anime_recommendations_sort_rating),
    ;

    fun toApiValue(ascending: Boolean) = listOf(
        when (this) {
            ID -> if (ascending) RecommendationSort.ID else RecommendationSort.ID_DESC
            RATING -> if (ascending) RecommendationSort.RATING else RecommendationSort.RATING_DESC
        }
    )
}
