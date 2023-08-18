package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object MediaHistoryScreen {

    private val SCREEN_KEY = AnimeNavDestinations.MEDIA_HISTORY.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: MediaHistoryViewModel = hiltViewModel(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val snackbarHostState = remember { SnackbarHostState() }
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.USER_LIST.id,
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.anime_media_history_header)) },
                        navigationIcon = { upIconOption?.let { UpIconButton(upIconOption) } },
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
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                            )
                        ),
                    )
                }
            },
            snackbarHostState = snackbarHostState,
        ) {
            val content = viewModel.content.collectAsLazyPagingItems()
            val refreshing = content.loadState.refresh is LoadState.Loading

            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = content::refresh,
                modifier = Modifier
                    .padding(it)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                // TODO: Error state
                val hasItems = content.itemCount > 0
                if (!refreshing && !hasItems) {
                    AnimeMediaListScreen.NoResults()
                } else {
                    val columns = when (viewModel.mediaViewOption) {
                        MediaViewOption.SMALL_CARD,
                        MediaViewOption.LARGE_CARD,
                        MediaViewOption.COMPACT,
                        -> GridCells.Adaptive(300.dp)
                        MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                    }

                    val viewer by viewModel.viewer.collectAsState()
                    LazyVerticalGrid(
                        columns = columns,
                        contentPadding = PaddingValues(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            count = content.itemCount,
                            key = content.itemKey { "media_${it.media.id}" },
                            contentType = content.itemContentType { "media" },
                        ) {
                            val networkEntry = content[it]
                            val showQuickEdit = networkEntry != null
                            val entry = networkEntry ?: viewModel.placeholder(it)
                            MediaRow(
                                viewer = viewer,
                                viewModel = viewModel,
                                entry = entry,
                                editViewModel = editViewModel,
                                showQuickEdit = showQuickEdit,
                                modifier = Modifier
                                    .animateItemPlacement(),
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MediaRow(
        viewer: AniListViewer?,
        viewModel: MediaHistoryViewModel,
        editViewModel: MediaEditViewModel,
        entry: MediaPreviewWithDescriptionEntry?,
        showQuickEdit: Boolean = false,
        modifier: Modifier = Modifier,
    ) {
        when (viewModel.mediaViewOption) {
            MediaViewOption.SMALL_CARD -> AnimeMediaListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onClickListEdit = { editViewModel.initialize(it.media) },
                onLongClick = {
                    if (entry != null) {
                        viewModel.ignoreList.toggle(entry.media.id.toString())
                    }
                },
                showQuickEdit = showQuickEdit,
                modifier = modifier,
            )
            MediaViewOption.LARGE_CARD -> AnimeMediaLargeCard(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onLongClick = {
                    if (entry != null) {
                        viewModel.ignoreList.toggle(entry.media.id.toString())
                    }
                },
                onClickListEdit = { editViewModel.initialize(it.media) },
                showQuickEdit = showQuickEdit,
                modifier = modifier,
            )
            MediaViewOption.COMPACT -> AnimeMediaCompactListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onLongClick = {
                    if (entry != null) {
                        viewModel.ignoreList.toggle(entry.media.id.toString())
                    }
                },
                onClickListEdit = { editViewModel.initialize(it.media) },
                showQuickEdit = showQuickEdit,
                modifier = modifier,
            )
            MediaViewOption.GRID -> MediaGridCard(
                screenKey = SCREEN_KEY,
                entry = entry,
                viewer = viewer,
                onClickListEdit = { editViewModel.initialize(it.media) },
                onLongClick = {
                    if (entry != null) {
                        viewModel.ignoreList.toggle(entry.media.id.toString())
                    }
                },
                showQuickEdit = showQuickEdit,
                modifier = modifier,
            )
        }
    }
}
