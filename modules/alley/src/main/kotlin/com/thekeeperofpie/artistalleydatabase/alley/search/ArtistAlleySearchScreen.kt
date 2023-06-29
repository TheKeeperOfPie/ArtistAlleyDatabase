package com.thekeeperofpie.artistalleydatabase.alley.search

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.compose.BottomSheetScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortAndFilterComposables.SortSection
import com.thekeeperofpie.artistalleydatabase.compose.optionalClickable
import com.thekeeperofpie.artistalleydatabase.compose.rememberBottomSheetScaffoldState
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
object ArtistAlleySearchScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        viewModel: ArtistAlleySearchViewModel = hiltViewModel<ArtistAlleySearchViewModel>(),
        onEntryClick: (ArtistEntryGridModel) -> Unit,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()

        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val listState = rememberLazyListState()
        var seen by remember { mutableStateOf(false) }
        LaunchedEffect(viewModel.query?.query, viewModel.sortOptions, viewModel.sortAscending) {
            if (seen) {
                listState.animateScrollToItem(0, 0)
            } else {
                seen = true
            }
        }

        BottomSheetScaffoldNoAppBarOffset(
            scaffoldState = scaffoldState,
            sheetContent = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = 320.dp)
                ) {
                    Divider()
                    var sortExpanded by remember { mutableStateOf(false) }
                    SortSection(
                        headerTextRes = R.string.alley_sort_label,
                        expanded = { sortExpanded },
                        onExpandedChange = { sortExpanded = it },
                        sortOptions = { viewModel.sortOptions },
                        onSortClick = viewModel::onSortClick,
                        sortAscending = { viewModel.sortAscending },
                        onSortAscendingChange = {
                            viewModel.sortAscending = !viewModel.sortAscending
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.alley_filter_favorites),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .weight(1f)
                        )

                        Switch(
                            checked = viewModel.showOnlyFavorites,
                            onCheckedChange = { viewModel.showOnlyFavorites = it },
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }

                    Divider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.alley_filter_only_catalogs),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .weight(1f)
                        )

                        Switch(
                            checked = viewModel.showOnlyWithCatalog,
                            onCheckedChange = { viewModel.showOnlyWithCatalog = it },
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }

                    Divider()
                }
            },
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        val isNotEmpty by remember {
                            derivedStateOf { viewModel.query?.query.orEmpty().isNotEmpty() }
                        }
                        BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                            viewModel.onQuery("")
                        }

                        val query = viewModel.query?.query.orEmpty()
                        var active by remember { mutableStateOf(false) }
                        DockedSearchBar(
                            query = query,
                            onQueryChange = viewModel::onQuery,
                            active = active,
                            onActiveChange = {},
                            placeholder = {
                                val entriesSize = viewModel.entriesSize
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

                                    val displayType = viewModel.displayType
                                    IconButton(onClick = {
                                        viewModel.onDisplayTypeToggle(
                                            when (displayType) {
                                                DisplayType.LIST -> DisplayType.CARD
                                                DisplayType.CARD -> DisplayType.LIST
                                            }
                                        )
                                    }) {
                                        Icon(
                                            imageVector = displayType.icon,
                                            contentDescription = stringResource(
                                                R.string.alley_display_type_icon_content_description,
                                            ),
                                        )
                                    }

                                    IconButton(onClick = { active = !active }) {
                                        Icon(
                                            imageVector = if (active) {
                                                Icons.Filled.ExpandLess
                                            } else {
                                                Icons.Filled.ExpandMore
                                            },
                                            contentDescription = stringResource(
                                                if (active) {
                                                    EntryStringR.entry_search_options_collapse
                                                } else {
                                                    EntryStringR.entry_search_options_expand
                                                }
                                            ),
                                        )
                                    }
                                }
                            },
                            onSearch = { active = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                        ) {
                            val options = viewModel.options
                            options.forEachIndexed { index, option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .conditionally(index == options.lastIndex) {
                                            padding(bottom = 8.dp)
                                        }
                                        .clickable {
                                            option.enabled = !option.enabled
                                            viewModel.refreshQuery()
                                        }
                                ) {
                                    Checkbox(
                                        checked = option.enabled,
                                        onCheckedChange = null,
                                        Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = 8.dp
                                        )
                                    )

                                    Text(stringResource(option.textRes))
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.nestedScroll(
                NestedScrollSplitter(
                    primary = scrollBehavior.nestedScrollConnection,
                    consumeNone = true,
                )
            ),
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
                val entries = viewModel.results.collectAsLazyPagingItems()
                val coroutineScope = rememberCoroutineScope()
                val displayType = viewModel.displayType
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 8.dp + topBarPadding, bottom = 80.dp),
                    verticalArrangement = when (displayType) {
                        DisplayType.LIST -> Arrangement.Top
                        DisplayType.CARD -> Arrangement.spacedBy(16.dp)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = entries.itemCount,
                        key = entries.itemKey { it.id.scopedId },
                        contentType = entries.itemContentType { "artist_entry" },
                    ) { index ->
                        val entry = entries[index] ?: return@items
                        val onFavoriteToggle: (Boolean) -> Unit = {
                            entry.favorite = it
                            viewModel.onFavoriteToggle(entry, it)
                        }

                        when (displayType) {
                            DisplayType.LIST -> {
                                ArtistListRow(
                                    entry,
                                    onFavoriteToggle = onFavoriteToggle,
                                    onClick = onEntryClick,
                                )

                                Divider()
                            }
                            DisplayType.CARD -> ArtistCard(
                                entry,
                                onFavoriteToggle = onFavoriteToggle,
                                onClick = onEntryClick,
                            )
                        }
                    }
                }

                if (viewModel.query?.query.orEmpty().isNotEmpty()
                    || viewModel.showOnlyFavorites
                    || viewModel.showOnlyWithCatalog
                ) {
                    val entriesSize = entries.itemCount
                    val stringRes = when (entriesSize) {
                        0 -> EntryStringR.entry_results_zero
                        1 -> EntryStringR.entry_results_one
                        else -> EntryStringR.entry_results_multiple
                    }

                    Text(
                        text = stringResource(stringRes, entriesSize),
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
                                    listState.animateScrollToItem(0, 0)
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun ArtistListRow(
        entry: ArtistEntryGridModel,
        onFavoriteToggle: (Boolean) -> Unit,
        onClick: ((ArtistEntryGridModel) -> Unit)? = null,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .optionalClickable(onClick = onClick?.let { { it(entry) } })
        ) {
            val artist = entry.value
            Text(
                text = artist.booth,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = entry.tableName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (entry.showArtistNames) {
                    Text(
                        text = entry.artistNamesText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            val favorite = entry.favorite
            IconButton(onClick = { onFavoriteToggle(!favorite) }) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        R.string.alley_artist_favorite_icon_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun ArtistCard(
        entry: ArtistEntryGridModel,
        onFavoriteToggle: (Boolean) -> Unit,
        onClick: (ArtistEntryGridModel) -> Unit,
    ) {
        ElevatedCard(
            onClick = { onClick(entry) },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val images = entry.images
            val pagerState = rememberPagerState(pageCount = { images.size })
            var minHeight by remember { mutableIntStateOf(0) }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .heightIn(min = LocalDensity.current.run { minHeight.toDp() })
                    .onSizeChanged {
                        if (it.height > minHeight) {
                            minHeight = it.height
                        }
                    }
                    .clipToBounds()
            ) {
                ZoomPanBox {
                    AsyncImage(
                        model = images[it],
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(R.string.alley_artist_catalog_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(entry) }
                    )
                }
            }

            ArtistListRow(
                entry = entry,
                onFavoriteToggle = onFavoriteToggle,
            )
        }
    }

    enum class DisplayType(val icon: ImageVector) {
        LIST(Icons.Filled.ViewList),
        CARD(Icons.Filled.ViewAgenda),
    }
}
