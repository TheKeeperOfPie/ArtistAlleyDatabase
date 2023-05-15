package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.MediaAdvancedSearchQuery.Data.Page.Medium
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        isRoot: Boolean = true,
        title: String? = null,
        viewModel: AnimeSearchViewModel = hiltViewModel(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState()
        AnimeMediaFilterOptionsBottomPanel(
            topBar = {
                if (isRoot) {
                    EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                        TextField(
                            viewModel.query,
                            placeholder = { Text(stringResource(id = R.string.anime_search)) },
                            onValueChange = viewModel::onQuery,
                            leadingIcon = { NavMenuIconButton(onClickNav) },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.onQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(
                                            R.string.anime_search_clear
                                        ),
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                } else if (title != null) {
                    AppBar(
                        text = title,
                        onClickBack = onClickNav,
                        scrollBehavior = scrollBehavior,
                    )
                }
            },
            filterData = viewModel::filterData,
            onTagLongClicked = viewModel::onTagLongClick,
            showMediaListStatus = false,
            showLoadSave = true,
            bottomNavigationState = bottomNavigationState,
        ) { scaffoldPadding ->
            val content = viewModel.content.collectAsLazyPagingItems()
            val refreshing = content.loadState.refresh is LoadState.Loading
            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = viewModel::onRefresh,
                tagShown = viewModel::tagShown,
                onTagDismiss = viewModel::onTagDismiss,
                pullRefreshTopPadding = {
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { LocalDensity.current.run { -it.toDp() } }
                        ?: 0.dp
                },
                modifier = Modifier.nestedScroll(
                    NestedScrollSplitter(
                        bottomNavigationState?.nestedScrollConnection,
                        scrollBehavior.nestedScrollConnection,
                    )
                )
            ) { onLongPressImage ->
                when (content.loadState.refresh) {
                    LoadState.Loading -> Unit
                    is LoadState.Error -> AnimeMediaListScreen.Error()
                    is LoadState.NotLoading -> {
                        if (content.itemCount == 0) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            LazyColumn(
                                state = scrollStateSaver.lazyListState(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp + (scrollBehavior.state.heightOffsetLimit
                                        .takeUnless { it == -Float.MAX_VALUE }
                                        ?.let { LocalDensity.current.run { -it.toDp() } }
                                        ?: 0.dp),
                                    bottom = 16.dp + scaffoldPadding.calculateBottomPadding()
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                items(
                                    count = content.itemCount,
                                    key = content.itemKey { it.id.scopedId },
                                    contentType = content.itemContentType()
                                ) { index ->
                                    when (val item = content[index]) {
                                        is AnimeMediaListScreen.Entry.Item -> AnimeMediaListRow(
                                            entry = item,
                                            onLongClick = viewModel::onMediaLongClick,
                                            onTagLongClick = viewModel::onTagLongClick,
                                            onLongPressImage = onLongPressImage,
                                            colorCalculationState = colorCalculationState,
                                            navigationCallback = navigationCallback,
                                        )
                                        null -> AnimeMediaListRow(AnimeMediaListRow.Entry.Loading)
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

@Preview
@Composable
private fun Preview() {
    val viewModel = hiltViewModel<AnimeSearchViewModel>().apply {
        content.value = PagingData.from(
            listOf(
                AnimeMediaListScreen.Entry.Item(
                    Medium(
                        title = Medium.Title(
                            userPreferred = "Ano Hi Mita Hana no Namae wo Bokutachi wa Mada Shiranai.",
                        ),
                    )
                )
            )
        )
    }
    AnimeSearchScreen(viewModel = viewModel)
}
