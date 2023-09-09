package com.thekeeperofpie.artistalleydatabase.anime.review.media

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class MediaReviewsSortFilterController(
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController<MediaReviewsSortFilterController.FilterParams>(
    settings,
    featureOverrideProvider
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = ReviewSortOption::class,
        defaultEnabled = ReviewSortOption.RATING,
        headerTextRes = R.string.anime_media_reviews_filter_sort_label,
    )

    override var sections = listOf(
        sortSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    override fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<ReviewSortOption>>,
        val sortAscending: Boolean,
    )
}
