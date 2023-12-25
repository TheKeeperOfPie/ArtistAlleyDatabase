@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.NotificationsQuery
import com.anilist.NotificationsQuery.Data.Page.ActivityLikeNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityMentionNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplyNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification
import com.anilist.fragment.ForumThread
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.activity.MessageActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadCardContent
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadCommentContent
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun NotificationPlaceholderCard() {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shape)
                    .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = "USERNAME",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
                Text(
                    text = "some placeholder context",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
            }

            Text(
                text = "5 minutes ago",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun ActivityMentionNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: ActivityMentionNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = { activityEntry?.id?.let(navigationCallback::onActivityDetailsClick) }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun ActivityMessageNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.ActivityMessageNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = { activityEntry?.id?.let(navigationCallback::onActivityDetailsClick) }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = null,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun ActivityReplyNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: ActivityReplyNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = {
        notification.activityId.toString().let(navigationCallback::onActivityDetailsClick)
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun ActivityReplySubscribedNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: ActivityReplySubscribedNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = {
        notification.activityId.toString().let(navigationCallback::onActivityDetailsClick)
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun ActivityLikedNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: ActivityLikeNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = {
        notification.activityId.toString().let(navigationCallback::onActivityDetailsClick)
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun ActivityReplyLikedNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: ActivityReplyLikeNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = {
        notification.activityId.toString().let(navigationCallback::onActivityDetailsClick)
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey = screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
        )
    }
}

@Composable
fun AiringNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.AiringNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.anime_notification_episode_aired,
                    notification.episode
                ),
                modifier = Modifier.weight(1f)
            )
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }

        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                viewer = viewer,
                entry = mediaEntry,
                onClickListEdit = onClickListEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun FollowingNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.FollowingNotificationNotification,
) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(onClick = {
        notification.user?.let {
            navigationCallback.onUserClick(it, 1f)
        }
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )
    }
}

@Composable
fun RelatedMediaAdditionNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.anime_notification_related_added),
                modifier = Modifier.weight(1f)
            )
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                viewer = viewer,
                entry = mediaEntry,
                onClickListEdit = onClickListEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaDataChangeNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                notification.context?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                notification.reason?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                viewer = viewer,
                entry = mediaEntry,
                onClickListEdit = onClickListEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaDeletionNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.MediaDeletionNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = "${notification.deletedMediaTitle?.trim()} ${notification.context?.trim()}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                notification.reason?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                viewer = viewer,
                entry = mediaEntry,
                onClickListEdit = onClickListEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaMergeNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.MediaMergeNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                notification.context?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                notification.reason?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                viewer = viewer,
                entry = mediaEntry,
                onClickListEdit = onClickListEdit,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ThreadCommentMentionNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.ThreadCommentMentionNotificationNotification,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
) {
    ThreadAndCommentNotificationCard(
        screenKey = screenKey,
        viewer = viewer,
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        entry = entry,
        onStatusUpdate = onStatusUpdate,
    )
}

@Composable
fun ThreadCommentLikeNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.ThreadCommentLikeNotificationNotification,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
) {
    ThreadAndCommentNotificationCard(
        screenKey = screenKey,
        viewer = viewer,
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        entry = entry,
        onStatusUpdate = onStatusUpdate,
    )
}

@Composable
fun ThreadCommentReplyNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.ThreadCommentReplyNotificationNotification,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
) {
    ThreadAndCommentNotificationCard(
        screenKey = screenKey,
        viewer = viewer,
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        entry = entry,
        onStatusUpdate = onStatusUpdate,
    )
}

@Composable
fun ThreadCommentSubscribedNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    notification: NotificationsQuery.Data.Page.ThreadCommentSubscribedNotificationNotification,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
) {
    ThreadAndCommentNotificationCard(
        screenKey = screenKey,
        viewer = viewer,
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        entry = entry,
        onStatusUpdate = onStatusUpdate,
    )
}

