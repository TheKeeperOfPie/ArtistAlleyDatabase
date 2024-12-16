package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_seasonal_season_year
import artistalleydatabase.modules.anime.generated.resources.anime_seasonal_title
import artistalleydatabase.modules.anime.media.data.generated.resources.anime_media_view_option_icon_content_description
import com.anilist.data.type.MediaSeason
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.widthAdaptiveCells
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOptionRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.bottomBorder
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.media.data.generated.resources.Res as MediaDataRes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object SeasonalScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: SeasonalViewModel = viewModel {
            animeComponent.seasonalViewModel(createSavedStateHandle())
        },
        upIconOption: UpIconOption? = null,
    ) {
        val pagerState = rememberPagerState(
            initialPage = viewModel.initialPage,
            pageCount = { Int.MAX_VALUE },
        )

        val currentSeasonYear = remember { AniListUtils.getCurrentSeasonYear() }
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val scrollBehavior =
                TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
            val sortFilterController = viewModel.sortFilterController
            sortFilterController.PromptDialog()

            SortFilterBottomScaffold(
                sortFilterController = sortFilterController,
                topBar = {
                    TopBar(
                        upIconOption = upIconOption,
                        viewModel = viewModel,
                        initialPage = viewModel.initialPage,
                        pagerState = pagerState,
                        currentSeasonYear = currentSeasonYear,
                        scrollBehavior = scrollBehavior
                    )
                },
            ) { scaffoldPadding ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                ) {
                    val viewer by viewModel.viewer.collectAsState()
                    val columns = viewModel.mediaViewOption.widthAdaptiveCells
                    val gridState = rememberLazyGridState()
                    sortFilterController.ImmediateScrollResetEffect(gridState)
                    VerticalList(
                        gridState = gridState,
                        itemHeaderText = null,
                        onRefresh = viewModel::onRefresh,
                        items = viewModel.items(it),
                        itemKey = { it.media.id },
                        item = {
                            MediaViewOptionRow(
                                mediaViewOption = viewModel.mediaViewOption,
                                viewer = viewer,
                                onClickListEdit = editViewModel::initialize,
                                entry = it,
                            )
                        },
                        columns = columns,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 16.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(scaffoldPadding)
                    )
                }
            }
        }
    }

    @Composable
    private fun TopBar(
        upIconOption: UpIconOption?,
        viewModel: SeasonalViewModel,
        initialPage: Int,
        pagerState: PagerState,
        currentSeasonYear: Pair<MediaSeason, Int>,
        scrollBehavior: TopAppBarScrollBehavior,
    ) {
        EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.anime_seasonal_title)) },
                    navigationIcon = {
                        if (upIconOption != null) {
                            UpIconButton(option = upIconOption)
                        }
                    },
                    actions = {
                        val mediaViewOption = viewModel.mediaViewOption
                        val nextMediaViewOption = MediaViewOption.values()
                            .let { it[(it.indexOf(mediaViewOption) + 1) % it.size] }
                        IconButton(onClick = {
                            viewModel.mediaViewOption = nextMediaViewOption
                        }) {
                            Icon(
                                imageVector = nextMediaViewOption.icon,
                                contentDescription = stringResource(
                                    MediaDataRes.string.anime_media_view_option_icon_content_description
                                ),
                            )
                        }
                    }
                )

                val state = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)
                val coroutineScope = rememberCoroutineScope()

                val settledPage = pagerState.settledPage
                LaunchedEffect(settledPage) {
                    val lastVisibleItem = state.layoutInfo.visibleItemsInfo.lastOrNull()
                    val lastVisibleIndex = lastVisibleItem?.index ?: 0
                    if (settledPage !in (state.firstVisibleItemIndex..lastVisibleIndex)) {
                        state.animateScrollToItem(settledPage)
                    }
                }

                LazyRow(
                    state = state,
                    flingBehavior = rememberSnapFlingBehavior(state),
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    items(
                        Int.MAX_VALUE,
                        key = { it },
                        contentType = { "seasonYear" },
                    ) { index ->
                        val selected = index == pagerState.currentPage
                        Tab(
                            selected = selected,
                            text = {
                                val (season, year) = AniListUtils.calculateSeasonYearWithOffset(
                                    seasonYear = currentSeasonYear,
                                    offset = Int.MAX_VALUE / 2 - index,
                                )
                                Text(
                                    text = stringResource(
                                        Res.string.anime_seasonal_season_year,
                                        stringResource(season.toTextRes()),
                                        year,
                                    ),
                                    color = if (index == Int.MAX_VALUE / 2) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        Color.Unspecified
                                    },
                                    maxLines = 1,
                                )
                            },
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            modifier = Modifier.conditionally(selected) {
                                bottomBorder(
                                    color = MaterialTheme.colorScheme.primary,
                                    width = 3.dp,
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
