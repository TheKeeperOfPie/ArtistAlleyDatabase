package com.thekeeperofpie.artistalleydatabase.anime.users

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_settings_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.log_out
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.anilist.data.MediaTitlesAndImagesQuery
import com.anilist.data.UserByIdQuery
import com.anilist.data.UserSocialFollowersQuery
import com.anilist.data.UserSocialFollowingQuery
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.UserMediaStatistics
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaGenreRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.SearchMediaTagRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StaffDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.StudioMediasRoute
import com.thekeeperofpie.artistalleydatabase.anime.users.social.UserSocialScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.stats.UserMediaScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.stats.UserStatsBasicScreen
import com.thekeeperofpie.artistalleydatabase.anime.users.stats.UserStatsDetailScreen
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AniListUserScreen {

    @Composable
    operator fun <MediaEntry : Any, StudioEntry : Any> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        viewer: () -> AniListViewer?,
        userId: String?,
        isFollowing: @Composable () -> Boolean,
        onFollowingToggle: () -> Unit,
        onRefresh: () -> Unit,
        entry: () -> LoadingResult<Entry>,
        animeGenresState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Genre>,
        animeStaffState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Staff>,
        animeTagsState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Tag>,
        animeVoiceActorsState: () -> UserStatsDetailScreen.State<UserMediaStatistics.VoiceActor>,
        animeStudiosState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Studio>,
        mangaGenresState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Genre>,
        mangaStaffState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Staff>,
        mangaTagsState: () -> UserStatsDetailScreen.State<UserMediaStatistics.Tag>,
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
        activitySortFilterState: SortFilterState<*>,
        activitySection: @Composable (onClickListEdit: (MediaNavigationData) -> Unit, Modifier) -> Unit,
        socialFollowing: LazyPagingItems<UserSocialFollowingQuery.Data.Page.Following>,
        socialFollowers: LazyPagingItems<UserSocialFollowersQuery.Data.Page.Follower>,
        upIconOption: UpIconOption?,
        headerValues: UserHeaderValues,
        bottomNavigationState: BottomNavigationState? = null,
        showLogOut: Boolean = false,
        onLogOutClick: () -> Unit,
        onClickSettings: (() -> Unit)? = null,
        mediaDetailsRoute: MediaDetailsRoute,
        searchMediaGenreRoute: SearchMediaGenreRoute,
        searchMediaTagRoute: SearchMediaTagRoute,
        staffDetailsRoute: StaffDetailsRoute,
        studioMediasRoute: StudioMediasRoute,
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
                                // TODO: Move settings to its owm module and a slot
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
                                                    UtilsRes.string.more_actions_content_description
                                                ),
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = menuExpanded,
                                            onDismissRequest = { menuExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(UtilsRes.string.log_out)) },
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
                        onRefresh = onRefresh,
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
                                val user = entry.user
                                when (UserTab.entries[it]) {
                                    UserTab.OVERVIEW -> // TODO: mediaListEntry doesn't load properly for these, figure out a way to show status
                                        UserOverviewScreen(
                                            userId = userId,
                                            entry = entry,
                                            anime = anime,
                                            manga = manga,
                                            characters = characters,
                                            staff = staff,
                                            studios = studios,
                                            viewer = viewer(),
                                            isFollowing = isFollowing,
                                            onFollowingClick = onFollowingToggle,
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
                                            activitySection(onClickListEdit, Modifier.padding(it))
                                        }
                                    }
                                    UserTab.ANIME_STATS -> UserMediaScreen(
                                        user = { user },
                                        statsEntry = { entry.statisticsAnime },
                                        isAnime = true,
                                        genresState = animeGenresState,
                                        staffState = animeStaffState,
                                        tagsState = animeTagsState,
                                        animeVoiceActorsState = animeVoiceActorsState,
                                        animeStudiosState = animeStudiosState,
                                        bottomNavigationState = bottomNavigationState,
                                        mediaDetailsRoute = mediaDetailsRoute,
                                        searchMediaGenreRoute = searchMediaGenreRoute,
                                        searchMediaTagRoute = searchMediaTagRoute,
                                        staffDetailsRoute = staffDetailsRoute,
                                        studioMediasRoute = studioMediasRoute,
                                    )
                                    UserTab.MANGA_STATS -> UserMediaScreen(
                                        user = { user },
                                        statsEntry = { entry.statisticsManga },
                                        isAnime = false,
                                        genresState = mangaGenresState,
                                        staffState = mangaStaffState,
                                        tagsState = mangaTagsState,
                                        animeVoiceActorsState = {
                                            object :
                                                UserStatsDetailScreen.State<UserMediaStatistics.VoiceActor> {
                                                // TODO: Change API to remove this
                                                @Composable
                                                override fun getMedia(value: UserMediaStatistics.VoiceActor): Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>?> =
                                                    Result.success(emptyMap())
                                            }
                                        },
                                        animeStudiosState = {
                                            object :
                                                UserStatsDetailScreen.State<UserMediaStatistics.Studio> {
                                                @Composable
                                                override fun getMedia(value: UserMediaStatistics.Studio): Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>?> =
                                                    Result.success(emptyMap())
                                            }
                                        },
                                        bottomNavigationState = bottomNavigationState,
                                        mediaDetailsRoute = mediaDetailsRoute,
                                        searchMediaGenreRoute = searchMediaGenreRoute,
                                        searchMediaTagRoute = searchMediaTagRoute,
                                        staffDetailsRoute = staffDetailsRoute,
                                        studioMediasRoute = studioMediasRoute,
                                    )
                                    UserTab.SOCIAL -> UserSocialScreen(
                                        userId = userId,
                                        user = entry.user,
                                        following = socialFollowing,
                                        followers = socialFollowers,
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
        override val user: UserByIdQuery.Data.User,
        override val about: MarkdownText?,
    ) : UserOverviewScreen.Entry {
        val statisticsAnime = user.statistics?.anime?.let(::Statistics)
        val statisticsManga = user.statistics?.manga?.let(::Statistics)

        data class Statistics(
            override val statistics: UserMediaStatistics,
        ) : UserStatsBasicScreen.Entry {
            override val scores = statistics.scores?.filterNotNull().orEmpty()
                .sortedBy { it.score }
            override val lengths = statistics.lengths?.filterNotNull().orEmpty()
                .sortedBy { it.length?.substringBefore("-")?.toIntOrNull() }
            override val releaseYears = statistics.releaseYears?.filterNotNull().orEmpty()
                .sortedBy { it.releaseYear }
            override val startYears = statistics.startYears?.filterNotNull().orEmpty()
                .sortedBy { it.startYear }
        }
    }
}
