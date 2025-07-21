package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Adaptive
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_card
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_image
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_list
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_table
import artistalleydatabase.modules.alley.generated.resources.alley_search_clear_filters
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.alley.generated.resources.alley_search_results_filtered_out
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplayTypeSearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemCard
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid.Column
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid.modifierDefaultCellPadding
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.border
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.isLoading
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class
)
object SearchScreen {

    @Composable
    operator fun <EntryModel, ColumnType> invoke(
        state: State<ColumnType>,
        eventSink: (Event<EntryModel>) -> Unit,
        query: MutableStateFlow<String>,
        entries: LazyPagingItems<EntryModel>,
        unfilteredCount: () -> Int = { 0 },
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        sortFilterState: SortFilterState<*>,
        gridState: LazyStaggeredGridState,
        onClickBack: (() -> Unit)? = null,
        title: () -> String? = { null },
        itemToSharedElementId: (EntryModel) -> Any,
        actions: (@Composable RowScope.() -> Unit)? = null,
        header: @Composable () -> Unit,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        SearchScreen(
            state = state,
            eventSink = eventSink,
            entries = entries,
            unfilteredCount = unfilteredCount,
            scaffoldState = scaffoldState,
            sortFilterState = sortFilterState,
            gridState = gridState,
            onClickBack = onClickBack,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    DisplayTypeSearchBar(
                        onClickBack = onClickBack,
                        query = query,
                        displayType = state.displayType,
                        itemCount = { entries.itemCount },
                        title = title,
                        actions = actions,
                    )
                }
            },
            topBarScrollBehavior = scrollBehavior,
            header = header,
            itemToSharedElementId = itemToSharedElementId,
            itemRow = itemRow,
            columnHeader = columnHeader,
            tableCell = tableCell,
        )
    }

    @Composable
    operator fun <EntryModel : SearchEntryModel, ColumnType> invoke(
        state: State<ColumnType>,
        eventSink: (Event<EntryModel>) -> Unit,
        entries: LazyPagingItems<EntryModel>,
        unfilteredCount: () -> Int,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        sortFilterState: SortFilterState<*>,
        gridState: LazyStaggeredGridState,
        onClickBack: (() -> Unit)? = null,
        topBar: @Composable () -> Unit,
        topBarScrollBehavior: TopAppBarScrollBehavior,
        header: @Composable () -> Unit,
        itemToSharedElementId: (EntryModel) -> Any,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
    ) where ColumnType : Enum<ColumnType>, ColumnType : Column {
        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        Box {
            var horizontalScrollBarWidth by remember { mutableStateOf(0) }
            val horizontalScrollState = rememberScrollState()
            SortFilterBottomScaffold(
                state = sortFilterState,
                scaffoldState = scaffoldState,
                sheetPeekHeight = 72.dp,
                topBar = topBar,
                modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            ) {
                Content(
                    state = state,
                    eventSink = eventSink,
                    entries = entries,
                    unfilteredCount = unfilteredCount,
                    horizontalScrollState = horizontalScrollState,
                    gridState = gridState,
                    scaffoldPadding = PaddingValues(top = it.calculateTopPadding()),
                    onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                    itemToSharedElementId = itemToSharedElementId,
                    header = header,
                    itemRow = itemRow,
                    columnHeader = columnHeader,
                    tableCell = tableCell,
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

    @Composable
    fun <EntryModel, ColumnType> Content(
        state: State<ColumnType>,
        eventSink: (Event<EntryModel>) -> Unit,
        entries: LazyPagingItems<EntryModel>,
        unfilteredCount: () -> Int = { 0 },
        horizontalScrollState: ScrollState,
        gridState: LazyStaggeredGridState,
        scaffoldPadding: PaddingValues,
        onHorizontalScrollBarWidth: (Int) -> Unit,
        itemToSharedElementId: (EntryModel) -> Any,
        header: @Composable () -> Unit,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit = {
            AutoSizeText(
                text = stringResource(it.text),
                modifier = Modifier.requiredWidth(it.size)
                    .then(modifierDefaultCellPadding)
            )
        },
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
        noResultsItem: (@Composable () -> Unit)? = null,
        moreResultsItem: (@Composable () -> Unit) = {
            val filteredOut = unfilteredCount() - entries.itemCount
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
                Button(onClick = { eventSink(Event.ClearFilters()) }) {
                    Text(stringResource(Res.string.alley_search_clear_filters))
                }
            }
        },
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val displayType by state.displayType.collectAsStateWithLifecycle()
        if (displayType == DisplayType.TABLE) {
            Table(
                horizontalScrollState = horizontalScrollState,
                header = {
                    item(key = "header") {
                        header()
                    }
                },
                entries = entries,
                unfilteredCount = unfilteredCount,
                columns = state.columns,
                scaffoldPadding = scaffoldPadding,
                onWidthChanged = onHorizontalScrollBarWidth,
                columnHeader = columnHeader,
                tableCell = tableCell,
                noResultsItem = noResultsItem,
                moreResultsItem = moreResultsItem,
            )
        } else {
            VerticalGrid(
                state = state,
                eventSink = eventSink,
                scaffoldPadding = scaffoldPadding,
                header = {
                    item(key = "header", span = StaggeredGridItemSpan.FullLine) {
                        header()
                    }
                },
                entries = entries,
                unfilteredCount = unfilteredCount,
                gridState = gridState,
                itemToSharedElementId = itemToSharedElementId,
                itemRow = itemRow,
                noResultsItem = noResultsItem,
                moreResultsItem = moreResultsItem,
            )
        }
    }

    @Composable
    private fun <EntryModel, ColumnType> Table(
        horizontalScrollState: ScrollState,
        header: LazyListScope.() -> Unit,
        entries: LazyPagingItems<EntryModel>,
        unfilteredCount: () -> Int,
        columns: EnumEntries<ColumnType>,
        scaffoldPadding: PaddingValues,
        onWidthChanged: (Int) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
        noResultsItem: (@Composable () -> Unit)? = null,
        moreResultsItem: (@Composable () -> Unit)? = null,
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth()
                .padding(scaffoldPadding)
        ) {
            val listState = rememberLazyListState()
            TwoWayGrid(
                header = header,
                rows = entries,
                unfilteredCount = unfilteredCount,
                columns = columns,
                listState = listState,
                horizontalScrollState = horizontalScrollState,
                contentPadding = PaddingValues(bottom = 80.dp),
                columnHeader = columnHeader,
                tableCell = tableCell,
                noResultsHeader = noResultsItem,
                moreResultsFooter = moreResultsItem,
                modifier = Modifier.onSizeChanged { onWidthChanged(it.width) }
            )

            VerticalScrollbar(
                state = listState,
                alwaysVisible = PlatformSpecificConfig.scrollbarsAlwaysVisible,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 72.dp)
            )
        }
    }

    @Composable
    private fun <EntryModel : SearchEntryModel> VerticalGrid(
        state: State<*>,
        eventSink: (Event<EntryModel>) -> Unit,
        scaffoldPadding: PaddingValues,
        header: LazyStaggeredGridScope.() -> Unit,
        entries: LazyPagingItems<EntryModel>,
        unfilteredCount: () -> Int,
        gridState: LazyStaggeredGridState,
        itemToSharedElementId: (EntryModel) -> Any,
        noResultsItem: (@Composable () -> Unit)? = null,
        moreResultsItem: (@Composable () -> Unit)? = null,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
    ) {
        Box(Modifier.padding(scaffoldPadding)) {
            val coroutineScope = rememberCoroutineScope()

            var displayType by state.displayType.collectAsMutableStateWithLifecycle()
            val showGridByDefault by state.showGridByDefault
                .collectAsMutableStateWithLifecycle()
            val showRandomCatalogImage by state.showRandomCatalogImage
                .collectAsMutableStateWithLifecycle()
            val forceOneDisplayColumn by state.forceOneDisplayColumn
                .collectAsMutableStateWithLifecycle()

            val width = LocalWindowConfiguration.current.screenWidthDp
            val horizontalContentPadding = if (width > 1200.dp) {
                (width - 1200.dp) / 2
            } else {
                0.dp
            }
            var maxLane by remember { mutableIntStateOf(0) }
            LazyVerticalStaggeredGrid(
                columns = if (forceOneDisplayColumn) {
                    StaggeredGridCells.Fixed(1)
                } else {
                    when (displayType) {
                        DisplayType.LIST,
                        DisplayType.CARD,
                            -> Adaptive(350.dp)
                        DisplayType.IMAGE,
                            -> StaggeredGridCellsAdaptiveWithMin(300.dp, 2)
                        DisplayType.TABLE -> throw IllegalArgumentException()
                    }
                },
                state = gridState,
                contentPadding = when (displayType) {
                    DisplayType.LIST,
                    DisplayType.IMAGE,
                        -> PaddingValues(
                        top = 8.dp,
                        start = horizontalContentPadding,
                        end = horizontalContentPadding,
                        bottom = 156.dp,
                    )
                    DisplayType.CARD,
                        -> PaddingValues(
                        start = 16.dp + horizontalContentPadding,
                        end = 16.dp + horizontalContentPadding,
                        top = 8.dp,
                        bottom = 156.dp,
                    )
                    DisplayType.TABLE -> throw IllegalArgumentException()
                },
                verticalItemSpacing = when (displayType) {
                    DisplayType.LIST,
                    DisplayType.IMAGE,
                        -> 0.dp
                    DisplayType.CARD,
                        -> 8.dp
                    DisplayType.TABLE -> throw IllegalArgumentException()
                },
                horizontalArrangement = when (displayType) {
                    DisplayType.CARD,
                        -> 8.dp
                    DisplayType.LIST,
                    DisplayType.IMAGE,
                        -> 0.dp
                    DisplayType.TABLE -> throw IllegalArgumentException()
                }.let(Arrangement::spacedBy),
                modifier = Modifier.fillMaxSize()
            ) {
                header()

                if (entries.itemCount == 0) {
                    if (entries.loadState.refresh.isLoading) {
                        item("searchLoadingIndicator", span = StaggeredGridItemSpan.FullLine) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
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

                        @Suppress("NAME_SHADOWING")
                        val onFavoriteToggle: (Boolean) -> Unit = {
                            entry.favorite = it
                            eventSink(Event.FavoriteToggle(entry, it))
                        }

                        @Suppress("NAME_SHADOWING")
                        val onIgnoredToggle: (Boolean) -> Unit = {
                            entry.ignored = it
                            eventSink(Event.IgnoreToggle(entry, it))
                        }

                        val sharedElementId = itemToSharedElementId(entry)
                        when (displayType) {
                            DisplayType.LIST -> {
                                val ignored = entry.ignored
                                val lane by remember(index) {
                                    derivedStateOf {
                                        gridState.layoutInfo.visibleItemsInfo
                                            .find { it.index - 1 == index }
                                            ?.lane
                                    }
                                }
                                val finalLane = lane
                                if (finalLane != null && maxLane < finalLane) {
                                    maxLane = finalLane
                                }
                                itemRow(
                                    entry,
                                    onFavoriteToggle,
                                    Modifier
                                        .sharedBounds("itemContainer", sharedElementId)
                                        .combinedClickable(
                                            onClick = { eventSink(Event.OpenEntry(entry, 1)) },
                                            onLongClick = { onIgnoredToggle(!ignored) }
                                        )
                                        .alpha(if (entry.ignored) 0.38f else 1f)
                                        .border(
                                            width = 1.dp,
                                            color = DividerDefaults.color,
                                            start = lane != 0,
                                            bottom = true,
                                        )
                                )
                            }
                            DisplayType.CARD -> ItemCard(
                                entry = entry,
                                sharedElementId = itemToSharedElementId(entry),
                                showGridByDefault = showGridByDefault,
                                showRandomCatalogImage = showRandomCatalogImage,
                                onFavoriteToggle = onFavoriteToggle,
                                onIgnoredToggle = onIgnoredToggle,
                                onClick = { entry, imageIndex ->
                                    eventSink(Event.OpenEntry(entry, imageIndex))
                                },
                                itemRow = itemRow,
                                modifier = Modifier.sharedBounds(
                                    "itemContainer",
                                    sharedElementId
                                ),
                            )
                            DisplayType.IMAGE -> ItemImage(
                                entry = entry,
                                sharedElementId = itemToSharedElementId(entry),
                                showGridByDefault = showGridByDefault,
                                showRandomCatalogImage = showRandomCatalogImage,
                                onFavoriteToggle = onFavoriteToggle,
                                onIgnoredToggle = onIgnoredToggle,
                                onClick = { entry, imageIndex ->
                                    eventSink(Event.OpenEntry(entry, imageIndex))
                                },
                                itemRow = itemRow,
                                modifier = Modifier.sharedBounds(
                                    "itemContainer",
                                    sharedElementId
                                ),
                            )
                            DisplayType.TABLE -> throw IllegalArgumentException()
                        }
                    }

                    if (moreResultsItem != null && unfilteredCount() > entries.itemCount) {
                        item("searchMoreResults", span = StaggeredGridItemSpan.FullLine) {
                            moreResultsItem()
                        }
                    }
                }
            }

            VerticalScrollbar(
                state = gridState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 72.dp)
            )
        }
    }

    interface SearchEntryModel : EntryGridModel {
        val booth: String?
        val images: List<CatalogImage>
        var favorite: Boolean
        var ignored: Boolean
    }

    enum class DisplayType(val label: StringResource, val icon: ImageVector) {
        CARD(Res.string.alley_display_type_card, Icons.Filled.ViewAgenda),
        IMAGE(Res.string.alley_display_type_image, Icons.Filled.Image),
        LIST(Res.string.alley_display_type_list, Icons.AutoMirrored.Filled.ViewList),
        TABLE(Res.string.alley_display_type_table, Icons.Default.TableChart),
    }

    @Stable
    class State<ColumnType>(
        val columns: EnumEntries<ColumnType>,
        val displayType: MutableStateFlow<DisplayType>,
        val showGridByDefault: MutableStateFlow<Boolean>,
        val showRandomCatalogImage: MutableStateFlow<Boolean>,
        val forceOneDisplayColumn: MutableStateFlow<Boolean>,
    ) where ColumnType : Enum<ColumnType>, ColumnType : Column

    sealed interface Event<EntryModel : SearchEntryModel> {
        data class FavoriteToggle<EntryModel : SearchEntryModel>(
            val entry: EntryModel,
            val favorite: Boolean,
        ) : Event<EntryModel>

        data class IgnoreToggle<EntryModel : SearchEntryModel>(
            val entry: EntryModel,
            val ignored: Boolean,
        ) : Event<EntryModel>

        data class OpenEntry<EntryModel : SearchEntryModel>(
            val entry: EntryModel,
            val imageIndex: Int,
        ) : Event<EntryModel>

        class ClearFilters<EntryModel : SearchEntryModel> : Event<EntryModel>
    }
}
