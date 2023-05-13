package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.AuthedUserQuery
import com.anilist.UserByIdQuery.Data.User
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.ui.CoverAndBannerHeader
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AniListUserScreen {

    @Composable
    operator fun invoke(
        nestedScrollConnection: NestedScrollConnection? = null,
        entry: @Composable () -> Entry?,
        viewer: @Composable () -> AuthedUserQuery.Data.Viewer?,
        callback: Callback,
        bottomNavBarPadding: @Composable () -> Dp = { 0.dp },
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Scaffold(
            topBar = {
                CollapsingToolbar(
                    maxHeight = 280.dp,
                    pinnedHeight = 104.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    val user = entry()?.user
                    CoverAndBannerHeader(
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

                HorizontalPager(
                    state = pagerState,
                    pageNestedScrollConnection = NestedScrollSplitter(
                        nestedScrollConnection,
                        scrollBehavior.nestedScrollConnection,
                    ),
                ) {
                    val user = entry()?.user
                    when (UserTab.values()[it]) {
                        UserTab.OVERVIEW -> UserOverviewScreen(
                            entry = entry,
                            viewer = viewer(),
                            callback = callback,
                            bottomNavBarPadding = bottomNavBarPadding,
                        )
                        UserTab.ANIME_STATS -> UserStatsScreen(
                            user = { user },
                            statistics = { entry()?.statisticsAnime },
                            isAnime = true,
                            bottomNavBarPadding = bottomNavBarPadding,
                        )
                        UserTab.MANGA_STATS -> UserStatsScreen(
                            user = { user },
                            statistics = { entry()?.statisticsManga },
                            isAnime = false,
                            bottomNavBarPadding = bottomNavBarPadding,
                        )
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
            ?.map {
                DetailsStaff(
                    id = it.node?.id.toString(),
                    name = it.node?.name?.userPreferred,
                    image = it.node?.image?.large,
                    role = it.node?.primaryOccupations?.filterNotNull()?.firstOrNull(),
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
        }

        data class Studio(
            val id: String,
            val name: String,
        )
    }

    interface Callback {
        fun onMediaClick(media: UserFavoriteMediaNode)
        fun onCharacterClicked(id: String) {
            // TODO
        }

        fun onCharacterLongClicked(id: String) {
            // TODO
        }

        fun onStaffClicked(id: String) {
            // TODO
        }

        fun onStaffLongClicked(id: String) {
            // TODO
        }

        fun onStudioClicked(id: String) {
            // TODO
        }
    }
}
