package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetScaffoldState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import artistalleydatabase.modules.anime.search.generated.resources.Res
import artistalleydatabase.modules.anime.search.generated.resources.anime_search_clear
import artistalleydatabase.modules.utils_compose.generated.resources.error_loading
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)
object AnimeSearchScreen {

    @Composable
    operator fun invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        state: State,
        onRefresh: () -> Unit,
        content: LazyPagingItems<AnimeSearchEntry>,
        animeSortFilterState: SortFilterState<*>,
        mangaSortFilterState: SortFilterState<*>,
        characterSortFilterState: SortFilterState<*>,
        staffSortFilterState: SortFilterState<*>,
        studiosSortFilterState: SortFilterState<*>,
        usersSortFilterState: SortFilterState<*>,
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState?,
        lockedFeatureTiers: @Composable () -> Unit,
        item: @Composable (
            AnimeSearchEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        var selectedType by state.selectedType.collectAsMutableStateWithLifecycle()
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            val sortFilterState = when (selectedType) {
                SearchType.ANIME -> animeSortFilterState
                SearchType.MANGA -> mangaSortFilterState
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
                    )
                },
                sheetState = bottomSheetScaffoldState.bottomSheetState,
                scaffoldState = bottomSheetScaffoldState,
                bottomNavigationState = bottomNavigationState,
                modifier = Modifier.Companion
                    .padding(padding)
                    .conditionally(bottomNavigationState != null) {
                        nestedScroll(bottomNavigationState!!.nestedScrollConnection)
                    }
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val gridState = scrollStateSaver.lazyGridState()
                sortFilterState.ImmediateScrollResetEffect(gridState)
                Content(
                    state = state,
                    onRefresh = onRefresh,
                    content = content,
                    selectedType = selectedType,
                    gridState = gridState,
                    scaffoldPadding = scaffoldPadding,
                    bottomSheetScaffoldState = bottomSheetScaffoldState,
                    onClickListEdit = onClickListEdit,
                    lockedFeatureTiers = lockedFeatureTiers,
                    item = item,
                )
            }
        }
    }

    @Composable
    private fun Content(
        state: State,
        onRefresh: () -> Unit,
        content: LazyPagingItems<AnimeSearchEntry>,
        selectedType: SearchType,
        gridState: LazyGridState,
        scaffoldPadding: PaddingValues,
        bottomSheetScaffoldState: BottomSheetScaffoldState,
        onClickListEdit: (MediaNavigationData) -> Unit,
        lockedFeatureTiers: @Composable () -> Unit,
        item: @Composable (
            AnimeSearchEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
    ) {
        val refreshState = content.loadState.refresh
        val errorText = (refreshState as? LoadState.Error)
            ?.let { stringResource(UtilsRes.string.error_loading) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                bottomSheetScaffoldState.snackbarHostState.showSnackbar(errorText)
            }
        }

        val unlocked by state.unlocked.collectAsStateWithLifecycle()
        val selectedUnlocked = selectedType == SearchType.ANIME
                || selectedType == SearchType.MANGA
                || unlocked

        val layoutDirection = LocalLayoutDirection.current
        if (!selectedUnlocked) {
            lockedFeatureTiers()
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
                onRefresh = onRefresh,
                columns = columns,
                items = content,
                itemKey = { it.entryId.scopedId },
                itemContentType = { it.entryId.type },
                item = {
                    item(it, onClickListEdit)
                },
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp + scaffoldPadding.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.Companion.padding(
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
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            var query by state.query.collectAsMutableStateWithLifecycle()
            val isNotEmpty by remember { derivedStateOf { query.isNotEmpty() } }
            BackHandler(
                isNotEmpty && !WindowInsets.Companion.isImeVisibleKmp
                        // Need to manually check sheet state because top bar
                        // takes precedence over all other handlers
                        && sheetState.targetValue != SheetValue.Expanded
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
                    modifier = Modifier.Companion.background(MaterialTheme.colorScheme.surface)
                )

                val selectedTypeIndex = SearchType.entries.indexOf(selectedType)
                ScrollableTabRow(
                    selectedTabIndex = selectedTypeIndex,
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .align(Alignment.Companion.CenterHorizontally),
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

    @Stable
    class State(
        val unlocked: StateFlow<Boolean>,
        val selectedType: MutableStateFlow<SearchType>,
        val query: MutableStateFlow<String>,
        val mediaViewOption: MutableStateFlow<MediaViewOption>,
    )
}
