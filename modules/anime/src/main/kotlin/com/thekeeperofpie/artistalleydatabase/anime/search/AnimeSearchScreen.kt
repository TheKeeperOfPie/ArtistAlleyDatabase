package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeSearchScreen {

    private val SCREEN_KEY = AnimeNavDestinations.SEARCH.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: AnimeSearchViewModel = hiltViewModel(),
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val editSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = editViewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            sheetState = editSheetState,
            bottomNavigationState = bottomNavigationState,
        ) {
            val sortFilterController = when (viewModel.selectedType) {
                AnimeSearchViewModel.SearchType.ANIME -> viewModel.animeSortFilterController
                AnimeSearchViewModel.SearchType.MANGA -> viewModel.mangaSortFilterController
                AnimeSearchViewModel.SearchType.CHARACTER -> viewModel.characterSortFilterController
                AnimeSearchViewModel.SearchType.STAFF -> viewModel.staffSortFilterController
                AnimeSearchViewModel.SearchType.STUDIO -> viewModel.studioSortFilterController
                AnimeSearchViewModel.SearchType.USER -> viewModel.userSortFilterController
            }
            sortFilterController.PromptDialog()

            val sheetState: SheetState =
                rememberStandardBottomSheetState(
                    confirmValueChange = { it != SheetValue.Hidden },
                    skipHiddenState = true,
                )
            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    TopBar(
                        viewModel,
                        upIconOption,
                        scrollBehavior,
                        editSheetState,
                        sheetState,
                    )
                },
                sheetState = sheetState,
                bottomNavigationState = bottomNavigationState,
                modifier = Modifier
                    .conditionally(bottomNavigationState != null) {
                        nestedScroll(bottomNavigationState!!.nestedScrollConnection)
                    }
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems()
                val selectedType = viewModel.selectedType
                val unlocked by viewModel.unlocked.collectAsState(false)
                val selectedUnlocked = selectedType == AnimeSearchViewModel.SearchType.ANIME
                        || selectedType == AnimeSearchViewModel.SearchType.MANGA
                        || unlocked

                val refreshing = content.loadState.refresh is LoadState.Loading && selectedUnlocked

                val viewer by viewModel.viewer.collectAsState()
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.padding(scaffoldPadding),
                ) {
                    if (!selectedUnlocked) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.anime_requires_unlock),
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                                val navigationCallback = LocalNavigationCallback.current
                                Button(
                                    onClick = {
                                        navigationCallback.navigate(
                                            AnimeNavDestinations.FEATURE_TIERS.id
                                        )
                                    }
                                ) {
                                    Text(
                                        text = stringResource(
                                            R.string.anime_open_feature_tiers_button
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        when (val refreshState = content.loadState.refresh) {
                            LoadState.Loading -> Unit
                            is LoadState.Error -> AnimeMediaListScreen.Error(
                                exception = refreshState.error,
                            )
                            is LoadState.NotLoading -> {
                                if (content.itemCount == 0) {
                                    AnimeMediaListScreen.NoResults()
                                } else {
                                    val columns =
                                        if (selectedType == AnimeSearchViewModel.SearchType.ANIME || selectedType == AnimeSearchViewModel.SearchType.MANGA) {
                                            when (viewModel.mediaViewOption) {
                                                MediaViewOption.SMALL_CARD,
                                                MediaViewOption.LARGE_CARD,
                                                MediaViewOption.COMPACT,
                                                -> GridCells.Adaptive(300.dp)
                                                MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                                            }
                                        } else {
                                            GridCells.Adaptive(300.dp)
                                        }
                                    LazyVerticalGrid(
                                        columns = columns,
                                        state = scrollStateSaver.lazyGridState(),
                                        contentPadding = PaddingValues(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 16.dp,
                                            bottom = 16.dp + scaffoldPadding.calculateBottomPadding()
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(
                                            count = content.itemCount,
                                            key = content.itemKey { it.entryId.scopedId },
                                            contentType = content.itemContentType { it.entryId.type }
                                        ) { index ->
                                            when (val item = content[index]) {
                                                is AnimeSearchEntry.Media -> MediaRow(
                                                    viewer = viewer,
                                                    viewModel = viewModel,
                                                    editViewModel = editViewModel,
                                                    entry = item,
                                                )
                                                is AnimeSearchEntry.Character -> CharacterListRow(
                                                    screenKey = SCREEN_KEY,
                                                    viewer = viewer,
                                                    entry = item.entry,
                                                    onClickListEdit = {
                                                        editViewModel.initialize(it.media)
                                                    },
                                                )
                                                is AnimeSearchEntry.Staff -> StaffListRow(
                                                    screenKey = SCREEN_KEY,
                                                    viewer = viewer,
                                                    entry = item.entry,
                                                    onClickListEdit = {
                                                        editViewModel.initialize(it.media)
                                                    },
                                                )
                                                is AnimeSearchEntry.Studio -> StudioListRow(
                                                    screenKey = SCREEN_KEY,
                                                    entry = item.entry,
                                                )
                                                is AnimeSearchEntry.User -> UserListRow(
                                                    screenKey = SCREEN_KEY,
                                                    viewer = viewer,
                                                    entry = item.entry,
                                                    onClickListEdit = {
                                                        editViewModel.initialize(it.media)
                                                    },
                                                )

                                                // TODO: Separated placeholder types
                                                null -> when (selectedType) {
                                                    AnimeSearchViewModel.SearchType.ANIME,
                                                    AnimeSearchViewModel.SearchType.MANGA,
                                                    -> MediaRow(
                                                        viewer = viewer,
                                                        viewModel = viewModel,
                                                        editViewModel = editViewModel,
                                                        entry = null,
                                                    )
                                                    AnimeSearchViewModel.SearchType.CHARACTER ->
                                                        CharacterListRow(
                                                            screenKey = SCREEN_KEY,
                                                            viewer = viewer,
                                                            entry = null,
                                                            onClickListEdit = {},
                                                        )
                                                    AnimeSearchViewModel.SearchType.STAFF ->
                                                        StaffListRow(
                                                            screenKey = SCREEN_KEY,
                                                            viewer = viewer,
                                                            entry = null,
                                                            onClickListEdit = {},
                                                        )
                                                    AnimeSearchViewModel.SearchType.STUDIO ->
                                                        StudioListRow(
                                                            screenKey = SCREEN_KEY,
                                                            entry = null,
                                                        )
                                                    AnimeSearchViewModel.SearchType.USER ->
                                                        UserListRow(
                                                            screenKey = SCREEN_KEY,
                                                            viewer = viewer,
                                                            entry = null,
                                                            onClickListEdit = {},
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
                }
            }
        }
    }

    @Composable
    private fun MediaRow(
        viewer: AniListViewer?,
        viewModel: AnimeSearchViewModel,
        editViewModel: MediaEditViewModel,
        entry: AnimeSearchEntry.Media?,
    ) {
        when (viewModel.mediaViewOption) {
            MediaViewOption.SMALL_CARD -> AnimeMediaListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onClickListEdit = { editViewModel.initialize(it.media) },
            )
            MediaViewOption.LARGE_CARD -> AnimeMediaLargeCard(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onClickListEdit = { editViewModel.initialize(it.media) },
            )
            MediaViewOption.COMPACT -> AnimeMediaCompactListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onClickListEdit = { editViewModel.initialize(it.media) },
            )
            MediaViewOption.GRID -> MediaGridCard(
                screenKey = SCREEN_KEY,
                entry = entry,
                viewer = viewer,
                onClickListEdit = { editViewModel.initialize(it.media) },
            )
        }
    }

    @Composable
    private fun TopBar(
        viewModel: AnimeSearchViewModel,
        upIconOption: UpIconOption? = null,
        scrollBehavior: TopAppBarScrollBehavior,
        sheetStateOne: SheetState,
        sheetStateTwo: SheetState,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            val isNotEmpty by remember { derivedStateOf { viewModel.query.isNotEmpty() } }
            BackHandler(
                isNotEmpty && !WindowInsets.isImeVisible
                        // Need to manually check sheet state because top bar
                        // takes precedence over all other handlers
                        && sheetStateOne.targetValue != SheetValue.Expanded
                        && sheetStateTwo.targetValue != SheetValue.Expanded
            ) {
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
                                    AnimeSearchViewModel.SearchType.STUDIO ->
                                        R.string.anime_search_studio
                                    AnimeSearchViewModel.SearchType.USER ->
                                        R.string.anime_search_user
                                }
                            )
                        )
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { viewModel.query = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        R.string.anime_search_clear
                                    ),
                                )
                            }

                            if (viewModel.selectedType == AnimeSearchViewModel.SearchType.ANIME
                                || viewModel.selectedType == AnimeSearchViewModel.SearchType.MANGA
                            ) {
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
                            }
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

                HorizontalDivider()
            }
        }
    }
}
