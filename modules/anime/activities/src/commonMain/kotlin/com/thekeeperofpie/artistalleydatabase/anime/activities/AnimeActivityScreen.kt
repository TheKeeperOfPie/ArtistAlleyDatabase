package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import artistalleydatabase.modules.anime.activities.generated.resources.Res
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_tab_following
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_tab_global
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_tab_own
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_title
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_title_with_media
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object AnimeActivityScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        viewer: () -> AniListViewer?,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        sortFilterState: () -> SortFilterController<*>.State?,
        mediaTitle: @Composable () -> String?,
        ownActivity: @Composable () -> LazyPagingItems<ActivityEntry<MediaEntry>>,
        globalActivity: @Composable () -> LazyPagingItems<ActivityEntry<MediaEntry>>,
        followingActivity: @Composable () -> LazyPagingItems<ActivityEntry<MediaEntry>>,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        mediaRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        val viewer = viewer()
        val pagerState = key(viewer) {
            rememberPagerState(
                initialPage = if (viewer == null) 0 else 1,
                pageCount = { if (viewer == null) 1 else 3 },
            )
        }
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    val mediaTitle = mediaTitle()
                    val title = if (mediaTitle == null) {
                        stringResource(Res.string.anime_activity_title)
                    } else {
                        stringResource(Res.string.anime_activity_title_with_media, mediaTitle)
                    }

                    val navHostController = LocalNavHostController.current
                    if (viewer == null) {
                        AppBar(
                            text = title,
                            upIconOption = UpIconOption.Back(navHostController),
                            scrollBehavior = scrollBehavior,
                        )
                    } else {
                        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                            Column {
                                AppBar(
                                    text = title,
                                    upIconOption = UpIconOption.Back(navHostController),
                                )

                                val scope = rememberCoroutineScope()
                                TabRow(selectedTabIndex = pagerState.targetPage) {
                                    Tab(
                                        selected = pagerState.targetPage == 0,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(0) }
                                        },
                                        text = {
                                            Text(text = stringResource(Res.string.anime_activity_tab_own))
                                        }
                                    )
                                    Tab(
                                        selected = pagerState.targetPage == 1,
                                        onClick = {
                                            scope.launch { pagerState.animateScrollToPage(1) }
                                        },
                                        text = {
                                            Text(
                                                text = stringResource(
                                                    Res.string.anime_activity_tab_following
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
                                                    Res.string.anime_activity_tab_global
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.Companion
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                HorizontalPager(state = pagerState) {
                    val activities = if (viewer == null) {
                        globalActivity
                    } else when (it) {
                        0 -> ownActivity
                        1 -> followingActivity
                        2 -> globalActivity
                        else -> throw IllegalArgumentException("Invalid page")
                    }
                    ActivityList<MediaEntry>(
                        viewer = viewer,
                        activities = activities(),
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        showMedia = mediaTitle() == null,
                        sortFilterState = sortFilterState,
                        userRoute = userRoute,
                        mediaRow = mediaRow,
                        onClickListEdit = onClickListEdit,
                    )
                }
            }
        }
    }
}
