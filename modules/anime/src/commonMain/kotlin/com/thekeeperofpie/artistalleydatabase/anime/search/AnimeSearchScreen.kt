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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffoldState
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
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_open_feature_tiers_button
import artistalleydatabase.modules.anime.generated.resources.anime_requires_unlock
import artistalleydatabase.modules.anime.generated.resources.anime_search_clear
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.characters.horizontalCharactersRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.characterMediaItems
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.horizontalMediaCardRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        state: State,
        viewModel: AnimeSearchViewModel<MediaPreviewWithDescriptionEntry>,
        animeSortFilterViewModel: MediaSortFilterViewModel<*>,
        mangaSortFilterViewModel: MediaSortFilterViewModel<*>,
        characterSortFilterState: SortFilterState<*>,
        staffSortFilterState: SortFilterState<*>,
        studiosSortFilterState: SortFilterState<*>,
        usersSortFilterState: SortFilterState<*>,
        upIconOption: UpIconOption? = null,
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)

        var selectedType by state.selectedType.collectAsMutableStateWithLifecycle()
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
            bottomNavigationState = bottomNavigationState,
        ) {
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            val sortFilterState = when (selectedType) {
                SearchType.ANIME -> animeSortFilterViewModel.state
                SearchType.MANGA -> mangaSortFilterViewModel.state
                SearchType.CHARACTER -> characterSortFilterState
                SearchType.STAFF -> staffSortFilterState
                SearchType.STUDIO -> studiosSortFilterState
                SearchType.USER -> usersSortFilterState
            }
            SortFilterBottomScaffold(
                state = sortFilterState,
                topBar = {
                    TopBar(
                        state = state,
                        upIconOption = upIconOption,
                        scrollBehavior = scrollBehavior,
                        sheetState = bottomSheetScaffoldState.bottomSheetState,
                        mediaEditState = editViewModel.state,
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
                val gridState = scrollStateSaver.lazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                Content(
                    state = state,
                    viewModel = viewModel,
                    selectedType = selectedType,
                    gridState = gridState,
                    scaffoldPadding = scaffoldPadding,
                    bottomSheetScaffoldState = bottomSheetScaffoldState,
                    onClickListEdit = editViewModel::initialize,
                )
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
                val navigationController = LocalNavigationController.current
                Button(
                    onClick = {
                        navigationController.navigate(AnimeDestination.FeatureTiers)
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
    private fun Content(
        state: State,
        viewModel: AnimeSearchViewModel<MediaPreviewWithDescriptionEntry>,
        selectedType: SearchType,
        gridState: LazyGridState,
        scaffoldPadding: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val content = viewModel.content.collectAsLazyPagingItems()
        val refreshState = content.loadState.refresh
        val errorText = (refreshState as? LoadState.Error)
            ?.let { stringResource(Res.string.anime_media_list_error_loading) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                bottomSheetScaffoldState.snackbarHostState.showSnackbar(errorText)
            }
        }

        val unlocked by state.unlocked.collectAsStateWithLifecycle()
        val selectedUnlocked = selectedType == SearchType.ANIME
                || selectedType == SearchType.MANGA
                || unlocked

        val viewer by viewModel.viewer.collectAsState()
        val layoutDirection = LocalLayoutDirection.current
        if (!selectedUnlocked) {
            LockedFeatureTiers()
        } else {
            val mediaViewOption by state.mediaViewOption.collectAsStateWithLifecycle()
            val columns =
                if (selectedType == SearchType.ANIME || selectedType == SearchType.MANGA) {
                    mediaViewOption.widthAdaptiveCells
                } else {
                    GridCells.Adaptive(300.dp)
                }
            VerticalList(
                gridState = gridState,
                itemHeaderText = null,
                onRefresh = viewModel::onRefresh,
                columns = columns,
                items = content,
                itemKey = { it.entryId.scopedId },
                itemContentType = { it.entryId.type },
                item = {
                    EntryItem(
                        viewer = viewer,
                        mediaViewOption = { mediaViewOption },
                        selectedType = selectedType,
                        entry = it,
                        onClickListEdit = onClickListEdit,
                    )
                },
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + scaffoldPadding.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(
                    start = scaffoldPadding.calculateStartPadding(layoutDirection),
                    end = scaffoldPadding.calculateEndPadding(layoutDirection),
                    top = scaffoldPadding.calculateTopPadding(),
                )
            )
        }
    }

    @Composable
    private fun TopBar(
        state: State,
        upIconOption: UpIconOption? = null,
        scrollBehavior: TopAppBarScrollBehavior,
        sheetState: SheetState,
        mediaEditState: MediaEditState,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            var query by state.query.collectAsMutableStateWithLifecycle()
            val isNotEmpty by remember { derivedStateOf { query.isNotEmpty() } }
            BackHandler(
                isNotEmpty && !WindowInsets.isImeVisibleKmp
                        // Need to manually check sheet state because top bar
                        // takes precedence over all other handlers
                        && sheetState.targetValue != SheetValue.Expanded
                        && !mediaEditState.showing
            ) {
                query = ""
            }
            Column {
                var selectedType by state.selectedType.collectAsMutableStateWithLifecycle()
                StaticSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    leadingIcon = if (upIconOption != null) {
                        { UpIconButton(upIconOption) }
                    } else null,
                    placeholder = { Text(stringResource(selectedType.searchLabel)) },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = stringResource(
                                        Res.string.anime_search_clear
                                    ),
                                )
                            }

                            if (selectedType == SearchType.ANIME
                                || selectedType == SearchType.MANGA
                            ) {
                                var mediaViewOption by state.mediaViewOption
                                    .collectAsMutableStateWithLifecycle()
                                val nextMediaViewOption = MediaViewOption.entries
                                    .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                                IconButton(onClick = {
                                    mediaViewOption = nextMediaViewOption
                                }) {
                                    Icon(
                                        imageVector = nextMediaViewOption.icon,
                                        contentDescription = stringResource(
                                            MediaDataRes.string.anime_media_view_option_icon_content_description
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )

                val selectedTypeIndex = SearchType.entries.indexOf(selectedType)
                ScrollableTabRow(
                    selectedTabIndex = selectedTypeIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    divider = { /* No divider, manually draw so that it's full width */ }
                ) {
                    SearchType.entries
                        .forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTypeIndex == index,
                                onClick = { selectedType = tab },
                                text = {
                                    Text(
                                        text = stringResource(tab.tabText),
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

    @Composable
    private fun EntryItem(
        viewer: AniListViewer?,
        mediaViewOption: () -> MediaViewOption,
        selectedType: SearchType,
        entry: AnimeSearchEntry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        when (entry) {
            is AnimeSearchEntry.Media<*> -> MediaViewOptionRow(
                mediaViewOption = mediaViewOption(),
                viewer = viewer,
                onClickListEdit = onClickListEdit,
                entry = entry.entry as? MediaPreviewWithDescriptionEntry,
            )
            is AnimeSearchEntry.Character -> CharacterListRow(
                entry = entry.entry,
                staffDetailsRoute =
                    StaffDestinations.StaffDetails.route,
                mediaItems = {
                    characterMediaItems(
                        characterId = entry.entry.character.id.toString(),
                        media = it,
                        viewer = { viewer },
                        onClickListEdit = onClickListEdit,
                    )
                },
            )
            is AnimeSearchEntry.Staff ->
                SharedTransitionKeyScope("staff_list_row", entry.entry.staff.id.toString()) {
                    StaffListRow(
                        entry = entry.entry,
                        charactersSection = { horizontalCharactersRow(it) },
                        mediaSection = { media ->
                            horizontalMediaCardRow(
                                viewer = { viewer },
                                media = media,
                                onClickListEdit = onClickListEdit,
                            )
                        },
                    )
                }
            is AnimeSearchEntry.Studio ->
                SharedTransitionKeyScope("studio_list_row", entry.entry.studio.id.toString()) {
                    StudioListRow(
                        entry = entry.entry,
                        mediaRow = { media ->
                            horizontalMediaCardRow(
                                viewer = { viewer },
                                media = media,
                                onClickListEdit = onClickListEdit,
                                mediaWidth = 120.dp,
                                mediaHeight = 180.dp,
                            )
                        },
                    )
                }
            is AnimeSearchEntry.User -> UserListRow(
                entry = entry.entry,
                mediaRow = { media ->
                    horizontalMediaCardRow(
                        viewer = { viewer },
                        media = media,
                        onClickListEdit = onClickListEdit,
                        forceListEditIcon = true,
                    )
                }
            )

            null -> when (selectedType) {
                SearchType.ANIME,
                SearchType.MANGA,
                    -> MediaViewOptionRow(
                    mediaViewOption = mediaViewOption(),
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    entry = null,
                )
                SearchType.CHARACTER ->
                    CharacterListRow<Unit>(
                        entry = null,
                        staffDetailsRoute =
                            StaffDestinations.StaffDetails.route,
                        mediaItems = {},
                    )
                SearchType.STAFF ->
                    StaffListRow<Unit>(
                        entry = null,
                        charactersSection = {},
                        mediaSection = {},
                    )
                SearchType.STUDIO ->
                    StudioListRow<Unit>(
                        entry = null,
                        mediaRow = {},
                        mediaHeight = 180.dp,
                    )
                SearchType.USER ->
                    UserListRow(
                        entry = null,
                        mediaRow = { media ->
                            horizontalMediaCardRow(
                                viewer = { viewer },
                                media = media,
                                onClickListEdit = onClickListEdit,
                                forceListEditIcon = true,
                            )
                        }
                    )
            }
        }
    }

    @Stable
    class State(
        val unlocked: StateFlow<Boolean>,
        val selectedType: MutableStateFlow<SearchType>,
        val query: MutableStateFlow<String>,
        val mediaViewOption: MutableStateFlow<MediaViewOption>,
    )
}
