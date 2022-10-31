package com.thekeeperofpie.artistalleydatabase.form.grid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.compose.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid
import com.thekeeperofpie.artistalleydatabase.compose.LazyStaggeredGrid.rememberLazyStaggeredGridState
import com.thekeeperofpie.artistalleydatabase.form.R

object EntryGrid {

    @Composable
    operator fun <T : EntryGridModel> invoke(
        imageScreenKey: String,
        columnCount: Int = 2,
        entries: @Composable () -> LazyPagingItems<T>,
        entriesSize: @Composable () -> Int?,
        paddingValues: PaddingValues,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onClickEdit: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            val lazyStaggeredGridState = rememberLazyStaggeredGridState(columnCount = columnCount)
            Column {
                EntriesGrid(
                    imageScreenKey = imageScreenKey,
                    entries = entries,
                    selectedItems = selectedItems,
                    onClickEntry = onClickEntry,
                    onLongClickEntry = onLongClickEntry,
                    lazyStaggeredGridState = lazyStaggeredGridState,
                    modifier = Modifier
                        .weight(1f, true)
                )

                if (selectedItems().isNotEmpty()) {
                    ButtonFooter(
                        R.string.delete to { showDeleteDialog = true },
                        R.string.edit to onClickEdit,
                        R.string.clear to onClickClear,
                    )
                }
            }

            entriesSize()?.let { size ->
                val stringRes = when (size) {
                    0 -> R.string.entry_results_zero
                    1 -> R.string.entry_results_one
                    else -> R.string.entry_results_multiple
                }

                Text(
                    text = stringResource(stringRes, size),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .wrapContentSize()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { lazyStaggeredGridState.scrollToTop() }
                        .padding(8.dp)
                )
            }
        }

        DeleteDialog(
            showDeleteDialog,
            { showDeleteDialog = false },
            onConfirmDelete
        )
    }

    @Composable
    fun <T : EntryGridModel> EntriesGrid(
        imageScreenKey: String,
        modifier: Modifier = Modifier,
        entries: @Composable () -> LazyPagingItems<T>,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        lazyStaggeredGridState: LazyStaggeredGrid.LazyStaggeredGridState,
    ) {
        val expectedWidth = LocalDensity.current.run {
            // TODO: Find a better way to calculate the optimal image size
            LocalConfiguration.current.screenWidthDp.dp.roundToPx() /
                    lazyStaggeredGridState.columnCount
        }.let(::Dimension)

        @Suppress("RemoveExplicitTypeArguments") // Kotlin can't infer it.id below
        LazyStaggeredGrid<T>(
            state = lazyStaggeredGridState,
            modifier = modifier,
        ) {
            items(entries(), key = { it.id }) { index, item ->
                Entry(
                    imageScreenKey,
                    expectedWidth,
                    index,
                    item,
                    selectedItems,
                    onClickEntry,
                    onLongClickEntry
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun <T : EntryGridModel> Entry(
        imageScreenKey: String,
        expectedWidth: Dimension.Pixels,
        index: Int,
        entry: T? = null,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
    ) {
        val entryModifier = Modifier.fillMaxWidth()
        if (entry == null) {
            Spacer(
                modifier = entryModifier
                    .heightIn(min = 80.dp)
                    .background(Color.LightGray)
            )
        } else {
            val selected = selectedItems().contains(index)
            Box(
                Modifier
                    .fillMaxWidth()
                    .let {
                        if (entry.localImageFile == null) {
                            it.heightIn(min = 120.dp)
                        } else it
                    }
                    .combinedClickable(
                        onClick = { onClickEntry(index, entry) },
                        onLongClick = { onLongClickEntry(index, entry) },
                        onLongClickLabel = stringResource(R.string.long_press_multi_select_label)
                    )
            ) {
                if (entry.localImageFile == null) {
                    // TODO: Better no-image placeholder
                    Text(
                        text = entry.placeholderText,
                        color = Color.Unspecified,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                    )

                    if (selected) {
                        Spacer(
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(0.25f)
                                .background(MaterialTheme.colorScheme.surfaceTint)
                        )
                    }
                } else {
                    SharedElement(
                        key = "${entry.id}_image",
                        screenKey = imageScreenKey,
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(entry.localImageFile)
                                .size(expectedWidth, Dimension.Undefined)
                                .crossfade(false)
                                .memoryCacheKey("coil_memory_entry_image_home_${entry.id}")
                                .build(),
                            contentDescription = stringResource(
                                R.string.entry_image_content_description
                            ),
                            contentScale = ContentScale.FillWidth,
                            modifier = entryModifier
                                .fillMaxWidth()
                                .heightIn(min = LocalDensity.current.run {
                                    if (entry.imageWidth != null) {
                                        (expectedWidth.px * entry.imageWidthToHeightRatio).toDp()
                                    } else {
                                        0.dp
                                    }
                                })
                                .alpha(if (selected) 0.38f else 1f)
                                .semantics { this.selected = selected }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = selected,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.selected_content_description),
                    )
                }
            }
        }
    }

    @Composable
    fun DeleteDialog(
        showDeleteDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirmDelete: () -> Unit
    ) {
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.delete_dialog_title)) },
                confirmButton = {
                    TextButton(onClick = {
                        onDismiss()
                        onConfirmDelete()
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}
