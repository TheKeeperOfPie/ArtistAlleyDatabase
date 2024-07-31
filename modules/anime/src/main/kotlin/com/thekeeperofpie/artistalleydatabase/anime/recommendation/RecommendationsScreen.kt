package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
object RecommendationsScreen {

    @Composable
    operator fun invoke(
        viewModel: RecommendationsViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
    ) {
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                sortFilterController = viewModel.sortFilterController,
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(text = stringResource(R.string.anime_recommendations_header)) },
                            navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                                )
                            ),
                        )
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val viewer by viewModel.viewer.collectAsState()
                Content(
                    scaffoldPadding = it,
                    viewer = viewer,
                    viewModel = viewModel,
                    editViewModel = editViewModel,
                )
            }
        }

    }

    @Composable
    private fun Content(
        scaffoldPadding: PaddingValues,
        viewer: AniListViewer?,
        viewModel: RecommendationsViewModel,
        editViewModel: MediaEditViewModel,
    ) {
        val recommendations = viewModel.recommendations.collectAsLazyPagingItems()
        val refreshState = recommendations.loadState.refresh
        val refreshing = refreshState is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = recommendations::refresh,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .pullRefresh(pullRefreshState)
        ) {
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
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = recommendations.itemCount,
                    key = recommendations.itemKey { it.id },
                    contentType = recommendations.itemContentType { "recommendation" }
                ) {
                    val entry = recommendations[it]
                    RecommendationCard(
                        viewer = viewer,
                        user = entry?.user,
                        media = entry?.media,
                        mediaRecommendation = entry?.mediaRecommendation,
                        onClickListEdit = {
                            if (entry != null) {
                                editViewModel.initialize(entry.media.media)
                            }
                        },
                        recommendation = entry?.data,
                        onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
