package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.NotificationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
object NotificationsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.NOTIFICATIONS.id

    @Composable
    operator fun invoke(
        viewModel: NotificationsViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        val viewer by viewModel.viewer.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_notifications_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                val content = viewModel.content.collectAsLazyPagingItems()
                val refreshing = content.loadState.refresh is LoadState.Loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = { content.refresh() },
                )
                when (val refreshState = content.loadState.refresh) {
                    is LoadState.Error -> AnimeMediaListScreen.Error(
                        exception = refreshState.error,
                        modifier = Modifier.pullRefresh(state = pullRefreshState)
                    )
                    else -> {
                        if (content.itemCount == 0
                            && content.loadState.refresh is LoadState.NotLoading
                        ) {
                            AnimeMediaListScreen.NoResults(
                                modifier = Modifier.pullRefresh(state = pullRefreshState)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 72.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.pullRefresh(state = pullRefreshState)
                            ) {
                                items(
                                    count = content.itemCount,
                                    key = content.itemKey { it.notificationId.scopedId },
                                    contentType = content.itemContentType { it.notificationId.type }
                                ) {
                                    val entry = content[it]
                                    when (val notification = entry?.notification) {
                                        is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification ->
                                            ActivityMentionNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.ActivityMessageNotificationNotification ->
                                            ActivityMessageNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.AiringNotificationNotification ->
                                            AiringNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.FollowingNotificationNotification ->
                                            FollowingNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                            )
                                        is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification ->
                                            ActivityReplyNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification ->
                                            ActivityReplySubscribedNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification ->
                                            ActivityLikedNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification ->
                                            ActivityReplyLikedNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                viewer = viewer,
                                                notification = notification,
                                                activityEntry = entry.activityEntry,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification ->
                                            RelatedMediaAdditionNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification ->
                                            MediaDataChangeNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.MediaDeletionNotificationNotification ->
                                            MediaDeletionNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.MediaMergeNotificationNotification ->
                                            MediaMergeNotificationCard(
                                                screenKey = SCREEN_KEY,
                                                notification = notification,
                                                mediaEntry = entry.mediaEntry?.rowEntry,
                                                colorCalculationState = colorCalculationState,
                                            )
                                        is NotificationsQuery.Data.Page.OtherNotification,
                                        null -> NotificationPlaceholderCard()
                                    }
                                }

                                when (content.loadState.append) {
                                    is LoadState.Loading -> item("load_more_append") {
                                        AnimeMediaListScreen.LoadingMore()
                                    }
                                    is LoadState.Error -> item("load_more_error") {
                                        AnimeMediaListScreen.AppendError { content.retry() }
                                    }
                                    is LoadState.NotLoading -> Unit
                                }
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}
