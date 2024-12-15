package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_genre_info_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_info_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_tag_search_show_when_spoiler
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenre
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@OptIn(ExperimentalMaterial3Api::class)
object MediaSearchScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        upIconOption: UpIconOption?,
        title: AnimeDestination.SearchMedia.Title?,
        viewModel: AnimeSearchViewModel = viewModel {
            animeComponent.animeSearchViewModel(createSavedStateHandle())
        },
        tagId: String?,
        genre: String?,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val sortFilterController = when (viewModel.selectedType) {
                AnimeSearchViewModel.SearchType.ANIME -> viewModel.animeSortFilterController
                AnimeSearchViewModel.SearchType.MANGA -> viewModel.mangaSortFilterController
                else -> throw IllegalStateException(
                    "Invalid search type for this screen: ${viewModel.selectedType}"
                )
            }
            sortFilterController.PromptDialog()

            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    TopBar(
                        viewModel = viewModel,
                        upIconOption = upIconOption,
                        title = title,
                        tagId = tagId,
                        genre = genre,
                        scrollBehavior = scrollBehavior,
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems(CustomDispatchers.IO)
                val refreshState = content.loadState.refresh
                val refreshing = content.loadState.refresh is LoadState.Loading
                val viewer by viewModel.viewer.collectAsState()
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.padding(scaffoldPadding)
                ) {
                    val gridState = rememberLazyGridState()
                    sortFilterController.ImmediateScrollResetEffect(gridState)
                    val showWithSpoiler =
                        if (viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME) {
                            viewModel.animeSortFilterController.tagShowWhenSpoiler
                        } else {
                            viewModel.mangaSortFilterController.tagShowWhenSpoiler
                        }
                    OnChangeEffect(showWithSpoiler) {
                        gridState.scrollToItem(0)
                    }

                    val columns = viewModel.mediaViewOption.widthAdaptiveCells

                    LazyVerticalGrid(
                        state = gridState,
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
                        when {
                            refreshState is LoadState.Error && content.itemCount == 0 ->
                                item {
                                    AnimeMediaListScreen.ErrorContent(
                                        errorTextRes = Res.string.anime_media_list_error_loading,
                                        exception = refreshState.error,
                                    )
                                }
                            refreshState is LoadState.NotLoading && content.itemCount == 0 ->
                                item {
                                    Box(
                                        contentAlignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            stringResource(Res.string.anime_media_list_no_results),
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            ),
                                        )
                                    }
                                }
                            else -> {
                                items(
                                    count = content.itemCount,
                                    key = content.itemKey { it.entryId.scopedId },
                                    contentType = content.itemContentType { it.entryId.type }
                                ) {
                                    val item = content[it] as? AnimeSearchEntry.Media
                                    MediaViewOptionRow(
                                        mediaViewOption = viewModel.mediaViewOption,
                                        viewer = viewer,
                                        onClickListEdit = editViewModel::initialize,
                                        entry = item?.entry,
                                    )
                                }
                            }
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

    @Composable
    private fun TopBar(
        viewModel: AnimeSearchViewModel,
        upIconOption: UpIconOption?,
        title: AnimeDestination.SearchMedia.Title?,
        tagId: String?,
        genre: String?,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                val text = title?.text().orEmpty()

                TopAppBar(
                    title = { Text(text = text, maxLines = 1) },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        val infoContentDescriptionRes = remember {
                            if (tagId != null) {
                                Res.string.anime_media_tag_info_content_description
                            } else if (genre != null) {
                                if (MediaGenre.values().any { it.id == genre }) {
                                    Res.string.anime_media_genre_info_content_description
                                } else null
                            } else null
                        }

                        if (infoContentDescriptionRes != null) {
                            val mediaTagDialogController = LocalMediaTagDialogController.current
                            val mediaGenreDialogController = LocalMediaGenreDialogController.current
                            IconButton(onClick = {
                                if (tagId != null) {
                                    mediaTagDialogController?.onLongClickTag(tagId)
                                } else if (genre != null) {
                                    mediaGenreDialogController.onLongClickGenre(genre)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = stringResource(infoContentDescriptionRes),
                                )
                            }
                        }

                        val mediaViewOption = viewModel.mediaViewOption
                        val nextMediaViewOption = MediaViewOption.values()
                            .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                        IconButton(onClick = {
                            viewModel.mediaViewOption = nextMediaViewOption
                        }) {
                            Icon(
                                imageVector = nextMediaViewOption.icon,
                                contentDescription = stringResource(
                                    MediaDataRes.string.anime_media_view_option_icon_content_description
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

                if (tagId != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        val showWithSpoiler =
                            if (viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME) {
                                viewModel.animeSortFilterController.tagShowWhenSpoiler
                            } else {
                                viewModel.mangaSortFilterController.tagShowWhenSpoiler
                            }

                        Switch(
                            checked = showWithSpoiler,
                            onCheckedChange = {
                                if (viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME) {
                                    viewModel.animeSortFilterController.tagShowWhenSpoiler = it
                                } else {
                                    viewModel.mangaSortFilterController.tagShowWhenSpoiler = it
                                }
                            },
                        )

                        Text(
                            text = stringResource(Res.string.anime_media_tag_search_show_when_spoiler),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                val selectedIsAnime =
                    viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME
                TabRow(
                    selectedTabIndex = if (selectedIsAnime) 0 else 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
                    Tab(
                        selected = selectedIsAnime,
                        onClick = {
                            viewModel.selectedType =
                                AnimeSearchViewModel.SearchType.ANIME
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    AnimeSearchViewModel.SearchType.ANIME.textRes
                                ),
                                maxLines = 1,
                            )
                        }
                    )
                    Tab(
                        selected = !selectedIsAnime,
                        onClick = {
                            viewModel.selectedType =
                                AnimeSearchViewModel.SearchType.MANGA
                        },
                        text = {
                            Text(
                                text = stringResource(
                                    AnimeSearchViewModel.SearchType.MANGA.textRes
                                ),
                                maxLines = 1,
                            )
                        }
                    )
                }
            }
        }
    }
}
