@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimeNewsViewModel @Inject constructor(
    settings: AnimeSettings,
    private val animeNewsController: AnimeNewsController,
) : ViewModel() {

    val filterData = FilterData(settings)

    var news by mutableStateOf<List<AnimeNewsArticleEntry>>(emptyList())
        private set

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                snapshotFlow {
                    filterData.sortOptions
                        .firstOrNull { it.state == FilterIncludeExcludeState.INCLUDE }?.value
                },
                snapshotFlow { filterData.sortAscending },
                ::Pair
            )
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (sortOption, sortAscending) ->
                    animeNewsController.news().mapLatest { news ->
                        if (sortOption != null) {
                            val baseComparator: Comparator<AnimeNewsArticleEntry> =
                                when (sortOption) {
                                    AnimeNewsSortOption.DATETIME -> compareBy { it.date }
                                    AnimeNewsSortOption.TITLE -> compareBy { it.title }
                                    AnimeNewsSortOption.SOURCE -> compareBy { it.type }
                                }

                            val comparator = nullsFirst(baseComparator).let {
                                if (sortAscending) it else it.reversed()
                            }

                            news.sortedWith(comparator)
                        } else {
                            news
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        news = it
                    }
                }
        }
    }

    class FilterData(private val settings: AnimeSettings) {
        var sortExpanded by mutableStateOf(false)
        var animeNewsNetworkRegionsExpanded by mutableStateOf(false)
        var animeNewsNetworkCategoriesExpanded by mutableStateOf(false)
        var crunchyrollNewsCategoriesExpanded by mutableStateOf(false)

        var sortOptions by mutableStateOf(
            SortEntry.options(AnimeNewsSortOption::class, AnimeNewsSortOption.DATETIME)
        )
            private set

        var sortAscending by mutableStateOf(false)

        var animeNewsNetworkRegions by mutableStateOf(
            FilterEntry.values(
                values = AnimeNewsNetworkRegion.values(),
                included = listOf(settings.animeNewsNetworkRegion.value),
            )
        )

        var animeNewsNetworkCategories by mutableStateOf(
            FilterEntry.values(
                values = AnimeNewsNetworkCategory.values(),
                included = settings.animeNewsNetworkCategoriesIncluded.value,
                excluded = settings.animeNewsNetworkCategoriesExcluded.value,
            )
        )

        var crunchyrollNewsCategories by mutableStateOf(
            FilterEntry.values(
                values = CrunchyrollNewsCategory.values(),
                included = settings.crunchyrollNewsCategoriesIncluded.value,
                excluded = settings.crunchyrollNewsCategoriesExcluded.value,
            )
        )

        fun onSortChanged(option: AnimeNewsSortOption) {
            sortOptions = sortOptions.toMutableList()
                .apply {
                    replaceAll {
                        it.copy(
                            state = if (it.value == option) {
                                FilterIncludeExcludeState.INCLUDE
                            } else {
                                FilterIncludeExcludeState.DEFAULT
                            }
                        )
                    }
                }
        }

        fun onRegionChanged(region: FilterEntry.FilterEntryImpl<AnimeNewsNetworkRegion>) {
            val newList = animeNewsNetworkRegions.toMutableList()
                .apply {
                    replaceAll {
                        if (it.value == region.value) {
                            it.copy(state = FilterIncludeExcludeState.INCLUDE)
                        } else it.copy(state = FilterIncludeExcludeState.DEFAULT)
                    }
                }
            settings.animeNewsNetworkRegion.value = region.value
            animeNewsNetworkRegions = newList
        }

        fun onAnimeNewsNetworkCategoryToggled(
            category: FilterEntry.FilterEntryImpl<AnimeNewsNetworkCategory>,
        ) {
            val newList = animeNewsNetworkCategories.toMutableList()
                .apply {
                    replaceAll {
                        if (it.value == category.value) {
                            it.copy(state = it.state.next())
                        } else it
                    }
                }
            settings.animeNewsNetworkCategoriesIncluded.value =
                newList.filter { it.state == FilterIncludeExcludeState.INCLUDE }.map { it.value }
            settings.animeNewsNetworkCategoriesExcluded.value =
                newList.filter { it.state == FilterIncludeExcludeState.EXCLUDE }.map { it.value }
            animeNewsNetworkCategories = newList
        }

        fun onCrunchyrollNewsCategoryToggled(
            category: FilterEntry.FilterEntryImpl<CrunchyrollNewsCategory>,
        ) {
            val newList = crunchyrollNewsCategories.toMutableList()
                .apply {
                    replaceAll {
                        if (it.value == category.value) {
                            it.copy(state = it.state.next())
                        } else it
                    }
                }
            settings.crunchyrollNewsCategoriesIncluded.value =
                newList.filter { it.state == FilterIncludeExcludeState.INCLUDE }.map { it.value }
            settings.crunchyrollNewsCategoriesExcluded.value =
                newList.filter { it.state == FilterIncludeExcludeState.EXCLUDE }.map { it.value }
            crunchyrollNewsCategories = newList
        }
    }
}
