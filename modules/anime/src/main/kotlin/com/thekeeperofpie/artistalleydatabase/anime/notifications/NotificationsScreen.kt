package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
object NotificationsScreen {

    @Composable
    operator fun invoke(
        viewModel: NotificationsViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
    ) {
        val viewer by viewModel.viewer.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            viewModel = editViewModel,
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_notifications_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            }
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
                                    SharedTransitionKeyScope(
                                        "notification",
                                        entry?.notificationId?.scopedId
                                    ) {
                                        when (val notification = entry?.notification) {
                                            is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification ->
                                                ActivityMentionNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    mediaEntry = entry.mediaEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.ActivityMessageNotificationNotification ->
                                                ActivityMessageNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.AiringNotificationNotification ->
                                                AiringNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    mediaEntry = entry.mediaEntry,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.FollowingNotificationNotification ->
                                                FollowingNotificationCard(
                                                    notification = notification,
                                                )
                                            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification ->
                                                ActivityReplyNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    mediaEntry = entry.mediaEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification ->
                                                ActivityReplySubscribedNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    mediaEntry = entry.mediaEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification ->
                                                ActivityLikedNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    mediaEntry = entry.mediaEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification ->
                                                ActivityReplyLikedNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    activityEntry = entry.activityEntry,
                                                    mediaEntry = entry.mediaEntry,
                                                    onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification ->
                                                RelatedMediaAdditionNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    mediaEntry = entry.mediaEntry,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification ->
                                                MediaDataChangeNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    mediaEntry = entry.mediaEntry,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.MediaDeletionNotificationNotification ->
                                                MediaDeletionNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    mediaEntry = entry.mediaEntry,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.MediaMergeNotificationNotification ->
                                                MediaMergeNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    mediaEntry = entry.mediaEntry,
                                                    onClickListEdit = editViewModel::initialize,
                                                )
                                            is NotificationsQuery.Data.Page.ThreadCommentMentionNotificationNotification ->
                                                ThreadCommentMentionNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    entry = entry.commentEntry,
                                                    onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                                                )
                                            is NotificationsQuery.Data.Page.ThreadCommentLikeNotificationNotification ->
                                                ThreadCommentLikeNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    entry = entry.commentEntry,
                                                    onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                                                )
                                            is NotificationsQuery.Data.Page.ThreadCommentReplyNotificationNotification ->
                                                ThreadCommentReplyNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    entry = entry.commentEntry,
                                                    onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                                                )
                                            is NotificationsQuery.Data.Page.ThreadCommentSubscribedNotificationNotification ->
                                                ThreadCommentSubscribedNotificationCard(
                                                    viewer = viewer,
                                                    notification = notification,
                                                    entry = entry.commentEntry,
                                                    onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                                                )
                                            is NotificationsQuery.Data.Page.ThreadLikeNotificationNotification ->
                                                ThreadLikeNotificationCard(
                                                    notification = notification,
                                                )
                                            is NotificationsQuery.Data.Page.OtherNotification,
                                            null,
                                            -> NotificationPlaceholderCard()
                                        }
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
