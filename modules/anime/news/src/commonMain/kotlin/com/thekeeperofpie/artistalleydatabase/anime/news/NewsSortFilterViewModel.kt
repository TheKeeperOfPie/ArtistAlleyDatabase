package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapMutableState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class NewsSortFilterViewModel(
    json: Json,
    settings: NewsSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val sortOption =
        savedStateHandle.getMutableStateFlow<AnimeNewsSortOption>(
            json,
            "sortOption",
            AnimeNewsSortOption.DATETIME
        )
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        enumClass = AnimeNewsSortOption::class,
        defaultSort = AnimeNewsSortOption.DATETIME,
        headerText = Res.string.anime_news_filter_sort_label,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val animeNewsNetworkRegionIn = settings.animeNewsNetworkRegion.mapMutableState(
        viewModelScope,
        deserialize = { setOf(it) },
        serialize = { it.firstOrNull() ?: AnimeNewsNetworkRegion.USA_CANADA }
    )
    private val animeNewsNetworkRegionSection = SortFilterSectionState.Filter(
        title = Res.string.anime_news_filter_anime_news_network_region_label,
        titleDropdownContentDescription = Res.string.anime_news_filter_anime_news_network_region_content_description,
        includeExcludeIconContentDescription = Res.string.anime_news_filter_anime_news_network_region_chip_state_content_description,
        options = MutableStateFlow(AnimeNewsNetworkRegion.entries),
        filterIn = animeNewsNetworkRegionIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.textRes) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.EXACTLY_ONE,
    )

    // TODO: Make these real sets
    private val animeNewsNetworkCategoryIn =
        settings.animeNewsNetworkCategoriesIncluded.mapMutableState(
            viewModelScope,
            deserialize = { it.toSet() },
            serialize = { it.toList() },
        )
    private val animeNewsNetworkCategoryNotIn =
        settings.animeNewsNetworkCategoriesExcluded.mapMutableState(
            viewModelScope,
            deserialize = { it.toSet() },
            serialize = { it.toList() },
        )
    private val animeNewsNetworkCategorySection = SortFilterSectionState.Filter(
        title = Res.string.anime_news_filter_anime_news_network_categories_label,
        titleDropdownContentDescription = Res.string.anime_news_filter_anime_news_network_categories_content_description,
        includeExcludeIconContentDescription = Res.string.anime_news_filter_anime_news_network_categories_chip_state_content_description,
        options = MutableStateFlow(AnimeNewsNetworkCategory.entries),
        filterIn = animeNewsNetworkCategoryIn,
        filterNotIn = animeNewsNetworkCategoryNotIn,
        valueToText = { stringResource(it.textRes) },
    )

    private val crunchyrollCategoryIn =
        settings.crunchyrollNewsCategoriesIncluded.mapMutableState(
            viewModelScope,
            deserialize = { it.toSet() },
            serialize = { it.toList() },
        )
    private val crunchyrollCategoryNotIn =
        settings.crunchyrollNewsCategoriesExcluded.mapMutableState(
            viewModelScope,
            deserialize = { it.toSet() },
            serialize = { it.toList() },
        )
    private val crunchyrollCategorySection = SortFilterSectionState.Filter(
        title = Res.string.anime_news_filter_crunchyroll_news_categories_label,
        titleDropdownContentDescription = Res.string.anime_news_filter_crunchyroll_news_categories_content_description,
        includeExcludeIconContentDescription = Res.string.anime_news_filter_crunchyroll_news_categories_chip_state_content_description,
        options = MutableStateFlow(CrunchyrollNewsCategory.entries),
        filterIn = crunchyrollCategoryIn,
        filterNotIn = crunchyrollCategoryNotIn,
        valueToText = { stringResource(it.textRes) },
    )

    private val animeNewsNetworkRegion = SortFilterSectionState.Group(
        title = Res.string.anime_news_filter_anime_news_network_group_label,
        titleDropdownContentDescription = Res.string.anime_news_filter_anime_news_network_group_content_description,
        children = ReadOnlyStateFlow(
            listOf(
                animeNewsNetworkRegionSection,
                animeNewsNetworkCategorySection
            )
        ),
    )

    private val sections = listOf(
        sortSection,
        animeNewsNetworkRegion,
        crunchyrollCategorySection,
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        sortOption,
        sortAscending,
        // Other values are persisted into NewsSetting and read directly in AnimeNewsController
    ) {
        FilterParams(
            sort = it[0] as AnimeNewsSortOption,
            sortAscending = it[1] as Boolean,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = MutableStateFlow(false), // TODO: Wire up collapse?
    )

    data class FilterParams(
        val sort: AnimeNewsSortOption,
        val sortAscending: Boolean,
    )
}

