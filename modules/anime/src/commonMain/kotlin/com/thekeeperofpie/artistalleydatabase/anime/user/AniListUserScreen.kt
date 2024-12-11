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
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_settings_content_description
import artistalleydatabase.modules.entry.generated.resources.log_out
import artistalleydatabase.modules.entry.generated.resources.more_actions_content_description
import com.anilist.data.UserByIdQuery.Data.User
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.user.social.UserSocialScreen
import com.thekeeperofpie.artistalleydatabase.anime.user.stats.UserMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.UserOverviewScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.UserStudiosEntry
import com.thekeeperofpie.artistalleydatabase.anime.users.UserTab
import com.thekeeperofpie.artistalleydatabase.entry.EntryStrings
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AniListUserScreen {

    @Composable
    operator fun <MediaEntry : Any, StudioEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        viewModel: AniListUserViewModel<MediaEntry, StudioEntry>,
        viewer: () -> AniListViewer?,
        entry: () -> LoadingResult<Entry>,
        anime: LazyPagingItems<MediaEntry>,
        manga: LazyPagingItems<MediaEntry>,
        mediaHorizontalRow: LazyGridScope.(
            titleRes: StringResource,
            LazyPagingItems<MediaEntry>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        characters: LazyPagingItems<CharacterDetails>,
        charactersSection: LazyGridScope.(
            titleRes: StringResource,
            characters: LazyPagingItems<CharacterDetails>,
            viewAllRoute: (() -> NavDestination)?,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        staff: LazyPagingItems<StaffDetails>,
        staffSection: LazyGridScope.(
            titleRes: StringResource?,
            staff: LazyPagingItems<StaffDetails>,
            viewAllRoute: NavDestination,
            viewAllContentDescriptionTextRes: StringResource,
        ) -> Unit,
        studios: () -> LoadingResult<UserStudiosEntry<StudioEntry>>,
        studiosSection: LazyGridScope.(
            List<StudioEntry>,
            hasMore: Boolean,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        activitySortFilterState: () -> SortFilterController<*>.State,
        activitySection: @Composable (onClickListEdit: (MediaNavigationData) -> Unit) -> Unit,
        upIconOption: UpIconOption?,
        headerValues: UserHeaderValues,
        bottomNavigationState: BottomNavigationState? = null,
        showLogOut: Boolean = false,
        onLogOutClick: () -> Unit,
        onClickSettings: (() -> Unit)? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            Scaffold(
                topBar = {
                    CollapsingToolbar(
                        maxHeight = 280.dp,
                        pinnedHeight = 104.dp,
                        scrollBehavior = scrollBehavior,
                    ) {
                        UserHeader(
                            upIconOption = upIconOption,
                            progress = it,
                            headerValues = headerValues,
                        )

                        val viewer = viewer()
                        val actuallyShowLogOut = showLogOut || (viewer != null
                                && viewer.id == headerValues.user()?.id?.toString())
                        if (onClickSettings != null || actuallyShowLogOut) {
                            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                                if (onClickSettings != null) {
                                    IconButton(onClick = onClickSettings) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(
                                                Res.string.anime_settings_content_description
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
                                                    EntryStrings.more_actions_content_description
                                                ),
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(EntryStrings.log_out)) },
                                                onClick = {
                                                    onLogOutClick()
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
                modifier = Modifier.padding(padding)
            ) { scaffoldPadding ->
                Column(modifier = Modifier.padding(scaffoldPadding)) {
                    val pagerState = rememberPagerState(pageCount = { UserTab.entries.size })
                    val scope = rememberCoroutineScope()
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        divider = { /* No divider, manually draw so that it's full width */ }
                    ) {
                        UserTab.entries.forEachIndexed { index, tab ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                text = { Text(text = stringResource(tab.textRes), maxLines = 1) }
                            )
                        }
                    }

                    HorizontalDivider()

                    val entryLoadingResult = entry()
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = entryLoadingResult.loading,
                        onRefresh = { viewModel.refresh() },
                    )
                    val entry = entryLoadingResult.result
                    if (entry == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pullRefresh(pullRefreshState)
                        ) {
                            // TODO: Better error UI consolidation
                            val error = entryLoadingResult.error
                            if (error != null) {
                                VerticalList.ErrorContent(
                                    errorText = error.message(),
                                    exception = error.throwable,
                                )
                            }

                            PullRefreshIndicator(
                                refreshing = entryLoadingResult.loading,
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
                            modifier = Modifier.pullRefresh(pullRefreshState)
                        ) {
                            Box {
                                val viewer by viewModel.viewer.collectAsState()
                                val user = entry.user
                                when (UserTab.entries[it]) {
                                    UserTab.OVERVIEW -> // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
                                        UserOverviewScreen(
                                            userId = viewModel.userId,
                                            entry = entry,
                                            anime = anime,
                                            manga = manga,
                                            characters = characters,
                                            staff = staff,
                                            studios = studios,
                                            viewer = viewer,
                                            isFollowing = { viewModel.isFollowing() },
                                            onFollowingClick = viewModel::toggleFollow,
                                            mediaRow = { titleRes, entries, viewAllRoute, viewAllContentDescriptionTextRes ->
                                                mediaHorizontalRow(
                                                    titleRes,
                                                    entries,
                                                    viewAllRoute,
                                                    viewAllContentDescriptionTextRes,
                                                    onClickListEdit,
                                                )
                                            },
                                            charactersSection = charactersSection,
                                            staffSection = staffSection,
                                            studiosSection = { entries, hasMore ->
                                                studiosSection(entries, hasMore, onClickListEdit)
                                            },
                                            bottomNavigationState = bottomNavigationState,
                                        )
                                    UserTab.ACTIVITY -> {
                                        SortFilterBottomScaffold(state = activitySortFilterState) {
                                            activitySection(onClickListEdit)
                                        }
                                    }
                                    UserTab.ANIME_STATS -> UserMediaScreen(
                                        user = { user },
                                        statistics = { entry.statisticsAnime },
                                        state = viewModel.animeStats,
                                        bottomNavigationState = bottomNavigationState,
                                    )
                                    UserTab.MANGA_STATS -> UserMediaScreen(
                                        user = { user },
                                        statistics = { entry.statisticsManga },
                                        state = viewModel.mangaStats,
                                        bottomNavigationState = bottomNavigationState,
                                    )
                                    UserTab.SOCIAL -> UserSocialScreen(
                                        userId = viewModel.userId,
                                        user = entry.user,
                                        bottomNavigationState = bottomNavigationState,
                                    )
                                }

                                PullRefreshIndicator(
                                    refreshing = entryLoadingResult.loading,
                                    state = pullRefreshState,
                                    modifier = Modifier.align(Alignment.TopCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: Filter out isAdult
    data class Entry(
        override val user: User,
        override val about: MarkdownText?,
    ) : UserOverviewScreen.Entry {
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
    }
}
