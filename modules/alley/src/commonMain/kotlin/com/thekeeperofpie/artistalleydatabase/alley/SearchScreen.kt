package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells.Adaptive
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_card
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_image
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_list
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_table
import artistalleydatabase.modules.alley.generated.resources.alley_search_no_results
import artistalleydatabase.modules.entry.generated.resources.entry_results_multiple
import artistalleydatabase.modules.entry.generated.resources.entry_results_one
import artistalleydatabase.modules.entry.generated.resources.entry_results_zero
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint
import artistalleydatabase.modules.entry.generated.resources.entry_search_hint_with_entry_count
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemCard
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid.Column
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid.modifierDefaultCellPadding
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.border
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.isLoading
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

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
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        bottomSheet: @Composable ColumnScope.() -> Unit,
        dataYearHeaderState: DataYearHeaderState,
        gridState: LazyStaggeredGridState,
        shouldShowCount: () -> Boolean,
        onClickBack: (() -> Unit)? = null,
        title: () -> String? = { null },
        itemToSharedElementId: (EntryModel) -> Any,
        actions: (@Composable RowScope.() -> Unit)? = null,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            .takeUnless { PlatformSpecificConfig.scrollbarsAlwaysVisible }

        Box {
            var horizontalScrollBarWidth by remember { mutableStateOf(0) }
            val horizontalScrollState = rememberScrollState()
            BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetPeekHeight = 72.dp,
                sheetDragHandle = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded) {
                                    scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                } else {
                                    scope.launch { scaffoldState.bottomSheetState.expand() }
                                }
                            })
                    ) {
                        BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
                    }
                },
                sheetContent = bottomSheet,
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
                            TopBar(
                                query = query,
                                displayType = state.displayType,
                                entries = entries,
                                scrollBehavior = scrollBehavior,
                                onClickBack = onClickBack,
                                onHeightChanged = { topBarHeight = it },
                                title = title,
                                actions = actions,
                            )
                        },
                        modifier = Modifier
                            .conditionally(displayType != DisplayType.TABLE) {
                                widthIn(max = 1200.dp)
                            }
                    ) {
                        Content(
                            state = state,
                            eventSink = eventSink,
                            entries = entries,
                            scrollBehavior = scrollBehavior,
                            horizontalScrollState = horizontalScrollState,
                            gridState = gridState,
                            scaffoldPadding = it,
                            topBarHeight = { topBarHeight },
                            onHorizontalScrollBarWidth = { horizontalScrollBarWidth = it },
                            shouldShowCount = shouldShowCount,
                            itemToSharedElementId = itemToSharedElementId,
                            header = { DataYearHeader(dataYearHeaderState) },
                            itemRow = itemRow,
                            columnHeader = columnHeader,
                            tableCell = tableCell,
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

    @Composable
    fun <EntryModel : SearchEntryModel> TopBar(
        query: MutableStateFlow<String>,
        displayType: MutableStateFlow<DisplayType>,
        entries: LazyPagingItems<EntryModel>,
        scrollBehavior: TopAppBarScrollBehavior?,
        onClickBack: (() -> Unit)?,
        onHeightChanged: (Int) -> Unit,
        title: () -> String?,
        actions: (@Composable RowScope.() -> Unit)?,
    ) {
        EnterAlwaysTopAppBar(
            scrollBehavior = scrollBehavior,
            modifier = Modifier.onSizeChanged { onHeightChanged(it.height) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .padding(bottom = 8.dp)
            ) {
                var query by query.collectAsMutableStateWithLifecycle()
                val isNotEmpty by remember { derivedStateOf { query.isNotEmpty() } }
                BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                    query = ""
                }

                StaticSearchBar(
                    leadingIcon = if (onClickBack != null) {
                        { ArrowBackIconButton(onClickBack) }
                    } else null,
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = {
                        @Suppress("NAME_SHADOWING")
                        val title = title()
                        if (title != null) {
                            Text(title)
                        } else {
                            val entriesSize = entries.itemCount
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
                        }
                    },
                    trailingIcon = {
                        Row {
                            AnimatedVisibility(isNotEmpty) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(
                                            EntryRes.string.entry_search_clear
                                        ),
                                    )
                                }
                            }
                            Box {
                                var displayType by displayType
                                    .collectAsMutableStateWithLifecycle()
                                var expanded by remember { mutableStateOf(false) }
                                IconButton(onClick = { expanded = true }) {
                                    Icon(
                                        imageVector = displayType.icon,
                                        contentDescription = stringResource(
                                            Res.string.alley_display_type_icon_content_description,
                                        ),
                                    )
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                ) {
                                    DisplayType.entries.forEach {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(it.label)) },
                                            leadingIcon = {
                                                RadioButton(
                                                    selected = displayType == it,
                                                    onClick = { displayType = it },
                                                )
                                            },
                                            onClick = { displayType = it },
                                        )
                                    }
                                }
                            }

                            actions?.invoke(this)
                        }
                    },
                    onSearch = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
            }
        }
    }

    @Composable
    fun <EntryModel, ColumnType> Content(
        state: State<ColumnType>,
        eventSink: (Event<EntryModel>) -> Unit,
        entries: LazyPagingItems<EntryModel>,
        scrollBehavior: TopAppBarScrollBehavior?,
        horizontalScrollState: ScrollState,
        gridState: LazyStaggeredGridState,
        scaffoldPadding: PaddingValues,
        topBarHeight: () -> Int,
        onHorizontalScrollBarWidth: (Int) -> Unit,
        shouldShowCount: () -> Boolean,
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
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        val density = LocalDensity.current
        val topBarPadding by remember(density) {
            derivedStateOf {
                if (scrollBehavior == null) {
                    density.run { topBarHeight().toDp() }.coerceAtLeast(0.dp)
                } else {
                    // Force a snapshot read so that this recomposes
                    // https://android-review.googlesource.com/c/platform/frameworks/support/+/3123371
                    scrollBehavior.state.heightOffset
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { density.run { -it.toDp() } }
                        ?: 0.dp
                }
            }
        }

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
                columns = state.columns,
                scaffoldPadding = scaffoldPadding,
                onWidthChanged = onHorizontalScrollBarWidth,
                columnHeader = columnHeader,
                tableCell = tableCell,
                noResultsItem = noResultsItem,
            )
        } else {
            val topOffset by remember {
                derivedStateOf {
                    (topBarPadding + density.run {
                        scrollBehavior?.state?.heightOffset?.toDp() ?: 0.dp
                    }).coerceAtLeast(0.dp)
                }
            }

            VerticalGrid(
                state = state,
                eventSink = eventSink,
                header = {
                    item(key = "header", span = StaggeredGridItemSpan.FullLine) {
                        header()
                    }
                },
                entries = entries,
                gridState = gridState,
                shouldShowCount = shouldShowCount,
                topOffset = topOffset,
                topBarPadding = topBarPadding,
                itemToSharedElementId = itemToSharedElementId,
                itemRow = itemRow,
                noResultsItem = noResultsItem,
            )
        }
    }

    @Composable
    private fun <EntryModel, ColumnType> Table(
        horizontalScrollState: ScrollState,
        header: LazyListScope.() -> Unit,
        entries: LazyPagingItems<EntryModel>,
        columns: EnumEntries<ColumnType>,
        scaffoldPadding: PaddingValues,
        onWidthChanged: (Int) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
        noResultsItem: (@Composable () -> Unit)? = null,
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
                columns = columns,
                listState = listState,
                horizontalScrollState = horizontalScrollState,
                contentPadding = PaddingValues(bottom = 80.dp),
                columnHeader = columnHeader,
                tableCell = tableCell,
                additionalHeader = noResultsItem,
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
        header: LazyStaggeredGridScope.() -> Unit,
        entries: LazyPagingItems<EntryModel>,
        gridState: LazyStaggeredGridState,
        shouldShowCount: () -> Boolean,
        topOffset: Dp,
        topBarPadding: Dp,
        itemToSharedElementId: (EntryModel) -> Any,
        noResultsItem: (@Composable () -> Unit)? = null,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
    ) {
        Box {
            val coroutineScope = rememberCoroutineScope()

            var displayType by state.displayType.collectAsMutableStateWithLifecycle()
            val showGridByDefault by state.showGridByDefault
                .collectAsMutableStateWithLifecycle()
            val showRandomCatalogImage by state.showRandomCatalogImage
                .collectAsMutableStateWithLifecycle()
            val forceOneDisplayColumn by state.forceOneDisplayColumn
                .collectAsMutableStateWithLifecycle()

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
                        top = 8.dp + topBarPadding,
                        bottom = 80.dp,
                    )
                    DisplayType.CARD,
                        -> PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp + topBarPadding,
                        bottom = 80.dp,
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
                }
            }

            VerticalScrollbar(
                state = gridState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(top = 8.dp + topBarPadding, bottom = 72.dp)
            )

            if (shouldShowCount()) {
                val stringRes = when (entries.itemCount) {
                    0 -> EntryRes.string.entry_results_zero
                    1 -> EntryRes.string.entry_results_one
                    else -> EntryRes.string.entry_results_multiple
                }

                Text(
                    text = stringResource(stringRes, entries.itemCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .wrapContentSize()
                        .padding(top = 8.dp + topOffset)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0, 0)
                            }
                        }
                        .padding(8.dp)
                )
            }
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
    }
}
