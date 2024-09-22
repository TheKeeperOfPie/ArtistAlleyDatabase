package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_categories_chip_state_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_categories_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_categories_label
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_group_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_group_label
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_region_chip_state_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_region_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_anime_news_network_region_label
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_crunchyroll_news_categories_chip_state_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_crunchyroll_news_categories_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_crunchyroll_news_categories_label
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

class NewsSortFilterController(
    scope: CoroutineScope,
    settings: NewsSettings,
) : SortFilterController<NewsSortFilterController.FilterParams>(scope = scope) {

    private val sortSection = SortFilterSection.Sort(
        AnimeNewsSortOption::class,
        AnimeNewsSortOption.DATETIME,
        Res.string.anime_news_filter_sort_label,
    )

    private val animeNewsNetworkRegionSection = SortFilterSection.Filter(
        titleRes = Res.string.anime_news_filter_anime_news_network_region_label,
        titleDropdownContentDescriptionRes = Res.string.anime_news_filter_anime_news_network_region_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_news_filter_anime_news_network_region_chip_state_content_description,
        values = AnimeNewsNetworkRegion.entries,
        includedSetting = settings.animeNewsNetworkRegion,
        valueToText = { ComposeResourceUtils.stringResource(it.value.textRes) },
        selectionMethod = SortFilterSection.Filter.SelectionMethod.SINGLE_EXCLUSIVE,
    )

    private val animeNewsNetworkCategorySection = SortFilterSection.Filter(
        titleRes = Res.string.anime_news_filter_anime_news_network_categories_label,
        titleDropdownContentDescriptionRes = Res.string.anime_news_filter_anime_news_network_categories_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_news_filter_anime_news_network_categories_chip_state_content_description,
        values = AnimeNewsNetworkCategory.entries,
        includedSettings = settings.animeNewsNetworkCategoriesIncluded,
        excludedSettings = settings.animeNewsNetworkCategoriesExcluded,
        valueToText = { ComposeResourceUtils.stringResource(it.value.textRes) },
    )

    private val crunchyrollCategorySection = SortFilterSection.Filter(
        titleRes = Res.string.anime_news_filter_crunchyroll_news_categories_label,
        titleDropdownContentDescriptionRes = Res.string.anime_news_filter_crunchyroll_news_categories_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_news_filter_crunchyroll_news_categories_chip_state_content_description,
        values = CrunchyrollNewsCategory.values().toList(),
        includedSettings = settings.crunchyrollNewsCategoriesIncluded,
        excludedSettings = settings.crunchyrollNewsCategoriesExcluded,
        valueToText = { ComposeResourceUtils.stringResource(it.value.textRes) },
    )

    private val animeNewsNetworkRegion = SortFilterSection.Group(
        titleRes = Res.string.anime_news_filter_anime_news_network_group_label,
        titleDropdownContentDescriptionRes = Res.string.anime_news_filter_anime_news_network_group_content_description,
        children = listOf(animeNewsNetworkRegionSection, animeNewsNetworkCategorySection)
    )

    override val sections = listOf(
        sortSection,
        animeNewsNetworkRegion,
        crunchyrollCategorySection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        animeNewsNetworkRegionOptions = animeNewsNetworkRegionSection.filterOptions,
        animeNewsNetworkCategoryOptions = animeNewsNetworkCategorySection.filterOptions,
        crunchyrollCategoryOptions = crunchyrollCategorySection.filterOptions,
    )

    data class FilterParams(
        val sort: List<SortEntry<AnimeNewsSortOption>>,
        val sortAscending: Boolean,
        val animeNewsNetworkRegionOptions: List<FilterEntry.FilterEntryImpl<AnimeNewsNetworkRegion>>,
        val animeNewsNetworkCategoryOptions: List<FilterEntry.FilterEntryImpl<AnimeNewsNetworkCategory>>,
        val crunchyrollCategoryOptions: List<FilterEntry.FilterEntryImpl<CrunchyrollNewsCategory>>,
    )
}
