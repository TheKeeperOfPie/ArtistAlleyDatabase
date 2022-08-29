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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import com.thekeeperofpie.artistalleydatabase.art.grid.ArtEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGrid
import com.thekeeperofpie.artistalleydatabase.form.search.EntrySearchOption
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
object ChooserScreen {

    @Composable
    operator fun invoke(
        columnCount: Int = 2,
        query: @Composable () -> String = { "" },
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChanged: (EntrySearchOption) -> Unit = {},
        entries: @Composable () -> LazyPagingItems<ArtEntryGridModel> =
            { emptyFlow<PagingData<ArtEntryGridModel>>().collectAsLazyPagingItems() },
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryGridModel) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onClickSelect: () -> Unit = {},
    ) {
        Chrome(
            query = query,
            onQueryChange = onQueryChange,
            options = options,
            onOptionChanged = onOptionChanged,
        ) { paddingValues ->
            Column {
                EntryGrid.EntriesGrid(
                    imageScreenKey = "chooser",
                    columnCount = columnCount,
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
        onQueryChange: (String) -> Unit = {},
        options: () -> List<EntrySearchOption> = { emptyList() },
        onOptionChanged: (EntrySearchOption) -> Unit = {},
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
                                bottomBorder(1.dp, MaterialTheme.colorScheme.onBackground)
                            } else this
                        }
                ) {
                    val appBarColors = TopAppBarDefaults.smallTopAppBarColors()
                    TextField(
                        query(),
                        placeholder = { Text(stringResource(id = R.string.search)) },
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
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = appBarColors.containerColor(
                                colorTransitionFraction = 0f
                            ).value
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
            content = content,
        )
    }
}