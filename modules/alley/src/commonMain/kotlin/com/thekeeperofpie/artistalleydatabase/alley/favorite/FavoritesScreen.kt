package com.thekeeperofpie.artistalleydatabase.alley.favorite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import artistalleydatabase.modules.alley.generated.resources.alley_favorites_search
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_artists
import artistalleydatabase.modules.alley.generated.resources.alley_nav_bar_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_no
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_text
import artistalleydatabase.modules.alley.generated.resources.alley_unfavorite_dialog_yes
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.PlatformSpecificConfig
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchSortOption
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
object FavoritesScreen {

    @Composable
    operator fun invoke(
        favoritesViewModel: FavoritesViewModel,
        artistSortViewModel: ArtistSortFilterViewModel,
        stampRallySortViewModel: StampRallySortFilterViewModel,
        scrollStateSaver: ScrollStateSaver,
    ) {
        val navigationController = LocalNavigationController.current
        FavoritesScreen(
            state = remember(favoritesViewModel) {
                State(
                    randomSeed = favoritesViewModel.randomSeed,
                    query = favoritesViewModel.query,
                    displayType = favoritesViewModel.displayType,
                    year = favoritesViewModel.year,
                    artistsEntries = favoritesViewModel.artistEntries,
                    artistsSearchState = favoritesViewModel.artistSearchState,
                    artistsSortOption = artistSortViewModel.sortOption,
                    artistsSortAscending = artistSortViewModel.sortAscending,
                    ralliesEntries = favoritesViewModel.stampRallyEntries,
                    ralliesSearchState = favoritesViewModel.stampRallySearchState,
                    ralliesSortOption = stampRallySortViewModel.sortOption,
                    ralliesSortAscending = stampRallySortViewModel.sortAscending,
                )
            },
            artistSortViewModel.state,
            stampRallySortViewModel.state,
            scrollStateSaver = scrollStateSaver,
            eventSink = { favoritesViewModel.onEvent(navigationController, it) },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        artistSortFilterState: SortFilterState<*>,
        stampRallySortFilterState: SortFilterState<*>,
        scrollStateSaver: ScrollStateSaver,
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
                .takeUnless { PlatformSpecificConfig.scrollbarsAlwaysVisible }
            val gridState = scrollStateSaver.lazyStaggeredGridState()

            var tab by rememberSaveable { mutableStateOf(EntryTab.ARTISTS) }
            val artistsEntries = state.artistsEntries.collectAsLazyPagingItemsWithLifecycle()
            val ralliesEntries = state.ralliesEntries.collectAsLazyPagingItemsWithLifecycle()
            val entries = when (tab) {
                EntryTab.ARTISTS -> artistsEntries
                EntryTab.RALLIES -> ralliesEntries
            }
            val searchState = when (tab) {
                EntryTab.ARTISTS -> state.artistsSearchState
                EntryTab.RALLIES -> state.ralliesSearchState
            }

            val dataYearHeaderState = rememberDataYearHeaderState(state.year, null)

            Box {
                var horizontalScrollBarWidth by remember { mutableStateOf(0) }
                val horizontalScrollState = rememberScrollState()
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = 72.dp,
                    sheetDragHandle = {
                        BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
                    },
                    sheetContent = {
                        val sortFilterState = when (tab) {
                            EntryTab.ARTISTS -> artistSortFilterState
                            EntryTab.RALLIES -> stampRallySortFilterState
                        }
                        SortFilterOptionsPanel(
                            state = sortFilterState,
                            showClear = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 320.dp)
                        )
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
                    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                        var topBarHeight by remember { mutableStateOf(0) }
                        val displayType by state.displayType.collectAsStateWithLifecycle()
                        Scaffold(
                            topBar = {
                                val title = stringResource(Res.string.alley_favorites_search)
                                SearchScreen.TopBar(
                                    query = state.query,
                                    displayType = state.displayType,
                                    entries = entries,
                                    scrollBehavior = scrollBehavior,
                                    onClickBack = null,
                                    onHeightChanged = { topBarHeight = it },
                                    title = { title },
                                    actions = null,
                                )
                            },
                            modifier = Modifier
                                .conditionally(displayType != DisplayType.TABLE) {
                                    widthIn(max = 1200.dp)
                                }
                        ) {
                            var unfavoriteDialogEntry by remember {
                                mutableStateOf<SearchScreen.SearchEntryModel?>(null)
                            }
                            val query by state.query.collectAsStateWithLifecycle()
                            SearchScreen.Content(
                                state = searchState,
                                eventSink = { eventSink(Event.SearchEvent(it)) },
                                entries = entries,
                                scrollBehavior = scrollBehavior,
                                horizontalScrollState = horizontalScrollState,
                                gridState = gridState,
                                scaffoldPadding = it,
                                topBarHeight = { topBarHeight },
                                onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                                shouldShowCount = { query.isNotEmpty() },
                                itemToSharedElementId = {
                                    (it as? ArtistEntryGridModel)?.id?.scopedId
                                        ?: (it as? StampRallyEntryGridModel)?.id?.scopedId
                                        ?: Unit
                                },
                                header = {
                                    Column {
                                        DataYearHeader(dataYearHeaderState)
                                        TabRow(EntryTab.entries.indexOf(tab)) {
                                            EntryTab.entries.forEach {
                                                Tab(
                                                    selected = tab == it,
                                                    text = { Text(text = stringResource(it.text)) },
                                                    onClick = { tab = it },
                                                )
                                            }
                                        }
                                    }
                                },
                                itemRow = { entry, onFavoriteToggle, modifier ->
                                    if (entry is ArtistEntryGridModel) {
                                        ArtistListRow(
                                            entry = entry,
                                            onFavoriteToggle = {
                                                if (it) {
                                                    onFavoriteToggle(it)
                                                } else {
                                                    unfavoriteDialogEntry = entry
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
                                    } else if (entry is StampRallyEntryGridModel) {
                                        StampRallyListRow(entry, onFavoriteToggle, modifier)
                                    }
                                },
                                columnHeader = {
                                    if (it is ArtistSearchScreen.ArtistColumn) {
                                        ArtistSearchScreen.ColumnHeader(
                                            column = it,
                                            sortOption = state.artistsSortOption,
                                            sortAscending = state.artistsSortAscending,
                                        )
                                    } else if (it is StampRallySearchScreen.StampRallyColumn) {
                                        StampRallySearchScreen.ColumnHeader(it)
                                    }
                                },
                                tableCell = { row, column ->
                                    if (row is ArtistEntryGridModel &&
                                        column is ArtistSearchScreen.ArtistColumn
                                    ) {
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
                                    } else if (row is StampRallyEntryGridModel &&
                                        column is StampRallySearchScreen.StampRallyColumn
                                    ) {
                                        StampRallySearchScreen.TableCell(row, column)
                                    }
                                },
                            )

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
    }

    @Composable
    private fun UnfavoriteDialog(
        entry: () -> SearchScreen.SearchEntryModel?,
        onClearEntry: () -> Unit,
        onRemoveFavorite: (SearchScreen.SearchEntryModel) -> Unit,
    ) {
        val entry = entry()
        if (entry != null) {
            val name = when (entry) {
                is ArtistEntryGridModel -> entry.artist.name
                is StampRallyEntryGridModel ->
                    "${entry.stampRally.hostTable}-${entry.stampRally.fandom}"
                else -> throw IllegalArgumentException()
            }
            AlertDialog(
                onDismissRequest = onClearEntry,
                text = {
                    Text(text = stringResource(Res.string.alley_unfavorite_dialog_text, name))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRemoveFavorite(entry)
                            onClearEntry()
                        },
                    ) {
                        Text(stringResource(Res.string.alley_unfavorite_dialog_yes))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onClearEntry) {
                        Text(stringResource(Res.string.alley_unfavorite_dialog_no))
                    }
                },
            )
        }
    }

    private enum class EntryTab(val text: StringResource) {
        ARTISTS(Res.string.alley_nav_bar_artists),
        RALLIES(Res.string.alley_nav_bar_stamp_rallies),
    }

    @Stable
    class State(
        val randomSeed: Int,
        val query: MutableStateFlow<String>,
        val displayType: MutableStateFlow<DisplayType>,
        val year: MutableStateFlow<DataYear>,
        val artistsEntries: Flow<PagingData<ArtistEntryGridModel>>,
        val artistsSearchState: SearchScreen.State<ArtistSearchScreen.ArtistColumn>,
        val artistsSortOption: MutableStateFlow<ArtistSearchSortOption>,
        val artistsSortAscending: MutableStateFlow<Boolean>,
        val ralliesEntries: Flow<PagingData<StampRallyEntryGridModel>>,
        val ralliesSearchState: SearchScreen.State<StampRallySearchScreen.StampRallyColumn>,
        val ralliesSortOption: MutableStateFlow<StampRallySearchSortOption>,
        val ralliesSortAscending: MutableStateFlow<Boolean>,
    )

    sealed interface Event {
        data class SearchEvent(val event: SearchScreen.Event<*>) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenMerch(val merch: String) : Event
    }
}
