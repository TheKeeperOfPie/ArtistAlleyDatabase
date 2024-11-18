package com.thekeeperofpie.artistalleydatabase.anime.recommendation.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendations_filter_setting_title_language
import artistalleydatabase.modules.anime.generated.resources.anime_media_recommendations_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class MediaRecommendationSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MediaDataSettingsSortFilterController<MediaRecommendationSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = RecommendationSortOption::class,
        defaultEnabled = RecommendationSortOption.RATING,
        headerTextRes = Res.string.anime_media_recommendations_filter_sort_label,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_media_recommendations_filter_setting_title_language,
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
