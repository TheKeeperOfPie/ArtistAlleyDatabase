@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.error_loading
import artistalleydatabase.modules.utils_compose.generated.resources.no_results
import artistalleydatabase.modules.utils_compose.generated.resources.retry
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.items
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object VerticalList {

    @Composable
    operator fun <Item : Any> invoke(
        itemHeaderText: StringResource?,
        items: LazyPagingItems<Item>,
        itemKey: (Item) -> Any,
        itemContentType: ((Item) -> String)? = null,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        gridState: LazyGridState = rememberLazyGridState(),
        columns: GridCells = GridUtils.standardWidthAdaptiveCells,
        nestedScrollConnection: NestedScrollConnection? = null,
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
        placeholderCount: Int = 10,
        showScrollbar: Boolean = false,
        item: @Composable LazyGridItemScope.(Item?) -> Unit,
    ) = VerticalList(
        itemHeaderText = itemHeaderText,
        itemCount = { items.itemCount },
        itemAtIndex = { items[it] },
        itemKey = items.itemKey(itemKey),
        itemContentType = items.itemContentType { itemContentType?.invoke(it) },
        onRefresh = onRefresh,
        modifier = modifier,
        gridState = gridState,
        columns = columns,
        nestedScrollConnection = nestedScrollConnection,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        placeholderCount = placeholderCount,
        pullRefreshState =
            rememberPullRefreshState(items.loadState.refresh is LoadState.Loading, onRefresh),
        pullRefreshIndicator = { refreshing, pullRefreshState ->
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        refreshState = { items.loadState.refresh },
        appendState = { items.loadState.append },
        showScrollbar = showScrollbar,
        item = item,
    )

    @Composable
    operator fun <Item : Any> invoke(
        itemHeaderText: StringResource?,
        items: List<Item>?,
        itemKey: (Item) -> Any,
        itemContentType: ((Item) -> String)? = null,
        modifier: Modifier = Modifier,
        gridState: LazyGridState = rememberLazyGridState(),
        columns: GridCells = GridUtils.standardWidthAdaptiveCells,
        nestedScrollConnection: NestedScrollConnection? = null,
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
        placeholderCount: Int = 10,
        showScrollbar: Boolean = false,
        item: @Composable LazyGridItemScope.(Item?) -> Unit,
    ) = VerticalList(
        itemHeaderText = itemHeaderText,
        itemCount = { items?.size ?: 0 },
        itemAtIndex = { items?.getOrNull(it) },
        itemKey = { items?.getOrNull(it)?.let(itemKey) },
        itemContentType = { items?.getOrNull(it)?.let { itemContentType?.invoke(it) } },
        onRefresh = {},
        modifier = modifier,
        gridState = gridState,
        columns = columns,
        nestedScrollConnection = nestedScrollConnection,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding,
        placeholderCount = placeholderCount,
        pullRefreshState = null,
        pullRefreshIndicator = null,
        refreshState = { if (items == null) LoadState.Loading else LoadState.NotLoading(true) },
        appendState = { LoadState.NotLoading(true) },
        showScrollbar = showScrollbar,
        item = item,
    )

    @Composable
    private fun <Item : Any> VerticalList(
        itemHeaderText: StringResource?,
        itemCount: () -> Int,
        itemAtIndex: (index: Int) -> Item?,
        itemKey: (index: Int) -> Any?,
        itemContentType: ((index: Int) -> Any?)? = null,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        gridState: LazyGridState = rememberLazyGridState(),
        columns: GridCells = GridUtils.standardWidthAdaptiveCells,
        nestedScrollConnection: NestedScrollConnection? = null,
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
        placeholderCount: Int = 10,
        pullRefreshState: PullRefreshState?,
        pullRefreshIndicator: (@Composable BoxScope.(
            refreshing: Boolean,
            pullRefreshState: PullRefreshState,
        ) -> Unit)? = { refreshing, pullRefreshState ->
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        },
        refreshState: () -> LoadState,
        appendState: () -> LoadState,
        showScrollbar: Boolean = false,
        item: @Composable LazyGridItemScope.(Item?) -> Unit,
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .conditionallyNonNull(pullRefreshState) { pullRefresh(it) }
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = columns,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier
                    .conditionallyNonNull(nestedScrollConnection) { nestedScroll(it) }
            ) {
                if (itemHeaderText != null) {
                    item(
                        key = "header",
                        span = GridUtils.maxSpanFunction,
                        contentType = "detailsSectionHeader",
                    ) {
                        DetailsSectionHeader(text = stringResource(itemHeaderText))
                    }
                }
                val refreshState = refreshState()
                when {
                    refreshState is LoadState.Error && itemCount() == 0 ->
                        item(key = "errorLoading", span = GridUtils.maxSpanFunction) {
                            ErrorContent(
                                errorText = stringResource(Res.string.error_loading),
                                exception = refreshState.error,
                            )
                        }
                    refreshState is LoadState.NotLoading && itemCount() == 0 ->
                        item(key = "errorNoResults", span = GridUtils.maxSpanFunction) {
                            NoResults()
                        }
                    else -> {
                        items(
                            itemCount = itemCount,
                            itemAtIndex = itemAtIndex,
                            refreshState = refreshState,
                            placeholderCount = placeholderCount,
                            key = itemKey,
                            contentType = { itemContentType?.invoke(it) ?: "defaultType" },
                        ) {
                            item(it)
                        }

                        when (appendState()) {
                            is LoadState.Loading -> item(
                                key = "load_more_append",
                                span = GridUtils.maxSpanFunction
                            ) {
                                LoadingMore()
                            }
                            is LoadState.Error -> item(
                                key = "load_more_error",
                                span = GridUtils.maxSpanFunction
                            ) {
                                ErrorAppend(onRefresh)
                            }
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
            if (pullRefreshState != null && pullRefreshIndicator != null) {
                pullRefreshIndicator(this, refreshState() is LoadState.Loading, pullRefreshState)
            }

            if (showScrollbar) {
                VerticalScrollbar(
                    state = gridState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }

    @Composable
    private fun LoadingMore() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun NoResults() {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(Res.string.no_results),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
            )
        }
    }

    @Composable
    fun ErrorContent(
        errorText: String,
        exception: Throwable? = null,
        onRetry: (() -> Unit)? = null,
    ) {
        Column {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )

            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(Res.string.retry))
                }
            }

            if (exception != null) {
                Text(
                    text = exception.stackTraceToString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }

    @Composable
    private fun ErrorAppend(onRetry: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRetry),
        ) {
            Text(
                stringResource(Res.string.error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }
}
