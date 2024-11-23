package com.thekeeperofpie.artistalleydatabase.anime.media.activity

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_header
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_tab_following
import artistalleydatabase.modules.anime.generated.resources.anime_media_activities_tab_global
import com.anilist.data.MediaActivityQuery
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeader
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.CollapsingToolbar
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object MediaActivitiesScreen {

    @Composable
    operator fun invoke(
        viewModel: MediaActivitiesViewModel,
        upIconOption: UpIconOption,
        headerValues: MediaHeaderValues,
    ) {
        val entry = viewModel.entry
        val media = entry.result?.data?.media

        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val viewer by viewModel.viewer.collectAsState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium)
        )
        val snackbarHostState = remember { SnackbarHostState() }

        val error = entry.error
        val errorString = error?.message()
        LaunchedEffect(errorString) {
            if (errorString != null) {
                snackbarHostState.showSnackbar(
                    message = errorString,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
            }
        }

        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                CollapsingToolbar(
                    maxHeight = 356.dp,
                    pinnedHeight = 120.dp,
                    scrollBehavior = scrollBehavior,
                ) {
                    MediaHeader(
                        viewer = viewer,
                        upIconOption = upIconOption,
                        mediaId = viewModel.mediaId,
                        mediaType = viewModel.entry.result?.data?.media?.type,
                        titles = entry.result?.titlesUnique,
                        episodes = media?.episodes,
                        format = media?.format,
                        averageScore = media?.averageScore,
                        popularity = media?.popularity,
                        progress = it,
                        headerValues = headerValues,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper.set(
                                headerValues.type.toFavoriteType(),
                                viewModel.mediaId,
                                it,
                            )
                        },
                        enableCoverImageSharedElement = false
                    )
                }
            },
            snackbarHostState = snackbarHostState,
        ) { scaffoldPadding ->
            val gridState = rememberLazyGridState()
            val following = viewModel.following.collectAsLazyPagingItems()
            val global = viewModel.global.collectAsLazyPagingItems()
            val selectedIsFollowing = viewModel.selectedIsFollowing
            val items = if (selectedIsFollowing && viewer != null) following else global

            Column {
                if (viewer != null) {
                    TabRow(selectedTabIndex = if (selectedIsFollowing) 0 else 1) {
                        Tab(
                            selected = selectedIsFollowing,
                            onClick = { viewModel.selectedIsFollowing = true },
                            text = {
                                Text(stringResource(Res.string.anime_media_activities_tab_following))
                            },
                        )
                        Tab(
                            selected = !selectedIsFollowing,
                            onClick = { viewModel.selectedIsFollowing = false },
                            text = {
                                Text(stringResource(Res.string.anime_media_activities_tab_global))
                            },
                        )
                    }
                }

                val refreshing = items.loadState.refresh is LoadState.Loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = items::refresh,
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    when (val refreshState = items.loadState.refresh) {
                        LoadState.Loading -> Unit
                        is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
                        is LoadState.NotLoading -> {
                            if (items.itemCount == 0) {
                                AnimeMediaListScreen.NoResults()
                            } else {
                                LazyVerticalGrid(
                                    state = gridState,
                                    columns = GridCells.Adaptive(450.dp),
                                    contentPadding = PaddingValues(
                                        top = 16.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 32.dp,
                                    ),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                                        .padding(scaffoldPadding)
                                ) {
                                    if (viewer == null) {
                                        item(
                                            key = "header",
                                            span = GridUtils.maxSpanFunction,
                                            contentType = "detailsSectionHeader",
                                        ) {
                                            DetailsSectionHeader(stringResource(Res.string.anime_media_activities_header))
                                        }
                                    }

                                    items(
                                        count = items.itemCount,
                                        key = items.itemKey { it.activityId },
                                        contentType = items.itemContentType { "item" },
                                    ) {
                                        val item = items[it]
                                        ListActivitySmallCard(
                                            viewer = viewer,
                                            activity = item?.activity,
                                            entry = item,
                                            onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                            onClickListEdit = editViewModel::initialize,
                                            clickable = true,
                                        )
                                    }

                                    when (items.loadState.append) {
                                        is LoadState.Loading -> item(
                                            "load_more_append",
                                            span = GridUtils.maxSpanFunction,
                                        ) {
                                            AnimeMediaListScreen.LoadingMore()
                                        }
                                        is LoadState.Error -> item(
                                            "load_more_error",
                                            span = GridUtils.maxSpanFunction,
                                        ) {
                                            AnimeMediaListScreen.AppendError { items.retry() }
                                        }
                                        is LoadState.NotLoading -> Unit
                                    }
                                }
                            }
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
    }

    data class Entry(
        val data: MediaActivityQuery.Data,
    ) {
        val titlesUnique = data.media.title
            ?.run { listOfNotNull(romaji, english, native) }
            ?.distinct()
            .orEmpty()
    }
}
