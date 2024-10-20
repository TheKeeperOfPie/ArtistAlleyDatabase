package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_no_results
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_title
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AnimeNewsScreen {

    @Composable
    operator fun invoke(
        viewModel: AnimeNewsViewModel,
        onBackClick: () -> Unit,
        onOpenImage: (url: String) -> Unit,
    ) {
        val scaffoldState = rememberBottomSheetScaffoldState()
        val scope = rememberCoroutineScope()
        BackHandler(enabled = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            scope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        SortFilterBottomScaffold(
            sortFilterController = viewModel.sortFilterController,
            topBar = {
                AppBar(
                    text = stringResource(Res.string.anime_news_title),
                    upIconOption = UpIconOption.Back(onBackClick),
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { scaffoldPadding ->
            // TODO: Error/loading indicator
            val newsResult by viewModel.news.collectAsState()
            val pullRefreshState = rememberPullRefreshState(
                refreshing = newsResult.loading,
                onRefresh = viewModel::refresh,
            )
            Box(
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .fillMaxSize()
            ) {
                val listState = rememberLazyListState()
                viewModel.sortFilterController.ImmediateScrollResetEffect(listState)
                val news = newsResult.result
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 72.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.pullRefresh(pullRefreshState)
                ) {
                    if (news != null) {
                        if (news.isEmpty()) {
                            item("no_results") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        stringResource(Res.string.anime_news_no_results),
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 10.dp
                                        ),
                                    )
                                }
                            }
                        } else {
                            items(
                                count = news.size,
                                key = { news[it].id },
                                contentType = { "news " }) {
                                AnimeNewsSmallCard(entry = news[it], onOpenImage = onOpenImage)
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = news == null,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
