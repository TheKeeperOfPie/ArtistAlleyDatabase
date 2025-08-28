package com.thekeeperofpie.artistalleydatabase.anime.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import artistalleydatabase.modules.anime.notifications.generated.resources.Res
import artistalleydatabase.modules.anime.notifications.generated.resources.anime_notifications_title
import com.anilist.data.NotificationMediaAndActivityQuery
import com.anilist.data.NotificationsQuery
import com.anilist.data.fragment.ForumThread
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsByIdRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.ActivityDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadCommentRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object NotificationsScreen {

    @Composable
    operator fun <MediaEntry, ForumCommentEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        viewer: () -> AniListViewer?,
        upIconOption: UpIconOption?,
        content: LazyPagingItems<NotificationEntry<MediaEntry, ForumCommentEntry>>,
        activityDetailsRoute: ActivityDetailsRoute,
        activityRow: @Composable (
            ActivityStatusAware,
            NotificationMediaAndActivityQuery.Data.Activity.Activity,
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
        ) -> Unit,
        mediaDetailsByIdRoute: MediaDetailsByIdRoute,
        forumThreadRoute: ForumThreadRoute,
        forumThreadCommentRoute: ForumThreadCommentRoute,
        threadRow: @Composable (ForumThread?, Modifier) -> Unit,
        commentId: (ForumCommentEntry) -> String,
        commentRow: @Composable (threadId: String, ForumCommentEntry) -> Unit,
        mediaRow: @Composable (
            AniListViewer?,
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
        userRoute: UserRoute,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            Scaffold(
                topBar = {
                    AppBar(
                        text = stringResource(Res.string.anime_notifications_title),
                        upIconOption = upIconOption,
                        scrollBehavior = scrollBehavior,
                    )
                },
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
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
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ActivityMessageNotificationNotification ->
                                ActivityMessageNotificationCard(
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.AiringNotificationNotification ->
                                AiringNotificationCard(
                                    notification = notification,
                                    mediaDetailsByIdRoute = mediaDetailsByIdRoute,
                                    mediaRow = { modifier ->
                                        mediaRow(viewer(), it.mediaEntry, onClickListEdit, modifier)
                                    },
                                )
                            is NotificationsQuery.Data.Page.FollowingNotificationNotification ->
                                FollowingNotificationCard(
                                    notification = notification,
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ActivityReplyNotificationNotification ->
                                ActivityReplyNotificationCard(
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification ->
                                ActivityReplySubscribedNotificationCard(
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ActivityLikeNotificationNotification ->
                                ActivityLikedNotificationCard(
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification ->
                                ActivityReplyLikedNotificationCard(
                                    notification = notification,
                                    activityEntry = it.activityEntry,
                                    activityDetailsRoute = activityDetailsRoute,
                                    activityRow = { activityEntry, activity ->
                                        activityRow(activityEntry, activity, it.mediaEntry, onClickListEdit)
                                    },
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification ->
                                RelatedMediaAdditionNotificationCard(
                                    notification = notification,
                                    mediaDetailsByIdRoute = mediaDetailsByIdRoute,
                                    mediaRow = { modifier ->
                                        mediaRow(viewer(), it.mediaEntry, onClickListEdit, modifier)
                                    },
                                )
                            is NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification ->
                                MediaDataChangeNotificationCard(
                                    notification = notification,
                                    mediaDetailsByIdRoute = mediaDetailsByIdRoute,
                                    mediaRow = { modifier ->
                                        mediaRow(viewer(), it.mediaEntry, onClickListEdit, modifier)
                                    },
                                )
                            is NotificationsQuery.Data.Page.MediaDeletionNotificationNotification ->
                                MediaDeletionNotificationCard(
                                    notification = notification,
                                    mediaRow = { modifier ->
                                        mediaRow(viewer(), it.mediaEntry, onClickListEdit, modifier)
                                    },
                                )
                            is NotificationsQuery.Data.Page.MediaMergeNotificationNotification ->
                                MediaMergeNotificationCard(
                                    notification = notification,
                                    mediaDetailsByIdRoute = mediaDetailsByIdRoute,
                                    mediaRow = { modifier ->
                                        mediaRow(viewer(), it.mediaEntry, onClickListEdit, modifier)
                                    },
                                )
                            is NotificationsQuery.Data.Page.ThreadCommentMentionNotificationNotification ->
                                ThreadCommentMentionNotificationCard(
                                    notification = notification,
                                    commentId = it.commentEntry?.let(commentId),
                                    commentEntry = it.commentEntry,
                                    forumThreadRoute = forumThreadRoute,
                                    forumThreadCommentRoute = forumThreadCommentRoute,
                                    threadRow = threadRow,
                                    commentRow = commentRow,
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ThreadCommentLikeNotificationNotification ->
                                ThreadCommentLikeNotificationCard(
                                    notification = notification,
                                    commentId = it.commentEntry?.let(commentId),
                                    commentEntry = it.commentEntry,
                                    forumThreadRoute = forumThreadRoute,
                                    forumThreadCommentRoute = forumThreadCommentRoute,
                                    threadRow = threadRow,
                                    commentRow = commentRow,
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ThreadCommentReplyNotificationNotification ->
                                ThreadCommentReplyNotificationCard(
                                    notification = notification,
                                    commentId = it.commentEntry?.let(commentId),
                                    commentEntry = it.commentEntry,
                                    forumThreadRoute = forumThreadRoute,
                                    forumThreadCommentRoute = forumThreadCommentRoute,
                                    threadRow = threadRow,
                                    commentRow = commentRow,
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ThreadCommentSubscribedNotificationNotification ->
                                ThreadCommentSubscribedNotificationCard(
                                    notification = notification,
                                    commentId = it.commentEntry?.let(commentId),
                                    commentEntry = it.commentEntry,
                                    forumThreadRoute = forumThreadRoute,
                                    forumThreadCommentRoute = forumThreadCommentRoute,
                                    threadRow = threadRow,
                                    commentRow = commentRow,
                                    userRoute = userRoute,
                                )
                            is NotificationsQuery.Data.Page.ThreadLikeNotificationNotification ->
                                ThreadLikeNotificationCard(
                                    notification = notification,
                                    forumThreadRoute = forumThreadRoute,
                                    threadRow = { threadRow(it, Modifier) },
                                    userRoute = userRoute,
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
}
