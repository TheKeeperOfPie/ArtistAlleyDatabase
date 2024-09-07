package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import artistalleydatabase.modules.anime.generated.resources.anime_news_title
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AnimeNewsScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: AnimeNewsViewModel = viewModel { animeComponent.animeNewsViewModel() },
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
                val navigationCallback = LocalNavigationCallback.current
                AppBar(
                    text = stringResource(Res.string.anime_news_title),
                    upIconOption = UpIconOption.Back { navigationCallback.navigateUp() },
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { scaffoldPadding ->
            val news = viewModel.news
            val pullRefreshState =
                rememberPullRefreshState(refreshing = news == null, onRefresh = viewModel::refresh)
            Box(modifier = Modifier
                .padding(scaffoldPadding)
                .fillMaxSize()) {
                val listState = rememberLazyListState()
                viewModel.sortFilterController.ImmediateScrollResetEffect(listState)
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
                                        stringResource(Res.string.anime_media_list_no_results),
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
                                AnimeNewsSmallCard(entry = news[it])
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
