package com.thekeeperofpie.artistalleydatabase.entry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchOption
import kotlinx.coroutines.flow.emptyFlow

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object EntryHomeScreen {

    @Composable
    operator fun <GridModel : EntryGridModel> invoke(
        screenKey: String,
        onClickNav: () -> Unit = {},
        query: () -> String = { "" },
        entriesSize: () -> Int,
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChange: (EntrySearchOption) -> Unit = {},
        entries: @Composable () -> LazyPagingItems<GridModel> =
            { emptyFlow<PagingData<GridModel>>().collectAsLazyPagingItems() },
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: GridModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: GridModel) -> Unit = { _, _ -> },
        onClickAddFab: () -> Unit = {},
        onClickClear: () -> Unit = {},
        onClickEdit: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
        lazyStaggeredGridState: LazyStaggeredGrid.LazyStaggeredGridState,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Chrome(
            onClickNav = onClickNav,
            query = query,
            entriesSize = entriesSize,
            onQueryChange = onQueryChange,
            showFab = { selectedItems().isEmpty() },
            options = options,
            onOptionChange = onOptionChange,
            onClickAddFab = onClickAddFab,
            scrollBehavior = scrollBehavior,
        ) {
            val density = LocalDensity.current
            val topBarPadding by remember {
                derivedStateOf {
                    PaddingValues(
                        top = scrollBehavior.state.heightOffsetLimit
                            .takeUnless { it == -Float.MAX_VALUE }
                            ?.let { density.run { -it.toDp() } }
                            ?: 0.dp
                    )
                }
            }
            val topOffset by remember {
                derivedStateOf {
                    topBarPadding.calculateTopPadding() + density.run { scrollBehavior.state.heightOffset.toDp() }
                }
            }
            EntryGrid(
                imageScreenKey = screenKey,
                entries = entries,
                entriesSize = { entries().itemCount.takeIf { query().isNotEmpty() } },
                selectedItems = selectedItems,
                onClickEntry = onClickEntry,
                onLongClickEntry = onLongClickEntry,
                onClickClear = onClickClear,
                onClickEdit = onClickEdit,
                onConfirmDelete = onConfirmDelete,
                lazyStaggeredGridState = lazyStaggeredGridState,
                contentPadding = topBarPadding,
                topOffset = topOffset,
            )
        }
    }

    @Composable
    private fun Chrome(
        onClickNav: () -> Unit = {},
        query: () -> String = { "" },
        entriesSize: () -> Int,
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChange: (EntrySearchOption) -> Unit = {},
        showFab: () -> Boolean = { true },
        onClickAddFab: () -> Unit = {},
        scrollBehavior: TopAppBarScrollBehavior,
        content: @Composable (PaddingValues) -> Unit,
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
                        val isNotEmpty by remember { derivedStateOf { query().isNotEmpty() } }
                        BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                            onQueryChange("")
                        }

                        var active by remember { mutableStateOf(false) }
                        DockedSearchBar(
                            query = query(),
                            onQueryChange = onQueryChange,
                            active = active,
                            onActiveChange = {},
                            leadingIcon = { NavMenuIconButton(onClickNav) },
                            placeholder = {
                                val entriesSize = entriesSize()
                                Text(
                                    if (entriesSize > 0) {
                                        stringResource(
                                            R.string.entry_search_hint_with_entry_count,
                                            entriesSize(),
                                        )
                                    } else {
                                        stringResource(R.string.entry_search_hint)
                                    }
                                )
                            },
                            trailingIcon = {
                                Row {
                                    val showClear by remember {
                                        derivedStateOf { query().isNotEmpty() }
                                    }
                                    AnimatedVisibility(showClear) {
                                        IconButton(onClick = { onQueryChange("") }) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = stringResource(
                                                    R.string.entry_search_clear
                                                ),
                                            )
                                        }
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
                                                    R.string.entry_search_options_collapse
                                                } else {
                                                    R.string.entry_search_options_expand
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
                            val options = options()
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
                                            onOptionChange(option)
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
            floatingActionButton = {
                if (showFab()) {
                    FloatingActionButton(
                        onClick = onClickAddFab,
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.entry_search_add_entry)
                        )
                    }
                }
            },
            content = content,
            modifier = Modifier.nestedScroll(
                NestedScrollSplitter(
                    primary = scrollBehavior.nestedScrollConnection,
                    consumeNone = true,
                )
            ),
        )
    }
}
