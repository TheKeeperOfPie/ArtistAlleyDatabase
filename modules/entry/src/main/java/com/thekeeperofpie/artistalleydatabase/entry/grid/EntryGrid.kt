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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.mxalbert.sharedelements.FadeMode
import com.mxalbert.sharedelements.ProgressThresholds
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsTransitionSpec
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.R
import kotlinx.coroutines.launch

@Suppress("NAME_SHADOWING")
object EntryGrid {

    @Composable
    operator fun <T : EntryGridModel> invoke(
        imageScreenKey: String,
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
                    imageScreenKey = imageScreenKey,
                    entries = entries,
                    selectedItems = selectedItems,
                    onClickEntry = onClickEntry,
                    onLongClickEntry = onLongClickEntry,
                    gridState = gridState,
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .weight(1f, true)
                )
            }

            val scope = rememberCoroutineScope()
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
        imageScreenKey: String,
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
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
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
                    imageScreenKey,
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
        imageScreenKey: String,
        expectedWidth: Dimension.Pixels,
        index: Int,
        entry: T? = null,
        selectedItems: () -> Collection<Int> = { emptyList() },
        onClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onLongClickEntry: (index: Int, entry: T) -> Unit = { _, _ -> },
        onSharedElementFractionChanged: ((fraction: Float) -> Unit)? = null,
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
                        onLongClickLabel = stringResource(R.string.long_press_multi_select_label)
                    )
            ) {
                if (entry.imageUri == null) {
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
                        key = "${entry.id.scopedId}_image",
                        screenKey = imageScreenKey,
                        // Try to disable the fade animation
                        transitionSpec = SharedElementsTransitionSpec(
                            fadeMode = FadeMode.In,
                            fadeProgressThresholds = ProgressThresholds(0f, 0f),
                        ),
                        onFractionChanged = onSharedElementFractionChanged,
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(entry.imageUri)
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
                                R.string.entry_image_content_description
                            ),
                            contentScale = ContentScale.FillWidth,
                            modifier = entryModifier
                                .fillMaxWidth()
                                .heightIn(min = LocalDensity.current.run {
                                    if (entry.imageWidth != null) {
                                        (expectedWidth.px * entry.imageWidthToHeightRatio).toDp()
                                    } else {
                                        56.dp
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

                entry.ErrorIcons(modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}
