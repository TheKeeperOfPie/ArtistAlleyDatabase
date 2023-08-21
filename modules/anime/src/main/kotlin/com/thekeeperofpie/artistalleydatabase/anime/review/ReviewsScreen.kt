package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
object ReviewsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.REVIEWS.id

    @Composable
    operator fun invoke(
        viewModel: ReviewsViewModel = hiltViewModel(),
        upIconOption: UpIconOption,
    ) {

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                sortFilterController = viewModel.sortFilterController,
                topBar = { TopBar(viewModel, upIconOption, scrollBehavior) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val viewer by viewModel.viewer.collectAsState()
                val anime = viewModel.anime.collectAsLazyPagingItems()
                val manga = viewModel.manga.collectAsLazyPagingItems()
                val reviews = if (viewModel.selectedType == MediaType.ANIME) anime else manga
                Content(
                    scaffoldPadding = it,
                    viewer = viewer,
                    reviews = reviews,
                    editViewModel = editViewModel,
                )
            }
        }
    }

    @Composable
    private fun TopBar(
        viewModel: ReviewsViewModel,
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                AppBar(
                    text = stringResource(R.string.anime_reviews_header),
                    upIconOption = upIconOption,
                )

                val selectedIsAnime = viewModel.selectedType == MediaType.ANIME
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = { viewModel.selectedType = MediaType.ANIME },
                        text = { Text(stringResource(R.string.anime_reviews_tab_anime)) },
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = { viewModel.selectedType = MediaType.MANGA },
                        text = { Text(stringResource(R.string.anime_reviews_tab_manga)) },
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(
        scaffoldPadding: PaddingValues,
        viewer: AniListViewer?,
        reviews: LazyPagingItems<ReviewEntry>,
        editViewModel: MediaEditViewModel,
    ) {
        val refreshing = reviews.loadState.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = reviews::refresh,
        )
        Box(
            modifier = Modifier
                .padding(scaffoldPadding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    count = reviews.itemCount,
                    key = reviews.itemKey { it.review.id },
                    contentType = reviews.itemContentType { "review" }
                ) {
                    val entry = reviews[it]
                    ReviewCard(
                        screenKey = SCREEN_KEY,
                        viewer = viewer,
                        review = entry?.review,
                        media = entry?.media,
                        onClick = {
                            if (entry != null) {
                                it.onReviewClick(
                                    reviewId = entry.review.id.toString(),
                                    media = null,
                                    favorite = null,
                                    imageWidthToHeightRatio = 1f,
                                )
                            }
                        },
                        onClickListEdit = {
                            if (entry != null) {
                                editViewModel.initialize(entry.media.media)
                            }
                        },
                    )
                }
            }
        }
    }
}
