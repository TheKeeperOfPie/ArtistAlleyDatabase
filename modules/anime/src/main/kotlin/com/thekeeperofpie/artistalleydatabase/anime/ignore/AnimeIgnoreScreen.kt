package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeIgnoreScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit = {},
        @StringRes titleRes: Int,
        viewModel: AnimeMediaIgnoreViewModel = hiltViewModel(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.IGNORED.id,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
        ) {
            val sortFilterController = viewModel.sortFilterController
            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = { TopBar(viewModel, onClickBack, titleRes, scrollBehavior) },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems()
                val refreshing = content.loadState.refresh is LoadState.Loading
                val viewer by viewModel.viewer.collectAsState()
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.padding(scaffoldPadding),
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
                                        key = content.itemKey { it.media.id },
                                        contentType = content.itemContentType { "media" },
                                    ) { index ->
                                        when (val item = content[index]) {
                                            is AnimeMediaListRow.Entry<*> -> AnimeMediaListRow(
                                                screenKey = AnimeNavDestinations.IGNORED.id,
                                                entry = item,
                                                viewer = viewer,
                                                onClickListEdit = {
                                                    editViewModel.initialize(it.media)
                                                },
                                                onLongClick = viewModel::onMediaLongClick,
                                                onLongPressImage = onLongPressImage,
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )
                                            null -> AnimeMediaListRow<MediaPreview>(
                                                screenKey = AnimeNavDestinations.IGNORED.id,
                                                entry = null,
                                                viewer = null,
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

    @Composable
    private fun TopBar(
        viewModel: AnimeMediaIgnoreViewModel,
        onClickBack: () -> Unit,
        @StringRes titleRes: Int,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            val isNotEmpty by remember { derivedStateOf { viewModel.query.isNotEmpty() } }
            BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                viewModel.query = ""
            }
            StaticSearchBar(
                query = viewModel.query,
                onQueryChange = { viewModel.query = it },
                leadingIcon = { ArrowBackIconButton(onClickBack) },
                placeholder = { Text(stringResource(titleRes)) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.query = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(
                                R.string.anime_search_clear
                            ),
                        )
                    }
                },
            )
        }
    }
}
