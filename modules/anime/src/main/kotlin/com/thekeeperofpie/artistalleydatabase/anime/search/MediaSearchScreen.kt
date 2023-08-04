package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object MediaSearchScreen {

    private val SCREEN_KEY = AnimeNavDestinations.SEARCH_MEDIA.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        title: Either<Int, String>,
        viewModel: AnimeSearchViewModel = hiltViewModel(),
        navigationCallback: AnimeNavigator.NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
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
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column {
                            val text = if (title is Either.Left) {
                                stringResource(title.value)
                            } else {
                                title.rightOrNull().orEmpty()
                            }
                            AppBar(
                                text = text,
                                upIconOption = upIconOption,
                            )

                            val selectedIsAnime =
                                viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME
                            TabRow(
                                selectedTabIndex = if (selectedIsAnime) 0 else 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.CenterHorizontally),
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
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems()
                val refreshing = content.loadState.refresh is LoadState.Loading
                val viewer by viewModel.viewer.collectAsState()
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.padding(scaffoldPadding)
                ) { onLongPressImage ->
                    when (val refreshState = content.loadState.refresh) {
                        LoadState.Loading -> Unit
                        is LoadState.Error -> AnimeMediaListScreen.Error(
                            exception = refreshState.error,
                        )
                        is LoadState.NotLoading -> {
                            if (content.itemCount == 0) {
                                AnimeMediaListScreen.NoResults()
                            } else {
                                LazyColumn(
                                    state = scrollStateSaver.lazyListState(),
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 16.dp + scaffoldPadding.calculateBottomPadding()
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    items(
                                        count = content.itemCount,
                                        key = content.itemKey { it.entryId.scopedId },
                                        contentType = content.itemContentType { it.entryId.type }
                                    ) { index ->
                                        when (val item = content[index]) {
                                            is AnimeSearchEntry.Media -> AnimeMediaListRow(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                entry = item,
                                                onClickListEdit = {
                                                    editViewModel.initialize(it.media)
                                                },
                                                onLongClick = {
                                                    viewModel.ignoreList
                                                        .toggle(item.media.id.toString())
                                                },
                                                onLongPressImage = onLongPressImage,
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )

                                            // TODO: Separated placeholder types
                                            else -> AnimeMediaListRow<MediaPreview>(
                                                screenKey = SCREEN_KEY,
                                                viewer = null,
                                                entry = null,
                                                onClickListEdit = {},
                                                onLongClick = {},
                                            )
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
            }
        }
    }
}
