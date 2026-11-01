package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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
import com.composeunstyled.rememberScrollbarState
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreenContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreenContent
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.series.search.SeriesSortFilterState
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.AlleySearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.QrCode2
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.utils_scrollbars.VerticalScrollableWithBar
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
object FavoritesScreen {

    @Composable
    operator fun invoke(
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        onNavigateToArtists: () -> Unit,
        onNavigateToRallies: () -> Unit,
        onNavigateToSeries: () -> Unit,
        onNavigateToMerch: () -> Unit,
        scaffoldState: SortFilterBottomScaffoldState = rememberSortFilterBottomScaffoldState(),
        viewModel: FavoritesViewModel = assistedMetroViewModel<FavoritesViewModel, FavoritesViewModel.Factory> {
            create(it.createSavedStateHandle())
        },
    ) {
        val series by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        val seriesAutocompleteResults by viewModel.seriesAutocompleteResults.collectAsStateWithLifecycle()
        FavoritesScreen(
            state = remember(viewModel) {
                State(
                    randomSeed = viewModel.randomSeed,
                    tab = viewModel.tab,
                    query = viewModel.query,
                    displayType = { viewModel.settings.displayType },
                    year = viewModel::year,
                    onYearChange = { viewModel.year = it },
                    artistsEntries = viewModel.artistEntries,
                    artistsSortFilterState = viewModel.artistSortFilterController.state,
                    artistsUnfilteredCount = viewModel::artistsUnfilteredCount,
                    ralliesEntries = viewModel.stampRallyEntries,
                    ralliesSortFilterState = viewModel.stampRallySortFilterController.state,
                    ralliesUnfilteredCount = viewModel::stampRallyUnfilteredCount,
                    seriesEntries = viewModel.seriesEntries,
                    seriesSortFilterState = viewModel.seriesSortFilterController.state,
                    merchEntries = viewModel.merchEntries,
                )
            },
            scaffoldState = scaffoldState,
            series = { series },
            artistsScrollStateSaver = artistsScrollStateSaver,
            ralliesScrollStateSaver = ralliesScrollStateSaver,
            seriesScrollStateSaver = seriesScrollStateSaver,
            merchScrollStateSaver = merchScrollStateSaver,
            seriesAutocompleteResults = { seriesAutocompleteResults },
            eventSink = {
                viewModel.onEvent(
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
        series: () -> Map<String, GetSeriesTitles>,
        artistsScrollStateSaver: ScrollStateSaver,
        ralliesScrollStateSaver: ScrollStateSaver,
        seriesScrollStateSaver: ScrollStateSaver,
        merchScrollStateSaver: ScrollStateSaver,
        seriesAutocompleteResults: () -> List<SeriesInfo>,
        eventSink: (Event) -> Unit,
        scaffoldState: SortFilterBottomScaffoldState = rememberSortFilterBottomScaffoldState(),
    ) {
        CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
            val scope = rememberCoroutineScope()
            BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }

            var tab by state.tab.collectAsMutableStateWithLifecycle()
            val artistsEntries = state.artistsEntries.collectAsLazyPagingItems()
            val ralliesEntries = state.ralliesEntries.collectAsLazyPagingItems()
            val seriesEntries = state.seriesEntries.collectAsLazyPagingItems()
            val merchEntries = state.merchEntries.collectAsLazyPagingItems()

            val dataYearHeaderState = rememberDataYearHeaderState(
                dataYear = state.year,
                onDataYearChange = state.onYearChange,
                lockedYear = null,
            )
            val entries = when (tab) {
                EntryTab.ARTISTS -> artistsEntries
                EntryTab.RALLIES -> ralliesEntries
                EntryTab.SERIES -> seriesEntries
                EntryTab.MERCH -> merchEntries
            }
            SortFilterBottomScaffold2(
                state = scaffoldState,
                enterAlwaysTopBar = {
                    val title = stringResource(Res.string.alley_favorites_search)
                    AlleySearchBar(
                        onClickBack = null,
                        query = state.query,
                        title = { title },
                        itemCount = { entries.itemCount },
                        onClickFilter = { scope.launch { scaffoldState.bottomSheetState.toggle() } },
                        modifier = Modifier
                            .animateEnterExit(
                                enter = slideInVertically { -it },
                                exit = slideOutVertically { -it },
                            )
                            .renderMaybeInSharedTransitionScopeOverlay(1f)
                    )
                },
                sheetContent = {
                    when (tab) {
                        EntryTab.ARTISTS -> ArtistSortFilterSheetContent(
                            state = state.artistsSortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                            seriesAutocompleteResults = seriesAutocompleteResults,
                            showHideFavorited = false,
                        )
                        EntryTab.RALLIES -> StampRallySortFilterSheetContent(
                            state = state.ralliesSortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                            seriesAutocompleteResults = seriesAutocompleteResults,
                            showHideFavorited = false,
                        )
                        EntryTab.SERIES -> SeriesSortFilterSheetContent(
                            state = state.seriesSortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                        )
                        // TODO: Real merch sort filter?
                        EntryTab.MERCH -> Box {}
                    }
                },
            ) { _, contentPadding ->
                when (tab) {
                    EntryTab.ARTISTS ->
                        ArtistContent(
                            state = state,
                            gridState = artistsScrollStateSaver.lazyStaggeredGridState(),
                            baseContentPadding = contentPadding,
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
                        )
                    EntryTab.RALLIES ->
                        RalliesContent(
                            state = state,
                            gridState = artistsScrollStateSaver.lazyStaggeredGridState(),
                            baseContentPadding = contentPadding,
                            sortFilterState = state.ralliesSortFilterState,
                            entries = ralliesEntries,
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
                        )
                    EntryTab.SERIES -> SeriesContent(
                        listState = seriesScrollStateSaver.lazyListState(),
                        baseContentPadding = contentPadding,
                        series = seriesEntries,
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
                        baseContentPadding = contentPadding,
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
        }
    }

    @Composable
    private fun ArtistContent(
        state: State,
        sortFilterState: ArtistSortFilterState,
        gridState: LazyStaggeredGridState,
        baseContentPadding: () -> PaddingValues,
        entries: LazyPagingItems<ArtistEntryGridModel>,
        series: () -> Map<String, GetSeriesTitles>,
        eventSink: (Event) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        ArtistSearchScreenContent(
            sortFilterState = sortFilterState,
            baseContentPadding = baseContentPadding,
            gridState = gridState,
            header = header,
            entries = entries,
            series = series,
            unfilteredCount = state.artistsUnfilteredCount,
            displayType = state.displayType,
            eventSink = { eventSink(Event.ArtistSearchEvent(it)) },
            noResultsItem = noResultsItem,
            modifier = modifier,
        )
    }

    @Composable
    private fun RalliesContent(
        state: State,
        sortFilterState: StampRallySortFilterState,
        gridState: LazyStaggeredGridState,
        baseContentPadding: () -> PaddingValues,
        entries: LazyPagingItems<StampRallyWithUserData>,
        eventSink: (Event) -> Unit,
        header: @Composable () -> Unit,
        noResultsItem: @Composable () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        StampRallySearchScreenContent(
            sortFilterState = sortFilterState,
            gridState = gridState,
            baseContentPadding = baseContentPadding,
            header = header,
            entries = entries,
            unfilteredCount = state.ralliesUnfilteredCount,
            displayType = state.displayType,
            eventSink = { eventSink(Event.StampRallySearchEvent(it)) },
            noResultsItem = noResultsItem,
            modifier = modifier,
        )
    }

    @Composable
    private fun SeriesContent(
        listState: LazyListState,
        baseContentPadding: () -> PaddingValues,
        series: LazyPagingItems<SeriesWithUserData>,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        VerticalScrollableWithBar(
            scrollbarState = rememberScrollbarState(listState),
        ) { horizontalContentPadding ->
            LazyColumn(
                state = listState,
                contentPadding = baseContentPadding() + PaddingValues(
                    start = horizontalContentPadding,
                    end = horizontalContentPadding,
                    bottom = 80.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
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
        }
    }

    @Composable
    private fun MerchContent(
        listState: LazyListState,
        baseContentPadding: () -> PaddingValues,
        merch: LazyPagingItems<MerchWithUserData>,
        header: @Composable () -> Unit,
        eventSink: (Event) -> Unit,
    ) {
        VerticalScrollableWithBar(
            scrollbarState = rememberScrollbarState(listState),
        ) { horizontalContentPadding ->
            LazyColumn(
                state = listState,
                contentPadding = baseContentPadding() + PaddingValues(
                    start = horizontalContentPadding,
                    end = horizontalContentPadding,
                    bottom = 80.dp
                ),
                modifier = Modifier.fillMaxSize()
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
        val query: TextFieldState,
        val displayType: () -> SearchDisplayType,
        val year: () -> DataYear,
        val onYearChange: (DataYear) -> Unit,
        val artistsEntries: Flow<PagingData<ArtistEntryGridModel>>,
        val artistsSortFilterState: ArtistSortFilterState,
        val artistsUnfilteredCount: () -> Int,
        val ralliesEntries: Flow<PagingData<StampRallyWithUserData>>,
        val ralliesSortFilterState: StampRallySortFilterState,
        val ralliesUnfilteredCount: () -> Int,
        val seriesEntries: Flow<PagingData<SeriesWithUserData>>,
        val seriesSortFilterState: SeriesSortFilterState,
        val merchEntries: Flow<PagingData<MerchWithUserData>>,
    )

    sealed interface Event {
        data class ArtistSearchEvent(val event: ArtistSearchScreen.Event) : Event
        data class StampRallySearchEvent(val event: StampRallySearchScreen.Event) : Event
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
