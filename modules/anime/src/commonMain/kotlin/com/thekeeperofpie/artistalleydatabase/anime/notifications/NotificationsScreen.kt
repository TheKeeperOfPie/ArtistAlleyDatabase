package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_notifications_title
import com.anilist.data.NotificationsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object NotificationsScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: NotificationsViewModel = viewModel { animeComponent.notificationsViewModel() },
        upIconOption: UpIconOption?,
    ) {
        val viewer by viewModel.viewer.collectAsState()
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            viewModel = editViewModel,
            topBar = {
                AppBar(
                    text = stringResource(Res.string.anime_notifications_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            }
        ) {
            val content = viewModel.content.collectAsLazyPagingItems()

            VerticalList(
                itemHeaderText = null,
                items = content,
                itemKey = { it.notificationId.scopedId },
                itemContentType = { it.notificationId.type },
                onRefresh = content::refresh,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 72.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(it)
            ) {

                SharedTransitionKeyScope(
                    "notification",
                    it?.notificationId?.scopedId
                ) {
                    when (val notification = it?.notification) {
                        is NotificationsQuery.Data.Page.ActivityMentionNotificationNotification ->
                            ActivityMentionNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                activityEntry = it.activityEntry,
                                mediaEntry = it.mediaEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.ActivityMessageNotificationNotification ->
                            ActivityMessageNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                activityEntry = it.activityEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.AiringNotificationNotification ->
                            AiringNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                mediaEntry = it.mediaEntry,
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
                                activityEntry = it.activityEntry,
                                mediaEntry = it.mediaEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification ->
                            ActivityReplySubscribedNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                activityEntry = it.activityEntry,
                                mediaEntry = it.mediaEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification ->
                            ActivityLikedNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                activityEntry = it.activityEntry,
                                mediaEntry = it.mediaEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification ->
                            ActivityReplyLikedNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                activityEntry = it.activityEntry,
                                mediaEntry = it.mediaEntry,
                                onActivityStatusUpdate = viewModel.activityToggleHelper::toggle,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification ->
                            RelatedMediaAdditionNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                mediaEntry = it.mediaEntry,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification ->
                            MediaDataChangeNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                mediaEntry = it.mediaEntry,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.MediaDeletionNotificationNotification ->
                            MediaDeletionNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                mediaEntry = it.mediaEntry,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.MediaMergeNotificationNotification ->
                            MediaMergeNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                mediaEntry = it.mediaEntry,
                                onClickListEdit = editViewModel::initialize,
                            )
                        is NotificationsQuery.Data.Page.ThreadCommentMentionNotificationNotification ->
                            ThreadCommentMentionNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                entry = it.commentEntry,
                                onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                            )
                        is NotificationsQuery.Data.Page.ThreadCommentLikeNotificationNotification ->
                            ThreadCommentLikeNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                entry = it.commentEntry,
                                onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                            )
                        is NotificationsQuery.Data.Page.ThreadCommentReplyNotificationNotification ->
                            ThreadCommentReplyNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                entry = it.commentEntry,
                                onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                            )
                        is NotificationsQuery.Data.Page.ThreadCommentSubscribedNotificationNotification ->
                            ThreadCommentSubscribedNotificationCard(
                                viewer = viewer,
                                notification = notification,
                                entry = it.commentEntry,
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
        }
    }
}
