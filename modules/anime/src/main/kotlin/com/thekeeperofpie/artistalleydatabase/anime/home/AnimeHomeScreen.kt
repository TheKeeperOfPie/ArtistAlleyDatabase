package com.thekeeperofpie.artistalleydatabase.anime.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.fragment.MediaPreviewWithDescription
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.NavMenuIconButton
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
object AnimeHomeScreen {

    private const val SCREEN_KEY = "anime_home"
    private val MEDIA_ROW_HEIGHT = 180.dp

    @Composable
    operator fun invoke(
        viewModel: AnimeHomeViewModel = hiltViewModel<AnimeHomeViewModel>(),
        onClickNav: () -> Unit = {},
        navigationCallback: AnimeNavigator.NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState,
    ) {
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val animeViewModel = hiltViewModel<AnimeHomeAnimeViewModel>()
        val mangaViewModel = hiltViewModel<AnimeHomeMangaViewModel>()
        val selectedItemTracker = remember { SelectedItemTracker() }
        LazyColumn(
            state = scrollStateSaver.lazyListState(),
            contentPadding = PaddingValues(
                bottom = 16.dp + bottomNavigationState.bottomNavBarPadding()
            ),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(bottomNavigationState.nestedScrollConnection)
        ) {
            item {
                Header(onClickNav = onClickNav, navigationCallback = navigationCallback)
            }

            item {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                text = stringResource(R.string.anime_home_tab_anime),
                                maxLines = 1,
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                text = stringResource(R.string.anime_home_tab_manga),
                                maxLines = 1,
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    list(
                        entry = animeViewModel.entry,
                        data = {
                            listOf(
                                Triple(
                                    "anime_trending",
                                    R.string.anime_home_trending,
                                    it.trending
                                ),
                                Triple(
                                    "anime_popular_this_season",
                                    R.string.anime_home_popular_this_season,
                                    it.popularThisSeason
                                ),
                                Triple(
                                    "anime_popular_last_season",
                                    R.string.anime_home_popular_last_season,
                                    it.popularLastSeason
                                ),
                                Triple(
                                    "anime_popular_next_season",
                                    R.string.anime_home_popular_next_season,
                                    it.popularNextSeason
                                ),
                                Triple(
                                    "anime_popular",
                                    R.string.anime_home_popular,
                                    it.popular
                                ),
                                Triple("anime_top", R.string.anime_home_top, it.top),
                            )
                        },
                        selectedItemTracker = selectedItemTracker,
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                    )
                }
                1 -> {
                    list(
                        entry = mangaViewModel.entry,
                        data = {
                            listOf(
                                Triple(
                                    "manga_trending",
                                    R.string.anime_home_trending,
                                    it.trending
                                ),
                                Triple(
                                    "manga_popular",
                                    R.string.anime_home_popular,
                                    it.popular
                                ),
                                Triple("manga_top", R.string.anime_home_top, it.top),
                            )
                        },
                        selectedItemTracker = selectedItemTracker,
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    @Composable
    private fun Header(
        onClickNav: () -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback
    ) {
        TopAppBar(
            title = {},
            navigationIcon = { NavMenuIconButton(onClickNav) },
            actions = {
                IconButton(onClick = navigationCallback::onAiringScheduleClick) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = stringResource(
                            R.string.anime_airing_schedule_icon_content_description
                        ),
                    )
                }
            }
        )
    }

    private fun <Entry> LazyListScope.list(
        entry: Entry?,
        data: (Entry) -> List<Triple<String, Int, List<MediaPreviewWithDescription>>>,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        if (entry == null) {
            item {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp)
                    )
                }
            }
            return
        }

