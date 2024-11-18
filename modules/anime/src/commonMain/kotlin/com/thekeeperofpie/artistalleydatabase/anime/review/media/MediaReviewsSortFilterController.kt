package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_reviews_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

class MediaReviewsSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MediaDataSettingsSortFilterController<MediaReviewsSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = ReviewSortOption::class,
        defaultEnabled = ReviewSortOption.RATING,
        headerTextRes = Res.string.anime_media_reviews_filter_sort_label,
    )

    override var sections = listOf(
        sortSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
    )

    data class FilterParams(
        val sort: List<SortEntry<ReviewSortOption>>,
        val sortAscending: Boolean,
    )
}
