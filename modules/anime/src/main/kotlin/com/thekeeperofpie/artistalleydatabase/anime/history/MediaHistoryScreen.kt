package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.type.MediaType
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
object MediaHistoryScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: MediaHistoryViewModel = hiltViewModel(),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val snackbarHostState = remember { SnackbarHostState() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    Column {
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
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                                )
                            ),
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
                                text = { Text(stringResource(R.string.anime_media_history_anime)) },
                            )
                            Tab(
                                selected = !selectedIsAnime,
                                onClick = {
                                    viewModel.selectedType = MediaType.MANGA
                                },
                                text = { Text(stringResource(R.string.anime_media_history_manga)) },
                            )
                        }
                    }
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
                val enabled by viewModel.enabled.collectAsState()
                if (!enabled) {
                    NotEnabledPrompt(viewModel)
                } else {
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
                                key = content.itemKey { "media_${it.media.id}" },
                                contentType = content.itemContentType { "media" },
                            ) {
                                val networkEntry = content[it]
                                val entry =
                                    networkEntry ?: viewModel.placeholder(it, mediaType)
                                MediaViewOptionRow(
                                    mediaViewOption = viewModel.mediaViewOption,
                                    viewer = viewer,
                                    onClickListEdit = editViewModel::initialize,
                                    entry = entry,
                                    showQuickEdit = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NotEnabledPrompt(viewModel: MediaHistoryViewModel) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.anime_media_history_not_enabled_prompt),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
            Button(onClick = { viewModel.enabled.value = true }) {
                Text(stringResource(R.string.anime_media_history_not_enabled_button))
            }
        }
    }
}
