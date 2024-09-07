package com.thekeeperofpie.artistalleydatabase.anime.user.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
object UserFavoriteCharactersScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        upIconOption: UpIconOption? = null,
        viewModel: UserFavoriteCharactersViewModel = viewModel {
            animeComponent.userFavoriteCharactersViewModel(createSavedStateHandle())
        },
        title: @Composable () -> String,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val editSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = editViewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )
        MediaEditBottomSheetScaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            viewModel = editViewModel,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { AutoResizeHeightText(text = title(), maxLines = 1) },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(option = upIconOption)
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
            sheetState = editSheetState
        ) { scaffoldPadding ->
            val characters = viewModel.characters.collectAsLazyPagingItems()
            val refreshing = characters.loadState.refresh is LoadState.Loading

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
                when (val refreshState = characters.loadState.refresh) {
                    LoadState.Loading -> Unit
                    is LoadState.Error -> AnimeMediaListScreen.Error(
                        exception = refreshState.error,
                    )
                    is LoadState.NotLoading -> {
                        if (characters.itemCount == 0) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(300.dp),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = characters.itemCount,
                                    key = characters.itemKey { it.character.id },
                                    contentType = characters.itemContentType { "character" }
                                ) {
                                    val entry = characters[it]
                                    CharacterListRow(
                                        viewer = viewer,
                                        entry = entry,
                                        onClickListEdit = editViewModel::initialize,
                                    )
                                }

                                when (characters.loadState.append) {
                                    is LoadState.Loading -> item(key = "load_more_append") {
                                        AnimeMediaListScreen.LoadingMore()
                                    }
                                    is LoadState.Error -> item(key = "load_more_error") {
                                        AnimeMediaListScreen.AppendError(characters::retry)
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
