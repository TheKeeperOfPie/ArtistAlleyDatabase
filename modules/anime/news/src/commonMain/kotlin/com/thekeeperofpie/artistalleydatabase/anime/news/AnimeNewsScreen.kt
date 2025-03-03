package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_title
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
object AnimeNewsScreen {

    @Composable
    operator fun invoke(
        viewModel: AnimeNewsViewModel,
        sortFilterState: SortFilterState<*>,
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
            state = sortFilterState,
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
            val newsResult by viewModel.news.collectAsStateWithLifecycle()
            val pullRefreshState = rememberPullRefreshState(
                refreshing = newsResult.loading,
                onRefresh = viewModel::refresh,
            )
            Box(
                modifier = Modifier
                    .padding(scaffoldPadding)
                    .fillMaxSize()
            ) {
                val gridState = rememberLazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                VerticalList(
                    gridState = gridState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 72.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    itemHeaderText = null,
                    items = newsResult.result,
                    itemKey = { it.id },
                    item = {
                        AnimeNewsSmallCard(entry = it, onOpenImage = onOpenImage)
                    }
                )

                // TODO: Move this into VerticalList?
                PullRefreshIndicator(
                    refreshing = newsResult.result == null,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
