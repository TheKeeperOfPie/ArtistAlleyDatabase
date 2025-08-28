package com.thekeeperofpie.artistalleydatabase.utils_compose.lists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import artistalleydatabase.modules.utils_compose.generated.resources.Res
import artistalleydatabase.modules.utils_compose.generated.resources.error_loading
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.getOrNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val LocalDefaultPageSize = compositionLocalWithComputedDefaultOf {
    val configuration = LocalWindowConfiguration.currentValue
    val screenWidthDp = configuration.screenWidthDp
    PageSize.Fixed(420.dp.coerceAtMost(screenWidthDp - 32.dp))
}

@Composable
fun <T : Any> HorizontalPagerItemsRow(
    title: StringResource,
    viewAllRoute: NavDestination,
    viewAllContentDescription: StringResource,
    items: List<T>?,
    pagerState: PagerState = rememberPagerState(data = items, placeholderCount = 3),
    pageSize: PageSize? = null,
    contentPadding: PaddingValues? = null,
    itemContent: @Composable (T?) -> Unit,
) {
    HorizontalPagerItemsRow(
        title = title,
        viewAllRoute = viewAllRoute,
        viewAllContentDescription = viewAllContentDescription,
        pagerState = pagerState,
        pageSize = pageSize,
        contentPadding = contentPadding,
        loading = null,
        error = null,
        item = { items?.getOrNull(it) },
        itemContent = itemContent,
    )
}

@Composable
fun <T : Any> HorizontalPagerItemsRow(
    title: StringResource,
    viewAllRoute: NavDestination,
    viewAllContentDescription: StringResource,
    items: LazyPagingItems<T>,
    pagerState: PagerState = rememberPagerState(data = items, placeholderCount = 3),
    pageSize: PageSize? = null,
    contentPadding: PaddingValues? = null,
    showLoadingBar: Boolean = false,
    itemContent: @Composable (T?) -> Unit,
) {
    val refreshState = items.loadState.source.refresh
    HorizontalPagerItemsRow(
        title = title,
        viewAllRoute = viewAllRoute,
        viewAllContentDescription = viewAllContentDescription,
        pagerState = pagerState,
        pageSize = pageSize,
        contentPadding = contentPadding,
        loading = if (showLoadingBar) {
            {
                val loading = refreshState is LoadState.Loading
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        } else null,
        error = if (refreshState is LoadState.Error) {
            {
                Text(
                    text = refreshState.error.message ?: stringResource(Res.string.error_loading),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        } else null,
        item = { items.getOrNull(it) },
        itemContent = itemContent,
    )
}

@Composable
fun <T : Any> HorizontalPagerItemsRow(
    title: StringResource,
    viewAllRoute: NavDestination,
    viewAllContentDescription: StringResource,
    pagerState: PagerState,
    pageSize: PageSize? = null,
    contentPadding: PaddingValues?,
    loading: (@Composable BoxScope.() -> Unit)?,
    error: (@Composable () -> Unit)?,
    item: (index: Int) -> T?,
    itemContent: @Composable (T?) -> Unit,
) {
    Column {
        Box {
            NavigationHeader(
                titleRes = title,
                viewAllRoute = viewAllRoute,
                viewAllContentDescriptionTextRes = viewAllContentDescription,
            )

            loading?.invoke(this)
        }
        if (error != null) {
            error()
        } else {
            HorizontalPager(
                state = pagerState,
                contentPadding = contentPadding ?: PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = pageSize ?: LocalDefaultPageSize.current,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.recomposeHighlighter()
            ) {
                itemContent(item(it))
            }
        }
    }
}
