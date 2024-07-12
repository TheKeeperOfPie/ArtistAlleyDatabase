package com.thekeeperofpie.artistalleydatabase.alley

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemCard
import com.thekeeperofpie.artistalleydatabase.alley.ui.ItemImage
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.VerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
object SearchScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun <SearchQuery, EntryModel : SearchEntryModel> invoke(
        viewModel: EntrySearchViewModel<SearchQuery, EntryModel>,
        entries: LazyPagingItems<EntryModel>,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            rememberStandardBottomSheetState(
                confirmValueChange = { it != SheetValue.Hidden },
                skipHiddenState = true,
            )
        ),
        bottomSheet: @Composable ColumnScope.() -> Unit,
        showGridByDefault: () -> Boolean,
        showRandomCatalogImage: () -> Boolean,
        forceOneDisplayColumn: () -> Boolean,
        displayType: () -> DisplayType,
        onDisplayTypeToggle: (DisplayType) -> Unit,
        gridState: LazyStaggeredGridState,
        onFavoriteToggle: (EntryModel, Boolean) -> Unit,
        onIgnoredToggle: (EntryModel, Boolean) -> Unit,
        onEntryClick: (EntryModel, Int) -> Unit,
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
    ) {
        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        var seen by remember { mutableStateOf(false) }

        val updateNotice = stringResource(R.string.alley_update_notice)
        val updateOpenUpdate = stringResource(R.string.alley_open_update)

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 72.dp,
            sheetDragHandle = {
                BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary)
            },
            sheetContent = bottomSheet,
            modifier = Modifier.nestedScroll(
                NestedScrollSplitter(
                    primary = scrollBehavior.nestedScrollConnection,
                    consumeNone = true,
                )
            ),
        ) {
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            val isNotEmpty by remember {
                                derivedStateOf { viewModel.query.isNotEmpty() }
                            }
                            BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
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
                                                    EntryStringR.entry_search_hint_with_entry_count,
                                                    entriesSize,
                                                )
                                            } else {
                                                stringResource(EntryStringR.entry_search_hint)
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
                                                        EntryStringR.entry_search_clear
                                                    ),
                                                )
                                            }
                                        }

                                        @Suppress("NAME_SHADOWING")
                                        val displayType = displayType()
                                        val entries = DisplayType.entries
                                        val nextDisplayType =
                                            entries[(entries.indexOf(displayType) + 1) % entries.size]
                                        IconButton(onClick = {
                                            onDisplayTypeToggle(nextDisplayType)
                                        }) {
                                            Icon(
                                                imageVector = nextDisplayType.icon,
                                                contentDescription = stringResource(
                                                    R.string.alley_display_type_icon_content_description,
                                                ),
                                            )
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
            ) {
                val density = LocalDensity.current
                val topBarPadding by remember {
                    derivedStateOf {
                        scrollBehavior.state.heightOffsetLimit
                            .takeUnless { it == -Float.MAX_VALUE }
                            ?.let { density.run { -it.toDp() } }
                            ?: 0.dp
                    }
                }
                val topOffset by remember {
                    derivedStateOf {
                        topBarPadding + density.run { scrollBehavior.state.heightOffset.toDp() }
                    }
                }

                Box {
                    val coroutineScope = rememberCoroutineScope()

                    @Suppress("NAME_SHADOWING")
                    val displayType = displayType()

                    @Suppress("NAME_SHADOWING")
                    val showGridByDefault = showGridByDefault()

                    @Suppress("NAME_SHADOWING")
                    val showRandomCatalogImage = showRandomCatalogImage()
                    LazyVerticalStaggeredGrid(
                        columns = if (forceOneDisplayColumn()) {
                            StaggeredGridCells.Fixed(1)
                        } else {
                            when (displayType) {
                                DisplayType.LIST,
                                DisplayType.CARD,
                                -> StaggeredGridCells.Adaptive(350.dp)
                                DisplayType.IMAGE,
                                -> StaggeredGridCellsAdaptiveWithMin(300.dp, 2)
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
                        },
                        verticalItemSpacing = when (displayType) {
                            DisplayType.LIST,
                            DisplayType.IMAGE,
                            -> 0.dp
                            DisplayType.CARD,
                            -> 8.dp
                        },
                        horizontalArrangement = when (displayType) {
                            DisplayType.LIST,
                            DisplayType.CARD,
                            -> 8.dp
                            DisplayType.IMAGE -> 0.dp
                        }.let(Arrangement::spacedBy),
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                                    )

                                    HorizontalDivider()
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
                            0 -> EntryStringR.entry_results_zero
                            1 -> EntryStringR.entry_results_one
                            else -> EntryStringR.entry_results_multiple
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
        }
    }

    interface SearchEntryModel : EntryGridModel {
        val booth: String
        val images: List<CatalogImage>
        var favorite: Boolean
        var ignored: Boolean
    }

    enum class DisplayType(val icon: ImageVector) {
        LIST(Icons.AutoMirrored.Filled.ViewList),
        CARD(Icons.Filled.ViewAgenda),
        IMAGE(Icons.Filled.Image),
        ;

        companion object {
            fun fromSerializedValue(value: String) =
                SearchScreen.DisplayType.entries.find { it.name == value }
                    ?: SearchScreen.DisplayType.CARD
        }
    }
}
