package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
object UserFavoriteMediaScreen {

    private val SCREEN_KEY = AnimeNavDestinations.USER_FAVORITE_MEDIA.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: UserFavoriteMediaViewModel = hiltViewModel(),
        title: @Composable () -> String,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val editSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = editViewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            sheetState = editSheetState,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { AutoResizeHeightText(text = title(), maxLines = 1) },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(option = upIconOption)
                            }
                        },
                        actions = {
                            val mediaViewOption = viewModel.mediaViewOption
                            val nextMediaViewOption = MediaViewOption.values()
                                .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                            IconButton(onClick = {
                                viewModel.mediaViewOption = nextMediaViewOption
                            }) {
                                Icon(
                                    imageVector = nextMediaViewOption.icon,
                                    contentDescription = stringResource(
                                        R.string.anime_media_view_option_icon_content_description
                                    ),
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                            )
                        ),
                    )
                }
            },
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { scaffoldPadding ->
            val media = viewModel.media.collectAsLazyPagingItems()
            val refreshing = media.loadState.refresh is LoadState.Loading

            val viewer by viewModel.viewer.collectAsState()
            val pullRefreshState = rememberPullRefreshState(
                refreshing = refreshing,
                onRefresh = viewModel::refresh,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .pullRefresh(pullRefreshState)
            ) {
                when (val refreshState = media.loadState.refresh) {
                    LoadState.Loading -> Unit
                    is LoadState.Error -> AnimeMediaListScreen.Error(
                        exception = refreshState.error,
                    )
                    is LoadState.NotLoading -> {
                        if (media.itemCount == 0) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            val columns = when (viewModel.mediaViewOption) {
                                MediaViewOption.SMALL_CARD,
                                MediaViewOption.LARGE_CARD,
                                MediaViewOption.COMPACT,
                                -> GridCells.Adaptive(300.dp)
                                MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                            }
                            LazyVerticalGrid(
                                columns = columns,
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = media.itemCount,
                                    key = media.itemKey { it.mediaId },
                                    contentType = media.itemContentType { "media" }
                                ) {
                                    val entry = media[it]
                                    MediaViewOptionRow(
                                        screenKey = SCREEN_KEY,
                                        mediaViewOption = viewModel.mediaViewOption,
                                        viewer = viewer,
                                        onClickListEdit = editViewModel::initialize,
                                        entry = entry,
                                        // API doesn't support returning viewer entry
                                        forceListEditIcon = true,
                                    )
                                }

                                when (media.loadState.append) {
                                    is LoadState.Loading -> item(key = "load_more_append") {
                                        AnimeMediaListScreen.LoadingMore()
                                    }
                                    is LoadState.Error -> item(key = "load_more_error") {
                                        AnimeMediaListScreen.AppendError(media::retry)
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
