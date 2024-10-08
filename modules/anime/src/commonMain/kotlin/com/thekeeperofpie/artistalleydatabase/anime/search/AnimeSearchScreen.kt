package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_no_results
import artistalleydatabase.modules.anime.generated.resources.anime_media_view_option_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_open_feature_tiers_button
import artistalleydatabase.modules.anime.generated.resources.anime_requires_unlock
import artistalleydatabase.modules.anime.generated.resources.anime_search_anime
import artistalleydatabase.modules.anime.generated.resources.anime_search_characters
import artistalleydatabase.modules.anime.generated.resources.anime_search_clear
import artistalleydatabase.modules.anime.generated.resources.anime_search_manga
import artistalleydatabase.modules.anime.generated.resources.anime_search_staff
import artistalleydatabase.modules.anime.generated.resources.anime_search_studio
import artistalleydatabase.modules.anime.generated.resources.anime_search_user
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.items
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        upIconOption: UpIconOption? = null,
        viewModel: AnimeSearchViewModel = viewModel {
            animeComponent.animeSearchViewModel(createSavedStateHandle())
        },
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val editSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = editViewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            bottomNavigationState = bottomNavigationState,
            sheetState = editSheetState,
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

            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    TopBar(
                        viewModel,
                        upIconOption,
                        scrollBehavior,
                        editSheetState,
                        bottomSheetScaffoldState.bottomSheetState,
                    )
                },
                sheetState = bottomSheetScaffoldState.bottomSheetState,
                scaffoldState = bottomSheetScaffoldState,
                bottomNavigationState = bottomNavigationState,
                modifier = Modifier
                    .conditionally(bottomNavigationState != null) {
                        nestedScroll(bottomNavigationState!!.nestedScrollConnection)
                    }
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val content = viewModel.content.collectAsLazyPagingItems(CustomDispatchers.IO)
                val refreshState = content.loadState.refresh
                val errorText = (refreshState as? LoadState.Error)
                    ?.let { stringResource(Res.string.anime_media_list_error_loading) }
                LaunchedEffect(errorText) {
                    if (errorText != null) {
                        bottomSheetScaffoldState.snackbarHostState.showSnackbar(errorText)
                    }
                }

                val selectedType = viewModel.selectedType
                val unlocked by viewModel.unlocked.collectAsState()
                val selectedUnlocked = selectedType == AnimeSearchViewModel.SearchType.ANIME
                        || selectedType == AnimeSearchViewModel.SearchType.MANGA
                        || unlocked

                val refreshing = refreshState is LoadState.Loading && selectedUnlocked

                val viewer by viewModel.viewer.collectAsState()
                val layoutDirection = LocalLayoutDirection.current
                AnimeMediaListScreen(
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    modifier = Modifier.padding(
                        start = scaffoldPadding.calculateStartPadding(layoutDirection),
                        end = scaffoldPadding.calculateEndPadding(layoutDirection),
                        top = scaffoldPadding.calculateTopPadding(),
                    ),
                ) {
                    if (!selectedUnlocked) {
                        LockedFeatureTiers()
                    } else {
                        val columns =
                            if (selectedType == AnimeSearchViewModel.SearchType.ANIME || selectedType == AnimeSearchViewModel.SearchType.MANGA) {
                                viewModel.mediaViewOption.widthAdaptiveCells
                            } else {
                                GridCells.Adaptive(300.dp)
                            }
                        val gridState = scrollStateSaver.lazyGridState()
                        sortFilterController.ImmediateScrollResetEffect(gridState)
                        LazyVerticalGrid(
                            columns = columns,
                            state = gridState,
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
                                        data = content,
                                        placeholderCount = 10,
                                        key = { it.entryId.scopedId },
                                        contentType = { it.entryId.type }
                                    ) { data ->
                                        when (data) {
                                            is AnimeSearchEntry.Media -> MediaViewOptionRow(
                                                mediaViewOption = viewModel.mediaViewOption,
                                                viewer = viewer,
                                                onClickListEdit = editViewModel::initialize,
                                                entry = data.entry,
                                            )
                                            is AnimeSearchEntry.Character -> CharacterListRow(
                                                viewer = viewer,
                                                entry = data.entry,
                                                onClickListEdit = editViewModel::initialize,
                                            )
                                            is AnimeSearchEntry.Staff -> StaffListRow(
                                                viewer = viewer,
                                                entry = data.entry,
                                                onClickListEdit = editViewModel::initialize,
                                            )
                                            is AnimeSearchEntry.Studio -> StudioListRow(
                                                viewer = viewer,
                                                entry = data.entry,
                                                onClickListEdit = editViewModel::initialize,
                                            )
                                            is AnimeSearchEntry.User -> UserListRow(
                                                viewer = viewer,
                                                entry = data.entry,
                                                onClickListEdit = editViewModel::initialize,
                                            )

                                            null -> when (selectedType) {
                                                AnimeSearchViewModel.SearchType.ANIME,
                                                AnimeSearchViewModel.SearchType.MANGA,
                                                    -> MediaViewOptionRow(
                                                    mediaViewOption = viewModel.mediaViewOption,
                                                    viewer = viewer,
                                                    onClickListEdit = editViewModel::initialize,
                                                    entry = null,
                                                )
                                                AnimeSearchViewModel.SearchType.CHARACTER ->
                                                    CharacterListRow(
                                                        viewer = viewer,
                                                        entry = null,
                                                        onClickListEdit = {},
                                                    )
                                                AnimeSearchViewModel.SearchType.STAFF ->
                                                    StaffListRow(
                                                        viewer = viewer,
                                                        entry = null,
                                                        onClickListEdit = {},
                                                    )
                                                AnimeSearchViewModel.SearchType.STUDIO ->
                                                    StudioListRow(
                                                        viewer = viewer,
                                                        entry = null,
                                                        onClickListEdit = {},
                                                    )
                                                AnimeSearchViewModel.SearchType.USER ->
                                                    UserListRow(
                                                        viewer = viewer,
                                                        entry = null,
                                                        onClickListEdit = {},
                                                    )
                                            }
                                        }
                                    }

                                    when (content.loadState.append) {
                                        is LoadState.Error -> item(
                                            key = "append_error",
                                            span = GridUtils.maxSpanFunction
                                        ) {
                                            Box(Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = stringResource(Res.string.anime_media_list_error_loading),
                                                    modifier = Modifier.align(Alignment.Center),
                                                )
                                            }
                                        }
                                        LoadState.Loading -> item(
                                            key = "append_loading",
                                            span = GridUtils.maxSpanFunction
                                        ) {
                                            Box(Modifier.fillMaxWidth()) {
                                                CircularProgressIndicator(
                                                    Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }
                                        is LoadState.NotLoading -> Unit
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

    @Composable
    private fun LockedFeatureTiers() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.anime_requires_unlock),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                val navigationCallback = LocalNavigationCallback.current
                Button(
                    onClick = {
                        navigationCallback.navigate(AnimeDestination.FeatureTiers)
                    }
                ) {
                    Text(
                        text = stringResource(
                            Res.string.anime_open_feature_tiers_button
                        )
                    )
                }
            }
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
                isNotEmpty && !WindowInsets.isImeVisibleKmp
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
                                        Res.string.anime_search_anime
                                    AnimeSearchViewModel.SearchType.MANGA ->
                                        Res.string.anime_search_manga
                                    AnimeSearchViewModel.SearchType.CHARACTER ->
                                        Res.string.anime_search_characters
                                    AnimeSearchViewModel.SearchType.STAFF ->
                                        Res.string.anime_search_staff
                                    AnimeSearchViewModel.SearchType.STUDIO ->
                                        Res.string.anime_search_studio
                                    AnimeSearchViewModel.SearchType.USER ->
                                        Res.string.anime_search_user
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
                                        Res.string.anime_search_clear
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
                                            Res.string.anime_media_view_option_icon_content_description
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
