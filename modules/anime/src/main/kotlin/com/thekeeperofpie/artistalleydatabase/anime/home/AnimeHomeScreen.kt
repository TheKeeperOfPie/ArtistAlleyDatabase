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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.UserSocialActivityQuery
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsArticleEntry
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.ui.GenericViewAllCard
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBar
import com.thekeeperofpie.artistalleydatabase.compose.NestedScrollSplitter
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
object AnimeHomeScreen {

    private val SCREEN_KEY = AnimeNavDestinations.HOME.id
    private val CURRENT_ROW_IMAGE_HEIGHT = 120.dp
    private val CURRENT_ROW_IMAGE_WIDTH = 80.dp
    private val MEDIA_ROW_IMAGE_HEIGHT = 180.dp
    private val MEDIA_ROW_IMAGE_WIDTH = 120.dp

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

        // TODO: Handle LoadStates
        val activity = viewModel.activity.collectAsLazyPagingItems()

        val pullRefreshState = rememberPullRefreshState(
            refreshing = viewModel.loading,
            onRefresh = {
                viewModel.refresh()
                when (selectedTabIndex) {
                    0 -> animeViewModel.refresh()
                }
            }
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold.NoAppBarOffset(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            bottomNavigationState = bottomNavigationState,
            topBar = {
                EnterAlwaysTopAppBar(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.anime_home_label)) },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(upIconOption)
                            }
                        },
                        actions = {
                            IconButton(onClick = navigationCallback::onSeasonalClick) {
                                Icon(
                                    imageVector = when (AniListUtils.getCurrentSeasonYear().first) {
                                        MediaSeason.WINTER -> Icons.Filled.AcUnit
                                        MediaSeason.SPRING -> Icons.Filled.Grass
                                        MediaSeason.SUMMER -> Icons.Filled.WbSunny
                                        // TODO: Use a better leaf
                                        MediaSeason.FALL -> Icons.Filled.EnergySavingsLeaf
                                        MediaSeason.UNKNOWN__ -> TODO()
                                    },
                                    contentDescription = stringResource(
                                        R.string.anime_seasonal_icon_content_description
                                    ),
                                )
                            }
                            IconButton(onClick = navigationCallback::onAiringScheduleClick) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = stringResource(
                                        R.string.anime_airing_schedule_icon_content_description
                                    ),
                                )
                            }
                        },
                    )
                }
            },
        ) {
            val density = LocalDensity.current
            val topBarPadding by remember {
                derivedStateOf {
                    scrollBehavior.state.heightOffsetLimit
                        .takeUnless { it == -Float.MAX_VALUE }
                        ?.let { density.run { -it.toDp() } }
                        ?: 0.dp
                }
            }
            val topOffset by remember {
                derivedStateOf {
                    (topBarPadding + density.run { scrollBehavior.state.heightOffset.toDp() })
                        .coerceAtLeast(0.dp)
                }
            }
            Box(
                modifier = Modifier
                    .pullRefresh(pullRefreshState)
                    .nestedScroll(
                        NestedScrollSplitter(
                            scrollBehavior.nestedScrollConnection,
                            bottomNavigationState.nestedScrollConnection,
                            consumeNone = true,
                        )
                    )
            ) {
                LazyColumn(
                    state = scrollStateSaver.lazyListState(),
                    contentPadding = PaddingValues(
                        top = topBarPadding,
                        bottom = 16.dp + bottomNavigationState.bottomNavBarPadding()
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item("tabs") {
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
                            onClickListEdit = { editViewModel.initialize(it) },
                            selectedItemTracker = selectedItemTracker,
                            navigationCallback = navigationCallback,
                            colorCalculationState = colorCalculationState,
                        )
                        1 -> mediaList(
                            mediaViewModel = mangaViewModel,
                            onClickListEdit = { editViewModel.initialize(it) },
                            selectedItemTracker = selectedItemTracker,
                            navigationCallback = navigationCallback,
                            colorCalculationState = colorCalculationState,
                        )
                    }
                }

                PullRefreshIndicator(
                    refreshing = viewModel.loading,
                    state = pullRefreshState,
                    modifier = Modifier
                        .padding(top = topOffset)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    private fun LazyListScope.mediaList(
        mediaViewModel: AnimeHomeMediaViewModel,
        onClickListEdit: (MediaPreview) -> Unit,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val entry = mediaViewModel.entry
        if (entry == null) {
            loading("entry-loading")
        } else {
            currentMediaRow(
                headerTextRes = mediaViewModel.currentHeaderTextRes,
                current = entry.current,
                onClickListEdit = onClickListEdit,
                colorCalculationState = colorCalculationState,
                navigationCallback = navigationCallback
            )

            if (entry.lists == null) {
                loading("entry-loading")
            } else {
                entry.lists.forEach {
                    mediaRow(
                        data = it,
                        onClickListEdit = onClickListEdit,
                        onLongClickEntry = mediaViewModel::onLongClickEntry,
                        selectedItemTracker = selectedItemTracker,
                        navigationCallback = navigationCallback,
                        colorCalculationState = colorCalculationState,
                    )
                }
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
        item("newsRow") {
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
        item("activityRow") {
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
                    null,
                    -> TextActivitySmallCard(
                        activity = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    private fun LazyListScope.currentMediaRow(
        @StringRes headerTextRes: Int,
        current: List<UserMediaListController.Entry.MediaEntry>?,
        onClickListEdit: (MediaPreview) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        if (current == null || current.isNotEmpty()) {
            rowHeader(
                titleRes = headerTextRes,
                navigationCallback = navigationCallback,
                viewAllRoute = null, // TODO: full current lists
            )
        }

        if (current == null) {
            loading("$headerTextRes-loading")
        } else {
            item("$headerTextRes-current") {
                val listState = rememberLazyListState()
                val colorPrimary = MaterialTheme.colorScheme.primary
                val cardOutlineBorder = remember { BorderStroke(1.5.dp, colorPrimary) }

                val density = LocalDensity.current
                val coilWidth =
                    coil.size.Dimension.Pixels(density.run { CURRENT_ROW_IMAGE_WIDTH.roundToPx() })
                val coilHeight =
                    coil.size.Dimension.Pixels(density.run { CURRENT_ROW_IMAGE_HEIGHT.roundToPx() })

                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(current, { _, item -> item.media.id }) { index, item ->
                        MediaCard(
                            media = item.media,
                            listStatus = item.mediaListStatus,
                            progress = item.progress,
                            progressVolumes = item.progressVolumes,
                            ignored = item.ignored,
                            cardOutlineBorder = cardOutlineBorder,
                            width = CURRENT_ROW_IMAGE_WIDTH,
                            height = CURRENT_ROW_IMAGE_HEIGHT,
                            coilWidth = coilWidth,
                            coilHeight = coilHeight,
                            selected = false,
                            onClickListEdit = onClickListEdit,
                            onLongClickEntry = { /* TODO */ },
                            colorCalculationState = colorCalculationState,
                            navigationCallback = navigationCallback,
                            modifier = Modifier.animateItemPlacement(),
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.loading(key: String) {
        item(key) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().animateItemPlacement()
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }

    private fun LazyListScope.rowHeader(
        @StringRes titleRes: Int,
        navigationCallback: AnimeNavigator.NavigationCallback,
        viewAllRoute: String?,
    ) {
        item("header_$titleRes") {
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
        onClickListEdit: (MediaPreview) -> Unit,
        onLongClickEntry: (MediaNavigationData) -> Unit,
        selectedItemTracker: SelectedItemTracker,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val (rowKey, titleRes, entries, viewAllRoute) = data
        rowHeader(
            titleRes = titleRes,
            navigationCallback = navigationCallback,
            viewAllRoute = viewAllRoute
        )

        if (entries.isNullOrEmpty()) {
            loading("$titleRes-loading")
            return
        }

        item("$titleRes-pager") {
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
                    onLongClick = { onLongClickEntry(entry.media) },
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                )
            }

            selectedItemTracker.attachPager(key = rowKey, pagerState = pagerState)
        }

        item("$titleRes-media") {
            val listState = rememberLazyListState()
            val snapLayoutInfoProvider =
                remember(listState) { SnapLayoutInfoProvider(listState) { _, _, _ -> 0 } }
            val colorPrimary = MaterialTheme.colorScheme.primary
            val cardOutlineBorder = remember { BorderStroke(1.5.dp, colorPrimary) }

            val density = LocalDensity.current
            val coilWidth =
                coil.size.Dimension.Pixels(density.run { MEDIA_ROW_IMAGE_WIDTH.roundToPx() })
            val coilHeight =
                coil.size.Dimension.Pixels(density.run { MEDIA_ROW_IMAGE_HEIGHT.roundToPx() })

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = (LocalConfiguration.current.screenWidthDp.dp
                            - MEDIA_ROW_IMAGE_WIDTH).let {
                        if (viewAllRoute == null) it else it - 16.dp - MEDIA_ROW_IMAGE_WIDTH
                    },
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider)
            ) {
                itemsIndexed(entries, { _, item -> item.media.id }) { index, item ->
                    MediaCard(
                        media = item.media,
                        listStatus = item.mediaListStatus,
                        progress = item.progress,
                        progressVolumes = item.progressVolumes,
                        ignored = item.ignored,
                        cardOutlineBorder = cardOutlineBorder,
                        width = MEDIA_ROW_IMAGE_WIDTH,
                        height = MEDIA_ROW_IMAGE_HEIGHT,
                        coilWidth = coilWidth,
                        coilHeight = coilHeight,
                        selected = selectedItemTracker.keyToPosition[rowKey]?.second == index,
                        onClickListEdit = onClickListEdit,
                        onLongClickEntry = onLongClickEntry,
                        colorCalculationState = colorCalculationState,
                        navigationCallback = navigationCallback,
                        modifier = Modifier.animateItemPlacement(),
                    )
                }

                if (viewAllRoute != null) {
                    item("view_all") {
                        GenericViewAllCard(onClick = {
                            navigationCallback.navigate(viewAllRoute)
                        })
                    }
                }
            }

            selectedItemTracker.attachLazyList(key = rowKey, listState = listState)
        }
    }

    @Composable
    private fun MediaCard(
        media: MediaPreview,
        listStatus: MediaListStatus?,
        progress: Int?,
        progressVolumes: Int?,
        ignored: Boolean,
        cardOutlineBorder: BorderStroke,
        width: Dp,
        height: Dp,
        coilWidth: coil.size.Dimension,
        coilHeight: coil.size.Dimension,
        selected: Boolean,
        onClickListEdit: (MediaPreview) -> Unit,
        onLongClickEntry: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
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
            navigationCallback.onMediaClick(media, widthToHeightRatio ?: 1f)
        }

        val baseModifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClickEntry(media) },
            )
            .alpha(if (ignored) 0.38f else 1f)

        val card: @Composable (@Composable ColumnScope.() -> Unit) -> Unit =
            if (selected) {
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
                Box {
                    val alpha by animateFloatAsState(
                        if (widthToHeightRatio == null) 0f else 1f,
                        label = "Cover image alpha",
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media.coverImage?.extraLarge)
                            .crossfade(false)
                            .allowHardware(colorCalculationState.hasColor(id))
                            .size(width = coilWidth, height = coilHeight)
                            .build(),
                        contentScale = ContentScale.Crop,
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
                            .size(width = width, height = height)
                            .animateContentSize()
                    )

                    MediaListQuickEditIconButton(
                        mediaType = media.type,
                        listStatus = listStatus,
                        progress = progress,
                        progressVolumes = progressVolumes,
                        maxProgress = media.episodes ?: media.volumes,
                        onClick = { onClickListEdit(media) },
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
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
