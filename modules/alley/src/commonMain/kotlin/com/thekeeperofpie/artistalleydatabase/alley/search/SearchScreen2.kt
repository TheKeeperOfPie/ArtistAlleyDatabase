package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_search_clear_filters
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.alley.generated.resources.alley_search_results_filtered_out
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.Event
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.SearchEntryModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.rememberScrollbarState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <EntryModel, ColumnType> SearchScreen2(
    state: SearchScreen.State<ColumnType>,
    eventSink: (Event<EntryModel>) -> Unit,
    entries: LazyPagingItems<EntryModel>,
    header: @Composable () -> Unit,
    itemRow: @Composable (DisplayType, entry: EntryModel) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    sortFilterState: SortFilterState<*>,
    topBar: @Composable () -> Unit = {},
    unfilteredCount: () -> Int = { 0 },
    columnHeader: @Composable (column: ColumnType) -> Unit = { TwoWayGrid.ColumnHeader(it) },
    tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
    noResultsItem: (@Composable () -> Unit)? = null,
    moreResultsItem: (@Composable () -> Unit) = {
        MoreResultsItem(
            unfilteredCount = unfilteredCount,
            itemCount = { entries.itemCount },
            onClick = { eventSink(Event.ClearFilters()) },
        )
    },
) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : TwoWayGrid.Column {
    val scope = rememberCoroutineScope()
    BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }
    SortFilterBottomScaffold(
        state = sortFilterState,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 72.dp,
        topBar = topBar,
        modifier = modifier,
        // TODO: This breaks vertical scrolling with 1.12.0-beta03+
//                .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
    ) {
        val displayType by state.displayType.collectAsStateWithLifecycle()
        if (displayType == DisplayType.TABLE) {
            TwoWayGrid(
                header = header,
                rows = entries,
                unfilteredCount = unfilteredCount,
                columns = state.columns,
                contentPadding = PaddingValues(bottom = 200.dp),
                columnHeader = columnHeader,
                tableCell = tableCell,
                noResultsHeader = noResultsItem,
                moreResultsFooter = moreResultsItem,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            VerticalGrid(
                state = state,
                header = header,
                entries = entries,
                unfilteredCount = unfilteredCount,
                gridState = gridState,
                itemRow = itemRow,
                noResultsItem = noResultsItem,
                moreResultsItem = moreResultsItem,
            )
        }
    }
}

@Composable
private fun <EntryModel : SearchEntryModel> VerticalGrid(
    state: SearchScreen.State<*>,
    header: @Composable () -> Unit,
    entries: LazyPagingItems<EntryModel>,
    unfilteredCount: () -> Int,
    gridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
    noResultsItem: (@Composable () -> Unit)? = null,
    moreResultsItem: (@Composable () -> Unit)? = null,
    itemRow: @Composable (DisplayType, entry: EntryModel) -> Unit,
) {
    Row(modifier = modifier) {
        var displayType by state.displayType.collectAsMutableStateWithLifecycle()
        val forceOneDisplayColumn by state.forceOneDisplayColumn
            .collectAsMutableStateWithLifecycle()

        val width = LocalWindowConfiguration.current.screenWidthDp
        val horizontalContentPadding = if (width > 1200.dp) {
            (width - 1200.dp) / 2
        } else {
            0.dp
        }
        val scrollbarState = rememberScrollbarState(gridState)
        LazyVerticalStaggeredGrid(
            columns = displayType.columns(forceOneDisplayColumn),
            state = gridState,
            contentPadding = displayType.contentPadding(horizontalContentPadding),
            verticalItemSpacing = displayType.verticalItemSpacing,
            horizontalArrangement = displayType.horizontalArrangement,
            modifier = Modifier.weight(1f)
        ) {
            item("header", span = StaggeredGridItemSpan.FullLine) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    header()
                    if (entries.loadState.refresh is LoadState.Loading) {
                        InfiniteProgressIndicator()
                    }
                }
            }

            if (entries.itemCount == 0) {
                if (entries.loadState.refresh !is LoadState.Loading) {
                    if (moreResultsItem != null && unfilteredCount() > 0) {
                        item("searchMoreResults", span = StaggeredGridItemSpan.FullLine) {
                            moreResultsItem()
                        }
                    } else {
                        item("searchNoResults", span = StaggeredGridItemSpan.FullLine) {
                            if (noResultsItem == null) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(Res.string.alley_search_no_results),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            } else {
                                noResultsItem()
                            }
                        }
                    }
                }
            } else {
                items(
                    count = entries.itemCount,
                    key = entries.itemKey { it.id.scopedId },
                    contentType = entries.itemContentType { "search_entry" },
                ) { index ->
                    val entry = entries[index] ?: return@items
                    itemRow(displayType, entry)
                }

                if (moreResultsItem != null && unfilteredCount() > entries.itemCount) {
                    item("searchMoreResults", span = StaggeredGridItemSpan.FullLine) {
                        moreResultsItem()
                    }
                }
            }
        }

        PrimaryVerticalScrollbar(scrollbarState = scrollbarState)
    }
}

@Composable
private fun MoreResultsItem(unfilteredCount: () -> Int, itemCount: () -> Int, onClick: () -> Unit) {
    val filteredOut = unfilteredCount() - itemCount()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            pluralStringResource(
                Res.plurals.alley_search_results_filtered_out,
                filteredOut,
                filteredOut,
            )
        )
        Button(onClick = onClick) {
            Text(stringResource(Res.string.alley_search_clear_filters))
        }
    }
}

private fun DisplayType.columns(forceOneDisplayColumn: Boolean) = if (forceOneDisplayColumn) {
    StaggeredGridCells.Fixed(1)
} else {
    when (this) {
        DisplayType.LIST,
        DisplayType.CARD,
            -> StaggeredGridCells.Adaptive(330.dp)
        DisplayType.IMAGE,
            -> StaggeredGridCellsAdaptiveWithMin(300.dp, 2)
        DisplayType.TABLE -> throw IllegalArgumentException()
    }
}

private fun DisplayType.contentPadding(horizontalContentPadding: Dp) = when (this) {
    DisplayType.LIST,
    DisplayType.IMAGE,
        -> PaddingValues(
        top = 8.dp,
        start = horizontalContentPadding,
        end = horizontalContentPadding,
        bottom = 200.dp,
    )
    DisplayType.CARD,
        -> PaddingValues(
        start = 16.dp + horizontalContentPadding,
        end = 16.dp + horizontalContentPadding,
        top = 8.dp,
        bottom = 200.dp,
    )
    DisplayType.TABLE -> throw IllegalArgumentException()
}

private val DisplayType.verticalItemSpacing
    get() = when (this) {
        DisplayType.LIST,
        DisplayType.IMAGE,
            -> 0.dp
        DisplayType.CARD,
            -> 8.dp
        DisplayType.TABLE -> throw IllegalArgumentException()
    }

private val DisplayType.horizontalArrangement
    get() = when (this) {
        DisplayType.CARD,
            -> 8.dp
        DisplayType.LIST,
        DisplayType.IMAGE,
            -> 0.dp
        DisplayType.TABLE -> throw IllegalArgumentException()
    }.let(Arrangement::spacedBy)
