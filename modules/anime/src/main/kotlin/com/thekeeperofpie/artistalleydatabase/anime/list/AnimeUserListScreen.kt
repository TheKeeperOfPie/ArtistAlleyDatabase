package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.fragment.MediaNavigationData
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusText
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.items
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.VerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.compose.animateItemPlacementFixed
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.rememberCallback
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class
)
object AnimeUserListScreen {

    private val SCREEN_KEY = AnimeNavDestinations.USER_LIST.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: AnimeUserListViewModel,
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val onClickListEdit = rememberCallback(editViewModel::initialize)
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val editSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = editViewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.USER_LIST.id,
            sheetState = editSheetState,
            viewModel = editViewModel,
            bottomNavigationState = bottomNavigationState,
            snackbarHostState = snackbarHostState,
        ) {
            val entry = viewModel.entry
            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { (entry.result?.lists?.size ?: 0) + 1 },
            )
            val sortFilterController = viewModel.sortFilterController
            sortFilterController.PromptDialog()
            val sortSheetState = rememberStandardBottomSheetState(
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
                        sortSheetState,
                        pagerState,
                    )
                },
                bottomNavigationState = bottomNavigationState,
                modifier = Modifier
                    .conditionally(bottomNavigationState != null) {
                        nestedScroll(bottomNavigationState!!.nestedScrollConnection)
                    }
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) { scaffoldPadding ->
                val error = entry.error
                val errorText = error?.first?.let { stringResource(it) }
                LaunchedEffect(error) {
                    if (errorText != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorText,
                                withDismissAction = true,
                                duration = SnackbarDuration.Long
                            )
                            viewModel.entry = viewModel.entry.copy(error = null)
                        }
                    }
                }

                val loading = entry.loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = loading,
                    onRefresh = viewModel::onRefresh,
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .padding(scaffoldPadding)
                            .pullRefresh(pullRefreshState)
                            .fillMaxSize()
                    ) { page ->
                        val listEntry = if (page == 0) null else entry.result?.lists?.get(page - 1)
                        val scoreFormat = listEntry?.scoreFormat
                        val mediaEntries = if (page == 0) {
                            entry.result?.all
                        } else {
                            listEntry?.entries
                        }
                        val hasItems = mediaEntries == null || mediaEntries.isNotEmpty()
                        when {
                            !entry.loading && !entry.success && !hasItems ->
                                AnimeMediaListScreen.Error(
                                    errorTextRes = error?.first,
                                    exception = error?.second,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            else -> {
                                if (!entry.loading && !hasItems) {
                                    AnimeMediaListScreen.NoResults()
                                } else {
                                    val columns = when (viewModel.mediaViewOption) {
                                        MediaViewOption.SMALL_CARD,
                                        MediaViewOption.LARGE_CARD,
                                        MediaViewOption.COMPACT,
                                        -> GridCells.Adaptive(300.dp)
                                        MediaViewOption.GRID -> GridCells.Adaptive(120.dp)
                                    }
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        val gridState = scrollStateSaver.lazyGridState()
                                        val viewer by viewModel.viewer.collectAsState()
                                        LazyVerticalGrid(
                                            columns = columns,
                                            state = gridState,
                                            contentPadding = PaddingValues(
                                                top = 16.dp,
                                                start = 16.dp,
                                                end = 16.dp,
                                                bottom = 88.dp,
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            items(
                                                data = mediaEntries,
                                                placeholderCount = 10,
                                                key = { "media_${it.entry.media.id}" },
                                                contentType = { "media" },
                                            ) {
                                                MediaRow(
                                                    entry = it,
                                                    viewer = viewer,
                                                    viewModel = viewModel,
                                                    onClickListEdit = onClickListEdit,
                                                    scoreFormat = scoreFormat
                                                        ?: viewer?.scoreFormat,
                                                    modifier = Modifier
                                                        .animateItemPlacementFixed(),
                                                )
                                            }
                                        }

                                        VerticalScrollbar(
                                            state = gridState,
                                            modifier = Modifier
                                                .align(Alignment.CenterEnd)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    PullRefreshIndicator(
                        refreshing = loading,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        viewModel: AnimeUserListViewModel,
        upIconOption: UpIconOption? = null,
        scrollBehavior: TopAppBarScrollBehavior,
        sheetStateOne: SheetState,
        sheetStateTwo: SheetState,
        pagerState: PagerState,
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
                        val userName = viewModel.userName
                        val mediaListStatus = viewModel.mediaListStatus
                        val mediaType = viewModel.mediaType
                        Text(
                            if (mediaListStatus != null) {
                                stringResource(mediaListStatus.toTextRes(viewModel.mediaType))
                            } else if (userName != null) {
                                stringResource(
                                    when (mediaType) {
                                        MediaType.ANIME,
                                        MediaType.UNKNOWN__,
                                        -> R.string.anime_user_list_user_name_anime_search
                                        MediaType.MANGA -> R.string.anime_user_list_user_name_manga_search
                                    },
                                    userName
                                )
                            } else {
                                stringResource(
                                    when (mediaType) {
                                        MediaType.ANIME,
                                        MediaType.UNKNOWN__,
                                        -> R.string.anime_user_list_anime_search
                                        MediaType.MANGA -> R.string.anime_user_list_manga_search
                                    }
                                )
                            }
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
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )

                if (viewModel.mediaListStatus == null) {
                    val entry = viewModel.entry
                    val lists = entry.result?.lists.orEmpty()
                    val selectedIndex = pagerState.currentPage
                    val scope = rememberCoroutineScope()
                    ScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        divider = { /* No divider, manually draw so that it's full width */ }
                    ) {
                        Tab(
                            selected = selectedIndex == 0,
                            onClick = { scope.launch { pagerState.scrollToPage(0) } },
                            text = { Text(stringResource(R.string.anime_user_list_tab_all)) }
                        )
                        val locale = LocalConfiguration.current.locales[0]
                        lists.forEachIndexed { index, list ->
                            Tab(
                                selected = selectedIndex == (index + 1),
                                onClick = { scope.launch { pagerState.scrollToPage(index + 1) } },
                                text = { Text(list.name.uppercase(locale)) }
                            )
                        }
                    }

                    HorizontalDivider()
                }
            }
        }
    }

    @Composable
    private fun Header(
        name: String,
        expanded: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            )

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(
                    R.string.anime_user_list_header_expand_content_description
                ),
                onClick = onClick,
            )
        }
    }

    @Composable
    private fun MediaRow(
        viewer: AniListViewer?,
        viewModel: AnimeUserListViewModel,
        scoreFormat: ScoreFormat?,
        entry: AnimeUserListViewModel.MediaEntry?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val authorData = entry?.authorData
        val statusText = if (entry == null) {
            "Placeholder status text"
        } else {
            authorData?.let {
                authorData.status.toStatusText(
                    mediaType = viewModel.mediaType,
                    progress = authorData.progress ?: 0,
                    progressMax = entry?.entry?.media?.let(MediaUtils::maxProgress),
                    score = authorData.rawScore,
                    scoreFormat = scoreFormat,
                )
            }
        }

        when (viewModel.mediaViewOption) {
            MediaViewOption.SMALL_CARD -> {
                AnimeMediaListRow(
                    screenKey = SCREEN_KEY,
                    viewer = viewer,
                    entry = entry?.entry,
                    onClickListEdit = onClickListEdit,
                    label = if (statusText == null) null else {
                        {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.surfaceTint,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(
                                        start = 12.dp,
                                        top = 8.dp,
                                        end = 16.dp,
                                    )
                                    .placeholder(
                                        visible = entry == null,
                                        highlight = PlaceholderHighlight.shimmer(),
                                    )
                            )
                        }
                    },
                    modifier = modifier,
                )
            }
            MediaViewOption.LARGE_CARD -> AnimeMediaLargeCard(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry?.entry,
                label = if (statusText == null) null else {
                    {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(
                                    start = 12.dp,
                                    top = 8.dp,
                                    end = 16.dp,
                                )
                                .placeholder(
                                    visible = entry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                },
                modifier = modifier,
            )
            MediaViewOption.COMPACT -> AnimeMediaCompactListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry?.entry,
                onClickListEdit = onClickListEdit,
                label = if (statusText == null) null else {
                    {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(
                                    start = 8.dp,
                                    top = 8.dp,
                                    end = 16.dp,
                                )
                                .placeholder(
                                    visible = entry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                },
                modifier = modifier,
            )
            MediaViewOption.GRID -> MediaGridCard(
                screenKey = SCREEN_KEY,
                entry = entry?.entry,
                viewer = viewer,
                onClickListEdit = onClickListEdit,
                modifier = modifier,
            )
        }
    }
}
