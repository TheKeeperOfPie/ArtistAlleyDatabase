package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class MediaRecommendationSortFilterController(
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController<MediaRecommendationSortFilterController.FilterParams>(settings, featureOverrideProvider) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = RecommendationSortOption::class,
        defaultEnabled = RecommendationSortOption.RATING,
        headerTextRes = R.string.anime_media_recommendations_filter_sort_label,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_media_recommendations_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionMedia,
    )

    override var sections = listOf(
        sortSection,
        titleLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    override fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<RecommendationSortOption>>,
        val sortAscending: Boolean,
    )
}
