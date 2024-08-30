package com.thekeeperofpie.artistalleydatabase.entry

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import artistalleydatabase.modules.utils_compose.generated.resources.clear
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridDeleteDialog
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object EntryHomeScreen {

    @Composable
    operator fun <GridModel : EntryGridModel> invoke(
        onClickNav: () -> Unit,
        query: () -> String,
        onQueryChange: (String) -> Unit,
        sections: List<EntrySection>,
        entries: @Composable () -> LazyPagingItems<GridModel>,
        selectedItems: () -> Collection<Int>,
        onClickEntry: (index: Int, entry: GridModel) -> Unit,
        onLongClickEntry: (index: Int, entry: GridModel) -> Unit,
        onClickAddFab: () -> Unit,
        onClickClear: () -> Unit,
        onClickEdit: () -> Unit,
        onConfirmDelete: () -> Unit,
        onNavigate: (String) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Chrome(
            onClickNav = onClickNav,
            query = query,
            entries = entries,
            onQueryChange = onQueryChange,
            sections = sections,
            onClickAddFab = onClickAddFab,
            onNavigate = onNavigate,
            scrollBehavior = scrollBehavior,
            onClickClear = onClickClear,
            onClickEdit = onClickEdit,
            onConfirmDelete = onConfirmDelete,
            selectedItems = selectedItems,
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
                entries = entries,
                entriesSize = {
                    val isQueryNotEmpty by remember { derivedStateOf { query().isNotEmpty() } }
                    entries().itemCount.takeIf { isQueryNotEmpty }
                },
                contentPadding = topBarPadding,
                topOffset = topOffset,
                selectedItems = selectedItems,
                onClickEntry = onClickEntry,
                onLongClickEntry = onLongClickEntry,
            )
        }
    }

    @Composable
    private fun <GridModel : EntryGridModel> Chrome(
        onClickNav: () -> Unit,
        query: () -> String,
        entries: @Composable () -> LazyPagingItems<GridModel>,
        onQueryChange: (String) -> Unit,
        sections: List<EntrySection>,
        onClickAddFab: () -> Unit,
        onNavigate: (String) -> Unit,
        scrollBehavior: TopAppBarScrollBehavior,
        selectedItems: () -> Collection<Int>,
        onClickClear: () -> Unit,
        onClickEdit: () -> Unit,
        onConfirmDelete: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val bottomSheetState = rememberStandardBottomSheetState(
            confirmValueChange = { it != SheetValue.Hidden },
            skipHiddenState = true,
        )
        val scope = rememberCoroutineScope()
        BackHandler(bottomSheetState.targetValue == SheetValue.Expanded) {
            scope.launch {
                bottomSheetState.partialExpand()
            }
        }
        BottomSheetScaffold(
            scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
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
            val isEditMode by remember { derivedStateOf { selectedItems().isNotEmpty() } }
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
                                leadingIcon = {
                                    if (isEditMode) {
                                        IconButton(onClick = onClickClear) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = stringResource(
                                                    R.string.entry_search_edit_mode_clear
                                                ),
                                            )
                                        }
                                    } else {
                                        NavMenuIconButton(onClickNav)
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(
                                            if (isEditMode) {
                                                R.string.entry_search_hint_edit_mode_selected
                                            } else {
                                                R.string.entry_search_hint_with_entry_count
                                            },
                                            if (isEditMode) {
                                                selectedItems().size
                                            } else {
                                                entries().itemCount
                                            },
                                        ),
                                    )
                                },
                                trailingIcon = {
                                    val showClear by remember {
                                        derivedStateOf { query().isNotEmpty() }
                                    }
                                    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
                                    if (isEditMode) {
                                        Row {
                                            IconButton(onClick = { showDeleteDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = stringResource(
                                                        R.string.entry_search_edit_mode_delete
                                                    )
                                                )
                                            }

                                            if (showDeleteDialog) {
                                                EntryGridDeleteDialog(
                                                    { showDeleteDialog = false },
                                                    onConfirmDelete
                                                )
                                            }

                                            IconButton(onClick = onClickEdit) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = stringResource(
                                                        R.string.entry_search_edit_mode_edit
                                                    )
                                                )
                                            }
                                        }
                                    } else {
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
                                    }
                                },
                                onSearch = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (!isEditMode) {
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
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            EntryForm(
                areSectionsLoading = { false },
                sections = { sections },
                onNavigate = onNavigate,
            )
        }
        HorizontalDivider()
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { sections.forEach { it.clear() } }) {
                Text(text = ComposeResourceUtils.stringResource(UtilsStrings.clear))
            }
        }
    }
}
