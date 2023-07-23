package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.UserSocialActivityQuery.Data.Page.ListActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.MessageActivityActivity
import com.anilist.UserSocialActivityQuery.Data.Page.OtherActivity
import com.anilist.UserSocialActivityQuery.Data.Page.TextActivityActivity
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AnimeActivityScreen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        viewModel: AnimeActivityViewModel = hiltViewModel<AnimeActivityViewModel>(),
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        val viewer by viewModel.viewer.collectAsState()
        val pagerState = rememberPagerState(
            initialPage = if (viewer == null) 0 else 1,
            pageCount = { if (viewer == null) 1 else 3 },
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        SortFilterBottomScaffoldNoAppBarOffset(
            sortFilterController = viewModel.sortFilterController,
            topBar = {
                if (viewer == null) {
                    AppBar(
                        text = stringResource(R.string.anime_activity_global_title),
                        upIconOption = UpIconOption.Back { navigationCallback.popUp() },
                        scrollBehavior = scrollBehavior,
                    )
                } else {
                    EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                        Column {
                            AppBar(
                                text = stringResource(R.string.anime_activity_title),
                                upIconOption = UpIconOption.Back { navigationCallback.popUp() },
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
                                        Text(text = stringResource(R.string.anime_activity_tab_following))
                                    }
                                )
                                Tab(
                                    selected = pagerState.targetPage == 2,
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(2) }
                                    },
                                    text = { Text(text = stringResource(R.string.anime_activity_tab_global)) }
                                )
                            }
                        }
                    }
                }
            },
            modifier = Modifier.nestedScroll(
                NestedScrollSplitter(
                    scrollBehavior.nestedScrollConnection,
                    consumeNone = true
                )
            )
        ) {
            val density = LocalDensity.current
            val topBarPadding by remember {
                derivedStateOf {
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { density.run { -it.toDp() } }
                        ?: 0.dp
                }
            }
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
                    activities = activities,
                    topBarPadding = topBarPadding,
                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }
        }
    }

    @Composable
    private fun ActivityList(
        activities: LazyPagingItems<AnimeActivityViewModel.ActivityEntry>,
        topBarPadding: Dp,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        when (val refreshState = activities.loadState.refresh) {
            LoadState.Loading -> Unit
            is LoadState.Error -> AnimeMediaListScreen.Error(
                exception = refreshState.error,
                modifier = Modifier.padding(top = topBarPadding)
            )
            is LoadState.NotLoading -> {
                if (activities.itemCount == 0) {
                    AnimeMediaListScreen.NoResults(modifier = Modifier.padding(top = topBarPadding))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp + topBarPadding,
                            bottom = 72.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            count = activities.itemCount,
                            key = activities.itemKey { it.activityId.scopedId },
                            contentType = activities.itemContentType { it.activityId.type }
                        ) {
                            val entry = activities[it]
                            when (val activity = entry?.activity) {
                                is TextActivityActivity -> TextActivitySmallCard(
                                    activity = activity,
                                    entry = entry,
                                    onActivityStatusUpdate = onActivityStatusUpdate,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                is ListActivityActivity -> ListActivitySmallCard(
                                    screenKey = AnimeNavDestinations.ACTIVITY.id,
                                    activity = activity,
                                    media = activity.media,
                                    entry = entry,
                                    onActivityStatusUpdate = onActivityStatusUpdate,
                                    colorCalculationState = colorCalculationState,
                                    navigationCallback = navigationCallback,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                is MessageActivityActivity,
                                is OtherActivity,
                                null,
                                -> TextActivitySmallCard(
                                    activity = null,
                                    entry = null,
                                    onActivityStatusUpdate = onActivityStatusUpdate,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        when (activities.loadState.append) {
                            is LoadState.Loading -> item("load_more_append") {
                                AnimeMediaListScreen.LoadingMore()
                            }
                            is LoadState.Error -> item("load_more_error") {
                                AnimeMediaListScreen.AppendError { activities.retry() }
                            }
                            is LoadState.NotLoading -> Unit
                        }
                    }
                }
            }
        }
    }
}
