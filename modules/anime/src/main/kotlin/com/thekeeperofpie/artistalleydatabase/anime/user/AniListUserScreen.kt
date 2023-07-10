package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.anilist.UserByIdQuery.Data.User
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.anime.user.stats.UserMediaScreen
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryStringR
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
object AniListUserScreen {

    @Composable
    operator fun invoke(
        viewModel: AniListUserViewModel,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
        showLogOut: Boolean = false,
        onClickSettings: (() -> Unit)? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 280.dp,
                    pinnedHeight = 104.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    val user = viewModel.entry?.user
                    CoverAndBannerHeader(
                        screenKey = AnimeNavDestinations.USER.id,
                        entryId = user?.id?.let { EntryId("anime_user", it.toString()) },
                        progress = it,
                        coverImage = { user?.avatar?.large },
                        bannerImage = { user?.bannerImage },
                        pinnedHeight = 104.dp,
                        coverSize = 180.dp,
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            AutoResizeHeightText(
                                text = user?.name.orEmpty(),
                                style = MaterialTheme.typography.headlineLarge,
                                maxLines = 1,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterStart)
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 10.dp,
                                        bottom = 10.dp
                                    ),
                            )
                        }
                    }

                    val actuallyShowLogOut = showLogOut || viewModel.viewer.collectAsState().value
                        .let { it != null && it.id == viewModel.entry?.user?.id }
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

                Divider()


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
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        pageNestedScrollConnection = NestedScrollSplitter(
                            scrollBehavior.nestedScrollConnection,
                            bottomNavigationState?.nestedScrollConnection,
                        ),
                    ) {
                        val user = viewModel.entry?.user
                        when (UserTab.values()[it]) {
                            UserTab.OVERVIEW -> UserOverviewScreen(
                                entry = entry,
                                viewer = viewModel.viewer.collectAsState(null).value,
                                colorMap = viewModel.colorMap,
                                navigationCallback = navigationCallback,
                                bottomNavigationState = bottomNavigationState,
                            )
                            UserTab.ANIME_STATS -> UserMediaScreen(
                                user = { user },
                                statistics = { viewModel.entry?.statisticsAnime },
                                state = viewModel.animeStates,
                                navigationCallback = navigationCallback,
                                bottomNavigationState = bottomNavigationState,
                            )
                            UserTab.MANGA_STATS -> UserMediaScreen(
                                user = { user },
                                statistics = { viewModel.entry?.statisticsManga },
                                state = viewModel.mangaStates,
                                navigationCallback = navigationCallback,
                                bottomNavigationState = bottomNavigationState,
                            )
                        }
                    }
                }
            }
        }
    }

    data class Entry(
        val user: User,
    ) {
        val anime = user.favourites?.anime?.edges
            ?.sortedBy { it?.favouriteOrder ?: 0 }
            ?.mapNotNull { it?.node }
            .orEmpty()

        val manga = user.favourites?.manga?.edges
            ?.sortedBy { it?.favouriteOrder ?: 0 }
            ?.mapNotNull { it?.node }
            .orEmpty()

        val characters = user.favourites?.characters?.edges
            ?.sortedBy { it?.favouriteOrder ?: 0 }
            .let(CharacterUtils::toDetailsCharacters)

        val staff = user.favourites?.staff?.edges?.filterNotNull()
            ?.sortedBy { it.favouriteOrder ?: 0 }
            ?.mapNotNull { it.node }
            ?.map {
                DetailsStaff(
                    id = it.id.toString(),
                    name = it.name?.userPreferred,
                    image = it.image?.large,
                    role = it.primaryOccupations?.filterNotNull()?.firstOrNull(),
                    staff = it,
                )
            }.orEmpty().distinctBy { it.id }

        val studios = user.favourites?.studios?.edges?.filterNotNull()
            ?.sortedBy { it.favouriteOrder ?: 0 }
            ?.mapNotNull {
                Studio(
                    id = it.node?.id?.toString() ?: return@mapNotNull null,
                    name = it.node?.name ?: return@mapNotNull null,
                )
            }
            .orEmpty()

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
