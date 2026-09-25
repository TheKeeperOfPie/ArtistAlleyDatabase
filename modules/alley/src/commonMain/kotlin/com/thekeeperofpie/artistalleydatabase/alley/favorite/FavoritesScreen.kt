package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.createSavedStateHandle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_con_upcoming_show_qr
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
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreenContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplayTypeSearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.QrCode2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
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
        graph: ArtistAlleyGraph,
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
        onOpenArtist: (ArtistEntry, Int) -> Unit,
        onOpenArtistImageFullscreen: (
            ArtistEntryGridModel,
            imageIndex: Int,
            showOutdatedCatalogs: Boolean,
        ) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenStampRally: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenStampRallyImageFullscreen: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenExport: (DataYear) -> Unit,
        onOpenFavoriteArtistsChangelog: (DataYear) -> Unit,
        onOpenFavoriteStampRalliesChangelog: (DataYear) -> Unit,
        onOpenFavoriteSeriesChangelog: (DataYear) -> Unit,
        onOpenFavoriteMerchChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        viewModel: FavoritesViewModel = assistedMetroViewModel<FavoritesViewModel, FavoritesViewModel.Factory> {
            create(it.createSavedStateHandle())
        },
    ) {
        val series by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        val showOutdatedCatalogs by viewModel.showOutdatedCatalogs.collectAsStateWithLifecycle()
        val seriesAutocompleteResults by viewModel.seriesAutocompleteResults.collectAsStateWithLifecycle()
        FavoritesScreen(
            state = remember(viewModel) {
                State(
                    randomSeed = viewModel.randomSeed,
                    tab = viewModel.tab,
                    query = viewModel.query,
                    displayType = viewModel.displayType,
                    year = viewModel.year,
                    artistsEntries = viewModel.artistEntries,
                    artistsSortFilterState = viewModel.artistSortFilterController.state,
                    artistsUnfilteredCount = viewModel.artistsUnfilteredCount,
                    ralliesEntries = viewModel.stampRallyEntries,
                    ralliesSearchState = viewModel.stampRallySearchState,
                    ralliesUnfilteredCount = viewModel.stampRallyUnfilteredCount,
                    seriesEntries = viewModel.seriesEntries,
                    merchEntries = viewModel.merchEntries,
                )
            },
            series = { series },
            stampRallySortFilterState = viewModel.stampRallySortFilterController.state,
            artistsScrollStateSaver = artistsScrollStateSaver,
            ralliesScrollStateSaver = ralliesScrollStateSaver,
            seriesScrollStateSaver = seriesScrollStateSaver,
            merchScrollStateSaver = merchScrollStateSaver,
            seriesImage = viewModel::seriesImage,
            seriesImageInfo = viewModel::seriesImageInfo,
            seriesAutocompleteResults = { seriesAutocompleteResults },
            eventSink = {
                viewModel.onEvent(
                    event = it,
                    onNavigateToArtists = onNavigateToArtists,
                    onNavigateToRallies = onNavigateToRallies,
                    onNavigateToSeries = onNavigateToSeries,
                    onNavigateToMerch = onNavigateToMerch,
                    onOpenArtist = onOpenArtist,
                    onOpenArtistImageFullscreen = { entry, imageIndex ->
                        onOpenArtistImageFullscreen(entry, imageIndex, showOutdatedCatalogs)
                    },
                    onOpenMerch = onOpenMerch,
                    onOpenSeries = onOpenSeries,
                    onOpenStampRally = onOpenStampRally,
                    onOpenStampRallyImageFullscreen = onOpenStampRallyImageFullscreen,
                    onOpenExport = onOpenExport,
                    onOpenFavoriteArtistsChangelog = onOpenFavoriteArtistsChangelog,
                    onOpenFavoriteStampRalliesChangelog = onOpenFavoriteStampRalliesChangelog,
                    onOpenFavoriteSeriesChangelog = onOpenFavoriteSeriesChangelog,
                    onOpenFavoriteMerchChangelog = onOpenFavoriteMerchChangelog,
                    onOpenSettings = onOpenSettings,
                )
            },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        series: () -> Map<String, GetSeriesTitles>,
        stampRallySortFilterState: SortFilterState<StampRallySortFilterController.FilterParams>,
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        seriesImage: (SeriesInfo) -> String?,
        seriesImageInfo: (SeriesImageInfo) -> String?,
        seriesAutocompleteResults: () -> List<SeriesInfo>,
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

            var tab by state.tab.collectAsMutableStateWithLifecycle()
            val artistsEntries = state.artistsEntries.collectAsLazyPagingItems()

            val dataYearHeaderState = rememberDataYearHeaderState(state.year, null)
            val entries = artistsEntries
            if (tab == EntryTab.ARTISTS) {
                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
                SortFilterBottomScaffold2(
                    scaffoldState = scaffoldState,
                    topBar = {
                        val title = stringResource(Res.string.alley_favorites_search)
                        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                            DisplayTypeSearchBar(
                                onClickBack = null,
                                query = state.query,
                                title = { title },
                                itemCount = { entries.itemCount },
                                displayType = state.displayType,
                                modifier = Modifier
                                    .animateEnterExit(
                                        enter = slideInVertically { -it },
                                        exit = slideOutVertically { -it },
                                    )
                                    .renderMaybeInSharedTransitionScopeOverlay(1f)
                            )
                        }
                    },
                    sheetContent = {
                        ArtistSortFilterSheetContent(
                            state = state.artistsSortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                            seriesImage = seriesImage,
                            seriesAutocompleteResults = seriesAutocompleteResults,
                            showHideFavorited = false,
                        )
                    },
                    modifier = Modifier
                        .conditionallyNonNull(scrollBehavior) {
                            nestedScroll(
                                NestedScrollSplitter(
                                    primary = it.nestedScrollConnection,
                                    consumeNone = true,
                                )
                            )
                        }
                ) {
                    ArtistContent(
                        state = state,
                        gridState = artistsScrollStateSaver.lazyStaggeredGridState(),
                        sortFilterState = state.artistsSortFilterState,
                        entries = artistsEntries,
                        series = series,
                        eventSink = eventSink,
                        header = {
                            Header(
                                tab = { tab },
                                onTabChange = { tab = it },
                                dataYearHeaderState = dataYearHeaderState,
                                eventSink = eventSink,
                            )
                        },
                        noResultsItem = { NoResultsItem(EntryTab.ARTISTS, eventSink) },
                        modifier = Modifier.padding(top = it.calculateTopPadding())
                    )
                }
            } else {
                LegacyScaffold(
                    state = state,
                    scaffoldState = scaffoldState,
                    dataYearHeaderState = dataYearHeaderState,
                    stampRallySortFilterState = stampRallySortFilterState,
                    ralliesScrollStateSaver = ralliesScrollStateSaver,
                    seriesScrollStateSaver = seriesScrollStateSaver,
                    merchScrollStateSaver = merchScrollStateSaver,
                    seriesImageInfo = seriesImageInfo,
                    eventSink = eventSink,
                )
            }
        }
    }

    @Composable
    private fun LegacyScaffold(
        state: State,
        scaffoldState: BottomSheetScaffoldState,
        dataYearHeaderState: DataYearHeaderState,
        stampRallySortFilterState: SortFilterState<StampRallySortFilterController.FilterParams>,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        seriesImageInfo: (SeriesImageInfo) -> String?,
        eventSink: (Event) -> Unit,
    ) {
        var tab by state.tab.collectAsMutableStateWithLifecycle()
        if (tab == EntryTab.ARTISTS) throw IllegalStateException()
        val ralliesEntries = state.ralliesEntries.collectAsLazyPagingItems()
        val seriesEntries = state.seriesEntries.collectAsLazyPagingItems()
        val merchEntries = state.merchEntries.collectAsLazyPagingItems()
        val entries = when (tab) {
            EntryTab.ARTISTS -> throw IllegalStateException()
            EntryTab.RALLIES -> ralliesEntries
            EntryTab.SERIES -> seriesEntries
            EntryTab.MERCH -> merchEntries
        }
        Box {
            var horizontalScrollBarWidth by remember { mutableStateOf(0) }
            val horizontalScrollState = rememberScrollState()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            SortFilterBottomScaffold(
                state = when (tab) {
                    EntryTab.ARTISTS -> throw IllegalStateException()
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
                when (tab) {
                    EntryTab.ARTISTS -> throw IllegalStateException()
                    EntryTab.RALLIES ->
                        RallyContent(
                            state = state,
                            gridState = ralliesScrollStateSaver.lazyStaggeredGridState(),
                            searchState = state.ralliesSearchState,
                            horizontalScrollState = horizontalScrollState,
                            entries = ralliesEntries,
                            eventSink = eventSink,
                            scaffoldPadding = PaddingValues(top = it.calculateTopPadding()),
                            onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                            onUnfavorite = {
                                if (it != null) {
                                    eventSink(
                                        Event.SearchEvent(
                                            SearchScreen.Event.FavoriteToggle(
                                                entry = it,
                                                favorite = false
                                            )
                                        )
                                    )
                                }
                            },
                            header = {
                                Header(
                                    tab = { tab },
                                    onTabChange = { tab = it },
                                    dataYearHeaderState = dataYearHeaderState,
                                    eventSink = eventSink,
                                )
                            },
                            seriesImage = seriesImageInfo,
                            noResultsItem = { NoResultsItem(EntryTab.RALLIES, eventSink) },
                        )
                    EntryTab.SERIES -> SeriesContent(
                        listState = seriesScrollStateSaver.lazyListState(),
                        series = seriesEntries,
                        getSeriesImage = seriesImageInfo,
                        header = {
                            Header(
                                tab = { tab },
                                onTabChange = { tab = it },
                                dataYearHeaderState = dataYearHeaderState,
                                eventSink = eventSink,
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
                                eventSink = eventSink,
                            )
                        },
                        eventSink = eventSink,
                    )
                }
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

    @Composable
    private fun ArtistContent(
        state: State,
        sortFilterState: ArtistSortFilterState,
        gridState: LazyStaggeredGridState,
        entries: LazyPagingItems<ArtistEntryGridModel>,
        series: () -> Map<String, GetSeriesTitles>,
        eventSink: (Event) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val unfilteredCount by state.artistsUnfilteredCount.collectAsStateWithLifecycle()
        val displayType by state.displayType.collectAsStateWithLifecycle()
        ArtistSearchScreenContent(
            sortFilterState = sortFilterState,
            gridState = gridState,
            header = header,
            entries = entries,
            series = series,
            unfilteredCount = { unfilteredCount },
            displayType = { displayType },
            eventSink = { eventSink(Event.ArtistSearchEvent(it)) },
            noResultsItem = noResultsItem,
            modifier = modifier,
        )
    }

    @Composable
    private fun RallyContent(
        state: State,
        gridState: LazyStaggeredGridState,
        searchState: SearchScreen.State<StampRallySearchScreen.StampRallyColumn>,
        horizontalScrollState: ScrollState,
        entries: LazyPagingItems<StampRallyEntryGridModel>,
        seriesImage: (SeriesImageInfo) -> String?,
        eventSink: (Event) -> Unit,
        scaffoldPadding: PaddingValues,
        onHorizontalScrollBarWidth: (Int) -> Unit,
        onUnfavorite: (SearchScreen.SearchEntryModel?) -> Unit,
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
            showOutdatedCatalogs = { false },
            header = header,
            noResultsItem = noResultsItem,
            itemRow = { entry, onFavoriteToggle, modifier ->
                StampRallyListRow(
                    entry = entry,
                    onFavoriteToggle = {
                        if (it) {
                            onFavoriteToggle(it)
                        } else {
                            onUnfavorite(entry)
                        }
                    },
                    seriesImage = seriesImage,
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
        getSeriesImage: (SeriesImageInfo) -> String?,
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
                    if (series.loadState.refresh is LoadState.Loading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                InfiniteProgressIndicator()
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
                                    data?.let { getSeriesImage(it.series.toImageInfo()) }
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
        val scrollAreaState = rememberScrollAreaState(listState)
        ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize()) {
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
                    if (merch.loadState.refresh is LoadState.Loading) {
                        item("loadingIndicator") {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                InfiniteProgressIndicator()
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

            PrimaryVerticalScrollbar(listState)
        }
    }

    @Composable
    private fun Header(
        tab: () -> EntryTab,
        onTabChange: (EntryTab) -> Unit,
        dataYearHeaderState: DataYearHeaderState,
        eventSink: (Event) -> Unit,
    ) {
        Column {
            DataYearHeader(
                state = dataYearHeaderState,
                onOpenChangelog = { eventSink(Event.OpenChangelog) },
                onOpenSettings = { eventSink(Event.OpenSettings) },
            ) {
                IconButton(onClick = { eventSink(Event.OpenExport(dataYearHeaderState.year)) }) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = stringResource(Res.string.alley_con_upcoming_show_qr),
                    )
                }
            }
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
        val displayType: MutableStateFlow<SearchDisplayType>,
        val year: MutableStateFlow<DataYear>,
        val artistsEntries: Flow<PagingData<ArtistEntryGridModel>>,
        val artistsSortFilterState: ArtistSortFilterState,
        val artistsUnfilteredCount: StateFlow<Int>,
        val ralliesEntries: Flow<PagingData<StampRallyEntryGridModel>>,
        val ralliesSearchState: SearchScreen.State<StampRallySearchScreen.StampRallyColumn>,
        val ralliesUnfilteredCount: StateFlow<Int>,
        val seriesEntries: Flow<PagingData<SeriesWithUserData>>,
        val merchEntries: Flow<PagingData<MerchWithUserData>>,
    )

    sealed interface Event {
        data class SearchEvent(val event: SearchScreen.Event<*>) : Event
        data class ArtistSearchEvent(val event: ArtistSearchScreen.Event) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenMerch(val merch: String) : Event
        data class OpenExport(val dataYear: DataYear) : Event
        data object OpenChangelog : Event
        data object OpenSettings : Event
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
