package com.thekeeperofpie.artistalleydatabase.art

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
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
import androidx.compose.runtime.remember
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
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.navigation.NavDestinations
import com.thekeeperofpie.artistalleydatabase.ui.ButtonFooter
import com.thekeeperofpie.artistalleydatabase.ui.LazyStaggeredGrid

object ArtEntryGrid {

    @Composable
    operator fun invoke(
        columnCount: Int = 2,
        entries: LazyPagingItems<ArtEntryModel>,
        paddingValues: PaddingValues,
        selectedItems: Collection<Int> = emptyList(),
        onClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: ArtEntryModel) -> Unit = { _, _ -> },
        onClickClear: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        val expectedWidth = LocalDensity.current.run {
            // Load at half width for better scrolling performance
            // TODO: Find a better way to calculate the optimal image size
            LocalConfiguration.current.screenWidthDp.dp.roundToPx() / columnCount / 2
        }.let(::Dimension)
        var showDeleteDialog by remember { mutableStateOf(false) }

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
                    R.string.delete to { showDeleteDialog = true },
                )
            }
        }

        DeleteDialog(
            showDeleteDialog,
            { showDeleteDialog = false },
            onConfirmDelete
        )
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
                    .heightIn(min = 80.dp)
                    .background(Color.LightGray)
            )
        } else {
            val selected = selectedItems.contains(index)
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
                        onLongClickLabel = stringResource(
                            R.string.art_entry_long_press_multi_select_label
                        )
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
                        key = "${entry.value.id}_image",
                        screenKey = NavDestinations.HOME
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(entry.localImageFile)
                                .size(expectedWidth, Dimension.Undefined)
                                .crossfade(true)
                                .memoryCacheKey("coil_memory_entry_image_home_${entry.value.id}")
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
                        )
                    }
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

    @Composable
    fun DeleteDialog(
        showDeleteDialog: Boolean,
        onDismiss: () -> Unit,
        onConfirmDelete: () -> Unit
    ) {
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.art_entry_delete_dialog_title)) },
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
