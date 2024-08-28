package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.UserByIdQuery.Data.User
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.user.social.UserSocialScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.stats.UserMediaScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AniListUserScreen {

    @Composable
    operator fun invoke(
        viewModel: AniListUserViewModel,
        upIconOption: UpIconOption?,
        headerValues: UserHeaderValues,
        bottomNavigationState: BottomNavigationState? = null,
        showLogOut: Boolean = false,
        onClickSettings: (() -> Unit)? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 280.dp,
                    pinnedHeight = 104.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    val user = viewModel.entry?.user
                    UserHeader(
                        upIconOption = upIconOption,
                        progress = it,
                        headerValues = headerValues,
                    )

                    val actuallyShowLogOut = showLogOut || viewModel.viewer.collectAsState().value
                        .let { it != null && it.id == viewModel.entry?.user?.id?.toString() }
                    if (onClickSettings != null || actuallyShowLogOut) {
                        Row(modifier = Modifier.align(Alignment.TopEnd)) {
                            if (onClickSettings != null) {
                                IconButton(onClick = onClickSettings) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = stringResource(
                                            R.string.anime_settings_content_description
                                        ),
                                    )
                                }
                            }

                            if (actuallyShowLogOut) {
                                Box {
                                    var menuExpanded by remember { mutableStateOf(false) }
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = stringResource(
                                                EntryStringR.more_actions_content_description
                                            ),
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(EntryStringR.log_out)) },
                                            onClick = {
                                                viewModel.logOut()
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomNavigationState = bottomNavigationState,
        ) { scaffoldPadding ->
            Column(modifier = Modifier.padding(scaffoldPadding)) {
                val pagerState = rememberPagerState(pageCount = { UserTab.values().size })
                val scope = rememberCoroutineScope()
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    divider = { /* No divider, manually draw so that it's full width */ }
                ) {
                    UserTab.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                        )
                    }
                }

                HorizontalDivider()


                val entry = viewModel.entry
                if (entry == null) {
                    val refreshing by remember { derivedStateOf { viewModel.errorResource == null } }
                    val errorResource = viewModel.errorResource
                    val pullRefreshState =
                        rememberPullRefreshState(refreshing, { viewModel.refresh() })
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
                        // TODO: Better error UI consolidation
                        if (errorResource != null) {
                            AnimeMediaListScreen.Error(
                                errorTextRes = errorResource.first,
                                exception = errorResource.second,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        PullRefreshIndicator(
                            refreshing = refreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                } else {
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = viewModel.entry == null,
                        onRefresh = { viewModel.refresh() },
                    )
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        pageNestedScrollConnection = NestedScrollSplitter(
                            scrollBehavior.nestedScrollConnection,
                            bottomNavigationState?.nestedScrollConnection,
                        ),
                        modifier = Modifier.pullRefresh(pullRefreshState)
                    ) {
                        Box {
                            val viewer by viewModel.viewer.collectAsState()
                            val user = viewModel.entry?.user
                            when (UserTab.values()[it]) {
                                UserTab.OVERVIEW -> UserOverviewScreen(
                                    userId = viewModel.userId,
                                    entry = entry,
                                    viewModel = viewModel,
                                    editViewModel = editViewModel,
                                    viewer = viewer,
                                    isFollowing = { viewModel.isFollowing },
                                    onFollowingClick = viewModel::toggleFollow,
                                    bottomNavigationState = bottomNavigationState,
                                )
                                UserTab.ACTIVITY -> UserActivityScreen(
                                    viewModel = viewModel,
                                    editViewModel = editViewModel,
                                    viewer = viewer,
                                )
                                UserTab.ANIME_STATS -> UserMediaScreen(
                                    user = { user },
                                    statistics = { viewModel.entry?.statisticsAnime },
                                    state = viewModel.animeStats,
                                    bottomNavigationState = bottomNavigationState,
                                )
                                UserTab.MANGA_STATS -> UserMediaScreen(
                                    user = { user },
                                    statistics = { viewModel.entry?.statisticsManga },
                                    state = viewModel.mangaStats,
                                    bottomNavigationState = bottomNavigationState,
                                )
                                UserTab.SOCIAL -> UserSocialScreen(
                                    userId = viewModel.userId,
                                    user = viewModel.entry?.user,
                                    bottomNavigationState = bottomNavigationState,
                                )
                            }

                            PullRefreshIndicator(
                                refreshing = viewModel.entry == null,
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
    }

    // TODO: Filter out isAdult
    data class Entry(
        val user: User,
        val about: MarkdownText?,
    ) {
        val statisticsAnime = user.statistics?.anime?.let(::Statistics)
        val statisticsManga = user.statistics?.manga?.let(::Statistics)

        data class Statistics(
            val statistics: UserMediaStatistics,
        ) {
            val scores = statistics.scores?.filterNotNull().orEmpty()
                .sortedBy { it.score }
            val lengths = statistics.lengths?.filterNotNull().orEmpty()
                .sortedBy { it.length?.substringBefore("-")?.toIntOrNull() }
            val releaseYears = statistics.releaseYears?.filterNotNull().orEmpty()
                .sortedBy { it.releaseYear }
            val startYears = statistics.startYears?.filterNotNull().orEmpty()
                .sortedBy { it.startYear }
        }

        data class Studio(
            val id: String,
            val name: String,
        )
    }
}