        data(entry).forEach { (key, titleRes, entries) ->
            mediaRow(
                key = key,
                titleRes = titleRes,
                entries = entries,
                selectedItemTracker = selectedItemTracker,
                navigationCallback = navigationCallback,
                colorCalculationState = colorCalculationState,
            )
        }
    }

    private fun LazyListScope.mediaRow(
        key: String,
        @StringRes titleRes: Int,
        entries: List<MediaPreviewWithDescription>,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        if (entries.isEmpty()) return
        item {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium.copy(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
            )
        }

        item {
            val pagerState = rememberPagerState(pageCount = { entries.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                pageSpacing = 16.dp,
            ) {
                AnimeMediaLargeCard(
                    screenKey = SCREEN_KEY,
                    // TODO: Move entry wrapping elsewhere and abstract
                    entry = AnimeMediaLargeCard.Entry(entries[it]),
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }

            selectedItemTracker.attachPager(key = key, pagerState = pagerState)
        }

        item {
            val listState = rememberLazyListState()
            val snapLayoutInfoProvider =
                remember(listState) { SnapLayoutInfoProvider(listState) { _, _, _ -> 0 } }
            val colorPrimary = MaterialTheme.colorScheme.primary
            val cardOutlineBorder = remember { BorderStroke(1.5.dp, colorPrimary) }

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = LocalConfiguration.current.screenWidthDp.dp - 80.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider)
            ) {
                itemsIndexed(entries, { _, item -> item.id }) { index, item ->
                    val id = item.id.toString()
                    val colors = colorCalculationState.colorMap[id]
                    val animationProgress by animateIntAsState(
                        if (colors == null) 0 else 255,
                        label = "Media card color fade in",
                    )

                    val surfaceColor = item.coverImage?.color?.let(ComposeColorUtils::hexToColor)
                        ?: MaterialTheme.colorScheme.surface
                    val containerColor = when {
                        colors == null || animationProgress == 0 -> surfaceColor
                        animationProgress == 255 -> colors.first
                        else -> Color(
                            ColorUtils.compositeColors(
                                ColorUtils.setAlphaComponent(
                                    colors.first.toArgb(),
                                    animationProgress
                                ),
                                surfaceColor.toArgb()
                            )
                        )
                    }

                    var widthToHeightRatio by remember(id) { mutableStateOf<Float?>(null) }
                    val onClick = {
                        navigationCallback.onMediaClick(item, widthToHeightRatio)
                    }

                    val card: @Composable (@Composable ColumnScope.() -> Unit) -> Unit =
                        if (selectedItemTracker.keyToPosition[key]?.second == index) {
                            {
                                OutlinedCard(
                                    onClick = onClick,
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = containerColor,
                                    ),
                                    border = cardOutlineBorder,
                                    content = it,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        } else {
                            {
                                ElevatedCard(
                                    onClick = onClick,
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = containerColor,
                                    ),
                                    content = it,
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        }

                    SharedElement(
                        key = "anime_media_${item.id}_image",
                        screenKey = SCREEN_KEY,
                    ) {
                        card {
                            val alpha by animateFloatAsState(
                                if (widthToHeightRatio == null) 0f else 1f,
                                label = "Cover image alpha",
                            )
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.coverImage?.extraLarge)
                                    .crossfade(false)
                                    .allowHardware(colorCalculationState.hasColor(id))
                                    .size(
                                        width = coil.size.Dimension.Undefined,
                                        height = coil.size.Dimension.Pixels(
                                            LocalDensity.current.run { MEDIA_ROW_HEIGHT.roundToPx() }
                                        ),
                                    )
                                    .build(),
                                contentScale = ContentScale.FillHeight,
                                contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                                onSuccess = {
                                    widthToHeightRatio = it.widthToHeightRatio()
                                    ComposeColorUtils.calculatePalette(
                                        id = id,
                                        success = it,
                                        colorCalculationState = colorCalculationState,
                                        heightStartThreshold = 3 / 4f,
                                        selectMaxPopulation = true,
                                    )
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .alpha(alpha)
                                    .height(MEDIA_ROW_HEIGHT)
                                    .conditionally(widthToHeightRatio == null) {
                                        widthIn(
                                            min = MEDIA_ROW_HEIGHT *
                                                    AniListUtils.COVER_IMAGE_WIDTH_TO_HEIGHT_RATIO
                                        )
                                    }
                                    .conditionally(widthToHeightRatio != null) {
                                        widthIn(max = MEDIA_ROW_HEIGHT)
                                    }
                                    .animateContentSize()
                            )
                        }
                    }
                }
            }

            selectedItemTracker.attachLazyList(key = key, listState = listState)
        }
    }

    @SuppressLint("ComposableNaming")
    class SelectedItemTracker {
        val keyToPosition = mutableStateMapOf<String, Pair<Boolean, Int>>()

        @Composable
        fun attachPager(key: String, pagerState: PagerState) {
            val settledPage = pagerState.settledPage
            LaunchedEffect(settledPage) {
                val sourceAndPosition = keyToPosition[key]
                if (sourceAndPosition == null || sourceAndPosition.second != settledPage) {
                    keyToPosition[key] = true to settledPage
                }
            }

            val sourceAndPosition = keyToPosition[key]
            LaunchedEffect(sourceAndPosition) {
                if (sourceAndPosition != null) {
                    if (!sourceAndPosition.first) {
                        pagerState.animateScrollToPage(sourceAndPosition.second)
                    }
                }
            }
        }

        @Composable
        fun attachLazyList(key: String, listState: LazyListState) {
            val firstItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
            val isScrollInProgress = listState.isScrollInProgress
            LaunchedEffect(isScrollInProgress, firstItemIndex) {
                if (!isScrollInProgress) {
                    val sourceAndPosition = keyToPosition[key]
                    if (sourceAndPosition == null || sourceAndPosition.second != firstItemIndex) {
                        keyToPosition[key] = false to firstItemIndex
                    }
                }
            }

            val sourceAndPosition = keyToPosition[key]
            LaunchedEffect(sourceAndPosition) {
                if (sourceAndPosition != null) {
                    if (sourceAndPosition.first) {
                        listState.animateScrollToItem(sourceAndPosition.second)
                    }
                }
            }
        }
    }
}
