package com.thekeeperofpie.artistalleydatabase.entry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
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
        sections: List<EntrySection>,
        entries: @Composable () -> LazyPagingItems<GridModel> =
            { emptyFlow<PagingData<GridModel>>().collectAsLazyPagingItems() },
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: GridModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: GridModel) -> Unit = { _, _ -> },
        onClickAddFab: () -> Unit = {},
        onClickClear: () -> Unit = {},
        onClickEdit: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
        onNavigate: (String) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Chrome(
            onClickNav = onClickNav,
            query = query,
            entriesSize = entriesSize,
            onQueryChange = onQueryChange,
            showFab = { selectedItems().isEmpty() },
            sections = sections,
            onClickAddFab = onClickAddFab,
            onNavigate = onNavigate,
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
        sections: List<EntrySection>,
        showFab: () -> Boolean = { true },
        onClickAddFab: () -> Unit = {},
        onNavigate: (String) -> Unit,
        scrollBehavior: TopAppBarScrollBehavior,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        BottomSheetScaffold(
            sheetContent = {
                SearchFilterSheet(sections = sections, onNavigate = onNavigate)
            },
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
                            val isNotEmpty by remember { derivedStateOf { query().isNotEmpty() } }
                            BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                                onQueryChange("")
                            }

                            StaticSearchBar(
                                query = query(),
                                onQueryChange = onQueryChange,
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
                                },
                                onSearch = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (showFab()) {
                        FloatingActionButton(
                            onClick = onClickAddFab,
                            modifier = Modifier.padding(bottom = 56.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource(R.string.entry_search_add_entry)
                            )
                        }
                    }
                },
                content = content,
            )
        }
    }

    @Composable
    private fun SearchFilterSheet(
        sections: List<EntrySection>,
        onNavigate: (String) -> Unit,
    ) {
        HorizontalDivider()
        EntryForm(
            areSectionsLoading = { false },
            sections = { sections },
            onNavigate = onNavigate,
        )
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { sections.forEach {  it.clear() } }) {
                Text(text = stringResource(UtilsStringR.clear))
            }
        }
    }
}
