package com.thekeeperofpie.artistalleydatabase.alley.browse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_merch
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_series
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.AlleySearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.DisplayConstants
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SwitchSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
object BrowseScreen {

    @Composable
    operator fun invoke(
        selectedTabIndex: () -> Int,
        onSelectedTabIndexChanged: (Int) -> Unit,
        onSeriesClick: (DataYear, String) -> Unit,
        onMerchClick: (DataYear, String) -> Unit,
        onOpenSeriesChangelog: (DataYear) -> Unit,
        onOpenMerchChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        scaffoldState: SortFilterBottomScaffoldState = rememberSortFilterBottomScaffoldState(),
        tagsViewModel: TagsViewModel = assistedMetroViewModel<TagsViewModel, TagsViewModel.Factory> {
            create(it.createSavedStateHandle())
        },
    ) {
        val dataYearHeaderState = rememberDataYearHeaderState(
            dataYear = tagsViewModel::dataYear,
            onDataYearChange = { tagsViewModel.dataYear = it },
            lockedYear = null,
        )
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val scope = rememberCoroutineScope()
                BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                    scope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                }
                val selectedTabIndex = selectedTabIndex()
                val series = tagsViewModel.series.collectAsLazyPagingItems()
                val merch = tagsViewModel.merch.collectAsLazyPagingItems()
                SortFilterBottomScaffold2(
                    state = scaffoldState,
                    sheetPeekHeight = 72.dp,
                    enterAlwaysTopBar = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                modifier = Modifier
                                    .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                            ) {
                                Tab.entries.forEachIndexed { index, tab ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { onSelectedTabIndexChanged(index) },
                                        text = { Text(stringResource(tab.textRes)) },
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                                    .padding(bottom = 8.dp)
                            ) {
                                val query = when (selectedTabIndex) {
                                    0 -> tagsViewModel.seriesQuery
                                    else -> tagsViewModel.merchQuery
                                }
                                val entries = when (selectedTabIndex) {
                                    0 -> series
                                    else -> merch
                                }
                                val isNotEmpty by remember {
                                    derivedStateOf { query.text.isNotEmpty() }
                                }
                                BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                                    query.clearText()
                                }

                                val scope = rememberCoroutineScope()
                                AlleySearchBar(
                                    onClickBack = null,
                                    query = query,
                                    itemCount = { entries.itemCount },
                                    title = { null },
                                    onClickFilter = { scope.launch { scaffoldState.bottomSheetState.toggle() } },
                                    modifier = Modifier
                                        .widthIn(max = DisplayConstants.MAX_CONTENT_WIDTH)
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                )
                            }
                        }
                    },
                    sheetContent = {
                        when (selectedTabIndex) {
                            0 -> SeriesSortFilterSheetContent(
                                state = tagsViewModel.seriesSortFilterController.state,
                                sheetState = scaffoldState.bottomSheetState,
                            )
                            // TODO: Real merch sort filter?
                            else -> Box {}
                        }
                    }
                ) { _, contentPadding ->
                    val saveableStateHolder = rememberSaveableStateHolder()
                    AnimatedContent(
                        targetState = Tab.entries[selectedTabIndex],
                        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        label = "Tag screen",
                    ) {
                        saveableStateHolder.SaveableStateProvider(it) {
                            when (it) {
                                Tab.SERIES -> {
                                    TabScreen(
                                        dataYearHeaderState = dataYearHeaderState,
                                        baseContentPadding = contentPadding,
                                        values = series,
                                        itemKey = { it.series.id },
                                        item = { data ->
                                            SeriesRow(
                                                data = data,
                                                textStyle = LocalTextStyle.current,
                                                onFavoriteToggle = {
                                                    if (data != null) {
                                                        tagsViewModel.onSeriesFavoriteToggle(
                                                            data,
                                                            it
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    data?.let {
                                                        onSeriesClick(
                                                            tagsViewModel.dataYear,
                                                            it.series.id,
                                                        )
                                                    }
                                                },
                                            )
                                        },
                                        onOpenChangelog = onOpenSeriesChangelog,
                                        onOpenSettings = onOpenSettings,
                                        additionalHeader = {
                                            item(key = "seriesFilterOptions") {
                                                Column {
                                                    tagsViewModel.seriesLanguageSection
                                                        .Content(Modifier.fillMaxWidth())

                                                    HorizontalDivider()

                                                    val saveableState =
                                                        tagsViewModel.seriesSortFilterController.state.saveableState
                                                    SwitchSection(
                                                        title = { Text(stringResource(Res.string.alley_filter_show_only_confirmed_tags)) },
                                                        enabled = saveableState::showOnlyConfirmedTags,
                                                        onEnabledChanged = {
                                                            saveableState.showOnlyConfirmedTags = it
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                                Tab.MERCH -> TabScreen(
                                    dataYearHeaderState = dataYearHeaderState,
                                    baseContentPadding = contentPadding,
                                    values = merch,
                                    itemKey = { it.merch.name },
                                    item = { data ->
                                        MerchRow(
                                            data = data,
                                            onFavoriteToggle = {
                                                if (data != null) {
                                                    tagsViewModel.onMerchFavoriteToggle(data, it)
                                                }
                                            },
                                            onClick = {
                                                if (data != null) {
                                                    onMerchClick(
                                                        tagsViewModel.dataYear,
                                                        data.merch.name,
                                                    )
                                                }
                                            },
                                        )
                                    },
                                    onOpenChangelog = onOpenMerchChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun <T : Any> TabScreen(
        dataYearHeaderState: DataYearHeaderState,
        baseContentPadding: () -> PaddingValues,
        values: LazyPagingItems<T>,
        itemKey: (T) -> Any,
        item: @Composable (T?) -> Unit,
        onOpenChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        additionalHeader: LazyListScope.() -> Unit = {},
    ) {
        val listState = rememberLazyListState()
        val scrollAreaState = rememberScrollAreaState(listState)
        ScrollArea(
            state = scrollAreaState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            BoxWithConstraints {
                val horizontalContentPadding =
                    if (maxWidth > DisplayConstants.MAX_CONTENT_WIDTH) {
                        (maxWidth - DisplayConstants.MAX_CONTENT_WIDTH) / 2
                    } else {
                        0.dp
                    }
                LazyColumn(
                    state = listState,
                    contentPadding = baseContentPadding() + PaddingValues(
                        start = horizontalContentPadding,
                        end = horizontalContentPadding,
                        bottom = 200.dp,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                ) {
                    item(key = "dataYearHeader") {
                        DataYearHeader(
                            state = dataYearHeaderState,
                            onOpenChangelog = onOpenChangelog,
                            onOpenSettings = onOpenSettings,
                        )
                    }

                    additionalHeader()

                    if (values.itemCount == 0) {
                        if (values.loadState.refresh is LoadState.Loading) {
                            item("loadingIndicator") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    InfiniteProgressIndicator()
                                }
                            }
                        } else {
                            item("noResults") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.alley_search_no_results),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            count = values.itemCount,
                            key = values.itemKey(itemKey),
                            contentType = values.itemContentType { "tag_entry" },
                        ) { index ->
                            val value = values[index]
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item(value)
                                HorizontalDivider()
                            }
                        }
                    }
                }

                PrimaryVerticalScrollbar(listState)
            }
        }
    }

    enum class Tab(val textRes: StringResource) {
        SERIES(Res.string.alley_browse_tab_series),
        MERCH(Res.string.alley_browse_tab_merch),
    }
}
