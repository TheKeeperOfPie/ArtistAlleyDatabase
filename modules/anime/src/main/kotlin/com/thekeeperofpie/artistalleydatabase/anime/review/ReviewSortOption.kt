package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.annotation.StringRes
import com.anilist.type.ReviewSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class ReviewSortOption(@StringRes override val textRes: Int) : SortOption {

    ID(R.string.anime_reviews_sort_id),
    SCORE(R.string.anime_reviews_sort_score),
    RATING(R.string.anime_reviews_sort_rating),
    CREATED_AT(R.string.anime_reviews_sort_created_at),
    UPDATED_AT(R.string.anime_reviews_sort_updated_at),
    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> if (ascending) ReviewSort.ID else ReviewSort.ID_DESC
        SCORE -> if (ascending) ReviewSort.SCORE else ReviewSort.SCORE_DESC
        RATING -> if (ascending) ReviewSort.RATING else ReviewSort.RATING_DESC
        CREATED_AT -> if (ascending) ReviewSort.CREATED_AT else ReviewSort.CREATED_AT_DESC
        UPDATED_AT -> if (ascending) ReviewSort.UPDATED_AT else ReviewSort.UPDATED_AT_DESC
    }.let(::listOf)
}
