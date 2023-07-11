package com.thekeeperofpie.artistalleydatabase.anime.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.UserSocialActivityQuery
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsArticleEntry
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsSmallCard
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
object AnimeHomeScreen {

    private val SCREEN_KEY = AnimeNavDestinations.HOME.id
    private val MEDIA_ROW_HEIGHT = 180.dp

    @Composable
    operator fun invoke(
        viewModel: AnimeHomeViewModel = hiltViewModel<AnimeHomeViewModel>(),
        upIconOption: UpIconOption?,
        navigationCallback: AnimeNavigator.NavigationCallback,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState,
    ) {
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val animeViewModel = hiltViewModel<AnimeHomeMediaViewModel.Anime>()
        val mangaViewModel = hiltViewModel<AnimeHomeMediaViewModel.Manga>()
        val selectedItemTracker = remember { SelectedItemTracker() }
        val activity = viewModel.activity.collectAsLazyPagingItems()
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
                Header(upIconOption = upIconOption, navigationCallback = navigationCallback)
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

            newsRow(
                data = viewModel.newsController.newsDateDescending(),
                navigationCallback = navigationCallback,
            )

            activityRow(
                data = activity,
                colorCalculationState = colorCalculationState,
                navigationCallback = navigationCallback,
            )

            when (selectedTabIndex) {
                0 -> mediaList(
                    mediaViewModel = animeViewModel,
                    selectedItemTracker = selectedItemTracker,
                    navigationCallback = navigationCallback,
                    colorCalculationState = colorCalculationState,
                )
                1 -> mediaList(
                    mediaViewModel = mangaViewModel,
                    selectedItemTracker = selectedItemTracker,
                    navigationCallback = navigationCallback,
                    colorCalculationState = colorCalculationState,
                )
            }
        }
    }

    @Composable
    private fun Header(
        upIconOption: UpIconOption?,
        navigationCallback: AnimeNavigator.NavigationCallback
    ) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.anime_home_label)) },
            navigationIcon = {
                if (upIconOption != null) {
                    UpIconButton(upIconOption)
                }
            },
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

    private fun LazyListScope.loading() {
        item {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            }
        }
    }

    private fun LazyListScope.mediaList(
        mediaViewModel: AnimeHomeMediaViewModel,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val entry = mediaViewModel.entry
        if (entry == null) {
            loading()
        } else {
            entry.data.forEach {
                mediaRow(
                    data = it,
                    onLongClickEntry = mediaViewModel::onLongClickEntry,
                    selectedItemTracker = selectedItemTracker,
                    navigationCallback = navigationCallback,
                    colorCalculationState = colorCalculationState,
                )
            }
        }
    }

    private fun LazyListScope.newsRow(
        data: List<AnimeNewsArticleEntry>,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        rowHeader(
            titleRes = R.string.anime_news_home_title,
            navigationCallback = navigationCallback,
            viewAllRoute = AnimeNavDestinations.NEWS.id
        )

        if (data.isEmpty()) return
        item {
            val pagerState = rememberPagerState(pageCount = { data.size })
            val uriHandler = LocalUriHandler.current
            val targetWidth = 350.coerceAtLeast(LocalConfiguration.current.screenWidthDp - 72).dp
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                AnimeNewsSmallCard(
                    entry = data[it],
                    uriHandler = uriHandler,
                )
            }
        }
    }

    private fun LazyListScope.activityRow(
        data: LazyPagingItems<UserSocialActivityQuery.Data.Page.Activity>,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        rowHeader(
            titleRes = R.string.anime_home_activity_label,
            navigationCallback = navigationCallback,
            viewAllRoute = AnimeNavDestinations.ACTIVITY.id
        )

        if (data.itemCount == 0) return
        item {
            val pagerState = rememberPagerState(pageCount = { data.itemCount })
            val targetWidth = 350.coerceAtLeast(LocalConfiguration.current.screenWidthDp - 72).dp
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                when (val activity = data[it]) {
                    is UserSocialActivityQuery.Data.Page.TextActivityActivity ->
                        TextActivitySmallCard(
                            activity = activity,
                            modifier = Modifier.fillMaxWidth()
                        )
                    is UserSocialActivityQuery.Data.Page.ListActivityActivity ->
                        ListActivitySmallCard(
                            screenKey = SCREEN_KEY,
                            activity = activity,
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                            modifier = Modifier.fillMaxWidth()
                        )
                    is UserSocialActivityQuery.Data.Page.MessageActivityActivity,
                    is UserSocialActivityQuery.Data.Page.OtherActivity,
                    null -> TextActivitySmallCard(
                        activity = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    private fun LazyListScope.rowHeader(
        @StringRes titleRes: Int,
        navigationCallback: AnimeNavigator.NavigationCallback,
        viewAllRoute: String?,
    ) {
        item {
            Row(
                modifier = Modifier.clickable(enabled = viewAllRoute != null) {
                    navigationCallback.navigate(viewAllRoute!!)
                }
            ) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium.copy(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
                )
                if (viewAllRoute != null) {
                    IconButton(
                        onClick = { navigationCallback.navigate(viewAllRoute) },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = stringResource(
                                R.string.anime_home_row_view_all_content_description
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.mediaRow(
        data: AnimeHomeDataEntry.RowData,
        onLongClickEntry: (AnimeHomeDataEntry.MediaEntry) -> Unit,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val (rowKey, titleRes, entries, viewAllRoute) = data
        if (entries.isEmpty()) return
        rowHeader(
            titleRes = titleRes,
            navigationCallback = navigationCallback,
            viewAllRoute = viewAllRoute
        )

        item {
            val pagerState = rememberPagerState(pageCount = { entries.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                pageSpacing = 16.dp,
            ) {
                val entry = entries[it]
                AnimeMediaLargeCard(
                    screenKey = SCREEN_KEY,
                    // TODO: Move entry wrapping elsewhere and abstract
                    entry = AnimeMediaLargeCard.Entry(entry.media, entry.ignored),
                    onLongClick = { onLongClickEntry(entry) },
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }

            selectedItemTracker.attachPager(key = rowKey, pagerState = pagerState)
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
                itemsIndexed(entries, { _, item -> item.media.id }) { index, item ->
                    val media = item.media
                    val id = media.id.toString()
                    val colors = colorCalculationState.colorMap[id]
                    val animationProgress by animateIntAsState(
                        if (colors == null) 0 else 255,
                        label = "Media card color fade in",
                    )

                    val surfaceColor = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
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
                        navigationCallback.onMediaClick(media, widthToHeightRatio)
                    }

                    val baseModifier = Modifier
                        .animateItemPlacement()
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = onClick,
                            onLongClick = { onLongClickEntry(item) },
                        )
                        .alpha(if (item.ignored) 0.38f else 1f)

                    val card: @Composable (@Composable ColumnScope.() -> Unit) -> Unit =
                        if (selectedItemTracker.keyToPosition[rowKey]?.second == index) {
                            {
                                OutlinedCard(
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = containerColor,
                                    ),
                                    border = cardOutlineBorder,
                                    content = it,
                                    modifier = baseModifier
                                )
                            }
                        } else {
                            {
                                ElevatedCard(
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = containerColor,
                                    ),
                                    content = it,
                                    modifier = baseModifier
                                )
                            }
                        }

                    SharedElement(
                        key = "anime_media_${media.id}_image",
                        screenKey = SCREEN_KEY,
                    ) {
                        card {
                            val alpha by animateFloatAsState(
                                if (widthToHeightRatio == null) 0f else 1f,
                                label = "Cover image alpha",
                            )
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(media.coverImage?.extraLarge)
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

            selectedItemTracker.attachLazyList(key = rowKey, listState = listState)
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
