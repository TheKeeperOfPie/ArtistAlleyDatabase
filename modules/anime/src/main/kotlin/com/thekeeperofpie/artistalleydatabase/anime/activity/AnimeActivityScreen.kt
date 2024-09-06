package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AnimeActivityScreen {

    @Composable
    operator fun invoke(
        viewModel: AnimeActivityViewModel = hiltViewModel<AnimeActivityViewModel>(),
    ) {
        val viewer by viewModel.viewer.collectAsState()
        val pagerState = rememberPagerState(
            initialPage = if (viewer == null) 0 else 1,
            pageCount = { if (viewer == null) 1 else 3 },
        )
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            val sortFilterController = viewModel.sortFilterController
            val selectedMedia = sortFilterController.selectedMedia()
            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    val navigationCallback = LocalNavigationCallback.current
                    val mediaTitle = selectedMedia?.title?.primaryTitle()
                    val title = if (mediaTitle == null) {
                        stringResource(R.string.anime_activity_title)
                    } else {
                        stringResource(R.string.anime_activity_title_with_media, mediaTitle)
                    }

                    if (viewer == null) {
                        AppBar(
                            text = title,
                            upIconOption = UpIconOption.Back { navigationCallback.navigateUp() },
                            scrollBehavior = scrollBehavior,
                        )
                    } else {
                        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                            Column {
                                AppBar(
                                    text = title,
                                    upIconOption = UpIconOption.Back {
                                        navigationCallback.navigateUp()
                                    },
                                )

                                val scope = rememberCoroutineScope()
                                TabRow(selectedTabIndex = pagerState.targetPage) {
                                    Tab(selected = pagerState.targetPage == 0,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(0) }
                                        },
                                        text = {
                                            Text(text = stringResource(R.string.anime_activity_tab_own))
                                        }
                                    )
                                    Tab(selected = pagerState.targetPage == 1,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(1) }
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(
                                                    R.string.anime_activity_tab_following
                                                )
                                            )
                                        }
                                    )
                                    Tab(
                                        selected = pagerState.targetPage == 2,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(2) }
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(
                                                    R.string.anime_activity_tab_global
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                HorizontalPager(state = pagerState) {
                    val activities = if (viewer == null) {
                        viewModel.globalActivity().collectAsLazyPagingItems()
                    } else when (it) {
                        0 -> viewModel.ownActivity().collectAsLazyPagingItems()
                        1 -> viewModel.followingActivity().collectAsLazyPagingItems()
                        2 -> viewModel.globalActivity().collectAsLazyPagingItems()
                        else -> throw IllegalArgumentException("Invalid page")
                    }
                    ActivityList(
                        editViewModel = editViewModel,
                        viewer = viewer,
                        activities = activities,
                        onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                        showMedia = selectedMedia == null,
                        sortFilterController = sortFilterController,
                    )
                }
            }
        }
    }
}
