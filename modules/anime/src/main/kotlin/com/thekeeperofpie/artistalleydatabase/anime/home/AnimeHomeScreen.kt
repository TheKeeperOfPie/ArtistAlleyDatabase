package com.thekeeperofpie.artistalleydatabase.anime.home

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.size.Dimension
import com.anilist.fragment.HomeMedia
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
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
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.ScrollStateSaver
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateSharedTransitionWithOtherState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.Flow

@Suppress("NAME_SHADOWING")
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class
)
object AnimeHomeScreen {

    private val CURRENT_ROW_IMAGE_HEIGHT = 144.dp
    private val CURRENT_ROW_IMAGE_WIDTH = 96.dp
    private val MEDIA_ROW_IMAGE_HEIGHT = 180.dp
    private val MEDIA_ROW_IMAGE_WIDTH = 120.dp

    private val LargeMediaPagerContentPadding =
        PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp)

    data object FillOr450 : PageSize {
        override fun Density.calculateMainAxisPageSize(availableSpace: Int, pageSpacing: Int) =
            availableSpace.coerceAtMost(450.dp.roundToPx())
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    operator fun invoke(
        viewModel: AnimeHomeViewModel = hiltViewModel<AnimeHomeViewModel>(),
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState?,
        mediaViewModel: @Composable (Boolean) -> AnimeHomeMediaViewModel = {
            if (it) {
                hiltViewModel<AnimeHomeMediaViewModel.Anime>()
            } else {
                hiltViewModel<AnimeHomeMediaViewModel.Manga>()
            }
        },
    ) {
        var selectedIsAnime by rememberSaveable {
            mutableStateOf(viewModel.preferredMediaType == MediaType.ANIME)
        }
        val mediaViewModel = mediaViewModel(selectedIsAnime)

        val activity = viewModel.activity.collectAsLazyPagingItems()
        val recommendations = viewModel.recommendations.collectAsLazyPagingItems()
        val reviews = mediaViewModel.reviews.collectAsLazyPagingItems()
        val refreshing by remember {
            derivedStateOf {
                viewModel.newsController.newsDateDescending() == null
                        || activity.loadState.refresh == LoadState.Loading
                        || recommendations.loadState.refresh == LoadState.Loading
                        || reviews.loadState.refresh == LoadState.Loading
                        || mediaViewModel.entry.loading
                        || mediaViewModel.currentMedia.loading
            }
        }
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = {
                viewModel.refresh()
                mediaViewModel.refresh()
            }
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            modifier = Modifier
                .conditionallyNonNull(bottomNavigationState) {
                    nestedScroll(it.nestedScrollConnection)
                }
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            viewModel = editViewModel,
            topBar = {
                val unlocked by viewModel.unlocked.collectAsState()
                val viewer by viewModel.viewer.collectAsState()
                TopBar(
                    scrollBehavior = scrollBehavior,
                    upIconOption = upIconOption,
                    selectedIsAnime = selectedIsAnime,
                    onSelectedIsAnimeChange = { selectedIsAnime = it },
                    unlocked = { unlocked },
                    viewer = { viewer },
                    unreadCount = { viewModel.notificationsController.unreadCount },
                )
            },
            bottomNavigationState = bottomNavigationState
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(pullRefreshState)
            ) {
                Content(
                    scrollState = scrollStateSaver.scrollState(),
                    bottomNavBarPadding = bottomNavigationState?.bottomNavBarPadding() ?: 0.dp,
                    viewModel = viewModel,
                    mediaViewModel = mediaViewModel,
                    editViewModel = editViewModel,
                    recommendations = recommendations,
                    reviews = reviews,
                )

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("rootRefreshIndicator")
                )
            }
        }
    }

    @Composable
    private fun TopBar(
        scrollBehavior: TopAppBarScrollBehavior,
        upIconOption: UpIconOption?,
        selectedIsAnime: Boolean,
        onSelectedIsAnimeChange: (Boolean) -> Unit,
        unlocked: () -> Boolean,
        viewer: () -> AniListViewer?,
        unreadCount: () -> Int,
    ) {
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
                    IconButton(onClick = { onSelectedIsAnimeChange(!selectedIsAnime) }) {
                        Icon(
                            imageVector = if (selectedIsAnime) {
                                Icons.AutoMirrored.Filled.MenuBook
                            } else {
                                Icons.Filled.Monitor
                            },
                            contentDescription = stringResource(
                                R.string.anime_home_media_type_switch_icon_content_description
                            ),
                        )
                    }

                    val navigationCallback = LocalNavigationCallback.current

                    if (unlocked()) {
                        IconButton(onClick = {
                            navigationCallback.navigate(AnimeDestination.Forum)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = stringResource(
                                    R.string.anime_forum_icon_content_description
                                ),
                            )
                        }
                    }

                    if (viewer() != null) {
                        BadgedBox(badge = {
                            val unreadCount = unreadCount()
                            if (unreadCount > 0) {
                                Badge(modifier = Modifier.offset(x = (-18).dp, y = 6.dp)) {
                                    Text(
                                        text = unreadCount.toString()
                                    )
                                }
                            }
                        }) {
                            IconButton(onClick = {
                                navigationCallback.navigate(AnimeDestination.Notifications)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = stringResource(
                                        R.string.anime_notifications_icon_content_description
                                    ),
                                )
                            }
                        }
                    }

                    IconButton(onClick = {
                        navigationCallback.navigate(AnimeDestination.AiringSchedule)
                    }) {
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
    }

    @Composable
    private fun Content(
        scrollState: ScrollState,
        bottomNavBarPadding: Dp,
        viewModel: AnimeHomeViewModel,
        mediaViewModel: AnimeHomeMediaViewModel,
        editViewModel: MediaEditViewModel,
        recommendations: LazyPagingItems<RecommendationEntry>,
        reviews: LazyPagingItems<ReviewEntry>,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("homeColumn")
                .verticalScroll(scrollState)
        ) {
            val configuration = LocalConfiguration.current
            val pageSize = remember {
                PageSize.Fixed(420.coerceAtMost(configuration.screenWidthDp - 32).dp)
            }
            NewsRow(
                data = viewModel.newsController.newsDateDescending(),
                pageSize = pageSize,
            )

            val viewer by viewModel.viewer.collectAsState()
            Activities(
                viewer = viewer,
                data = viewModel.activity,
                pageSize = pageSize,
                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                onClickListEdit = editViewModel::initialize,
            )

            CurrentMediaRow(
                mediaViewModel = mediaViewModel,
                viewer = viewer,
                onClickListEdit = editViewModel::initialize,
                onClickIncrementProgress = editViewModel::incrementProgress,
            )

            Recommendations(
                viewModel = viewModel,
                editViewModel = editViewModel,
                viewer = viewer,
                recommendations = recommendations,
                pageSize = pageSize,
            )

            val selectedItemTracker = remember { SelectedItemTracker() }
            val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
            val contentPadding = PaddingValues(
                start = 16.dp,
                end = (screenWidthDp - MEDIA_ROW_IMAGE_WIDTH).let {
                    it - 16.dp - MEDIA_ROW_IMAGE_WIDTH
                },
            )
            val placeholderCount = (screenWidthDp / (MEDIA_ROW_IMAGE_WIDTH + 16.dp)).toInt()
                .coerceAtLeast(1) + 1
            mediaViewModel.entry.result?.lists?.forEach {
                SharedTransitionKeyScope("anime_home_media_list_row_${it.id}") {
                    MediaRow(
                        data = it,
                        viewer = viewer,
                        onClickListEdit = editViewModel::initialize,
                        selectedItemTracker = selectedItemTracker,
                        contentPadding = contentPadding,
                        placeholderCount = placeholderCount,
                    )
                }
            }

            Reviews(
                viewer = viewer,
                reviews = reviews,
                pageSize = pageSize,
                onClickListEdit = editViewModel::initialize,
            )

            Suggestions(
                mediaViewModel = mediaViewModel,
            )

            Spacer(modifier = Modifier.height(32.dp + bottomNavBarPadding))
        }
    }

    @Composable
    private fun NewsRow(
        data: List<AnimeNewsEntry<*>>?,
        pageSize: PageSize,
    ) {
        RowHeader(
            titleRes = R.string.anime_news_home_title,
            viewAllRoute = AnimeDestination.News,
        )

        val itemCount = data?.size ?: 3
        if (itemCount == 0) return
        val pagerState = rememberPagerState(pageCount = { itemCount })
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            pageSpacing = 16.dp,
            pageSize = pageSize,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.recomposeHighlighter()
        ) {
            AnimeNewsSmallCard(entry = data?.get(it))
        }
    }

    @Composable
    private fun Activities(
        viewer: AniListViewer?,
        data: Flow<PagingData<ActivityEntry>>,
        pageSize: PageSize,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        RowHeader(
            titleRes = R.string.anime_home_activity_label,
            viewAllRoute = AnimeDestination.Activity
        )

        ActivityRow(
            viewer = viewer,
            data = data,
            pageSize = pageSize,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }

    @Composable
    private fun ActivityRow(
        viewer: AniListViewer?,
        data: Flow<PagingData<ActivityEntry>>,
        pageSize: PageSize,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val activities = data.collectAsLazyPagingItems()
        val pagerState = rememberPagerState(data = activities, placeholderCount = 3)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            pageSpacing = 16.dp,
            pageSize = pageSize,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.recomposeHighlighter()
        ) {
            val entry = activities.getOrNull(it)
            SharedTransitionKeyScope("anime_home_activity_${entry?.activityId?.valueId}") {
                ActivitySmallCard(
                    viewer = viewer,
                    activity = entry?.activity,
                    mediaEntry = entry?.media,
                    entry = entry,
                    onActivityStatusUpdate = onActivityStatusUpdate,
                    onClickListEdit = onClickListEdit,
                )
            }
        }
    }

    /**
     * @return true if loading shown
     */
    @Composable
    private fun ColumnScope.CurrentMediaRow(
        mediaViewModel: AnimeHomeMediaViewModel,
        viewer: AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
    ) {
        val media = mediaViewModel.currentMedia.result
        if (media?.isEmpty() == true) return
        val headerTextRes = mediaViewModel.currentHeaderTextRes
        RowHeader(
            titleRes = headerTextRes,
            viewAllRoute = AnimeDestination.UserList(
                userId = null,
                userName = null,
                mediaType = mediaViewModel.mediaType,
                mediaListStatus = MediaListStatus.CURRENT,
            ),
        )

        SharedTransitionKeyScope("anime_home_current_media_row") {
            CurrentMediaRow(
                viewer = viewer,
                mediaResult = mediaViewModel::currentMedia,
                currentMediaPreviousSize = mediaViewModel.currentMediaPreviousSize.collectAsState().value,
                onClickListEdit = onClickListEdit,
                onClickIncrementProgress = onClickIncrementProgress,
            )
        }
    }

    @Composable
    private fun CurrentMediaRow(
        viewer: AniListViewer?,
        mediaResult: () -> LoadingResult<List<UserMediaListController.MediaEntry>>,
        currentMediaPreviousSize: Int,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .recomposeHighlighter()
        ) {
            val media = mediaResult().result
            val placeholderCount = if (media == null) currentMediaPreviousSize else 0
            items(
                data = media,
                placeholderCount = placeholderCount,
                key = { it.media.id },
                contentType = { "media" },
            ) {
                CurrentMediaCard(
                    entry = it,
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    onClickIncrementProgress = onClickIncrementProgress,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    @Composable
    private fun RowHeader(
        @StringRes titleRes: Int,
        viewAllRoute: AnimeDestination?,
    ) {
        NavigationHeader(
            titleRes = titleRes,
            viewAllRoute = viewAllRoute,
            viewAllContentDescriptionTextRes = R.string.anime_home_row_view_all_content_description,
        )
    }

    @Composable
    private fun MediaRow(
        data: AnimeHomeDataEntry.RowData,
        viewer: AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        selectedItemTracker: SelectedItemTracker,
        contentPadding: PaddingValues,
        placeholderCount: Int,
    ) {
        val (rowKey, titleRes, entries, viewAllRoute) = data
        RowHeader(
            titleRes = titleRes,
            viewAllRoute = viewAllRoute
        )

        val pagerState = rememberPagerState(data = entries, placeholderCount = 3)
        HorizontalPager(
            state = pagerState,
            contentPadding = LargeMediaPagerContentPadding,
            pageSize = FillOr450,
            pageSpacing = 8.dp,
            modifier = Modifier.recomposeHighlighter()
        ) {
            val entry = entries?.getOrNull(it)
            AnimeMediaLargeCard(
                viewer = viewer,
                entry = entry,
                shouldTransitionCoverImageIfUsed = false,
            )
        }

        selectedItemTracker.ScrollEffect(key = rowKey, pagerState = pagerState)

        val listState = rememberLazyListState()
        val snapLayoutInfoProvider = remember(listState) {
            SnapLayoutInfoProvider(listState, object : SnapPosition {
                override fun position(
                    layoutSize: Int,
                    itemSize: Int,
                    beforeContentPadding: Int,
                    afterContentPadding: Int,
                    itemIndex: Int,
                    itemCount: Int,
                ) = 0
            })
        }
        LazyRow(
            state = listState,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            modifier = Modifier.recomposeHighlighter()
        ) {
            itemsIndexed(
                data = entries,
                placeholderCount = placeholderCount,
                key = { _, item -> item.media.id },
                contentType = { _, _ -> "media" },
            ) { index, item ->
                val media = item?.media
                MediaCard(
                    media = media,
                    mediaStatusAware = item,
                    ignored = item?.ignored ?: false,
                    viewer = viewer,
                    selected = remember {
                        derivedStateOf {
                            selectedItemTracker.keyToPosition[rowKey]?.second == index
                        }
                    }.value,
                    onClickListEdit = onClickListEdit,
                    modifier = Modifier.animateItem()
                )
            }

            item("view_all") {
                val navigationCallback = LocalNavigationCallback.current
                GenericViewAllCard(onClick = {
                    navigationCallback.navigate(viewAllRoute)
                })
            }
        }

        selectedItemTracker.ScrollEffect(key = rowKey, listState = listState)
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

        val coverImageState =
            rememberCoilImageState(media?.coverImage?.extraLarge, heightStartThreshold = 3 / 4f)
        val colors = coverImageState.colors
        val containerColor = colors.containerColor.takeOrElse {
            media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
                ?: MaterialTheme.colorScheme.surface
        }

        val navigationCallback = LocalNavigationCallback.current
        val title = media?.title?.primaryTitle()
        val sharedTransitionKey = SharedTransitionKey.makeKeyForId(media?.id.toString())

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            onClick = {
                if (media != null) {
                    navigationCallback.navigate(
                        AnimeDestination.MediaDetails(
                            mediaId = media.id.toString(),
                            title = title,
                            coverImage = coverImageState.toImageState(),
                            sharedTransitionKey = sharedTransitionKey,
                            headerParams = MediaHeaderParams(
                                coverImage = coverImageState.toImageState(),
                                title = title,
                                media = media,
                            ),
                        )
                    )
                }
            },
            modifier = modifier
                .widthIn(max = CURRENT_ROW_IMAGE_WIDTH)
                .clip(RoundedCornerShape(12.dp))
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
                .padding(2.dp)
        ) {
            CurrentMediaCardContent(
                entry = entry,
                sharedTransitionKey = sharedTransitionKey,
                viewer = viewer,
                textColor = ComposeColorUtils.bestTextColor(containerColor),
                onClickListEdit = onClickListEdit,
                onClickIncrementProgress = onClickIncrementProgress,
                coverImageState = coverImageState,
            )
        }
    }

    @Suppress("UnusedReceiverParameter")
    @Composable
    private fun ColumnScope.CurrentMediaCardContent(
        entry: UserMediaListController.MediaEntry?,
        sharedTransitionKey: SharedTransitionKey,
        viewer: AniListViewer?,
        textColor: Color?,
        onClickListEdit: (MediaPreview) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
        coverImageState: CoilImageState,
    ) {
        val media = entry?.media
        Box(modifier = Modifier.recomposeHighlighter()) {
            val density = LocalDensity.current
            val coilWidth = Dimension.Pixels(
                density.run { CURRENT_ROW_IMAGE_WIDTH.roundToPx() / 4 * 3 }
            )
            val coilHeight = Dimension.Pixels(
                density.run { CURRENT_ROW_IMAGE_HEIGHT.roundToPx() / 4 * 3 }
            )
            val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
            MediaCoverImage(
                imageState = coverImageState,
                image = coverImageState.request()
                    .size(width = coilWidth, height = coilHeight)
                    .build(),
                modifier = Modifier
                    .sharedElement(sharedContentState)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .size(
                        width = CURRENT_ROW_IMAGE_WIDTH,
                        height = CURRENT_ROW_IMAGE_HEIGHT
                    )
                    .animateContentSize()
                    .placeholder(
                        visible = media == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
                contentScale = ContentScale.Crop,
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
                    modifier = Modifier
                        .animateSharedTransitionWithOtherState(sharedContentState)
                        .align(Alignment.BottomStart)
                )

                if ((entry.progress ?: 0) < (maxProgress ?: 1)) {
                    IconButton(
                        onClick = { onClickIncrementProgress(entry) },
                        modifier = Modifier
                            .animateSharedTransitionWithOtherState(sharedContentState)
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
            color = textColor ?: Color.Unspecified,
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

    @Composable
    private fun MediaCard(
        media: HomeMedia?,
        mediaStatusAware: MediaStatusAware?,
        ignored: Boolean,
        viewer: AniListViewer?,
        selected: Boolean,
        onClickListEdit: (MediaNavigationData) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        val title = MediaUtils.userPreferredTitle(
            userPreferred = media?.title?.userPreferred,
            romaji = media?.title?.romaji,
            english = media?.title?.english,
            native = media?.title?.native,
        )

        val fullscreenImageHandler = LocalFullscreenImageHandler.current
        val sharedTransitionKey =
            media?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
        val coverImageState =
            rememberCoilImageState(media?.coverImage?.extraLarge, heightStartThreshold = 3 / 4f)
        val containerColor = coverImageState.colors.containerColor.takeOrElse {
            media?.coverImage?.color?.let(ComposeColorUtils::hexToColor)
                ?: MaterialTheme.colorScheme.surface
        }

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
            modifier = modifier
                .height(MEDIA_ROW_IMAGE_HEIGHT)
                .widthIn(min = MEDIA_ROW_IMAGE_WIDTH)
                .sharedElement(sharedTransitionKey, "media_image")
                .combinedClickable(
                    onClick = {
                        if (media != null) {
                            navigationCallback.navigate(
                                AnimeDestination.MediaDetails(
                                    mediaId = media.id.toString(),
                                    title = title,
                                    coverImage = coverImageState.toImageState(),
                                    headerParams = MediaHeaderParams(
                                        coverImage = coverImageState.toImageState(),
                                        title = title,
                                        media = media,
                                    ),
                                    sharedTransitionKey = sharedTransitionKey,
                                )
                            )
                        }
                    },
                    onLongClick = {
                        media?.coverImage?.extraLarge?.let(fullscreenImageHandler::openImage)
                    },
                )
                .alpha(if (ignored) 0.38f else 1f)
                .conditionally(selected) {
                    border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp),
                    )
                }
                .padding(2.dp)
        ) {
            MediaCardContent(
                media = media,
                mediaStatusAware = mediaStatusAware,
                viewer = viewer,
                onClickListEdit = onClickListEdit,
                textColor = ComposeColorUtils.bestTextColor(containerColor),
                title = title,
                coverImageState = coverImageState,
            )
        }
    }

    @Composable
    private fun MediaCardContent(
        media: HomeMedia?,
        mediaStatusAware: MediaStatusAware?,
        viewer: AniListViewer?,
        textColor: Color?,
        title: String?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        coverImageState: CoilImageState,
    ) {
        Box(modifier = Modifier.recomposeHighlighter()) {
            var showTitle by remember(media) { mutableStateOf(false) }
            val density = LocalDensity.current
            val coilWidth = Dimension.Pixels(
                density.run { MEDIA_ROW_IMAGE_WIDTH.roundToPx() / 2 }
            )
            val coilHeight = Dimension.Pixels(
                density.run { MEDIA_ROW_IMAGE_HEIGHT.roundToPx() / 2 }
            )

            CoilImage(
                state = coverImageState,
                model = coverImageState.request()
                    .size(width = coilWidth, height = coilHeight)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                onError = { showTitle = true },
                modifier = Modifier
                    .fillMaxSize()
                    .sizeIn(maxWidth = MEDIA_ROW_IMAGE_WIDTH, maxHeight = MEDIA_ROW_IMAGE_HEIGHT)
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
                    color = textColor ?: Color.Unspecified,
                    modifier = Modifier
                        .size(
                            width = MEDIA_ROW_IMAGE_WIDTH,
                            height = MEDIA_ROW_IMAGE_HEIGHT,
                        )
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
                        onClickListEdit(object : MediaNavigationData {
                            override val id = media.id
                            override val title = object : MediaNavigationData.Title {
                                override val __typename = "Default"
                                override val userPreferred = media.title?.userPreferred
                                override val romaji = media.title?.romaji
                                override val english = media.title?.english
                                override val native = media.title?.native

                            }
                            override val coverImage =
                                object : MediaNavigationData.CoverImage {
                                    override val extraLarge = media.coverImage?.extraLarge

                                }
                            override val type = media.type
                            override val isAdult = media.isAdult

                        })
                    },
                    modifier = Modifier
                        .animateEnterExit()
                        .align(Alignment.BottomStart)
                )
            }
        }
    }

    @Composable
    private fun Recommendations(
        viewModel: AnimeHomeViewModel,
        editViewModel: MediaEditViewModel,
        viewer: AniListViewer?,
        recommendations: LazyPagingItems<RecommendationEntry>,
        pageSize: PageSize,
    ) {
        RowHeader(
            titleRes = R.string.anime_recommendations_home_title,
            viewAllRoute = AnimeDestination.Recommendations,
        )

        val pagerState = rememberPagerState(data = recommendations, placeholderCount = 3)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            pageSpacing = 16.dp,
            pageSize = pageSize,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.recomposeHighlighter()
        ) {
            val entry = recommendations.getOrNull(it)
            SharedTransitionKeyScope("anime_home_recommendation_card_${entry?.id}") {
                RecommendationCard(
                    viewer = viewer,
                    user = entry?.user,
                    media = entry?.media,
                    mediaRecommendation = entry?.mediaRecommendation,
                    onClickListEdit = editViewModel::initialize,
                    recommendation = entry?.data,
                    onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
                )
            }
        }
    }

    @Composable
    private fun Reviews(
        viewer: AniListViewer?,
        reviews: LazyPagingItems<ReviewEntry>,
        pageSize: PageSize,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        RowHeader(
            titleRes = R.string.anime_reviews_home_title,
            viewAllRoute = AnimeDestination.Reviews,
        )

        val pagerState = rememberPagerState(data = reviews, placeholderCount = 3)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            pageSpacing = 16.dp,
            pageSize = pageSize,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.recomposeHighlighter()
        ) {
            val entry = reviews.getOrNull(it)
            val mediaTitle = entry?.media?.media?.title?.primaryTitle()
            SharedTransitionKeyScope("anime_home_review_${entry?.review?.id}") {
                val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
                ReviewCard(
                    viewer = viewer,
                    review = entry?.review,
                    media = entry?.media,
                    onClick = { navigationCallback, coverImageState ->
                        if (entry != null) {
                            navigationCallback.navigate(
                                AnimeDestination.ReviewDetails(
                                    reviewId = entry.review.id.toString(),
                                    sharedTransitionScopeKey = sharedTransitionScopeKey,
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImage = coverImageState.toImageState(),
                                        mediaCompactWithTags = entry.media.media,
                                        favorite = null,
                                    )
                                )
                            )
                        }
                    },
                    onClickListEdit = onClickListEdit,
                )
            }
        }
    }

    @Composable
    private fun Suggestions(
        mediaViewModel: AnimeHomeMediaViewModel,
    ) {
        RowHeader(
            titleRes = R.string.anime_home_suggestions_header,
            viewAllRoute = null,
        )

        val navigationCallback = LocalNavigationCallback.current
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .recomposeHighlighter()
        ) {
            items(
                items = mediaViewModel.suggestions,
                key = { "${mediaViewModel.mediaType}-suggestion-${it.second}" },
                contentType = { "suggestion" },
            ) {
                SuggestionChip(
                    onClick = { navigationCallback.navigate(it.second) },
                    label = { Text(stringResource(it.first)) },
                    modifier = Modifier.recomposeHighlighter()
                )
            }
        }
    }

    class SelectedItemTracker {
        val keyToPosition = mutableStateMapOf<String, Pair<Boolean, Int>>()

        @Composable
        fun ScrollEffect(key: String, pagerState: PagerState) {
            val settledPage = pagerState.settledPage
            val sourceAndPosition by remember { derivedStateOf { keyToPosition[key] } }
            LaunchedEffect(settledPage) {
                val currentPage = sourceAndPosition?.second
                if (currentPage == null || currentPage != settledPage) {
                    keyToPosition[key] = true to settledPage
                }
            }

            ScrollEffect(pagerState = pagerState, sourceAndPosition = sourceAndPosition)
        }

        @Composable
        private fun ScrollEffect(pagerState: PagerState, sourceAndPosition: Pair<Boolean, Int>?) {
            LaunchedEffect(sourceAndPosition) {
                if (sourceAndPosition != null) {
                    if (!sourceAndPosition.first) {
                        pagerState.animateScrollToPage(sourceAndPosition.second)
                    }
                }
            }
        }

        @Composable
        fun ScrollEffect(key: String, listState: LazyListState) {
            val firstItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
            val isScrollInProgress = listState.isScrollInProgress
            val sourceAndPosition by remember { derivedStateOf { keyToPosition[key] } }
            LaunchedEffect(isScrollInProgress, firstItemIndex) {
                if (!isScrollInProgress) {
                    val currentIndex = sourceAndPosition?.second
                    if (currentIndex == null || currentIndex != firstItemIndex) {
                        keyToPosition[key] = false to firstItemIndex
                    }
                }
            }

            ScrollEffect(listState = listState, sourceAndPosition = sourceAndPosition)
        }

        @Composable
        private fun ScrollEffect(listState: LazyListState, sourceAndPosition: Pair<Boolean, Int>?) {
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
