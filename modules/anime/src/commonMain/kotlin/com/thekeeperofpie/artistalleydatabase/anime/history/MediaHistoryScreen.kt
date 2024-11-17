package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_history_anime
import artistalleydatabase.modules.anime.generated.resources.anime_media_history_header
import artistalleydatabase.modules.anime.generated.resources.anime_media_history_manga
import artistalleydatabase.modules.anime.generated.resources.anime_media_history_not_enabled_button
import artistalleydatabase.modules.anime.generated.resources.anime_media_history_not_enabled_prompt
import artistalleydatabase.modules.anime.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object MediaHistoryScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        upIconOption: UpIconOption? = null,
        viewModel: MediaHistoryViewModel = viewModel {
            animeComponent.mediaHistoryViewModel(createSavedStateHandle())
        },
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val snackbarHostState = remember { SnackbarHostState() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    Column {
                        TopAppBar(
                            title = { Text(text = stringResource(Res.string.anime_media_history_header)) },
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
                                            Res.string.anime_media_view_option_icon_content_description
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
                                text = { Text(stringResource(Res.string.anime_media_history_anime)) },
                            )
                            Tab(
                                selected = !selectedIsAnime,
                                onClick = {
                                    viewModel.selectedType = MediaType.MANGA
                                },
                                text = { Text(stringResource(Res.string.anime_media_history_manga)) },
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
                        val columns = viewModel.mediaViewOption.widthAdaptiveCells
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
                text = stringResource(Res.string.anime_media_history_not_enabled_prompt),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
            Button(onClick = { viewModel.enabled.value = true }) {
                Text(stringResource(Res.string.anime_media_history_not_enabled_button))
            }
        }
    }
}
