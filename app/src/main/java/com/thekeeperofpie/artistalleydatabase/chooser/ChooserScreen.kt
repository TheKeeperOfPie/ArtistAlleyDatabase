package com.thekeeperofpie.artistalleydatabase.chooser

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Checkbox
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
import androidx.paging.compose.LazyPagingItems
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchOption

@Suppress("NAME_SHADOWING")
object ChooserScreen {

    @Composable
    operator fun invoke(
        query: @Composable () -> String,
        onQueryChange: (String) -> Unit,
        options: () -> List<EntrySearchOption>,
        onOptionChange: (EntrySearchOption) -> Unit,
        entries: @Composable () -> LazyPagingItems<ArtEntryGridModel>,
        selectedItems: () -> Collection<Int>,
        onClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit,
        onLongClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit,
        onClickClear: () -> Unit,
        onClickSelect: () -> Unit,
    ) {
        Chrome(
            query = query,
            entries = entries,
            onQueryChange = onQueryChange,
            options = options,
            onOptionChange = onOptionChange,
        ) { paddingValues ->
            Column {
                EntryGrid.EntriesGrid(
                    imageScreenKey = "chooser",
                    entries = entries,
                    selectedItems = selectedItems,
                    onClickEntry = onClickEntry,
                    onLongClickEntry = onLongClickEntry,
                    modifier = Modifier
                        .padding(paddingValues)
                        .weight(1f, true)
                )


                if (selectedItems().isNotEmpty()) {
                    ButtonFooter(
                        R.string.clear to onClickClear,
                        R.string.select to onClickSelect,
                    )
                }
            }
        }
    }

    @Composable
    private fun Chrome(
        query: @Composable () -> String = { "" },
        entries: @Composable () -> LazyPagingItems<ArtEntryGridModel>,
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChange: (EntrySearchOption) -> Unit = {},
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
                        placeholder = {
                            Text(
                                text = stringResource(
                                    EntryStringR.entry_search_hint_with_entry_count,
                                    entries().itemCount,
                                ),
                            )
                        },
                        onValueChange = onQueryChange,
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
            content = content,
        )
    }
}
