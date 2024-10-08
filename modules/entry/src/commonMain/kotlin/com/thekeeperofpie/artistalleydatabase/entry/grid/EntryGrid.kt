package com.thekeeperofpie.artistalleydatabase.entry.grid

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
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.entry_image_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_results_multiple
import artistalleydatabase.modules.entry.generated.resources.entry_results_one
import artistalleydatabase.modules.entry.generated.resources.entry_results_zero
import artistalleydatabase.modules.entry.generated.resources.long_press_multi_select_label
import artistalleydatabase.modules.entry.generated.resources.selected_content_description
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Suppress("NAME_SHADOWING")
object EntryGrid {

    @Composable
    operator fun <T : EntryGridModel> invoke(
        entries: @Composable () -> LazyPagingItems<T>,
        entriesSize: @Composable () -> Int?,
        paddingValues: PaddingValues? = null,
        contentPadding: PaddingValues? = null,
        topOffset: Dp = 0.dp,
        selectedItems: () -> Collection<Int>,
        onClickEntry: (index: Int, entry: T) -> Unit,
        onLongClickEntry: (index: Int, entry: T) -> Unit,
    ) {
        val gridState = rememberLazyStaggeredGridState()
        Box(
            modifier = Modifier
                .conditionally(paddingValues != null) {
                    padding(paddingValues!!)
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                EntriesGrid(
                    modifier = Modifier
                        .weight(1f, true),
                    contentPadding = contentPadding,
                    entries = entries,
                    selectedItems = selectedItems,
                    onClickEntry = onClickEntry,
                    onLongClickEntry = onLongClickEntry,
                    gridState = gridState
                )
            }

            val scope = rememberCoroutineScope()
            entriesSize()?.let { size ->
                val stringRes = when (size) {
                    0 -> Res.string.entry_results_zero
                    1 -> Res.string.entry_results_one
                    else -> Res.string.entry_results_multiple
                }

                Text(
                    text = stringResource(stringRes, size),
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .wrapContentSize()
                        .padding(top = 8.dp + topOffset)
                        .background(
                            MaterialTheme.colorScheme.secondary,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { scope.launch { gridState.scrollToItem(0, 0) } }
                        .padding(8.dp)
                )
            }
        }
    }

    @Composable
    fun <T : EntryGridModel> EntriesGrid(
        modifier: Modifier = Modifier,
        contentPadding: PaddingValues? = null,
        entries: @Composable () -> LazyPagingItems<T>,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    ) {
        val expectedWidth = LocalDensity.current.run {
            // TODO: Find a better way to calculate the optimal image size
            val screenWidth = LocalWindowConfiguration.current.screenWidthDp
            val columns = (screenWidth / 160.dp).toInt()
            screenWidth.roundToPx() / columns
        }.let(::Dimension)

        val entries = entries()
        val columns = StaggeredGridCells.Adaptive(160.dp)
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = columns,
            contentPadding = contentPadding ?: PaddingValues(0.dp),
            modifier = modifier,
        ) {
            items(
                count = entries.itemCount,
                key = entries.itemKey { it.id.scopedId },
                contentType = entries.itemContentType { "entry" }
            ) {
                Entry(
                    expectedWidth,
                    it,
                    entries[it],
                    selectedItems,
                    onClickEntry,
                    onLongClickEntry
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun <T : EntryGridModel> Entry(
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
                        if (entry.imageUri == null) {
                            it.heightIn(min = 120.dp)
                        } else it
                    }
                    .combinedClickable(
                        onClick = { onClickEntry(index, entry) },
                        onLongClick = { onLongClickEntry(index, entry) },
                        onLongClickLabel = stringResource(Res.string.long_press_multi_select_label)
                    )
            ) {
                val imageUri = entry.imageUri
                if (imageUri == null) {
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
                    val sharedTransitionKey = SharedTransitionKey.makeKeyForId(entry.id.scopedId)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(imageUri)
                            .size(expectedWidth, Dimension.Undefined)
                            .crossfade(true)
                            .memoryCacheKey(
                                EntryUtils.getImageCacheKey(
                                    entry,
                                    entry.imageWidth ?: -1,
                                    entry.imageHeight ?: -1,
                                )
                            )
                            .build(),
                        contentDescription = stringResource(
                            Res.string.entry_image_content_description
                        ),
                        contentScale = ContentScale.FillWidth,
                        modifier = entryModifier
                            .alpha(if (selected) 0.38f else 1f)
                            .semantics { this.selected = selected }
                            .sharedElement(sharedTransitionKey, "entry_image")
                            .fillMaxWidth()
                            .heightIn(min = LocalDensity.current.run {
                                if (entry.imageWidth != null) {
                                    (expectedWidth.px * entry.imageWidthToHeightRatio).toDp()
                                } else {
                                    56.dp
                                }
                            })
                    )
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
                        contentDescription = stringResource(Res.string.selected_content_description),
                    )
                }

                entry.ErrorIcons(modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}
