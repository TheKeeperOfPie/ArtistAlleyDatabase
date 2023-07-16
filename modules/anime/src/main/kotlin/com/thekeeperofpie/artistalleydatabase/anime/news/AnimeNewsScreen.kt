package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
object AnimeNewsScreen {

    @Composable
    operator fun invoke(
        viewModel: AnimeNewsViewModel = hiltViewModel<AnimeNewsViewModel>(),
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                confirmValueChange = { it != SheetValue.Hidden },
                skipHiddenState = true,
            )
        )

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = { FilterPanel(viewModel.filterData) },
            sheetTonalElevation = 4.dp,
            sheetShadowElevation = 4.dp,
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_news_title),
                    upIconOption = UpIconOption.Back { navigationCallback.popUp() },
                )
            },
        ) {
            val uriHandler = LocalUriHandler.current
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val news = viewModel.news
                if (news.isEmpty()) {
                    item("no_results") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                stringResource(id = R.string.anime_media_list_no_results),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                    }
                } else {
                    items(count = news.size, key = { news[it].id }, contentType = { "news " }) {
                        AnimeNewsSmallCard(entry = news[it], uriHandler = uriHandler)
                    }
                }
            }
        }
    }

    // TODO: Collapse into other sort/filter infra
    @Composable
    private fun FilterPanel(data: AnimeNewsViewModel.FilterData) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            Divider()

            SortAndFilterComposables.SortSection(
                headerTextRes = R.string.anime_news_filter_sort_label,
                expanded = { data.sortExpanded },
                onExpandedChange = { data.sortExpanded = it },
                sortOptions = { data.sortOptions },
                onSortClick = { data.onSortChanged(it) },
                sortAscending = { data.sortAscending },
                onSortAscendingChange = { data.sortAscending = it },
                showDivider = true,
            )

            FilterSection(
                expanded = { data.animeNewsNetworkRegionsExpanded },
                onExpandedChange = { data.animeNewsNetworkRegionsExpanded = it },
                entries = { data.animeNewsNetworkRegions },
                onEntryClick = { data.onRegionChanged(it) },
                titleRes = R.string.anime_news_filter_anime_news_network_region_label,
                titleDropdownContentDescriptionRes = R.string.anime_news_filter_anime_news_network_region_content_description,
                valueToText = { stringResource(it.value.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_news_filter_anime_news_network_region_chip_state_content_description,
                showDivider = true,
            )

            FilterSection(
                expanded = { data.animeNewsNetworkCategoriesExpanded },
                onExpandedChange = { data.animeNewsNetworkCategoriesExpanded = it },
                entries = { data.animeNewsNetworkCategories },
                onEntryClick = { data.onAnimeNewsNetworkCategoryToggled(it) },
                titleRes = R.string.anime_news_filter_anime_news_network_categories_label,
                titleDropdownContentDescriptionRes = R.string.anime_news_filter_anime_news_network_categories_content_description,
                valueToText = { stringResource(it.value.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_news_filter_anime_news_network_categories_chip_state_content_description,
                showDivider = true,
            )

            FilterSection(
                expanded = { data.crunchyrollNewsCategoriesExpanded },
                onExpandedChange = { data.crunchyrollNewsCategoriesExpanded = it },
                entries = { data.crunchyrollNewsCategories },
                onEntryClick = { data.onCrunchyrollNewsCategoryToggled(it) },
                titleRes = R.string.anime_news_filter_crunchyroll_news_categories_label,
                titleDropdownContentDescriptionRes = R.string.anime_news_filter_crunchyroll_news_categories_content_description,
                valueToText = { stringResource(it.value.textRes) },
                includeExcludeIconContentDescriptionRes = R.string.anime_news_filter_crunchyroll_news_categories_chip_state_content_description,
                showDivider = true,
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}
