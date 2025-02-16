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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_card
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_image
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_list
import artistalleydatabase.modules.alley.generated.resources.alley_display_type_table
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
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.border
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.HorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.EnumEntries
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
object SearchScreen {

    @Composable
    operator fun <SearchQuery, EntryModel, ColumnType> invoke(
        viewModel: EntrySearchViewModel<SearchQuery, EntryModel>,
        entries: LazyPagingItems<EntryModel>,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        bottomSheet: @Composable ColumnScope.() -> Unit,
        showGridByDefault: () -> Boolean,
        showRandomCatalogImage: () -> Boolean,
        forceOneDisplayColumn: () -> Boolean,
        dataYearHeaderState: DataYearHeaderState,
        displayType: () -> DisplayType,
        onDisplayTypeToggle: (DisplayType) -> Unit,
        gridState: LazyStaggeredGridState,
        onFavoriteToggle: (EntryModel, Boolean) -> Unit,
        onIgnoredToggle: (EntryModel, Boolean) -> Unit,
        onEntryClick: (EntryModel, imageIndex: Int) -> Unit,
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
        columns: EnumEntries<ColumnType>,
        columnHeader: @Composable (column: ColumnType) -> Unit = {
            AutoSizeText(
                text = stringResource(it.text),
                modifier = Modifier.requiredWidth(it.size)
                    .then(modifierDefaultCellPadding)
            )
        },
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
                    BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
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
                    Scaffold(
                        topBar = {
                            TopBar(
                                viewModel = viewModel,
                                entries = entries,
                                scrollBehavior = scrollBehavior,
                                displayType = displayType,
                                onDisplayTypeToggle = onDisplayTypeToggle,
                                onClickBack = onClickBack,
                                onHeightChanged = { topBarHeight = it },
                                title = title,
                                actions = actions,
                            )
                        },
                        modifier = Modifier
                            .conditionally(displayType() != DisplayType.TABLE) {
                                widthIn(max = 1200.dp)
                            }
                    ) {
                        val density = LocalDensity.current
                        val topBarPadding by remember(density) {
                            derivedStateOf {
                                if (scrollBehavior == null) {
                                    density.run { topBarHeight.toDp() }.coerceAtLeast(0.dp)
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

                        if (displayType() == DisplayType.TABLE) {
                            Table(
                                horizontalScrollState = horizontalScrollState,
                                dataYearHeaderState = dataYearHeaderState,
                                entries = entries,
                                columns = columns,
                                scaffoldPadding = it,
                                onWidthChanged = { horizontalScrollBarWidth = it },
                                columnHeader = columnHeader,
                                tableCell = tableCell,
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
                                entries = entries,
                                showGridByDefault = showGridByDefault,
                                showRandomCatalogImage = showRandomCatalogImage,
                                forceOneDisplayColumn = forceOneDisplayColumn,
                                dataYearHeaderState = dataYearHeaderState,
                                displayType = displayType,
                                gridState = gridState,
                                onFavoriteToggle = onFavoriteToggle,
                                onIgnoredToggle = onIgnoredToggle,
                                onEntryClick = onEntryClick,
                                shouldShowCount = shouldShowCount,
                                topOffset = topOffset,
                                topBarPadding = topBarPadding,
                                itemToSharedElementId = itemToSharedElementId,
                                itemRow = itemRow,
                            )
                        }
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
    private fun <SearchQuery, EntryModel : SearchEntryModel> TopBar(
        viewModel: EntrySearchViewModel<SearchQuery, EntryModel>,
        entries: LazyPagingItems<EntryModel>,
        scrollBehavior: TopAppBarScrollBehavior?,
        displayType: () -> DisplayType,
        onDisplayTypeToggle: (DisplayType) -> Unit,
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
                val isNotEmpty by remember {
                    derivedStateOf { viewModel.query.isNotEmpty() }
                }
                BackHandler(isNotEmpty && !WindowInsets.isImeVisibleKmp) {
                    viewModel.onQuery("")
                }

                StaticSearchBar(
                    leadingIcon = if (onClickBack != null) {
                        { ArrowBackIconButton(onClickBack) }
                    } else null,
                    query = viewModel.query,
                    onQueryChange = viewModel::onQuery,
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
                                IconButton(onClick = { viewModel.onQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(
                                            EntryRes.string.entry_search_clear
                                        ),
                                    )
                                }
                            }
                            Box {
                                val displayType = displayType()
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
                                                    onClick = { onDisplayTypeToggle(it) },
                                                )
                                            },
                                            onClick = { onDisplayTypeToggle(it) },
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
    private fun <EntryModel, ColumnType> Table(
        horizontalScrollState: ScrollState,
        dataYearHeaderState: DataYearHeaderState,
        entries: LazyPagingItems<EntryModel>,
        columns: EnumEntries<ColumnType>,
        scaffoldPadding: PaddingValues,
        onWidthChanged: (Int) -> Unit,
        columnHeader: @Composable (column: ColumnType) -> Unit,
        tableCell: @Composable (row: EntryModel?, column: ColumnType) -> Unit,
    ) where EntryModel : SearchEntryModel, ColumnType : Enum<ColumnType>, ColumnType : Column {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth()
                .padding(scaffoldPadding)
        ) {
            val listState = rememberLazyListState()
            TwoWayGrid(
                dataYearHeaderState = dataYearHeaderState,
                rows = entries,
                columns = columns,
                listState = listState,
                horizontalScrollState = horizontalScrollState,
                contentPadding = PaddingValues(bottom = 80.dp),
                columnHeader = columnHeader,
                tableCell = tableCell,
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
        entries: LazyPagingItems<EntryModel>,
        showGridByDefault: () -> Boolean,
        showRandomCatalogImage: () -> Boolean,
        forceOneDisplayColumn: () -> Boolean,
        dataYearHeaderState: DataYearHeaderState,
        displayType: () -> DisplayType,
        gridState: LazyStaggeredGridState,
        onFavoriteToggle: (EntryModel, Boolean) -> Unit,
        onIgnoredToggle: (EntryModel, Boolean) -> Unit,
        onEntryClick: (EntryModel, imageIndex: Int) -> Unit,
        shouldShowCount: () -> Boolean,
        topOffset: Dp,
        topBarPadding: Dp,
        itemToSharedElementId: (EntryModel) -> Any,
        itemRow: @Composable (
            entry: EntryModel,
            onFavoriteToggle: (Boolean) -> Unit,
            modifier: Modifier,
        ) -> Unit,
    ) {
        Box {
            val coroutineScope = rememberCoroutineScope()

            @Suppress("NAME_SHADOWING")
            val displayType = displayType()

            @Suppress("NAME_SHADOWING")
            val showGridByDefault = showGridByDefault()

            @Suppress("NAME_SHADOWING")
            val showRandomCatalogImage = showRandomCatalogImage()
            var maxLane by remember { mutableIntStateOf(0) }
            LazyVerticalStaggeredGrid(
                columns = if (forceOneDisplayColumn()) {
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
                item(key = "dataYearHeader", span = StaggeredGridItemSpan.FullLine) {
                    DataYearHeader(dataYearHeaderState)
                }

                items(
                    count = entries.itemCount,
                    key = entries.itemKey { it.id.scopedId },
                    contentType = entries.itemContentType { "search_entry" },
                ) { index ->
                    val entry = entries[index] ?: return@items

                    @Suppress("NAME_SHADOWING")
                    val onFavoriteToggle: (Boolean) -> Unit = {
                        entry.favorite = it
                        onFavoriteToggle(entry, it)
                    }

                    @Suppress("NAME_SHADOWING")
                    val onIgnoredToggle: (Boolean) -> Unit = {
                        entry.ignored = it
                        onIgnoredToggle(entry, it)
                    }

                    val sharedElementId = itemToSharedElementId(entry)
                    when (displayType) {
                        DisplayType.LIST -> {
                            val ignored = entry.ignored
                            val lane by remember(index) {
                                derivedStateOf {
                                    gridState.layoutInfo.visibleItemsInfo.find { it.index == index }
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
                                        onClick = { onEntryClick(entry, 1) },
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
                            onClick = onEntryClick,
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
                            onClick = onEntryClick,
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
        ;

        companion object {
            fun fromSerializedValue(value: String) = DisplayType.entries.find { it.name == value }
                ?: DisplayType.CARD
        }
    }
}
