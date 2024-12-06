package com.thekeeperofpie.artistalleydatabase.anime.reviews

import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_sort_created_at
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_sort_id
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_sort_rating
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_sort_score
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_reviews_sort_updated_at
import com.anilist.data.type.ReviewSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class ReviewSortOption(override val textRes: StringResource) : SortOption {

    ID(Res.string.anime_reviews_sort_id),
    SCORE(Res.string.anime_reviews_sort_score),
    RATING(Res.string.anime_reviews_sort_rating),
    CREATED_AT(Res.string.anime_reviews_sort_created_at),
    UPDATED_AT(Res.string.anime_reviews_sort_updated_at),
    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> if (ascending) ReviewSort.ID else ReviewSort.ID_DESC
        SCORE -> if (ascending) ReviewSort.SCORE else ReviewSort.SCORE_DESC
        RATING -> if (ascending) ReviewSort.RATING else ReviewSort.RATING_DESC
        CREATED_AT -> if (ascending) ReviewSort.CREATED_AT else ReviewSort.CREATED_AT_DESC
        UPDATED_AT -> if (ascending) ReviewSort.UPDATED_AT else ReviewSort.UPDATED_AT_DESC
    }.let(::listOf)
}
