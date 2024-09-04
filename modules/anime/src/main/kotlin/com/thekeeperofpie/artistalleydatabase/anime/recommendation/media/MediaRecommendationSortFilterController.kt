package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class MediaRecommendationSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : AnimeSettingsSortFilterController<MediaRecommendationSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = RecommendationSortOption::class,
        defaultEnabled = RecommendationSortOption.RATING,
        headerTextRes = R.string.anime_media_recommendations_filter_sort_label,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_media_recommendations_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { ComposeResourceUtils.stringResource(it.textRes) },
        property = settings.languageOptionMedia,
    )

    override var sections = listOf(
        sortSection,
        titleLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
    )

    data class FilterParams(
        val sort: List<SortEntry<RecommendationSortOption>>,
        val sortAscending: Boolean,
    )
}
