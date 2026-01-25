package com.thekeeperofpie.artistalleydatabase.alley.browse

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_merch
import artistalleydatabase.modules.alley.generated.resources.alley_browse_tab_series
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint_with_entry_count
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesFilterOption
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagsViewModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
object BrowseScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onSeriesClick: (DataYear, String) -> Unit,
        onMerchClick: (DataYear, String) -> Unit,
        onOpenExport: () -> Unit,
        onOpenChangelog: () -> Unit,
        onOpenSettings: () -> Unit,
        tagsViewModel: TagsViewModel = viewModel {
            graph.tagsViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val dataYearHeaderState =
            rememberDataYearHeaderState(tagsViewModel.dataYear, null)
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val scaffoldState = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()
                BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                    scope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                }
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                SortFilterBottomScaffold(
                    state = when (selectedTabIndex) {
                        0 -> tagsViewModel.seriesSortFilterController.state
                        else -> null
                    },
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 72.dp,
                    topBar = {
                        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                            Box(Modifier.fillMaxWidth()) {
                                TabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    modifier = Modifier
                                        .widthIn(max = 1200.dp)
                                        .align(Alignment.TopCenter)
                                ) {
                                    Tab.entries.forEachIndexed { index, tab ->
                                        Tab(
                                            selected = selectedTabIndex == index,
                                            onClick = { selectedTabIndex = index },
                                            text = { Text(stringResource(tab.textRes)) },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .conditionallyNonNull(scrollBehavior) {
                            nestedScroll(
                                NestedScrollSplitter(
                                    primary = it.nestedScrollConnection,
                                    consumeNone = true,
                                )
                            )
                        }
                ) {
                    val scrollPositions = ScrollStateSaver.scrollPositions()
                    val series = tagsViewModel.series.collectAsLazyPagingItems()
                    val merch = tagsViewModel.merch.collectAsLazyPagingItems()
                    AnimatedContent(
                        targetState = Tab.entries[selectedTabIndex],
                        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                        label = "Tag screen",
                    ) {
                        val scrollStateSaver =
                            ScrollStateSaver.fromMap(it.name, scrollPositions)
                        when (it) {
                            Tab.SERIES -> {
                                var seriesQuery by tagsViewModel.seriesQuery
                                    .collectAsMutableStateWithLifecycle()
                                TabScreen(
                                    dataYearHeaderState = dataYearHeaderState,
                                    scaffoldState = scaffoldState,
                                    query = { seriesQuery },
                                    onQueryChange = { seriesQuery = it },
                                    entriesSize = { series.itemCount },
                                    values = series,
                                    itemKey = { it.series.id },
                                    item = { data ->
                                        SeriesRow(
                                            data = data,
                                            image = {
                                                data?.let {
                                                    tagsViewModel.getSeriesImage(it.series)
                                                }
                                            },
                                            textStyle = LocalTextStyle.current,
                                            onFavoriteToggle = {
                                                if (data != null) {
                                                    tagsViewModel.onSeriesFavoriteToggle(data, it)
                                                }
                                            },
                                            onClick = {
                                                data?.let {
                                                    onSeriesClick(
                                                        tagsViewModel.dataYear.value,
                                                        it.series.id
                                                    )
                                                }
                                            },
                                        )
                                    },
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                    scrollStateSaver = scrollStateSaver,
                                    additionalHeader = {
                                        item(key = "seriesLanguageOption") {
                                            tagsViewModel.seriesLanguageSection
                                                .Content(Modifier.fillMaxWidth())
                                        }
                                    }
                                )
                            }
                            Tab.MERCH -> TabScreen(
                                dataYearHeaderState = dataYearHeaderState,
                                scaffoldState = null,
                                query = { tagsViewModel.merchQuery },
                                onQueryChange = { tagsViewModel.merchQuery = it },
                                entriesSize = { merch.itemCount },
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
                                                    tagsViewModel.dataYear.value,
                                                    data.merch.name
                                                )
                                            }
                                        },
                                    )
                                },
                                onOpenExport = onOpenExport,
                                onOpenChangelog = onOpenChangelog,
                                onOpenSettings = onOpenSettings,
                                scrollStateSaver = scrollStateSaver,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun <T : Any> TabScreen(
        dataYearHeaderState: DataYearHeaderState,
        scaffoldState: BottomSheetScaffoldState?,
        query: () -> String,
        onQueryChange: (String) -> Unit,
        entriesSize: () -> Int,
        values: LazyPagingItems<T>,
        itemKey: (T) -> Any,
        item: @Composable (T?) -> Unit,
        scrollStateSaver: ScrollStateSaver,
        onOpenExport: () -> Unit,
        onOpenChangelog: () -> Unit,
        onOpenSettings: () -> Unit,
        additionalHeader: LazyListScope.() -> Unit = {},
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Scaffold(
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        val isNotEmpty by remember {
                            derivedStateOf { query().isNotEmpty() }
                        }
                        BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                            onQueryChange("")
                        }

                        StaticSearchBar(
                            query = query(),
                            onQueryChange = onQueryChange,
                            placeholder = {
                                @Suppress("NAME_SHADOWING")
                                val entriesSize = entriesSize()
                                Text(
                                    if (entriesSize > 0) {
                                        stringResource(
                                            EntryRes.string.entry_search_hint_with_entry_count,
                                            entriesSize,
                                        )
                                    } else {
                                        stringResource(EntryRes.string.entry_search_hint)
                                    }
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(isNotEmpty) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(
                                                EntryRes.string.entry_search_clear
                                            ),
                                        )
                                    }
                                }
                            },
                            onSearch = {},
                            modifier = Modifier
                                .widthIn(max = 1200.dp)
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                }
            },
        ) {
            val listState = scrollStateSaver.lazyListState()
            val scrollAreaState = rememberScrollAreaState(listState)
            ScrollArea(
                state = scrollAreaState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                val width = LocalWindowConfiguration.current.screenWidthDp
                val horizontalContentPadding = if (width > 800.dp) {
                    (width - 800.dp) / 2
                } else {
                    0.dp
                }
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = horizontalContentPadding,
                        end = horizontalContentPadding,
                        bottom = 80.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                ) {
                    item(key = "dataYearHeader") {
                        BottomSheetFilterDataYearHeader(
                            dataYearHeaderState = dataYearHeaderState,
                            scaffoldState = scaffoldState,
                            onOpenExport = onOpenExport,
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
                                    CircularProgressIndicator()
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

    @Composable
    private fun SeriesFilters(
        seriesFiltersState: List<Pair<SeriesFilterOption, Boolean>>,
        onSeriesFilterClick: (SeriesFilterOption) -> Unit,
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            seriesFiltersState.forEach { (option, selected) ->
                FilterChip(
                    selected = selected,
                    label = { Text(stringResource(option.title)) },
                    onClick = { onSeriesFilterClick(option) },
                )
            }
        }
    }

    enum class Tab(val textRes: StringResource) {
        SERIES(Res.string.alley_browse_tab_series),
        MERCH(Res.string.alley_browse_tab_merch),
    }
}
