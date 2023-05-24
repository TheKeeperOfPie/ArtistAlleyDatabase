package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterOptionsBottomPanel
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeIgnoreScreen {

    @Composable
    operator fun invoke(
        onClickNav: () -> Unit = {},
        @StringRes titleRes: Int,
        viewModel: AnimeMediaIgnoreViewModel = hiltViewModel(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        AnimeMediaFilterOptionsBottomPanel(
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    BackHandler(viewModel.query.isNotEmpty() && !WindowInsets.isImeVisible) {
                        viewModel.query = ""
                    }
                    TextField(
                        viewModel.query,
                        placeholder = { Text(stringResource(titleRes)) },
                        onValueChange = { viewModel.query = it },
                        leadingIcon = { NavMenuIconButton(onClickNav) },
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
            },
            filterData = viewModel::filterData,
            onTagLongClick = viewModel::onTagLongClick,
            showLoadSave = true,
            showIgnoredFilter = false,
        ) { scaffoldPadding ->
            val content = viewModel.content.collectAsLazyPagingItems()
            val refreshing = content.loadState.refresh is LoadState.Loading
            AnimeMediaListScreen(
                refreshing = refreshing,
                onRefresh = viewModel::onRefresh,
                tagShown = viewModel::tagShown,
                onTagDismiss = { viewModel.tagShown = null },
                pullRefreshTopPadding = {
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { LocalDensity.current.run { -it.toDp() } }
                        ?: 0.dp
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            ) { onLongPressImage ->
                when (val refreshState = content.loadState.refresh) {
                    LoadState.Loading -> Unit
                    is LoadState.Error -> AnimeMediaListScreen.Error(exception = refreshState.error)
                    is LoadState.NotLoading -> {
                        if (content.itemCount == 0) {
                            AnimeMediaListScreen.NoResults()
                        } else {
                            LazyColumn(
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
                                    contentType = content.itemContentType { it.id.type }
                                ) { index ->
                                    when (val item = content[index]) {
                                        is AnimeMediaListRow.MediaEntry<*> -> AnimeMediaListRow(
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
