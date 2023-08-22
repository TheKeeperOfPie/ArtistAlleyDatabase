package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

@OptIn(ExperimentalMaterial3Api::class)
object AnimeIgnoreScreen {

    private val SCREEN_KEY = AnimeNavDestinations.IGNORED.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        viewModel: AnimeMediaIgnoreViewModel = hiltViewModel(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.IGNORED.id,
            viewModel = editViewModel,
            topBar = { TopBar(viewModel, upIconOption, scrollBehavior) },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        ) { scaffoldPadding ->
            val content = viewModel.content.collectAsLazyPagingItems()
            val refreshing = content.loadState.refresh is LoadState.Loading
            val viewer by viewModel.viewer.collectAsState()
            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = viewModel::onRefresh,
                modifier = Modifier.padding(scaffoldPadding),
            ) {
                when (val refreshState = content.loadState.refresh) {
                    LoadState.Loading -> Unit
                    is LoadState.Error -> AnimeMediaListScreen.Error(
                        exception = refreshState.error,
                    )
                    is LoadState.NotLoading -> {
                        if (content.itemCount == 0) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            val columns = when (viewModel.mediaViewOption) {
                                MediaViewOption.SMALL_CARD,
                                MediaViewOption.LARGE_CARD,
                                MediaViewOption.COMPACT,
                                -> GridCells.Adaptive(300.dp)
                                MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                            }
                            val mediaType = viewModel.selectedType
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
                                    key = content.itemKey { it.media.id },
                                    contentType = content.itemContentType { "media" },
                                ) { index ->
                                    val networkEntry = content[index]
                                    val showQuickEdit = networkEntry != null
                                    val entry =
                                        networkEntry ?: viewModel.placeholder(index, mediaType)
                                    MediaViewOptionRow(
                                        screenKey = SCREEN_KEY,
                                        mediaViewOption = viewModel.mediaViewOption,
                                        viewer = viewer,
                                        editViewModel = editViewModel,
                                        entry = entry,
                                        showQuickEdit = showQuickEdit,
                                    )
                                }

                                when (content.loadState.append) {
                                    is LoadState.Loading -> item(key = "load_more_append") {
                                        AnimeMediaListScreen.LoadingMore()
                                    }
                                    is LoadState.Error -> item(key = "load_more_error") {
                                        AnimeMediaListScreen.AppendError { content.retry() }
                                    }
                                    is LoadState.NotLoading -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        viewModel: AnimeMediaIgnoreViewModel,
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.anime_media_ignore_header)) },
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
                )

                val selectedIsAnime = viewModel.selectedType == MediaType.ANIME
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = {
                            viewModel.selectedType = MediaType.ANIME
                        },
                        text = { Text(stringResource(R.string.anime_media_ignore_tab_anime)) },
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = {
                            viewModel.selectedType = MediaType.MANGA
                        },
                        text = { Text(stringResource(R.string.anime_media_ignore_tab_manga)) },
                    )
                }
            }
        }
    }
}
