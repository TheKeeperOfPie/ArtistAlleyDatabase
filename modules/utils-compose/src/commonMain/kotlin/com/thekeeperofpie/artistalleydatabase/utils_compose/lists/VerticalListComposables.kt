@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.utils_compose.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object VerticalList {

    @Composable
    operator fun <Item : Any> invoke(
        itemHeaderText: StringResource?,
        items: LazyPagingItems<Item>,
        itemKey: (Item) -> Any,
        itemContentType: ((Item) -> Any)? = null,
        item: @Composable LazyGridItemScope.(Item?) -> Unit,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        gridState: LazyGridState = rememberLazyGridState(),
        nestedScrollConnection: NestedScrollConnection? = null,
        verticalArrangement: Arrangement.Vertical = Arrangement.Top,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
        contentPadding: PaddingValues = PaddingValues(bottom = 32.dp),
    ) {
        val refreshState = items.loadState.refresh
        val refreshing = refreshState is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)
        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyVerticalGrid(
                state = gridState,
                columns = GridUtils.standardWidthAdaptiveCells,
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
                when {
                    refreshState is LoadState.Error && items.itemCount == 0 ->
                        item(key = "errorLoading", span = GridUtils.maxSpanFunction) {
                            ErrorContent(
                                errorText = stringResource(Res.string.error_loading),
                                exception = refreshState.error,
                            )
                        }
                    refreshState is LoadState.NotLoading && items.itemCount == 0 ->
                        item(key = "errorNoResults", span = GridUtils.maxSpanFunction) {
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
                    else -> {
                        items(
                            count = items.itemCount,
                            key = items.itemKey { itemKey(it) },
                            contentType = items.itemContentType { itemContentType?.invoke(it) },
                        ) {
                            item(items[it])
                        }

                        when (items.loadState.append) {
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
                                ErrorAppend { items.retry() }
                            }
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
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
