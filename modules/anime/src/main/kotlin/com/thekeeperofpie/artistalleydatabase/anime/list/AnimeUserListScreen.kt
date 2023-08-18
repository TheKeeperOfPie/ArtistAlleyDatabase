package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.VerticalScrollbar
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
object AnimeUserListScreen {

    private val SCREEN_KEY = AnimeNavDestinations.USER_LIST.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: AnimeUserListViewModel,
        mediaType: MediaType = MediaType.ANIME,
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.USER_LIST.id,
            viewModel = editViewModel,
            bottomNavigationState = bottomNavigationState,
            snackbarHostState = snackbarHostState,
        ) {
            val sortFilterController = viewModel.sortFilterController
            sortFilterController.PromptDialog()
            SortFilterBottomScaffoldNoAppBarOffset(
                sortFilterController = sortFilterController,
                topBar = { TopBar(viewModel, mediaType, upIconOption, scrollBehavior) },
                bottomNavigationState = bottomNavigationState,
            ) { scaffoldPadding ->
                val content = viewModel.content
                val error = content.error
                val errorText = error?.first?.let { stringResource(it) }
                LaunchedEffect(error) {
                    if (errorText != null) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorText,
                                withDismissAction = true,
                                duration = SnackbarDuration.Long
                            )
                            viewModel.content = viewModel.content.copy(error = null)
                        }
                    }
                }
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
                val expandedState = rememberSaveable(saver = listSaver(
                    save = { it.filterValues { !it }.keys.toList() },
                    restore = {
                        mutableStateMapOf<String, Boolean>().apply {
                            it.forEach { put(it, false) }
                        }
                    }
                )) { mutableStateMapOf<String, Boolean>() }

                AnimeMediaListScreen(
                    refreshing = content.loading,
                    onRefresh = viewModel::onRefresh,
                    pullRefreshTopPadding = { topBarPadding },
                    modifier = Modifier.nestedScroll(
                        NestedScrollSplitter(
                            bottomNavigationState?.nestedScrollConnection,
                            scrollBehavior.nestedScrollConnection,
                            consumeNone = bottomNavigationState == null,
                        )
                    )
                ) {
                    val hasItems = (content.result?.sumOf { it.entries.size } ?: 0) != 0
                    when {
                        !content.loading && !content.success && !hasItems ->
                            AnimeMediaListScreen.Error(
                                errorTextRes = error?.first,
                                exception = error?.second,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = topBarPadding),
                            )
                        else -> {
                            if (!content.loading && !hasItems) {
                                AnimeMediaListScreen.NoResults(Modifier.padding(top = topBarPadding))
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
                                    LazyVerticalGrid(
                                        columns = columns,
                                        state = gridState,
                                        contentPadding = PaddingValues(
                                            top = 16.dp + (scrollBehavior.state.heightOffsetLimit
                                                .takeUnless { it == -Float.MAX_VALUE }
                                                ?.let { LocalDensity.current.run { -it.toDp() } }
                                                ?: 0.dp),
                                            start = 16.dp,
                                            end = 16.dp,
                                            bottom = 88.dp + scaffoldPadding.calculateBottomPadding()
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        content.result?.forEach {
                                            if (it.entries.isNotEmpty()) {
                                                val expanded = expandedState[it.name] ?: true
                                                if (viewModel.mediaListStatus == null) {
                                                    item(
                                                        "header-${it.name}",
                                                        span = { GridItemSpan(maxLineSpan) },
                                                    ) {
                                                        Header(
                                                            name = it.name,
                                                            expanded = expanded,
                                                            onClick = {
                                                                expandedState[it.name] = !expanded
                                                            },
                                                            modifier = Modifier.animateItemPlacement(),
                                                        )
                                                    }
                                                }

                                                if (expanded) {
                                                    items(
                                                        items = it.entries,
                                                        key = { "media_${it.media.id}" },
                                                        contentType = { "media" },
                                                    ) {
                                                        MediaRow(
                                                            entry = it,
                                                            viewer = viewer,
                                                            viewModel = viewModel,
                                                            editViewModel = editViewModel,
                                                            modifier = Modifier
                                                                .animateItemPlacement(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    VerticalScrollbar(
                                        state = gridState,
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .fillMaxHeight()
                                            .padding(
                                                bottom = 56.dp + (bottomNavigationState?.bottomOffsetPadding()
                                                    ?: 0.dp)
                                            )
                                    )
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
        viewModel: AnimeUserListViewModel,
        mediaType: MediaType,
        upIconOption: UpIconOption?,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
            val isNotEmpty by remember { derivedStateOf { viewModel.query.isNotEmpty() } }
            BackHandler(isNotEmpty && !WindowInsets.isImeVisible) {
                viewModel.query = ""
            }
            StaticSearchBar(
                query = viewModel.query,
                onQueryChange = { viewModel.query = it },
                leadingIcon = if (upIconOption != null) {
                    { UpIconButton(upIconOption) }
                } else null,
                placeholder = {
                    val userName = viewModel.userName
                    val mediaListStatus = viewModel.mediaListStatus
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
            )
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
        editViewModel: MediaEditViewModel,
        entry: MediaPreviewWithDescriptionEntry,
        modifier: Modifier = Modifier,
    ) {
        when (viewModel.mediaViewOption) {
            MediaViewOption.SMALL_CARD -> AnimeMediaListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onClickListEdit = { editViewModel.initialize(it.media) },
                onLongClick = { viewModel.ignoreList.toggle(entry.media.id.toString()) },
                modifier = modifier,
            )
            MediaViewOption.LARGE_CARD -> AnimeMediaLargeCard(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onLongClick = { viewModel.ignoreList.toggle(entry.media.id.toString()) },
                onClickListEdit = { editViewModel.initialize(it.media) },
                modifier = modifier,
            )
            MediaViewOption.COMPACT -> AnimeMediaCompactListRow(
                screenKey = SCREEN_KEY,
                viewer = viewer,
                entry = entry,
                onLongClick = { viewModel.ignoreList.toggle(entry.media.id.toString()) },
                onClickListEdit = { editViewModel.initialize(it.media) },
                modifier = modifier,
            )
            MediaViewOption.GRID -> MediaGridCard(
                screenKey = SCREEN_KEY,
                entry = entry,
                viewer = viewer,
                onClickListEdit = { editViewModel.initialize(it.media) },
                onLongClick = { viewModel.ignoreList.toggle(entry.media.id.toString()) },
                modifier = modifier,
            )
        }
    }
}
