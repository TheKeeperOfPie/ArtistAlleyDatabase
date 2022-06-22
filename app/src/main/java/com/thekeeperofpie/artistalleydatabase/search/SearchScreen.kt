package com.thekeeperofpie.artistalleydatabase.search

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Checkbox
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.ui.theme.ArtistAlleyDatabaseTheme
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
object SearchScreen {

    @Composable
    operator fun invoke(
        query: String = "",
        onQueryChange: (String) -> Unit = {},
        onSearchFocusChange: (FocusState) -> Unit = {},
        showOptions: Boolean = false,
        options: List<Option> = emptyList(),
        onOptionChanged: (Option) -> Unit = {},
        entries: LazyPagingItems<ArtEntryModel> =
            emptyFlow<PagingData<ArtEntryModel>>().collectAsLazyPagingItems(),
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onClickAddFab: () -> Unit = {},
        onClickClear: () -> Unit = {},
        onClickDelete: () -> Unit = {},
    ) {
        ArtistAlleyDatabaseTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Chrome(
                    query = query,
                    onQueryChange = onQueryChange,
                    showFab = selectedItems.isEmpty(),
                    showOptions = showOptions,
                    options = options,
                    onOptionChanged = onOptionChanged,
                    onSearchFocusChange = onSearchFocusChange,
                    onClickAddFab = onClickAddFab,
                ) {
                    Content(
                        entries = entries,
                        paddingValues = it,
                        selectedItems = selectedItems,
                        onClickEntry = onClickEntry,
                        onLongClickEntry = onLongClickEntry,
                        onClickClear = onClickClear,
                        onClickDelete = onClickDelete,
                    )
                }
            }
        }
    }

    @Composable
    private fun Chrome(
        query: String,
        onQueryChange: (String) -> Unit,
        onSearchFocusChange: (FocusState) -> Unit,
        showOptions: Boolean = false,
        options: List<Option> = emptyList(),
        onOptionChanged: (Option) -> Unit,
        showFab: Boolean = true,
        onClickAddFab: () -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                ) {
                    TextField(
                        query,
                        placeholder = { Text(stringResource(id = R.string.search)) },
                        onValueChange = onQueryChange,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged(onSearchFocusChange),
                    )

                    if (showOptions) {
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

    @Composable
    private fun Content(
        columnCount: Int = 2,
        entries: LazyPagingItems<ArtEntryModel>,
        paddingValues: PaddingValues,
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onClickDelete: () -> Unit = {},
    ) {
        val expectedWidth = LocalDensity.current.run {
            // Load at half width for better scrolling performance
            // TODO: Find a better way to calculate the optimal image size
            LocalConfiguration.current.screenWidthDp.dp.roundToPx() / columnCount / 2
        }.let(::Dimension)
        Column {
            LazyStaggeredGrid<ArtEntryModel>(
                columnCount = columnCount,
                modifier = Modifier
                    .padding(paddingValues)
                    .weight(1f, true)
            ) {
                items(entries, key = { it.value.id }) { index, item ->
                    ArtEntry(
                        expectedWidth,
                        index,
                        item,
                        selectedItems,
                        onClickEntry,
                        onLongClickEntry
                    )
                }
            }

            if (selectedItems.isNotEmpty()) {
                ButtonFooter(
                    R.string.clear to onClickClear,
                    R.string.delete to onClickDelete,
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun ArtEntry(
        expectedWidth: Dimension.Pixels,
        index: Int,
        entry: ArtEntryModel? = null,
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
    ) {
        val entryModifier = Modifier.fillMaxWidth()
        if (entry == null) {
            Spacer(
                modifier = entryModifier
                    .background(Color.LightGray)
            )
        } else {
            Box(Modifier.fillMaxWidth()) {
                val selected = selectedItems.contains(index)

                SharedElement(
                    key = "${entry.value.id}_image",
                    screenKey = NavDestinations.SEARCH
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(entry.localImageFile)
                            .size(expectedWidth, Dimension.Undefined)
                            .crossfade(true)
                            .memoryCacheKey("coil_memory_entry_image_search_${entry.value.id}")
                            .build(),
                        contentDescription = stringResource(
                            R.string.art_entry_image_content_description
                        ),
                        contentScale = ContentScale.FillWidth,
                        modifier = entryModifier
                            .fillMaxWidth()
                            .heightIn(min = LocalDensity.current.run {
                                if (entry.value.imageWidth != null) {
                                    (expectedWidth.px * entry.value.imageWidthToHeightRatio).toDp()
                                } else {
                                    0.dp
                                }
                            })
                            .alpha(if (selected) ContentAlpha.disabled else 1f)
                            .semantics { this.selected = selected }
                            .combinedClickable(
                                onClick = { onClickEntry(index, entry) },
                                onLongClick = { onLongClickEntry(index, entry) },
                                onLongClickLabel = stringResource(
                                    R.string.art_entry_long_press_multi_select_label
                                )
                            )
                    )
                }

                Crossfade(
                    targetState = selected,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp)
                ) {
                    if (it) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = stringResource(
                                R.string.art_entry_selected_content_description
                            ),
                        )
                    }
                }
            }
        }
    }

    class Option(@StringRes val textRes: Int) {
        var enabled by mutableStateOf(true)
    }
}

@Preview
@Composable
fun Preview() {
    SearchScreen(
        showOptions = true,
        options = listOf(
            SearchScreen.Option(R.string.search_option_artists),
            SearchScreen.Option(R.string.search_option_source),
            SearchScreen.Option(R.string.search_option_series),
            SearchScreen.Option(R.string.search_option_characters),
            SearchScreen.Option(R.string.search_option_tags),
            SearchScreen.Option(R.string.search_option_notes),
            SearchScreen.Option(R.string.search_option_other),
        )
    )
}