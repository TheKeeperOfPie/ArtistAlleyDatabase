package com.thekeeperofpie.artistalleydatabase.anime.home

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
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
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
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_forum_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_home_activity_label
import artistalleydatabase.modules.anime.generated.resources.anime_home_label_anime
import artistalleydatabase.modules.anime.generated.resources.anime_home_label_manga
import artistalleydatabase.modules.anime.generated.resources.anime_home_media_increment_progress_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_home_media_type_switch_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_home_row_view_all_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_home_suggestions_header
import artistalleydatabase.modules.anime.generated.resources.anime_media_cover_image_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_notifications_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_recommendations_home_title
import artistalleydatabase.modules.anime.generated.resources.anime_reviews_home_title
import com.anilist.data.fragment.HomeMedia
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.RecommendationRating
import com.anilist.data.type.ScoreFormat
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.data.MediaFilterable
import com.thekeeperofpie.artistalleydatabase.anime.home.AnimeHomeMediaViewModel.CurrentMediaState
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaHeaderParams
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditState
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaListQuickEditIconButton
import com.thekeeperofpie.artistalleydatabase.anime.news.AnimeNewsEntry
import com.thekeeperofpie.artistalleydatabase.anime.news.NewsRow
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationCard
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationEntry
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewCard
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.ui.GenericViewAllCard
import com.thekeeperofpie.artistalleydatabase.anime.ui.MediaCoverImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoResizeHeightText
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.HorizontalPagerItemsRow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.items
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemsIndexed
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.rememberPagerState
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: AnimeHomeViewModel = viewModel { animeComponent.animeHomeViewModel() },
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState?,
        mediaViewModel: @Composable (Boolean) -> AnimeHomeMediaViewModel = {
            if (it) {
                viewModel { animeComponent.animeHomeMediaViewModelAnime() }
            } else {
                viewModel { animeComponent.animeHomeMediaViewModelManga() }
            }
        },
    ) {
        var selectedIsAnime by rememberSaveable {
            mutableStateOf(viewModel.preferredMediaType == MediaType.ANIME)
        }
        val mediaViewModel = mediaViewModel(selectedIsAnime)
        val unlocked by viewModel.unlocked.collectAsState()
        val viewer by viewModel.viewer.collectAsState()
        val currentMediaState = mediaViewModel.currentMediaState()
        val news by viewModel.newsController.newsDateDescending.collectAsState()
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        val initialParams by editViewModel.initialParams.collectAsState()
        val scoreFormat by editViewModel.scoreFormat.collectAsState()
        AnimeHomeScreen(
            upIconOption = upIconOption,
            scrollStateSaver = scrollStateSaver,
            bottomNavigationState = bottomNavigationState,
            selectedIsAnime = selectedIsAnime,
            onSelectedIsAnimeChanged = { selectedIsAnime = it },
            onRefresh = {
                viewModel.refresh()
                mediaViewModel.refresh()
            },
            activity = viewModel.activity.collectAsLazyPagingItems(),
            recommendations = viewModel.recommendations.collectAsLazyPagingItems(),
            reviews = mediaViewModel.reviews.collectAsLazyPagingItems(),
            news = { news },
            homeEntry = { mediaViewModel.entry },
            currentMedia = { mediaViewModel.currentMedia },
            currentMediaState = { currentMediaState },
            suggestions = mediaViewModel.suggestions,
            notificationsUnreadCount = { viewModel.notificationsController.unreadCount },
            unlocked = { unlocked },
            viewer = { viewer },
            onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
            onUserRecommendationRating = viewModel.recommendationToggleHelper::toggle,
            editData = editViewModel.editData,
            onEditSheetValueChange = editViewModel::onEditSheetValueChange,
            onHide = editViewModel::hide,
            onAttemptDismiss = editViewModel::attemptDismiss,
            initialParams = { initialParams },
            onClickSave = editViewModel::onClickSave,
            onClickDelete = editViewModel::onClickDelete,
            onStatusChange = editViewModel::onStatusChange,
            scoreFormat = { scoreFormat },
            onDateChange = editViewModel::onDateChange,
            onClickListEdit = editViewModel::initialize,
            onClickIncrementProgress = editViewModel::incrementProgress,
        )
    }

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption?,
        scrollStateSaver: ScrollStateSaver,
        bottomNavigationState: BottomNavigationState?,
        selectedIsAnime: Boolean,
        onSelectedIsAnimeChanged: (Boolean) -> Unit,
        onRefresh: () -> Unit,
        activity: LazyPagingItems<ActivityEntry>,
        recommendations: LazyPagingItems<RecommendationEntry>,
        reviews: LazyPagingItems<ReviewEntry>,
        news: () -> LoadingResult<List<AnimeNewsEntry<*>>>,
        homeEntry: () -> LoadingResult<AnimeHomeDataEntry>,
        currentMedia: () -> LoadingResult<List<UserMediaListController.MediaEntry>>,
        currentMediaState: () -> CurrentMediaState,
        suggestions: List<Pair<StringResource, AnimeDestination>>,
        notificationsUnreadCount: () -> Int,
        unlocked: () -> Boolean,
        viewer: () -> AniListViewer?,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        onUserRecommendationRating: (recommendation: RecommendationData, newRating: RecommendationRating) -> Unit = { _, _ -> },
        editData: MediaEditState,
        onEditSheetValueChange: (SheetValue) -> Boolean,
        onHide: () -> Unit,
        onAttemptDismiss: () -> Boolean,
        initialParams: () -> MediaEditState.InitialParams?,
        onClickSave: () -> Unit,
        onClickDelete: () -> Unit,
        onStatusChange: (MediaListStatus?) -> Unit,
        scoreFormat: () -> ScoreFormat,
        onDateChange: (start: Boolean, Long?) -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
    ) {
        val refreshing by remember {
            derivedStateOf {
                news().loading
                        || activity.loadState.refresh == LoadState.Loading
                        || recommendations.loadState.refresh == LoadState.Loading
                        || reviews.loadState.refresh == LoadState.Loading
                        || homeEntry().loading
                        || currentMedia().loading
            }
        }
        val pullRefreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = onRefresh,
        )

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null)
        MediaEditBottomSheetScaffold(
            editData = editData,
            onEditSheetValueChange = onEditSheetValueChange,
            onHide = onHide,
            onAttemptDismiss = onAttemptDismiss,
            initialParams = initialParams,
            onClickSave = onClickSave,
            onClickDelete = onClickDelete,
            onStatusChange = onStatusChange,
            scoreFormat = scoreFormat,
            onDateChange = onDateChange,
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    upIconOption = upIconOption,
                    selectedIsAnime = selectedIsAnime,
                    onSelectedIsAnimeChange = onSelectedIsAnimeChanged,
                    unlocked = unlocked,
                    viewer = viewer,
                    unreadCount = notificationsUnreadCount,
                )
            },
            bottomNavigationState = bottomNavigationState,
            modifier = Modifier
                .conditionallyNonNull(bottomNavigationState) {
                    nestedScroll(it.nestedScrollConnection)
                }
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(pullRefreshState)
            ) {
                Content(
                    scrollState = scrollStateSaver.scrollState(),
                    bottomNavBarPadding = bottomNavigationState?.bottomNavBarPadding() ?: 0.dp,
                    viewer = viewer,
                    news = news,
                    homeEntry = homeEntry,
                    currentMediaState = currentMediaState,
                    suggestions = suggestions,
                    onClickListEdit = onClickListEdit,
                    onClickIncrementProgress = onClickIncrementProgress,
                    recommendations = recommendations,
                    reviews = reviews,
                    activities = activity,
                    onActivityStatusUpdate = onActivityStatusUpdate,
                    onUserRecommendationRating = onUserRecommendationRating,
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
                                Res.string.anime_home_label_anime
                            } else {
                                Res.string.anime_home_label_manga
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
                                Res.string.anime_home_media_type_switch_icon_content_description
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
                                    Res.string.anime_forum_icon_content_description
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
                                        Res.string.anime_notifications_icon_content_description
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
                                Res.string.anime_airing_schedule_icon_content_description
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
        viewer: () -> AniListViewer?,
        news: () -> LoadingResult<List<AnimeNewsEntry<*>>>,
        homeEntry: () -> LoadingResult<AnimeHomeDataEntry>,
        activities: LazyPagingItems<ActivityEntry>,
        currentMediaState: () -> CurrentMediaState,
        suggestions: List<Pair<StringResource, AnimeDestination>>,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
        recommendations: LazyPagingItems<RecommendationEntry>,
        onUserRecommendationRating: (recommendation: RecommendationData, newRating: RecommendationRating) -> Unit = { _, _ -> },
        reviews: LazyPagingItems<ReviewEntry>,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("homeColumn")
                .verticalScroll(scrollState)
        ) {
            val configuration = LocalWindowConfiguration.current
            val screenWidthDp = configuration.screenWidthDp
            val pageSize = remember(screenWidthDp) {
                PageSize.Fixed(420.dp.coerceAtMost(screenWidthDp - 32.dp))
            }
            NewsRow(result = news, pageSize = pageSize)

            Activities(
                viewer = viewer,
                activities = activities,
                onActivityStatusUpdate = onActivityStatusUpdate,
                onClickListEdit = onClickListEdit,
            )

            CurrentMediaRow(
                currentMediaState = currentMediaState,
                viewer = viewer,
                onClickListEdit = onClickListEdit,
                onClickIncrementProgress = onClickIncrementProgress,
            )

            Recommendations(
                viewer = viewer,
                recommendations = recommendations,
                onUserRecommendationRating = onUserRecommendationRating,
                onClickListEdit = onClickListEdit,
            )

            MediaRows(homeEntry = homeEntry, viewer = viewer, onClickListEdit = onClickListEdit)

            Reviews(
                viewer = viewer,
                reviews = reviews,
                onClickListEdit = onClickListEdit,
            )

            Suggestions(
                mediaType = currentMediaState().mediaType,
                suggestions = suggestions,
            )

            Spacer(modifier = Modifier.height(32.dp + bottomNavBarPadding))
        }
    }

    @Composable
    private fun Activities(
        viewer: () -> AniListViewer?,
        activities: LazyPagingItems<ActivityEntry>,
        onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        HorizontalPagerItemsRow(
            title = Res.string.anime_home_activity_label,
            viewAllRoute = AnimeDestination.Activity,
            viewAllContentDescription = Res.string.anime_home_row_view_all_content_description,
            items = activities,
        ) {
            SharedTransitionKeyScope("anime_home_activity_${it?.activityId?.valueId}") {
                ActivitySmallCard(
                    viewer = viewer(),
                    activity = it?.activity,
                    mediaEntry = it?.media,
                    entry = it,
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
        currentMediaState: () -> CurrentMediaState,
        viewer: () -> AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
    ) {
        val currentMediaState = currentMediaState()
        val media = currentMediaState.result.result
        if (media?.isEmpty() == true) return
        RowHeader(
            titleRes = currentMediaState.headerTextRes,
            viewAllRoute = AnimeDestination.UserList(
                userId = null,
                userName = null,
                mediaType = currentMediaState.mediaType,
                mediaListStatus = MediaListStatus.CURRENT,
            ),
        )

        SharedTransitionKeyScope("anime_home_current_media_row") {
            CurrentMediaRow(
                viewer = viewer,
                mediaResult = { currentMediaState.result },
                currentMediaPreviousSize = currentMediaState.previousSize,
                onClickListEdit = onClickListEdit,
                onClickIncrementProgress = onClickIncrementProgress,
            )
        }
    }

    @Composable
    private fun CurrentMediaRow(
        viewer: () -> AniListViewer?,
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
        titleRes: StringResource,
        viewAllRoute: AnimeDestination?,
    ) {
        NavigationHeader(
            titleRes = titleRes,
            viewAllRoute = viewAllRoute,
            viewAllContentDescriptionTextRes = Res.string.anime_home_row_view_all_content_description,
        )
    }

    @Composable
    private fun MediaRow(
        data: AnimeHomeDataEntry.RowData,
        viewer: () -> AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        selectedItemTracker: SelectedItemTracker,
        contentPadding: PaddingValues,
        placeholderCount: Int,
    ) {
        val (rowKey, titleRes, entries, viewAllRoute) = data
        val pagerState = rememberPagerState(data = entries, placeholderCount = 3)
        HorizontalPagerItemsRow(
            title = titleRes,
            viewAllRoute = viewAllRoute,
            viewAllContentDescription = Res.string.anime_home_row_view_all_content_description,
            items = entries,
            contentPadding = LargeMediaPagerContentPadding,
            pageSize = FillOr450,
            pagerState = pagerState,
        ) {
            AnimeMediaLargeCard(
                viewer = viewer(),
                entry = it,
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
                    mediaFilterable = item?.mediaFilterable,
                    ignored = item?.ignored == true,
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
        viewer: () -> AniListViewer?,
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
                .alpha(if (entry?.mediaFilterable?.ignored == true) 0.38f else 1f)
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
        viewer: () -> AniListViewer?,
        textColor: Color?,
        onClickListEdit: (MediaPreview) -> Unit,
        onClickIncrementProgress: (UserMediaListController.MediaEntry) -> Unit,
        coverImageState: CoilImageState,
    ) {
        val media = entry?.media
        Box(modifier = Modifier.recomposeHighlighter()) {
            val sharedContentState = rememberSharedContentState(sharedTransitionKey, "media_image")
            MediaCoverImage(
                imageState = coverImageState,
                image = coverImageState.request().build(),
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

            val viewer = viewer()
            if (viewer != null && media != null) {
                val maxProgress = MediaUtils.maxProgress(media)
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = media.type,
                    media = entry.mediaFilterable,
                    maxProgress = maxProgress,
                    maxProgressVolumes = media.volumes,
                    onClick = { onClickListEdit(media) },
                    iconSize = 12.dp,
                    textVerticalPadding = 2.dp,
                    modifier = Modifier
                        .animateSharedTransitionWithOtherState(sharedContentState)
                        .align(Alignment.BottomStart)
                )

                val progress = when (media.type) {
                    MediaType.ANIME -> entry.mediaFilterable.progress
                    MediaType.MANGA -> entry.mediaFilterable.progressVolumes
                    MediaType.UNKNOWN__,
                    null,
                        -> 0
                } ?: 0
                if (progress < (maxProgress ?: 1)) {
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
                                Res.string.anime_home_media_increment_progress_content_description
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
        mediaFilterable: MediaFilterable?,
        ignored: Boolean,
        viewer: () -> AniListViewer?,
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
                mediaFilterable = mediaFilterable,
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
        mediaFilterable: MediaFilterable?,
        viewer: () -> AniListViewer?,
        textColor: Color?,
        title: String?,
        onClickListEdit: (MediaNavigationData) -> Unit,
        coverImageState: CoilImageState,
    ) {
        Box(modifier = Modifier.recomposeHighlighter()) {
            var showTitle by remember(media) { mutableStateOf(false) }

            // TODO: Size constraints removed because it doesn't work on desktop
            CoilImage(
                state = coverImageState,
                model = coverImageState.request().build(),
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(Res.string.anime_media_cover_image_content_description),
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

            val viewer = viewer()
            if (viewer != null && media != null && mediaFilterable != null) {
                MediaListQuickEditIconButton(
                    viewer = viewer,
                    mediaType = media.type,
                    media = mediaFilterable,
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
        viewer: () -> AniListViewer?,
        recommendations: LazyPagingItems<RecommendationEntry>,
        onUserRecommendationRating: (recommendation: RecommendationData, newRating: RecommendationRating) -> Unit = { _, _ -> },
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        HorizontalPagerItemsRow(
            title = Res.string.anime_recommendations_home_title,
            viewAllRoute = AnimeDestination.Recommendations,
            viewAllContentDescription = Res.string.anime_home_row_view_all_content_description,
            items = recommendations,
        ) {
            SharedTransitionKeyScope("anime_home_recommendation_card_${it?.id}") {
                RecommendationCard(
                    viewer = viewer(),
                    user = it?.user,
                    media = it?.media,
                    mediaRecommendation = it?.mediaRecommendation,
                    onClickListEdit = onClickListEdit,
                    recommendation = it?.data,
                    onUserRecommendationRating = onUserRecommendationRating,
                )
            }
        }
    }

    @Composable
    private fun MediaRows(
        homeEntry: () -> LoadingResult<AnimeHomeDataEntry>,
        viewer: () -> AniListViewer?,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        val selectedItemTracker = remember { SelectedItemTracker() }
        val configuration = LocalWindowConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val contentPadding = PaddingValues(
            start = 16.dp,
            end = (screenWidthDp - MEDIA_ROW_IMAGE_WIDTH).let {
                it - 16.dp - MEDIA_ROW_IMAGE_WIDTH
            }.coerceAtLeast(0.dp),
        )
        val placeholderCount = (screenWidthDp / (MEDIA_ROW_IMAGE_WIDTH + 16.dp)).toInt()
            .coerceAtLeast(1) + 1
        homeEntry().result?.lists?.forEach {
            SharedTransitionKeyScope("anime_home_media_list_row_${it.id}") {
                MediaRow(
                    data = it,
                    viewer = viewer,
                    onClickListEdit = onClickListEdit,
                    selectedItemTracker = selectedItemTracker,
                    contentPadding = contentPadding,
                    placeholderCount = placeholderCount,
                )
            }
        }
    }

    @Composable
    private fun Reviews(
        viewer: () -> AniListViewer?,
        reviews: LazyPagingItems<ReviewEntry>,
        onClickListEdit: (MediaNavigationData) -> Unit,
    ) {
        HorizontalPagerItemsRow(
            title = Res.string.anime_reviews_home_title,
            viewAllRoute = AnimeDestination.Reviews,
            viewAllContentDescription = Res.string.anime_home_row_view_all_content_description,
            items = reviews,
        ) {
            val mediaTitle = it?.media?.media?.title?.primaryTitle()
            SharedTransitionKeyScope("anime_home_review_${it?.review?.id}") {
                val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
                ReviewCard(
                    viewer = viewer(),
                    review = it?.review,
                    media = it?.media,
                    onClick = { navigationCallback, coverImageState ->
                        if (it != null) {
                            navigationCallback.navigate(
                                AnimeDestination.ReviewDetails(
                                    reviewId = it.review.id.toString(),
                                    sharedTransitionScopeKey = sharedTransitionScopeKey,
                                    headerParams = MediaHeaderParams(
                                        title = mediaTitle,
                                        coverImage = coverImageState.toImageState(),
                                        mediaCompactWithTags = it.media.media,
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
        mediaType: MediaType,
        suggestions: List<Pair<StringResource, AnimeDestination>>,
    ) {
        RowHeader(
            titleRes = Res.string.anime_home_suggestions_header,
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
                items = suggestions,
                key = { "${mediaType}-suggestion-${it.second}" },
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
