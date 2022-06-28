package com.thekeeperofpie.artistalleydatabase.home

import androidx.annotation.StringRes
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryGrid
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.ui.bottomBorder
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
object HomeScreen {

    @Composable
    operator fun invoke(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        options: List<SearchOption> = emptyList(),
        onOptionChanged: (SearchOption) -> Unit = {},
        entries: LazyPagingItems<ArtEntryModel> =
            emptyFlow<PagingData<ArtEntryModel>>().collectAsLazyPagingItems(),
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onClickAddFab: () -> Unit = {},
        onClickClear: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        Chrome(
            query = query,
            onQueryChange = onQueryChange,
            showFab = selectedItems.isEmpty(),
            options = options,
            onOptionChanged = onOptionChanged,
            onClickAddFab = onClickAddFab,
        ) {
            ArtEntryGrid(
                entries = entries,
                paddingValues = it,
                selectedItems = selectedItems,
                onClickEntry = onClickEntry,
                onLongClickEntry = onLongClickEntry,
                onClickClear = onClickClear,
                onConfirmDelete = onConfirmDelete,
            )
        }
    }

    @Composable
    private fun Chrome(
        query: String,
        onQueryChange: (String) -> Unit,
        options: List<SearchOption> = emptyList(),
        onOptionChanged: (SearchOption) -> Unit,
        showFab: Boolean = true,
        onClickAddFab: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                var showOptions by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .run {
                            if (showOptions) {
                                bottomBorder(1.dp, MaterialTheme.colorScheme.onBackground)
                            } else this
                        }
                ) {
                    TextField(
                        query,
                        placeholder = { Text(stringResource(id = R.string.search)) },
                        onValueChange = onQueryChange,
                        trailingIcon = {
                            if (options.isNotEmpty()) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    AnimatedVisibility(
                        visible = showOptions,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        Column {
                            options.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            option.enabled = !option.enabled
                                            onOptionChanged(option)
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
                if (showFab) {
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
            content = content
        )
    }

    class SearchOption(@StringRes val textRes: Int, enabled: Boolean = true) {
        var enabled by mutableStateOf(enabled)
    }
}

@Preview
@Composable
fun Preview() {
    HomeScreen(
        options = listOf(
            HomeScreen.SearchOption(R.string.search_option_artists),
            HomeScreen.SearchOption(R.string.search_option_source),
            HomeScreen.SearchOption(R.string.search_option_series),
            HomeScreen.SearchOption(R.string.search_option_characters),
            HomeScreen.SearchOption(R.string.search_option_tags),
            HomeScreen.SearchOption(R.string.search_option_notes),
            HomeScreen.SearchOption(R.string.search_option_other),
        )
    )
}