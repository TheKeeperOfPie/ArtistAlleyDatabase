package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.PagingData
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_artists
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_series
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_go_to_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_series
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_empty_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_merch
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_search
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_series
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplayTypeSearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.isLoading
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
object FavoritesScreen {

    @Composable
    operator fun invoke(
        favoritesViewModel: FavoritesViewModel,
        artistSortFilterController: ArtistSortFilterController,
        stampRallySortFilterController: StampRallySortFilterController,
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
    ) {
        val navigationController = LocalNavigationController.current
        FavoritesScreen(
            state = remember(favoritesViewModel) {
                State(
                    randomSeed = favoritesViewModel.randomSeed,
                    tab = favoritesViewModel.tab,
                    query = favoritesViewModel.query,
                    displayType = favoritesViewModel.displayType,
                    year = favoritesViewModel.year,
                    artistsEntries = favoritesViewModel.artistEntries,
                    artistsSearchState = favoritesViewModel.artistSearchState,
                    artistsSortOption = artistSortFilterController.sortOption,
                    artistsSortAscending = artistSortFilterController.sortAscending,
                    artistsUnfilteredCount = favoritesViewModel.artistsUnfilteredCount,
                    ralliesEntries = favoritesViewModel.stampRallyEntries,
                    ralliesSearchState = favoritesViewModel.stampRallySearchState,
                    ralliesSortOption = stampRallySortFilterController.sortOption,
                    ralliesSortAscending = stampRallySortFilterController.sortAscending,
                    ralliesUnfilteredCount = favoritesViewModel.stampRallyUnfilteredCount,
                    seriesEntries = favoritesViewModel.seriesEntries,
                    merchEntries = favoritesViewModel.merchEntries,
                )
            },
            artistSortFilterState = artistSortFilterController.state,
            stampRallySortFilterState = stampRallySortFilterController.state,
            artistsScrollStateSaver = artistsScrollStateSaver,
            ralliesScrollStateSaver = ralliesScrollStateSaver,
            seriesScrollStateSaver = seriesScrollStateSaver,
            merchScrollStateSaver = merchScrollStateSaver,
            getSeriesImage = favoritesViewModel::getSeriesImage,
            eventSink = {
                favoritesViewModel.onEvent(
                    navigationController = navigationController,
                    event = it,
                    onNavigateToArtists = onNavigateToArtists,
                    onNavigateToRallies = onNavigateToRallies,
                    onNavigateToSeries = onNavigateToSeries,
                    onNavigateToMerch = onNavigateToMerch,
                )
            },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        artistSortFilterState: SortFilterState<*>,
        stampRallySortFilterState: SortFilterState<*>,
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        getSeriesImage: (SeriesEntry) -> String?,
        eventSink: (Event) -> Unit,
    ) {
        CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
            val scaffoldState = rememberBottomSheetScaffoldState()
            val scope = rememberCoroutineScope()
            BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

            var tab by state.tab.collectAsMutableStateWithLifecycle()
            val artistsEntries = state.artistsEntries.collectAsLazyPagingItemsWithLifecycle()
            val ralliesEntries = state.ralliesEntries.collectAsLazyPagingItemsWithLifecycle()
            val seriesEntries = state.seriesEntries.collectAsLazyPagingItemsWithLifecycle()
            val merchEntries = state.merchEntries.collectAsLazyPagingItemsWithLifecycle()
            val entries = when (tab) {
                EntryTab.ARTISTS -> artistsEntries
                EntryTab.RALLIES -> ralliesEntries
                EntryTab.SERIES -> seriesEntries
                EntryTab.MERCH -> merchEntries
            }

            Box {
                var horizontalScrollBarWidth by remember { mutableStateOf(0) }
                val horizontalScrollState = rememberScrollState()
                SortFilterBottomScaffold(
                    state = when (tab) {
                        EntryTab.ARTISTS -> artistSortFilterState
                        EntryTab.RALLIES -> stampRallySortFilterState
                        EntryTab.SERIES -> null // TODO: SortFilterState
                        EntryTab.MERCH -> null // TODO: SortFilterState
                    },
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 72.dp,
                    topBar = {
                        val title = stringResource(Res.string.alley_favorites_search)
                        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                            DisplayTypeSearchBar(
                                onClickBack = null,
                                query = state.query,
                                title = { title },
                                itemCount = { entries.itemCount },
                                displayType = state.displayType,
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .conditionallyNonNull(scrollBehavior) {
                            nestedScroll(
                                NestedScrollSplitter(
                                    primary = it.nestedScrollConnection,
                                    consumeNone = true,
                                )
                            )
                        }
                ) {
                    var unfavoriteDialogEntry by remember {
                        mutableStateOf<SearchScreen.SearchEntryModel?>(null)
                    }
                    val dataYearHeaderState = rememberDataYearHeaderState(state.year, null)
                    when (tab) {
                        EntryTab.ARTISTS -> ArtistContent(
                            state = state,
                            gridState = artistsScrollStateSaver.lazyStaggeredGridState(),
                            searchState = state.artistsSearchState,
                            horizontalScrollState = horizontalScrollState,
                            entries = artistsEntries,
                            eventSink = eventSink,
                            scaffoldPadding = PaddingValues(top = it.calculateTopPadding()),
                            onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                            onUnfavoriteDialogEntryChange = { unfavoriteDialogEntry = it },
                            header = {
                                Header(
                                    tab = { tab },
                                    onTabChange = { tab = it },
                                    dataYearHeaderState = dataYearHeaderState,
                                    scaffoldState = scaffoldState,
                                )
                            },
                            noResultsItem = { NoResultsItem(EntryTab.ARTISTS, eventSink) },
                        )
                        EntryTab.RALLIES -> RallyContent(
                            state = state,
                            gridState = ralliesScrollStateSaver.lazyStaggeredGridState(),
                            searchState = state.ralliesSearchState,
                            horizontalScrollState = horizontalScrollState,
                            entries = ralliesEntries,
                            eventSink = eventSink,
                            scaffoldPadding = PaddingValues(top = it.calculateTopPadding()),
                            onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                            onUnfavoriteDialogEntryChange = { unfavoriteDialogEntry = it },
                            header = {
                                Header(
                                    tab = { tab },
                                    onTabChange = { tab = it },
                                    dataYearHeaderState = dataYearHeaderState,
                                    scaffoldState = scaffoldState,
                                )
                            },
                            noResultsItem = { NoResultsItem(EntryTab.RALLIES, eventSink) },
                        )
                        EntryTab.SERIES -> SeriesContent(
                            listState = seriesScrollStateSaver.lazyListState(),
                            series = seriesEntries,
                            getSeriesImage = getSeriesImage,
                            header = {
                                Header(
                                    tab = { tab },
                                    onTabChange = { tab = it },
                                    dataYearHeaderState = dataYearHeaderState,
                                    scaffoldState = null,
                                )
                            },
                            eventSink = eventSink,
                        )
                        EntryTab.MERCH -> MerchContent(
                            listState = merchScrollStateSaver.lazyListState(),
                            merch = merchEntries,
                            header = {
                                Header(
                                    tab = { tab },
                                    onTabChange = { tab = it },
                                    dataYearHeaderState = dataYearHeaderState,
                                    scaffoldState = null,
                                )
                            },
                            eventSink = eventSink,
                        )
                    }

                    UnfavoriteDialog(
                        entry = { unfavoriteDialogEntry },
                        onClearEntry = { unfavoriteDialogEntry = null },
                        onRemoveFavorite = {
                            eventSink(
                                Event.SearchEvent(
                                    SearchScreen.Event.FavoriteToggle<SearchScreen.SearchEntryModel>(
                                        entry = it,
                                        favorite = false
                                    )
                                )
                            )
                        },
                    )
                }

                if (PlatformSpecificConfig.scrollbarsAlwaysVisible) {
                    HorizontalScrollbar(
                        state = horizontalScrollState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .width(LocalDensity.current.run { horizontalScrollBarWidth.toDp() })
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun ArtistContent(
        state: State,
        gridState: LazyStaggeredGridState,
        searchState: SearchScreen.State<ArtistSearchScreen.ArtistColumn>,
        horizontalScrollState: ScrollState,
        entries: LazyPagingItems<ArtistEntryGridModel>,
        eventSink: (Event) -> Unit,
        scaffoldPadding: PaddingValues,
        onHorizontalScrollBarWidth: (Int) -> Unit,
        onUnfavoriteDialogEntryChange: (SearchScreen.SearchEntryModel?) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
    ) {
        val unfilteredCount by state.artistsUnfilteredCount.collectAsStateWithLifecycle()
        SearchScreen.Content(
            state = searchState,
            eventSink = { eventSink(Event.SearchEvent(it)) },
            entries = entries,
            unfilteredCount = { unfilteredCount },
            horizontalScrollState = horizontalScrollState,
            gridState = gridState,
            scaffoldPadding = scaffoldPadding,
            onHorizontalScrollBarWidth = onHorizontalScrollBarWidth,
            itemToSharedElementId = { it.id.scopedId },
            header = header,
            noResultsItem = noResultsItem,
            itemRow = { entry, onFavoriteToggle, modifier ->
                ArtistListRow(
                    entry = entry,
                    onFavoriteToggle = {
                        if (it) {
                            onFavoriteToggle(it)
                        } else {
                            onUnfavoriteDialogEntryChange(entry)
                        }
                    },
                    onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                    onMoreClick = {
                        eventSink(
                            Event.SearchEvent(
                                SearchScreen.Event.OpenEntry(entry, 1)
                            )
                        )
                    },
                    modifier = modifier
                )
            },
            columnHeader = {
                ArtistSearchScreen.ColumnHeader(
                    column = it,
                    sortOption = state.artistsSortOption,
                    sortAscending = state.artistsSortAscending,
                )
            },
            tableCell = { row, column ->
                ArtistSearchScreen.TableCell(
                    row = row,
                    column = column,
                    onEntryClick = { entry, imageIndex ->
                        eventSink(
                            Event.SearchEvent(
                                SearchScreen.Event.OpenEntry(
                                    entry,
                                    imageIndex
                                )
                            )
                        )
                    },
                    onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                    onMerchClick = { eventSink(Event.OpenMerch(it)) },
                )
            },
        )
    }

    @Composable
    private fun RallyContent(
        state: State,
        gridState: LazyStaggeredGridState,
        searchState: SearchScreen.State<StampRallySearchScreen.StampRallyColumn>,
        horizontalScrollState: ScrollState,
        entries: LazyPagingItems<StampRallyEntryGridModel>,
        eventSink: (Event) -> Unit,
        scaffoldPadding: PaddingValues,
        onHorizontalScrollBarWidth: (Int) -> Unit,
        onUnfavoriteDialogEntryChange: (SearchScreen.SearchEntryModel?) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
    ) {
        val unfilteredCount by state.ralliesUnfilteredCount.collectAsStateWithLifecycle()
        SearchScreen.Content(
            state = searchState,
            eventSink = { eventSink(Event.SearchEvent(it)) },
            entries = entries,
            unfilteredCount = { unfilteredCount },
            horizontalScrollState = horizontalScrollState,
            gridState = gridState,
            scaffoldPadding = scaffoldPadding,
            onHorizontalScrollBarWidth = onHorizontalScrollBarWidth,
            itemToSharedElementId = { it.id.scopedId },
            header = header,
            noResultsItem = noResultsItem,
            itemRow = { entry, onFavoriteToggle, modifier ->
                StampRallyListRow(
                    entry = entry,
                    onFavoriteToggle = {
                        if (it) {
                            onFavoriteToggle(it)
                        } else {
                            onUnfavoriteDialogEntryChange(entry)
                        }
                    },
                    modifier = modifier,
                )
            },
            columnHeader = { StampRallySearchScreen.ColumnHeader(it) },
            tableCell = { row, column -> StampRallySearchScreen.TableCell(row, column) },
        )
    }

    @Composable
    private fun SeriesContent(
        listState: LazyListState,
        series: LazyPagingItems<SeriesWithUserData>,
        getSeriesImage: (SeriesEntry) -> String?,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                item("header") { header() }

                if (series.itemCount == 0) {
                    if (series.loadState.refresh.isLoading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        item("noResults") {
                            NoResultsItem(
                                tab = EntryTab.SERIES,
                                eventSink = eventSink,
                            )
                        }
                    }
                } else {
                    items(
                        count = series.itemCount,
                        key = series.itemKey { it.series.id },
                        contentType = series.itemContentType { "series" },
                    ) { index ->
                        val data = series[index]
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SeriesRow(
                                data = data,
                                image = {
                                    data?.let { getSeriesImage(it.series) }
                                },
                                textStyle = LocalTextStyle.current,
                                onFavoriteToggle = {
                                    if (data != null) {
                                        eventSink(Event.SeriesFavoriteToggle(data, it))
                                    }
                                },
                                onClick = {
                                    data?.let {
                                        eventSink(Event.OpenSeries(it.series.id))
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            VerticalScrollbar(
                state = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(bottom = 72.dp)
            )
        }
    }

    @Composable
    private fun MerchContent(
        listState: LazyListState,
        merch: LazyPagingItems<MerchWithUserData>,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                item("header") { header() }

                if (merch.itemCount == 0) {
                    if (merch.loadState.refresh.isLoading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        item("noResults") {
                            NoResultsItem(
                                tab = EntryTab.MERCH,
                                eventSink = eventSink,
                            )
                        }
                    }
                } else {
                    items(
                        count = merch.itemCount,
                        key = merch.itemKey { it.merch.name },
                        contentType = merch.itemContentType { "merch" },
                    ) { index ->
                        val data = merch[index]
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MerchRow(
                                data = data,
                                showNotes = false,
                                onFavoriteToggle = {
                                    if (data != null) {
                                        eventSink(Event.MerchFavoriteToggle(data, it))
                                    }
                                },
                                onClick = {
                                    if (data != null) {
                                        eventSink(Event.OpenMerch(data.merch.name))
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            VerticalScrollbar(
                state = listState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(bottom = 72.dp)
            )
        }
    }

    @Composable
    private fun Header(
        tab: () -> EntryTab,
        onTabChange: (EntryTab) -> Unit,
        dataYearHeaderState: DataYearHeaderState,
        scaffoldState: BottomSheetScaffoldState?,
    ) {
        Column {
            BottomSheetFilterDataYearHeader(dataYearHeaderState, scaffoldState)
            val tab = tab()
            PrimaryScrollableTabRow(EntryTab.entries.indexOf(tab)) {
                EntryTab.entries.forEach {
                    Tab(
                        selected = tab == it,
                        text = { Text(text = stringResource(it.text)) },
                        onClick = { onTabChange(it) },
                    )
                }
            }
        }
    }

    @Composable
    private fun NoResultsItem(
        tab: EntryTab,
        eventSink: (Event) -> Unit,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            val textRes = when (tab) {
                EntryTab.ARTISTS -> Res.string.alley_favorites_empty_artists
                EntryTab.RALLIES -> Res.string.alley_favorites_empty_stamp_rallies
                EntryTab.SERIES -> Res.string.alley_favorites_empty_series
                EntryTab.MERCH -> Res.string.alley_favorites_empty_merch
            }
            Text(
                text = stringResource(textRes),
                modifier = Modifier.padding(16.dp)
            )
            FilledTonalButton(onClick = {
                when (tab) {
                    EntryTab.ARTISTS -> eventSink(Event.NavigateToArtists)
                    EntryTab.RALLIES -> eventSink(Event.NavigateToRallies)
                    EntryTab.SERIES -> eventSink(Event.NavigateToSeries)
                    EntryTab.MERCH -> eventSink(Event.NavigateToMerch)
                }
            }) {
                val buttonTextRes = when (tab) {
                    EntryTab.ARTISTS -> Res.string.alley_favorites_empty_go_to_artists
                    EntryTab.RALLIES -> Res.string.alley_favorites_empty_go_to_stamp_rallies
                    EntryTab.SERIES -> Res.string.alley_favorites_empty_go_to_series
                    EntryTab.MERCH -> Res.string.alley_favorites_empty_go_to_merch
                }
                Text(stringResource(buttonTextRes))
            }
        }
    }

    enum class EntryTab(val text: StringResource) {
        ARTISTS(Res.string.alley_favorites_artists),
        RALLIES(Res.string.alley_favorites_rallies),
        SERIES(Res.string.alley_favorites_series),
        MERCH(Res.string.alley_favorites_merch),
    }

    @Stable
    class State(
        val randomSeed: Int,
        val tab: MutableStateFlow<EntryTab>,
        val query: MutableStateFlow<String>,
        val displayType: MutableStateFlow<DisplayType>,
        val year: MutableStateFlow<DataYear>,
        val artistsEntries: Flow<PagingData<ArtistEntryGridModel>>,
        val artistsSearchState: SearchScreen.State<ArtistSearchScreen.ArtistColumn>,
        val artistsSortOption: MutableStateFlow<ArtistSearchSortOption>,
        val artistsSortAscending: MutableStateFlow<Boolean>,
        val artistsUnfilteredCount: StateFlow<Int>,
        val ralliesEntries: Flow<PagingData<StampRallyEntryGridModel>>,
        val ralliesSearchState: SearchScreen.State<StampRallySearchScreen.StampRallyColumn>,
        val ralliesSortOption: MutableStateFlow<StampRallySearchSortOption>,
        val ralliesSortAscending: MutableStateFlow<Boolean>,
        val ralliesUnfilteredCount: StateFlow<Int>,
        val seriesEntries: Flow<PagingData<SeriesWithUserData>>,
        val merchEntries: Flow<PagingData<MerchWithUserData>>,
    )

    sealed interface Event {
        data class SearchEvent(val event: SearchScreen.Event<*>) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenMerch(val merch: String) : Event
        data object NavigateToArtists : Event
        data object NavigateToRallies : Event
        data object NavigateToSeries : Event
        data object NavigateToMerch : Event
        data class SeriesFavoriteToggle(
            val series: SeriesWithUserData,
            val favorite: Boolean,
        ) : Event

        data class MerchFavoriteToggle(
            val merch: MerchWithUserData,
            val favorite: Boolean,
        ) : Event
    }
}