@Composable
private fun ThreadAndCommentNotificationCard(
    screenKey: String,
    viewer: AniListViewer?,
    user: UserNavigationData?,
    context: String?,
    createdAt: Int?,
    thread: ForumThread?,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
) {
    val navigationCallback = LocalNavigationCallback.current
    val threadId = thread?.id?.toString()
    ElevatedCard(onClick = {
        val commentId = entry?.comment?.id?.toString()
        if (threadId != null && commentId != null) {
            navigationCallback.onForumThreadCommentClick(
                title = thread.title,
                threadId = threadId,
                commentId = commentId,
            )
        }
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = user,
            context = context,
            createdAt = createdAt,
        )

        OutlinedCard(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            ThreadCardContent(
                screenKey = screenKey,
                thread = thread,
                modifier = Modifier.clickable {
                    if (threadId != null) {
                        navigationCallback.onForumThreadClick(
                            title = thread.title,
                            threadId = threadId,
                        )
                    }
                }
            )

            HorizontalDivider()

            val comment = entry?.comment
            if (threadId != null && comment != null) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState())
                        .clickable {
                            navigationCallback.onForumThreadCommentClick(
                                title = thread.title,
                                threadId = threadId,
                                commentId = comment.id.toString(),
                            )
                        }
                ) {
                    ThreadCommentContent(
                        screenKey = screenKey,
                        threadId = threadId,
                        viewer = viewer,
                        loading = false,
                        commentId = comment.id.toString(),
                        commentMarkdown = entry.commentMarkdown,
                        createdAt = comment.createdAt,
                        liked = entry.liked,
                        likeCount = comment.likeCount,
                        user = entry.user,
                        onStatusUpdate = onStatusUpdate,
                    )
                }
            }
        }
    }
}

@Composable
fun ThreadLikeNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.ThreadLikeNotificationNotification,
) {
    val navigationCallback = LocalNavigationCallback.current
    val thread = notification.thread
    ElevatedCard(onClick = {
        if (thread != null) {
            navigationCallback.onForumThreadClick(
                title = thread.title,
                threadId = thread.id.toString(),
            )
        }
    }) {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        if (thread != null) {
            OutlinedCard(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                ThreadCardContent(screenKey = screenKey, thread = thread)
            }
        }
    }
}

@Composable
private fun ContextHeader(
    screenKey: String,
    user: UserNavigationData?,
    context: String?,
    createdAt: Int?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        val shape = RoundedCornerShape(12.dp)
        val navigationCallback = LocalNavigationCallback.current
        UserAvatarImage(
            screenKey = screenKey,
            userId = user?.id?.toString(),
            image = user?.avatar?.large,
            modifier = Modifier
                .size(40.dp)
                .clip(shape)
                .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                .clickable {
                    if (user != null) {
                        navigationCallback.onUserClick(user, 1f)
                    }
                }
        )

        Column(Modifier.weight(1f)) {
            user?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = context.orEmpty().trim().removeSuffix(".").removeSuffix(","),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Timestamp(createdAt = createdAt, modifier = Modifier.padding(top = 4.dp))
    }
}


@Composable
private fun ActivityCard(
    screenKey: String,
    viewer: AniListViewer?,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    // TODO: Load activity manually if notification doesn't provide it
    val activity = activityEntry?.activity ?: return
    val navigationCallback = LocalNavigationCallback.current
    OutlinedCard(
        onClick = {
            activityEntry?.id?.let { navigationCallback.onActivityDetailsClick(it) }
        },
        modifier = Modifier.padding(8.dp)
    ) {
        when (activity) {
            is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> ListActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                user = activity.user,
                activity = activity,
                entry = mediaEntry,
                liked = activityEntry.liked,
                subscribed = activityEntry.subscribed,
                onActivityStatusUpdate = onActivityStatusUpdate,
                onClickListEdit = onClickListEdit,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> MessageActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                activity = activity,
                messenger = activity.messenger,
                entry = activityEntry,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> TextActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                activity = activity,
                entry = activityEntry,
                user = activity.user,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity,
            -> TextActivityCardContent(
                screenKey = screenKey,
                activity = null,
                viewer = viewer,
                entry = null,
                user = null,
                onActivityStatusUpdate = onActivityStatusUpdate,
            )
        }
    }
}

@Composable
private fun Timestamp(createdAt: Int?, modifier: Modifier = Modifier) {
    if (createdAt == null) return
    val timestamp = remember(createdAt) {
        createdAt.let {
            DateUtils.getRelativeTimeSpanString(
                it * 1000L,
                Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                0,
                DateUtils.FORMAT_ABBREV_ALL,
            )
        }
    }

    Text(
        text = timestamp.toString(),
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
    )
}
