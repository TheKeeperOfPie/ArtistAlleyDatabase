package com.thekeeperofpie.artistalleydatabase.anime.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.core.graphics.ColorUtils
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.UserSocialActivityQuery
import com.anilist.fragment.HomeMedia
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.MessageActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsArticleEntry
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsSmallCard
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationCard
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewCard
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.ui.GenericViewAllCard
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime.utils.getOrNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.items
import com.thekeeperofpie.artistalleydatabase.anime.utils.itemsIndexed
import com.thekeeperofpie.artistalleydatabase.anime.utils.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
object AnimeHomeScreen {

    private val SCREEN_KEY = AnimeNavDestinations.HOME.id
    private val CURRENT_ROW_IMAGE_HEIGHT = 144.dp
    private val CURRENT_ROW_IMAGE_WIDTH = 96.dp
    private val MEDIA_ROW_IMAGE_HEIGHT = 180.dp
    private val MEDIA_ROW_IMAGE_WIDTH = 120.dp

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        viewModel: AnimeHomeViewModel = hiltViewModel<AnimeHomeViewModel>(),
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState,
    ) {
        var selectedIsAnime by rememberSaveable {
            mutableStateOf(viewModel.preferredMediaType == MediaType.ANIME)
        }
        val selectedItemTracker = remember { SelectedItemTracker() }

        val mediaViewModel = if (selectedIsAnime) {
            hiltViewModel<AnimeHomeMediaViewModel.Anime>()
        } else {
            hiltViewModel<AnimeHomeMediaViewModel.Manga>()
        }

        val activity = viewModel.activity.collectAsLazyPagingItems()
        val recommendations = viewModel.recommendations.collectAsLazyPagingItems()
        val reviews = mediaViewModel.reviews.collectAsLazyPagingItems()
        val refreshing = activity.loadState.refresh == LoadState.Loading
                || recommendations.loadState.refresh == LoadState.Loading
                || reviews.loadState.refresh == LoadState.Loading
                || mediaViewModel.entry.loading
                || mediaViewModel.current.loading
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = {
                viewModel.refresh()
                mediaViewModel.refresh()
            }
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        val viewer by viewModel.viewer.collectAsState()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
            bottomNavigationState = bottomNavigationState,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(
                                    if (selectedIsAnime) {
                                        R.string.anime_home_label_anime
                                    } else {
                                        R.string.anime_home_label_manga
                                    }
                                )
                            )
                        },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(upIconOption)
                            }
                        },
                        actions = {
                            IconButton(onClick = { selectedIsAnime = !selectedIsAnime }) {
                                Icon(
                                    imageVector = if (selectedIsAnime) {
                                        Icons.Filled.MenuBook
                                    } else {
                                        Icons.Filled.Monitor
                                    },
                                    contentDescription = stringResource(
                                        R.string.anime_home_media_type_switch_icon_content_description
                                    ),
                                )
                            }

                            val navigationCallback = LocalNavigationCallback.current

                            val unlocked by viewModel.unlocked.collectAsState(initial = false)
                            if (unlocked) {
                                IconButton(onClick = navigationCallback::onForumRootClick) {
                                    Icon(
                                        imageVector = Icons.Filled.Forum,
                                        contentDescription = stringResource(
                                            R.string.anime_forum_icon_content_description
                                        ),
                                    )
                                }
                            }

                            if (viewer != null) {
                                BadgedBox(badge = {
                                    val unreadCount = viewModel.notificationsController.unreadCount
                                    if (unreadCount > 0) {
                                        Badge(modifier = Modifier.offset(x = (-18).dp, y = 6.dp)) {
                                            Text(
                                                text = unreadCount.toString()
                                            )
                                        }
                                    }
                                }) {
                                    IconButton(onClick = navigationCallback::onNotificationsClick) {
                                        Icon(
                                            imageVector = Icons.Filled.Notifications,
                                            contentDescription = stringResource(
                                                R.string.anime_notifications_icon_content_description
                                            ),
                                        )
                                    }
                                }
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
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                            )
                        ),
                    )
                }
            },
            modifier = Modifier
                .nestedScroll(bottomNavigationState.nestedScrollConnection)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = scrollStateSaver.lazyListState(),
                    contentPadding = PaddingValues(
                        bottom = 16.dp + bottomNavigationState.bottomNavBarPadding()
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    newsRow(
                        data = viewModel.newsController.newsDateDescending(),
                    )

                    activityRow(
                        editViewModel = editViewModel,
                        viewer = viewer,
                        data = activity,
                        onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                    )

                    currentMediaRow(
                        mediaViewModel = mediaViewModel,
                        viewer = viewer,
                        onClickListEdit = { editViewModel.initialize(it) },
                        onClickIncrementProgress = { editViewModel.incrementProgress(it) },
                    )

                    recommendations(
                        viewModel = viewModel,
                        editViewModel = editViewModel,
                        viewer = viewer,
                        recommendations = recommendations,
                    )

                    mediaList(
                        mediaViewModel = mediaViewModel,
                        viewer = viewer,
                        editViewModel = editViewModel,
                        selectedItemTracker = selectedItemTracker,
                    )

                    reviews(
                        editViewModel = editViewModel,
                        viewer = viewer,
                        reviews = reviews,
                    )

                    suggestions(
                        mediaViewModel = mediaViewModel,
                    )
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    private fun LazyListScope.mediaList(
        mediaViewModel: AnimeHomeMediaViewModel,
        viewer: AniListViewer?,
        editViewModel: MediaEditViewModel,
        selectedItemTracker: SelectedItemTracker,
    ) {
        val entry = mediaViewModel.entry
        entry.result?.lists?.forEach {
            mediaRow(
                data = it,
                viewer = viewer,
                editViewModel = editViewModel,
                selectedItemTracker = selectedItemTracker,
            )
        }
    }

    private fun LazyListScope.newsRow(
        data: List<AnimeNewsArticleEntry<*>>?,
    ) {
        rowHeader(
            titleRes = R.string.anime_news_home_title,
            viewAllRoute = AnimeNavDestinations.NEWS.id
        )

        val itemCount = data?.size ?: 1
        if (itemCount == 0) return
        item("newsRow") {
            val pagerState = rememberPagerState(pageCount = { itemCount })
            val uriHandler = LocalUriHandler.current
            val targetWidth = targetPageWidth()
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                AnimeNewsSmallCard(
                    entry = data?.get(it),
                    uriHandler = uriHandler,
                )
            }
        }
    }

    private fun LazyListScope.activityRow(
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        data: LazyPagingItems<ActivityEntry>,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    ) {
        rowHeader(
            titleRes = R.string.anime_home_activity_label,
            viewAllRoute = AnimeNavDestinations.ACTIVITY.id
        )

        item("activityRow") {
            val pagerState = rememberPagerState(data = data, placeholderCount = 1)
            val targetWidth = targetPageWidth()
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                val entry = data.getOrNull(it)
                when (val activity = entry?.activity) {
                    is UserSocialActivityQuery.Data.Page.TextActivityActivity ->
                        TextActivitySmallCard(
                            screenKey = SCREEN_KEY,
                            viewer = viewer,
                            activity = activity,
                            entry = entry,
                            onActivityStatusUpdate = onActivityStatusUpdate,
                            clickable = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    is UserSocialActivityQuery.Data.Page.ListActivityActivity ->
                        ListActivitySmallCard(
                            screenKey = SCREEN_KEY,
                            viewer = viewer,
                            activity = activity,
                            mediaEntry = entry.media,
                            entry = entry,
                            onActivityStatusUpdate = onActivityStatusUpdate,
                            onClickListEdit = { editViewModel.initialize(it.media) },
                            clickable = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    is UserSocialActivityQuery.Data.Page.MessageActivityActivity ->
                        MessageActivitySmallCard(
                            screenKey = SCREEN_KEY,
                            viewer = viewer,
                            activity = activity,
                            entry = entry,
                            onActivityStatusUpdate = onActivityStatusUpdate,
                            clickable = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    is UserSocialActivityQuery.Data.Page.OtherActivity,
                    null,
                    -> ListActivitySmallCard(
                        screenKey = SCREEN_KEY,
                        viewer = viewer,
                        activity = null,
                        mediaEntry = null,
                        entry = null,
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        onClickListEdit = { editViewModel.initialize(it.media) },
                        clickable = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    /**
     * @return true if loading shown
     */
    private fun LazyListScope.currentMediaRow(
        mediaViewModel: AnimeHomeMediaViewModel,
        viewer: AniListViewer?,
        onClickListEdit: (MediaCompactWithTags) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
    ) {
        val result = mediaViewModel.current.result
        if (result != null && result.isEmpty()) return
        val headerTextRes = mediaViewModel.currentHeaderTextRes
        rowHeader(
            titleRes = headerTextRes,
            viewAllRoute = viewer?.let {
                AnimeNavDestinations.USER_LIST.id +
                        "?mediaType=${mediaViewModel.mediaType.rawValue}" +
                        "&mediaListStatus=${MediaListStatus.CURRENT.rawValue}"
            }
        )

        item("$headerTextRes-current") {
            val listState = rememberLazyListState()
            val placeholderCount = if (result == null) {
                mediaViewModel.currentMediaPreviousSize.collectAsState().value
            } else {
                0
            }
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.animateItemPlacement()
            ) {
                items(
                    data = result,
                    placeholderCount = placeholderCount,
                    key = { it.media.id },
                    contentType = { "media" },
                ) {
                    CurrentMediaCard(
                        entry = it,
                        viewer = viewer,
                        onClickListEdit = onClickListEdit,
                        onClickIncrementProgress = onClickIncrementProgress,
                        modifier = Modifier.animateItemPlacement(),
                    )
                }
            }
        }
    }

    private fun LazyListScope.rowHeader(
        @StringRes titleRes: Int,
        viewAllRoute: String?,
    ) {
        item("header_$titleRes") {
            NavigationHeader(
                titleRes = titleRes,
                viewAllRoute = viewAllRoute,
                viewAllContentDescriptionTextRes = R.string.anime_home_row_view_all_content_description,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }

    private fun LazyListScope.mediaRow(
        data: AnimeHomeDataEntry.RowData,
        viewer: AniListViewer?,
        editViewModel: MediaEditViewModel,
        selectedItemTracker: SelectedItemTracker,
    ) {
        val (rowKey, titleRes, entries, viewAllRoute) = data
        rowHeader(
            titleRes = titleRes,
            viewAllRoute = viewAllRoute
        )

        item("$titleRes-pager") {
            val pagerState = rememberPagerState(data = entries, placeholderCount = 1)
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
                pageSpacing = 8.dp,
            ) {
                val entry = entries?.getOrNull(it)
                AnimeMediaLargeCard(
                    screenKey = SCREEN_KEY,
                    viewer = viewer,
                    entry = entry,
                    editViewModel = editViewModel,
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
            val coilWidth = coil.size.Dimension.Pixels(
                density.run { MEDIA_ROW_IMAGE_WIDTH.roundToPx() / 4 * 3 }
            )
            val coilHeight = coil.size.Dimension.Pixels(
                density.run { MEDIA_ROW_IMAGE_HEIGHT.roundToPx() / 4 * 3 }
            )

            val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
            val placeholderCount = (screenWidthDp / (MEDIA_ROW_IMAGE_WIDTH + 16.dp)).toInt()
                .coerceAtLeast(1) + 1

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = (screenWidthDp - MEDIA_ROW_IMAGE_WIDTH).let {
                        if (viewAllRoute == null) it else it - 16.dp - MEDIA_ROW_IMAGE_WIDTH
                    },
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider)
            ) {
                itemsIndexed(
                    data = entries,
                    placeholderCount = placeholderCount,
                    key = { _, item -> item.media.id },
                    contentType = { _, _ -> "media" },
                ) { index, item ->
                    MediaCard(
                        editViewModel = editViewModel,
                        media = item?.media,
                        mediaStatusAware = item,
                        ignored = item?.ignored ?: false,
                        viewer = viewer,
                        cardOutlineBorder = cardOutlineBorder,
                        width = MEDIA_ROW_IMAGE_WIDTH,
                        height = MEDIA_ROW_IMAGE_HEIGHT,
                        coilWidth = coilWidth,
                        coilHeight = coilHeight,
                        selected = selectedItemTracker.keyToPosition[rowKey]?.second == index,
                        modifier = Modifier.animateItemPlacement(),
                    )
                }

                if (viewAllRoute != null) {
                    item("view_all") {
                        val navigationCallback = LocalNavigationCallback.current
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
    private fun CurrentMediaCard(
        entry: UserMediaListController.MediaEntry?,
        viewer: AniListViewer?,
        onClickListEdit: (MediaPreview) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val media = entry?.media
        val mediaId = media?.id?.toString()

        val colorCalculationState = LocalColorCalculationState.current
        val colors = colorCalculationState.getColors(mediaId)
        val animationProgress by animateIntAsState(
            if (colors.first.isUnspecified) 0 else 255,
            label = "Media card color fade in",
        )

        val surfaceColor = media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
            ?: MaterialTheme.colorScheme.surface
        val containerColor = when {
            colors.first.isUnspecified || animationProgress == 0 -> surfaceColor
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

        val navigationCallback = LocalNavigationCallback.current
        var widthToHeightRatio by remember(mediaId) { mutableStateOf<Float?>(null) }
        val onClick = {
            navigationCallback.onMediaClick(media as MediaPreview, widthToHeightRatio ?: 1f)
        }

        val ignoreController = LocalIgnoreController.current
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = containerColor,
            ),
            onClick = onClick,
            modifier = modifier
                .widthIn(max = CURRENT_ROW_IMAGE_WIDTH)
                .clip(RoundedCornerShape(12.dp))
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .padding(2.dp)
        ) {
            Box {
                val alpha by animateFloatAsState(
                    if (widthToHeightRatio == null) 0f else 1f,
                    label = "Cover image alpha",
                )

                val density = LocalDensity.current
                val coilWidth = coil.size.Dimension.Pixels(
                    density.run { CURRENT_ROW_IMAGE_WIDTH.roundToPx() / 4 * 3 }
                )
                val coilHeight = coil.size.Dimension.Pixels(
                    density.run { CURRENT_ROW_IMAGE_HEIGHT.roundToPx() / 4 * 3 }
                )
                MediaCoverImage(
                    screenKey = SCREEN_KEY,
                    mediaId = mediaId,
                    image = ImageRequest.Builder(LocalContext.current)
                        .data(media?.coverImage?.extraLarge)
                        .allowHardware(colorCalculationState.hasColor(mediaId))
                        .size(width = coilWidth, height = coilHeight)
                        .build(),
                    contentScale = ContentScale.Crop,
                    onSuccess = {
                        widthToHeightRatio = it.widthToHeightRatio()
                        if (mediaId != null) {
                            ComposeColorUtils.calculatePalette(
                                id = mediaId,
                                success = it,
                                colorCalculationState = colorCalculationState,
                                heightStartThreshold = 3 / 4f,
                                selectMaxPopulation = true,
                            )
                        }
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .alpha(alpha)
                        .size(
                            width = CURRENT_ROW_IMAGE_WIDTH,
                            height = CURRENT_ROW_IMAGE_HEIGHT
                        )
                        .animateContentSize()
                        .placeholder(
                            visible = media == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

                if (viewer != null && media != null) {
                    val maxProgress = MediaUtils.maxProgress(media)
                    MediaListQuickEditIconButton(
                        viewer = viewer,
                        mediaType = media.type,
                        media = entry,
                        maxProgress = maxProgress,
                        maxProgressVolumes = media.volumes,
                        onClick = { onClickListEdit(media) },
                        iconSize = 12.dp,
                        textVerticalPadding = 2.dp,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )

                    if ((entry.progress ?: 0) < (maxProgress ?: 1)) {
                        IconButton(
                            onClick = { onClickIncrementProgress(entry) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlusOne,
                                contentDescription = stringResource(
                                    R.string.anime_home_media_increment_progress_content_description
                                )
                            )
                        }
                    }
                }
            }

            Text(
                text = if (media == null) {
                    "Some long media title that fills 2 lines"
                } else {
                    media.title?.primaryTitle().orEmpty()
                },
                style = MaterialTheme.typography.labelSmall,
                color = ComposeColorUtils.bestTextColor(containerColor)
                    ?: Color.Unspecified,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                minLines = 2,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .placeholder(
                        visible = media == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    @Composable
    private fun MediaCard(
        editViewModel: MediaEditViewModel,
        media: HomeMedia?,
        mediaStatusAware: MediaStatusAware?,
        ignored: Boolean,
        viewer: AniListViewer?,
        cardOutlineBorder: BorderStroke,
        width: Dp,
        height: Dp,
        coilWidth: coil.size.Dimension,
        coilHeight: coil.size.Dimension,
        selected: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val mediaId = media?.id?.toString()
        val colorCalculationState = LocalColorCalculationState.current
        val colors = colorCalculationState.getColors(mediaId)
        val animationProgress by animateIntAsState(
            if (colors.first.isUnspecified) 0 else 255,
            label = "Media card color fade in",
        )

        val surfaceColor = media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
            ?: MaterialTheme.colorScheme.surface
        val containerColor = when {
            colors.first.isUnspecified || animationProgress == 0 -> surfaceColor
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

        val navigationCallback = LocalNavigationCallback.current
        var widthToHeightRatio by remember(mediaId) { mutableStateOf<Float?>(null) }
        val title = MediaUtils.userPreferredTitle(
            userPreferred = media?.title?.userPreferred,
            romaji = media?.title?.romaji,
            english = media?.title?.english,
            native = media?.title?.native,
        )
        val onClick = {
            if (media != null) {
                navigationCallback.onMediaClick(
                    mediaId = media.id.toString(),
                    title = title,
                    coverImage = media.coverImage?.extraLarge,
                    imageWidthToHeightRatio = widthToHeightRatio ?: 1f,
                )
            }
        }

        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        val baseModifier = modifier
            .height(height)
            .widthIn(min = width)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    media?.coverImage?.extraLarge?.let(fullscreenImageHandler::openImage)
                },
            )
            .alpha(if (ignored) 0.38f else 1f)
            .padding(2.dp)

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
            key = "anime_media_${mediaId}_image",
            screenKey = SCREEN_KEY,
        ) {
            card {
                Box {
                    var showTitle by remember(media) { mutableStateOf(false) }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(media?.coverImage?.extraLarge)
                            .crossfade(true)
                            .allowHardware(colorCalculationState.hasColor(mediaId))
                            .size(width = coilWidth, height = coilHeight)
                            .build(),
                        contentScale = ContentScale.Crop,
                        contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                        onSuccess = {
                            widthToHeightRatio = it.widthToHeightRatio()
                            if (mediaId != null) {
                                ComposeColorUtils.calculatePalette(
                                    id = mediaId,
                                    success = it,
                                    colorCalculationState = colorCalculationState,
                                    heightStartThreshold = 3 / 4f,
                                    selectMaxPopulation = true,
                                )
                            }
                        },
                        onError = { showTitle = true },
                        modifier = Modifier
                            .size(width = width, height = height)
                            .blurForScreenshotMode()
                            .placeholder(
                                visible = media == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )

                    if (showTitle && title != null) {
                        AutoResizeHeightText(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ComposeColorUtils.bestTextColor(containerColor)
                                ?: Color.Unspecified,
                            modifier = Modifier
                                .size(width = width, height = height)
                                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 40.dp)
                        )
                    }

                    if (viewer != null && media != null && mediaStatusAware != null) {
                        MediaListQuickEditIconButton(
                            viewer = viewer,
                            mediaType = media.type,
                            media = mediaStatusAware,
                            maxProgress = MediaUtils.maxProgress(
                                type = media.type,
                                chapters = media.chapters,
                                episodes = media.episodes,
                                nextAiringEpisode = media.nextAiringEpisode?.episode,
                            ),
                            maxProgressVolumes = media.volumes,
                            onClick = {
                                editViewModel.initialize(
                                    mediaId = media.id.toString(),
                                    coverImage = media.coverImage?.extraLarge,
                                    type = media.type,
                                    titleRomaji = media.title?.romaji,
                                    titleEnglish = media.title?.english,
                                    titleNative = media.title?.native,
                                )
                            },
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }
        }
    }

    private fun LazyListScope.recommendations(
        viewModel: AnimeHomeViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        recommendations: LazyPagingItems<RecommendationEntry>,
    ) {
        rowHeader(
            titleRes = R.string.anime_recommendations_home_title,
            viewAllRoute = AnimeNavDestinations.RECOMMENDATIONS.id,
        )

        item("recommendationsRow") {
            val pagerState = rememberPagerState(data = recommendations, placeholderCount = 1)
            val targetWidth = targetPageWidth()
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                val entry = recommendations.getOrNull(it)
                RecommendationCard(
                    screenKey = SCREEN_KEY,
                    viewer = viewer,
                    user = entry?.user,
                    media = entry?.media,
                    mediaRecommendation = entry?.mediaRecommendation,
                    recommendation = entry?.data,
                    onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
                    onClickListEdit = {
                        if (entry != null) {
                            editViewModel.initialize(entry.media.media)
                        }
                    },
                )
            }
        }
    }

    private fun LazyListScope.reviews(
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        reviews: LazyPagingItems<ReviewEntry>,
    ) {
        rowHeader(
            titleRes = R.string.anime_reviews_home_title,
            viewAllRoute = AnimeNavDestinations.REVIEWS.id,
        )

        item("reviewsRow") {
            val pagerState = rememberPagerState(data = reviews, placeholderCount = 1)
            val targetWidth = targetPageWidth()
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(targetWidth),
                verticalAlignment = Alignment.Top,
            ) {
                val entry = reviews.getOrNull(it)
                ReviewCard(
                    screenKey = SCREEN_KEY,
                    viewer = viewer,
                    review = entry?.review,
                    media = entry?.media,
                    onClick = {
                        if (entry != null) {
                            it.onReviewClick(
                                reviewId = entry.review.id.toString(),
                                media = null,
                                favorite = null,
                                imageWidthToHeightRatio = 1f,
                            )
                        }
                    },
                    onClickListEdit = {
                        if (entry != null) {
                            editViewModel.initialize(entry.media.media)
                        }
                    },
                )
            }
        }
    }

    private fun LazyListScope.suggestions(
        mediaViewModel: AnimeHomeMediaViewModel,
    ) {
        rowHeader(
            titleRes = R.string.anime_home_suggestions_header,
            viewAllRoute = null,
        )

        item("${mediaViewModel.mediaType}-suggestions") {
            val navigationCallback = LocalNavigationCallback.current
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                mediaViewModel.suggestions.forEach {
                    item("${mediaViewModel.mediaType}-suggestion-${it.second}") {
                        SuggestionChip(
                            onClick = { navigationCallback.navigate(it.second) },
                            label = { Text(stringResource(it.first)) },
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun targetPageWidth() =
        420.coerceAtMost(LocalConfiguration.current.screenWidthDp - 32).dp

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
