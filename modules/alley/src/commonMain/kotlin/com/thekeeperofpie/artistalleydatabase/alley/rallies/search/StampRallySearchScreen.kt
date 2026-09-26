package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_search_title_results_suffix
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen.Event
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchList
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchMoreResults
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplayTypeSearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallySearchScreen(
    lockedYear: DataYear?,
    lockedSeries: String?,
    scrollStateSaver: ScrollStateSaver,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    viewModel: StampRallySearchViewModel = assistedMetroViewModel<StampRallySearchViewModel, StampRallySearchViewModel.Factory> {
        create(
            lockedYear = lockedYear,
            lockedSeries = lockedSeries,
            savedStateHandle = it.createSavedStateHandle(),
        )
    },
) {
    val sortFilterController = viewModel.sortFilterController
    val state = remember(viewModel, sortFilterController) {
        StampRallySearchScreen.State(viewModel, sortFilterController)
    }
    val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
    val seriesAutocompleteResults by viewModel.seriesAutocompleteResults.collectAsStateWithLifecycle()
    StampRallySearchScreen(
        state = state,
        eventSink = viewModel::onEvent,
        header = {
            BottomSheetFilterDataYearHeader(
                dataYearHeaderState = dataYearHeaderState,
                scaffoldState = scaffoldState,
                onOpenChangelog = { viewModel.onEvent(Event.OpenChangelog) },
                onOpenSettings = { viewModel.onEvent(Event.OpenSettings) },
            )
        },
        scaffoldState = scaffoldState,
        scrollStateSaver = scrollStateSaver,
        seriesAutocompleteResults = { seriesAutocompleteResults },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StampRallySearchScreen(
    state: StampRallySearchScreen.State,
    eventSink: (Event) -> Unit,
    header: @Composable () -> Unit,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    scrollStateSaver: ScrollStateSaver,
    seriesAutocompleteResults: () -> List<SeriesInfo>,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val gridState = scrollStateSaver.lazyStaggeredGridState()
    // TODO
//    sortFilterState.ImmediateScrollResetEffect(gridState)

    CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
        val lockedSeriesEntry by state.lockedSeriesEntry.collectAsStateWithLifecycle()
        val entries = state.results.collectAsLazyPagingItems()
        val unfilteredCount by state.unfilteredCount.collectAsStateWithLifecycle()
        val count = entries.itemCount
        val title = lockedSeriesEntry?.name(LocalLanguageOptionMedia.current)
            ?.let {
                @Suppress("USELESS_IS_CHECK")
                if (entries.loadState.refresh is LoadState.Loading) {
                    it
                } else {
                    pluralStringResource(
                        Res.plurals.alley_search_title_results_suffix,
                        count,
                        it,
                        count,
                    )
                }
            }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
        SortFilterBottomScaffold2(
            scaffoldState = scaffoldState,
            topBar = {
                DisplayTypeSearchBar(
                    onClickBack = { eventSink(Event.Back) },
                    query = state.query,
                    displayType = state.displayType,
                    itemCount = { entries.itemCount },
                    title = { title },
                    actions = actions,
                    modifier = Modifier
                        .animateEnterExit(
                            enter = slideInVertically { -it },
                            exit = slideOutVertically { -it },
                        )
                        .renderMaybeInSharedTransitionScopeOverlay(1f)
                )
            },
            sheetContent = {
                StampRallySortFilterSheetContent(
                    state = state.sortFilterState,
                    sheetState = scaffoldState.bottomSheetState,
                    seriesAutocompleteResults = seriesAutocompleteResults
                )
            },
            // TODO: This breaks vertical scrolling with 1.12.0-beta03+
//                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
        ) {
            val displayType by state.displayType.collectAsStateWithLifecycle()
            StampRallySearchScreenContent(
                sortFilterState = state.sortFilterState,
                header = header,
                entries = entries,
                unfilteredCount = { unfilteredCount },
                displayType = { displayType },
                eventSink = eventSink,
                gridState = gridState,
            )
        }
    }
}


@Composable
internal fun StampRallySearchScreenContent(
    sortFilterState: StampRallySortFilterState,
    header: @Composable () -> Unit,
    entries: LazyPagingItems<StampRallyWithUserData>,
    unfilteredCount: () -> Int,
    displayType: () -> SearchDisplayType,
    eventSink: (Event) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    noResultsItem: (@Composable () -> Unit)? = null,
) {
    val showGridByDefault by sortFilterState.persistentState.showGridByDefault
        .collectAsMutableStateWithLifecycle()
    val showRandomCatalogImage by sortFilterState.persistentState.showRandomCatalogImage
        .collectAsMutableStateWithLifecycle()
    val displayType = displayType()
    if (displayType == SearchDisplayType.TABLE) {
        TwoWayGrid(
            columns = StampRallySearchColumn.entries,
            header = header,
            rows = entries,
            unfilteredCount = unfilteredCount,
            columnHeader = { StampRallySearchScreen.ColumnHeader(column = it) },
            tableCell = { row, column ->
                StampRallySearchScreen.TableCell(row = row, column = column)
            },
            noResultsHeader = noResultsItem,
            moreResultsFooter = {
                SearchMoreResults(
                    unfilteredCount = unfilteredCount,
                    itemCount = { entries.itemCount },
                    onClick = { eventSink(Event.ClearFilters) },
                )
            },
            modifier = modifier,
        )
    } else {
        val forceOneDisplayColumn by sortFilterState.persistentState.forceOneDisplayColumn.collectAsStateWithLifecycle()
        SearchList(
            entries = entries,
            itemToId = { it.stampRally.id },
            displayType = { displayType },
            forceOneDisplayColumn = { forceOneDisplayColumn },
            unfilteredCount = unfilteredCount,
            gridState = gridState,
            header = header,
            itemRow = { entry ->
                StampRallySearchItem(
                    displayType = displayType,
                    stampRallyWithUserData = entry,
                    showGridByDefault = showGridByDefault,
                    showRandomCatalogImage = showRandomCatalogImage,
                    blockCrossAxisScrolling = { gridState.isScrollInProgress },
                    onFavoriteToggle = { eventSink(Event.FavoriteToggle(entry, it)) },
                    onIgnoredToggle = { eventSink(Event.IgnoreToggle(entry, it)) },
                    onClick = { imageIndex ->
                        eventSink(
                            Event.OpenEntry(
                                entry,
                                imageIndex
                            )
                        )
                    },
                    onClickFullscreen = { imageIndex ->
                        eventSink(Event.OpenImageFullscreen(entry, imageIndex))
                    },
                )
            },
            noResultsItem = noResultsItem,
            moreResultsItem = {
                SearchMoreResults(
                    unfilteredCount = unfilteredCount,
                    itemCount = { entries.itemCount },
                    onClick = { eventSink(Event.ClearFilters) },
                )
            },
            modifier = modifier,
        )
    }
}

object StampRallySearchScreen {

    @Composable
    fun ColumnHeader(column: StampRallySearchColumn) {
        // TODO: Support sort
        AutoSizeText(
            text = stringResource(column.text),
            modifier = Modifier.requiredWidth(column.size)
                .then(TwoWayGrid.DefaultCellPaddingModifier)
        )
    }

    @Composable
    fun TableCell(row: StampRallyWithUserData?, column: StampRallySearchColumn) {
        when (column) {
            StampRallySearchColumn.BOOTH -> AutoSizeText(
                text = row?.stampRally?.hostTable.orEmpty(),
                modifier = Modifier.requiredSize(column.size)
                    .then(TwoWayGrid.DefaultCellPaddingModifier)
            )
            StampRallySearchColumn.FANDOM -> Text(
                text = row?.stampRally?.fandom.orEmpty(),
                modifier = TwoWayGrid.DefaultCellPaddingModifier
            )
        }
    }

    @Stable
    class State(
        val lockedSeriesEntry: StateFlow<SeriesInfo?>,
        val lockedYear: DataYear?,
        val randomSeed: Int,
        val year: MutableStateFlow<DataYear>,
        val query: MutableStateFlow<String>,
        val results: StateFlow<PagingData<StampRallyWithUserData>>,
        val unfilteredCount: StateFlow<Int>,
        val sortFilterState: StampRallySortFilterState,
        val displayType: MutableStateFlow<SearchDisplayType>,
    ) {
        constructor(
            viewModel: StampRallySearchViewModel,
            sortFilterController: StampRallySortFilterController,
        ) : this(
            lockedSeriesEntry = viewModel.lockedSeriesEntry,
            lockedYear = viewModel.lockedYear,
            randomSeed = viewModel.randomSeed,
            year = viewModel.dataYear,
            query = viewModel.query,
            results = viewModel.results,
            unfilteredCount = viewModel.unfilteredCount,
            sortFilterState = sortFilterController.state,
            displayType = viewModel.displayType,
        )
    }

    sealed interface Event {
        data class FavoriteToggle(val stampRally: StampRallyWithUserData, val favorite: Boolean) :
            Event

        data class IgnoreToggle(val stampRally: StampRallyWithUserData, val ignored: Boolean) : Event
        data class OpenEntry(val stampRally: StampRallyWithUserData, val imageIndex: Int) : Event
        data class OpenImageFullscreen(val stampRally: StampRallyWithUserData, val imageIndex: Int) :
            Event

        data object Back : Event
        data object ClearFilters : Event
        data object OpenChangelog : Event
        data object OpenSettings : Event
    }
}
