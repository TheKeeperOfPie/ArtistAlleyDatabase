package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.SortFilterBottomScaffoldNoAppBarOffset
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
object AnimeUserListScreen {

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption? = null,
        viewModel: AnimeUserListViewModel,
        mediaType: MediaType = MediaType.ANIME,
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
        scrollStateSaver: ScrollStateSaver = ScrollStateSaver.STUB,
        bottomNavigationState: BottomNavigationState? = null,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = AnimeNavDestinations.USER_LIST.id,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            bottomNavigationState = bottomNavigationState,
        ) {
            val sortFilterController = viewModel.sortFilterController
            if (sortFilterController.airingDateShown != null) {
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
                topBar = { TopBar(viewModel, mediaType, upIconOption, scrollBehavior) },
                bottomNavigationState = bottomNavigationState,
            ) { scaffoldPadding ->
                val content = viewModel.content
                val refreshing = when (content) {
                    ContentState.LoadingEmpty -> true
                    is ContentState.Success -> content.loading
                    is ContentState.Error -> false
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
                    refreshing = refreshing,
                    onRefresh = viewModel::onRefresh,
                    tagShown = viewModel::tagShown,
                    onTagDismiss = { viewModel.tagShown = null },
                    pullRefreshTopPadding = { topBarPadding },
                    modifier = Modifier.nestedScroll(
                        NestedScrollSplitter(
                            bottomNavigationState?.nestedScrollConnection,
                            scrollBehavior.nestedScrollConnection,
                            consumeNone = bottomNavigationState == null,
                        )
                    )
                ) { onLongPressImage ->
                    when (content) {
                        is ContentState.Error -> AnimeMediaListScreen.Error(
                            errorTextRes = content.errorRes,
                            exception = content.exception,
                            modifier = Modifier.padding(top = topBarPadding),
                        )
                        ContentState.LoadingEmpty ->
                            // Empty column to allow pull refresh to work
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                            ) {
                            }
                        is ContentState.Success -> {
                            if (content.lists.sumOf { it.entries.size } == 0) {
                                AnimeMediaListScreen.NoResults(Modifier.padding(top = topBarPadding))
                            } else {
                                LazyColumn(
                                    state = scrollStateSaver.lazyListState(),
                                    contentPadding = PaddingValues(
                                        top = 16.dp + (scrollBehavior.state.heightOffsetLimit
                                            .takeUnless { it == -Float.MAX_VALUE }
                                            ?.let { LocalDensity.current.run { -it.toDp() } }
                                            ?: 0.dp),
                                        bottom = scaffoldPadding.calculateBottomPadding()
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    content.lists.forEach {
                                        if (it.entries.isNotEmpty()) {
                                            val expanded = expandedState[it.name] ?: true
                                            item("header-${it.name}") {
                                                Header(
                                                    name = it.name,
                                                    expanded = expanded,
                                                    onClick = {
                                                        expandedState[it.name] = !expanded
                                                    },
                                                    modifier = Modifier.animateItemPlacement(),
                                                )
                                            }

                                            if (expanded) {
                                                items(
                                                    items = it.entries,
                                                    key = { "media_${it.media.id}" },
                                                    contentType = { "media" },
                                                ) {
                                                    AnimeMediaListRow(
                                                        screenKey = AnimeNavDestinations.USER_LIST.id,
                                                        entry = it,
                                                        viewer = viewer,
                                                        onClickListEdit = {
                                                            editViewModel.initialize(it.media)
                                                        },
                                                        onLongClick = viewModel::onMediaLongClick,
                                                        onTagLongClick = viewModel::onTagLongClick,
                                                        onLongPressImage = onLongPressImage,
                                                        colorCalculationState = colorCalculationState,
                                                        navigationCallback = navigationCallback,
                                                        modifier = Modifier.animateItemPlacement()
                                                            .padding(horizontal = 16.dp),
                                                    )
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
                    Text(
                        if (userName != null) {
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
                    IconButton(onClick = { viewModel.query = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = stringResource(
                                R.string.anime_search_clear
                            ),
                        )
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

    sealed interface ContentState {
        data object LoadingEmpty : ContentState

        data class Success(
            val lists: List<ListEntry>,
            val loading: Boolean = false,
        ) : ContentState {
            data class ListEntry(
                val name: String,
                val entries: List<AnimeMediaListRow.Entry<*>>,
            )
        }

        data class Error(
            @StringRes val errorRes: Int? = null,
            val exception: Throwable? = null,
        ) : ContentState
    }
}
