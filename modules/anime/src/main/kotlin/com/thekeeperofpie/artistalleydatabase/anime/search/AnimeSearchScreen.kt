package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
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
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        isRoot: Boolean = true,
        title: Either<Int, String>? = null,
        viewModel: AnimeSearchViewModel = hiltViewModel(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.SEARCH.id,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            bottomNavigationState = bottomNavigationState,
        ) {
            val sortFilterController = viewModel.animeSortFilterController
                .takeIf { viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME}
            if (sortFilterController?.airingDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = sortFilterController.airingDateShown,
                    onShownForStartDateChange = {
                        sortFilterController.airingDateShown = it
                    },
                    onDateChange = sortFilterController::onAiringDateChange,
                )
            }
            SortFilterBottomScaffoldNoAppBarOffset(
                sortFilterController = sortFilterController,
                topBar = { TopBar(viewModel, upIconOption, isRoot, title, scrollBehavior) },
                bottomNavigationState = bottomNavigationState,
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems()
                val refreshing = content.loadState.refresh is LoadState.Loading
                val density = LocalDensity.current
                val topBarPadding by remember {
                    derivedStateOf {
                        scrollBehavior.state.heightOffsetLimit
                            .takeUnless { it == -Float.MAX_VALUE }
                            ?.let { density.run { -it.toDp() } }
                            ?: 0.dp
                    }
                }
                val viewer by viewModel.viewer.collectAsState()
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    tagShown = viewModel::tagShown,
                    onTagDismiss = { viewModel.tagShown = null },
                    pullRefreshTopPadding = { topBarPadding },
                    modifier = Modifier.nestedScroll(
                        NestedScrollSplitter(
                            bottomNavigationState?.nestedScrollConnection,
                            scrollBehavior.nestedScrollConnection,
                            consumeNone = true,
                        )
                    )
                ) { onLongPressImage ->
                    when (val refreshState = content.loadState.refresh) {
                        LoadState.Loading -> Unit
                        is LoadState.Error -> AnimeMediaListScreen.Error(
                            exception = refreshState.error,
                            modifier = Modifier.padding(top = topBarPadding),
                        )
                        is LoadState.NotLoading -> {
                            if (content.itemCount == 0) {
                                AnimeMediaListScreen.NoResults(Modifier.padding(top = topBarPadding))
                            } else {
                                LazyColumn(
                                    state = scrollStateSaver.lazyListState(),
                                    contentPadding = PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp + topBarPadding,
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
                                            is AnimeSearchEntry.Media<*> -> AnimeMediaListRow(
                                                screenKey = AnimeNavDestinations.SEARCH.id,
                                                viewer = viewer,
                                                entry = item,
                                                onClickListEdit = {
                                                    editViewModel.initialize(it.media)
                                                },
                                                onLongClick = {
                                                    viewModel.ignoreList
                                                        .toggle(item.media.id.toString())
                                                },
                                                onTagLongClick = viewModel::onTagLongClick,
                                                onLongPressImage = onLongPressImage,
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )
                                            is AnimeSearchEntry.Character -> CharacterListRow(
                                                screenKey = AnimeNavDestinations.SEARCH.id,
                                                entry = item,
                                                onLongPressImage = { /* TODO */ },
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )
                                            is AnimeSearchEntry.Staff -> StaffListRow(
                                                screenKey = AnimeNavDestinations.SEARCH.id,
                                                entry = item,
                                                onLongPressImage = { /* TODO */ },
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )
                                            is AnimeSearchEntry.User -> UserListRow(
                                                entry = item,
                                                onLongPressImage = { /* TODO */ },
                                                colorCalculationState = colorCalculationState,
                                                navigationCallback = navigationCallback,
                                            )

                                            // TODO: Separated placeholder types
                                            null -> AnimeMediaListRow<MediaPreview>(
                                                screenKey = AnimeNavDestinations.SEARCH.id,
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

    @Composable
    private fun TopBar(
        viewModel: AnimeSearchViewModel,
        upIconOption: UpIconOption? = null,
        isRoot: Boolean = true,
        title: Either<Int, String>? = null,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        if (isRoot) {
            EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                val isNotEmpty by remember { derivedStateOf { viewModel.query.isNotEmpty() } }
                BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                    viewModel.query = ""
                }
                Column {
                    StaticSearchBar(
                        query = viewModel.query,
                        onQueryChange = { viewModel.query = it },
                        leadingIcon = if (upIconOption != null) {
                            { UpIconButton(upIconOption) }
                        } else null,
                        placeholder = {
                            Text(
                                stringResource(
                                    when (viewModel.selectedType) {
                                        AnimeSearchViewModel.SearchType.ANIME ->
                                            R.string.anime_search_anime
                                        AnimeSearchViewModel.SearchType.MANGA ->
                                            R.string.anime_search_manga
                                        AnimeSearchViewModel.SearchType.CHARACTER ->
                                            R.string.anime_search_characters
                                        AnimeSearchViewModel.SearchType.STAFF ->
                                            R.string.anime_search_staff
                                        AnimeSearchViewModel.SearchType.USER ->
                                            R.string.anime_search_user
                                    }
                                )
                            )
                        },
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
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    )

                    val selectedTypeIndex = AnimeSearchViewModel.SearchType.values()
                        .indexOf(viewModel.selectedType)
                    ScrollableTabRow(
                        selectedTabIndex = selectedTypeIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        divider = { /* No divider, manually draw so that it's full width */ }
                    ) {
                        AnimeSearchViewModel.SearchType.values()
                            .forEachIndexed { index, tab ->
                                Tab(
                                    selected = selectedTypeIndex == index,
                                    onClick = { viewModel.selectedType = tab },
                                    text = {
                                        Text(
                                            text = stringResource(tab.textRes),
                                            maxLines = 1,
                                        )
                                    }
                                )
                            }
                    }

                    Divider()
                }
            }
        } else if (title != null) {
            val text = if (title is Either.Left) {
                stringResource(title.value)
            } else {
                title.rightOrNull().orEmpty()
            }
            AppBar(
                text = text,
                upIconOption = upIconOption,
                scrollBehavior = scrollBehavior,
            )
        }
    }

}
