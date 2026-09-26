package com.thekeeperofpie.artistalleydatabase.alley.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaggeredGridCellsAdaptiveWithMin
import com.thekeeperofpie.artistalleydatabase.utils_scrollbars.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils_scrollbars.rememberScrollbarState

@Composable
fun <T : Any> SearchList(
    header: @Composable () -> Unit,
    entries: LazyPagingItems<T>,
    displayType: () -> SearchDisplayType,
    forceOneDisplayColumn: () -> Boolean,
    itemToId: (T) -> Any,
    unfilteredCount: () -> Int,
    gridState: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
    noResultsItem: (@Composable () -> Unit)? = null,
    moreResultsItem: (@Composable () -> Unit)? = null,
    itemRow: @Composable (T) -> Unit,
) {
    Row(modifier = modifier) {
        val width = LocalWindowConfiguration.current.screenWidthDp
        val horizontalContentPadding = if (width > 1200.dp) {
            (width - 1200.dp) / 2
        } else {
            0.dp
        }
        val scrollbarState = rememberScrollbarState(gridState)
        val displayType = displayType()
        LazyVerticalStaggeredGrid(
            columns = displayType.columns(forceOneDisplayColumn()),
            state = gridState,
            contentPadding = displayType.contentPadding(horizontalContentPadding),
            verticalItemSpacing = displayType.verticalItemSpacing,
            horizontalArrangement = displayType.horizontalArrangement,
            modifier = Modifier.weight(1f)
        ) {
            item("header", span = StaggeredGridItemSpan.FullLine) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    header()
                    if (entries.loadState.refresh is LoadState.Loading) {
                        InfiniteProgressIndicator()
                    }
                }
            }

            val showMoreResultsItem = entries.loadState.refresh !is LoadState.Loading
                    && moreResultsItem != null && unfilteredCount() > entries.itemCount
            if (!showMoreResultsItem && entries.itemCount == 0) {
                item("searchNoResults", span = StaggeredGridItemSpan.FullLine) {
                    if (noResultsItem != null) {
                        noResultsItem()
                    } else {
                        SearchNoResults()
                    }
                }
            }

            items(
                count = entries.itemCount,
                key = entries.itemKey { itemToId(it) },
                contentType = entries.itemContentType { "search_entry" },
            ) { index ->
                itemRow(entries[index] ?: return@items)
            }

            if (showMoreResultsItem) {
                item("searchMoreResults", span = StaggeredGridItemSpan.FullLine) {
                    moreResultsItem()
                }
            }
        }

        PrimaryVerticalScrollbar(scrollbarState = scrollbarState)
    }
}

private fun SearchDisplayType.columns(forceOneDisplayColumn: Boolean) = if (forceOneDisplayColumn) {
    StaggeredGridCells.Fixed(1)
} else {
    when (this) {
        SearchDisplayType.LIST,
        SearchDisplayType.CARD,
            -> StaggeredGridCells.Adaptive(330.dp)
        SearchDisplayType.IMAGE,
            -> StaggeredGridCellsAdaptiveWithMin(300.dp, 2)
        SearchDisplayType.TABLE -> throw IllegalArgumentException()
    }
}

private fun SearchDisplayType.contentPadding(horizontalContentPadding: Dp) = when (this) {
    SearchDisplayType.LIST,
    SearchDisplayType.IMAGE,
        -> PaddingValues(
        top = 8.dp,
        start = horizontalContentPadding,
        end = horizontalContentPadding,
        bottom = 200.dp,
    )
    SearchDisplayType.CARD,
        -> PaddingValues(
        start = 16.dp + horizontalContentPadding,
        end = 16.dp + horizontalContentPadding,
        top = 8.dp,
        bottom = 200.dp,
    )
    SearchDisplayType.TABLE -> throw IllegalArgumentException()
}

private val SearchDisplayType.verticalItemSpacing
    get() = when (this) {
        SearchDisplayType.LIST,
        SearchDisplayType.IMAGE,
            -> 0.dp
        SearchDisplayType.CARD,
            -> 8.dp
        SearchDisplayType.TABLE -> throw IllegalArgumentException()
    }

private val SearchDisplayType.horizontalArrangement
    get() = when (this) {
        SearchDisplayType.CARD,
            -> 8.dp
        SearchDisplayType.LIST,
        SearchDisplayType.IMAGE,
            -> 0.dp
        SearchDisplayType.TABLE -> throw IllegalArgumentException()
    }.let(Arrangement::spacedBy)
