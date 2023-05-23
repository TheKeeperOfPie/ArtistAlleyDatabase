package com.thekeeperofpie.artistalleydatabase.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchOption
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
object HomeScreen {

    @Composable
    operator fun <GridModel : EntryGridModel> invoke(
        onClickNav: () -> Unit = {},
        query: @Composable () -> String = { "" },
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
        Chrome(
            onClickNav = onClickNav,
            query = query,
            onQueryChange = onQueryChange,
            showFab = { selectedItems().isEmpty() },
            options = options,
            onOptionChange = onOptionChange,
            onClickAddFab = onClickAddFab,
        ) {
            EntryGrid(
                imageScreenKey = NavDestinations.HOME,
                entries = entries,
                entriesSize = { entries().itemCount.takeIf { query().isNotEmpty() }},
                paddingValues = it,
                selectedItems = selectedItems,
                onClickEntry = onClickEntry,
                onLongClickEntry = onLongClickEntry,
                onClickClear = onClickClear,
                onClickEdit = onClickEdit,
                onConfirmDelete = onConfirmDelete,
                lazyStaggeredGridState = lazyStaggeredGridState,
            )
        }
    }

    @Composable
    private fun Chrome(
        onClickNav: () -> Unit = {},
        query: @Composable () -> String = { "" },
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChange: (EntrySearchOption) -> Unit = {},
        showFab: () -> Boolean = { true },
        onClickAddFab: () -> Unit = {},
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                var showOptions by rememberSaveable { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .run {
                            if (showOptions) {
                                bottomBorder(MaterialTheme.colorScheme.onBackground)
                            } else this
                        }
                ) {
                    TextField(
                        query(),
                        placeholder = { Text(stringResource(id = R.string.search)) },
                        onValueChange = onQueryChange,
                        leadingIcon = { NavMenuIconButton(onClickNav) },
                        trailingIcon = {
                            if (options().isNotEmpty()) {
                                IconButton(onClick = { showOptions = !showOptions }) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = stringResource(
                                            R.string.search_filter_content_description
                                        )
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { showOptions = false }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    AnimatedVisibility(
                        visible = showOptions,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            options().forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
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
                            contentDescription = stringResource(R.string.search_add_entry)
                        )
                    }
                }
            },
            content = content,
        )
    }
}
