package com.thekeeperofpie.artistalleydatabase.anime.notifications

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.notifications.generated.resources.Res
import artistalleydatabase.modules.anime.notifications.generated.resources.anime_notification_episode_aired
import artistalleydatabase.modules.anime.notifications.generated.resources.anime_notification_related_added
import com.anilist.data.NotificationMediaAndActivityQuery
import com.anilist.data.NotificationsQuery
import com.anilist.data.NotificationsQuery.Data.Page.ActivityLikeNotificationNotification
import com.anilist.data.NotificationsQuery.Data.Page.ActivityMentionNotificationNotification
import com.anilist.data.NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification
import com.anilist.data.NotificationsQuery.Data.Page.ActivityReplyNotificationNotification
import com.anilist.data.NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification
import com.anilist.data.fragment.ForumThread
import com.anilist.data.fragment.UserNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsByIdRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ActivityDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadCommentRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.ForumThreadRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

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
    notification: ActivityMentionNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        activityEntry?.id?.let {
            navigationController.navigate(
                activityDetailsRoute(it, sharedTransitionScopeKey)
            )
        }
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun ActivityMessageNotificationCard(
    notification: NotificationsQuery.Data.Page.ActivityMessageNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        activityEntry?.id?.let {
            navigationController.navigate(
                activityDetailsRoute(it, sharedTransitionScopeKey)
            )
        }
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun ActivityReplyNotificationCard(
    notification: ActivityReplyNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        notification.activityId.toString().let {
            navigationController.navigate(
                activityDetailsRoute(it, sharedTransitionScopeKey)
            )
        }
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun ActivityReplySubscribedNotificationCard(
    notification: ActivityReplySubscribedNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        navigationController.navigate(
            activityDetailsRoute(notification.activityId.toString(), sharedTransitionScopeKey)
        )
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun ActivityLikedNotificationCard(
    notification: ActivityLikeNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        navigationController.navigate(
            activityDetailsRoute(notification.activityId.toString(), sharedTransitionScopeKey)
        )
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun ActivityReplyLikedNotificationCard(
    notification: ActivityReplyLikeNotificationNotification,
    activityEntry: NotificationActivityEntry?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val activitySharedTransitionKey =
        SharedTransitionKey.makeKeyForId(notification.activityId.toString())
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    ElevatedCard(onClick = {
        navigationController.navigate(
            activityDetailsRoute(notification.activityId.toString(), sharedTransitionScopeKey)
        )
    }) {
        val userImageState = rememberCoilImageState(notification.user?.avatar?.large)
        val userSharedTransitionKey = notification.user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = userSharedTransitionKey,
            imageState = userImageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        ActivityCard(
            activityEntry = activityEntry,
            sharedTransitionKey = activitySharedTransitionKey,
            activityDetailsRoute = activityDetailsRoute,
            activityRow = activityRow,
        )
    }
}

@Composable
fun AiringNotificationCard(
    notification: NotificationsQuery.Data.Page.AiringNotificationNotification,
    mediaDetailsByIdRoute: MediaDetailsByIdRoute,
    mediaRow: @Composable (Modifier) -> Unit,
) {
    val navigationController = LocalNavigationController.current
    val sharedTransitionKey = notification.mediaId?.let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        notification.mediaId?.let {
            navigationController.navigate(mediaDetailsByIdRoute(it, sharedTransitionKey))
        }
    }) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Text(
                text = stringResource(
                    Res.string.anime_notification_episode_aired,
                    notification.episode
                ),
                modifier = Modifier.weight(1f)
            )
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }

        mediaRow(Modifier.padding(8.dp))
    }
}

@Composable
fun FollowingNotificationCard(
    notification: NotificationsQuery.Data.Page.FollowingNotificationNotification,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val imageState = rememberCoilImageState(notification.user?.avatar?.large)
    val sharedTransitionKey = notification.user?.id?.toString()
        ?.let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        notification.user?.let {
            navigationController.navigate(
                userRoute(it.id.toString(), sharedTransitionKey, it.name, imageState.toImageState())
            )
        }
    }) {
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = sharedTransitionKey,
            imageState = imageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )
    }
}

@Composable
fun RelatedMediaAdditionNotificationCard(
    notification: NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification,
    mediaDetailsByIdRoute: MediaDetailsByIdRoute,
    mediaRow: @Composable (Modifier) -> Unit,
) {
    val navigationController = LocalNavigationController.current
    val sharedTransitionKey =
        notification.mediaId.toString().let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        notification.mediaId.takeIf { it > 0 }?.toString()?.let {
            navigationController.navigate(
                mediaDetailsByIdRoute(it, sharedTransitionKey)
            )
        }
    }) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Text(
                text = stringResource(Res.string.anime_notification_related_added),
                modifier = Modifier.weight(1f)
            )
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }

        mediaRow(Modifier.padding(8.dp))
    }
}

@Composable
fun MediaDataChangeNotificationCard(
    notification: NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification,
    mediaDetailsByIdRoute: MediaDetailsByIdRoute,
    mediaRow: @Composable (Modifier) -> Unit,
) {
    val navigationController = LocalNavigationController.current
    val sharedTransitionKey =
        notification.mediaId.toString().let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        notification.mediaId.takeIf { it > 0 }?.toString()?.let {
            navigationController.navigate(
                mediaDetailsByIdRoute(it, sharedTransitionKey)
            )
        }
    }) {
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

        mediaRow(Modifier.padding(8.dp))
    }
}

@Composable
fun MediaDeletionNotificationCard(
    notification: NotificationsQuery.Data.Page.MediaDeletionNotificationNotification,
    mediaRow: @Composable (Modifier) -> Unit,
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

        mediaRow(Modifier.padding(8.dp))
    }
}

@Composable
fun MediaMergeNotificationCard(
    notification: NotificationsQuery.Data.Page.MediaMergeNotificationNotification,
    mediaDetailsByIdRoute: MediaDetailsByIdRoute,
    mediaRow: @Composable (Modifier) -> Unit,
) {
    val navigationController = LocalNavigationController.current
    val sharedTransitionKey =
        notification.mediaId.toString().let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        notification.mediaId.takeIf { it > 0 }?.toString()?.let {
            navigationController.navigate(
                mediaDetailsByIdRoute(it, sharedTransitionKey)
            )
        }
    }) {
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

        mediaRow(Modifier.padding(8.dp))
    }
}

@Composable
fun <CommentEntry> ThreadCommentMentionNotificationCard(
    notification: NotificationsQuery.Data.Page.ThreadCommentMentionNotificationNotification,
    commentId: String?,
    commentEntry: CommentEntry?,
    forumThreadRoute: ForumThreadRoute,
    forumThreadCommentRoute: ForumThreadCommentRoute,
    threadRow: @Composable (ForumThread?, Modifier) -> Unit,
    commentRow: @Composable (threadId: String, CommentEntry) -> Unit,
    userRoute: UserRoute,
) {
    ThreadAndCommentNotificationCard(
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        commentId = commentId,
        commentEntry = commentEntry,
        forumThreadRoute = forumThreadRoute,
        forumThreadCommentRoute = forumThreadCommentRoute,
        threadRow = threadRow,
        commentRow = commentRow,
        userRoute = userRoute,
    )
}

@Composable
fun <CommentEntry> ThreadCommentLikeNotificationCard(
    notification: NotificationsQuery.Data.Page.ThreadCommentLikeNotificationNotification,
    commentId: String?,
    commentEntry: CommentEntry?,
    forumThreadRoute: ForumThreadRoute,
    forumThreadCommentRoute: ForumThreadCommentRoute,
    threadRow: @Composable (ForumThread?, Modifier) -> Unit,
    commentRow: @Composable (threadId: String, CommentEntry) -> Unit,
    userRoute: UserRoute,
) {
    ThreadAndCommentNotificationCard(
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        commentId = commentId,
        commentEntry = commentEntry,
        forumThreadRoute = forumThreadRoute,
        forumThreadCommentRoute = forumThreadCommentRoute,
        threadRow = threadRow,
        commentRow = commentRow,
        userRoute = userRoute,
    )
}

@Composable
fun <CommentEntry> ThreadCommentReplyNotificationCard(
    notification: NotificationsQuery.Data.Page.ThreadCommentReplyNotificationNotification,
    commentId: String?,
    commentEntry: CommentEntry?,
    forumThreadRoute: ForumThreadRoute,
    forumThreadCommentRoute: ForumThreadCommentRoute,
    threadRow: @Composable (ForumThread?, Modifier) -> Unit,
    commentRow: @Composable (threadId: String, CommentEntry) -> Unit,
    userRoute: UserRoute,
) {
    ThreadAndCommentNotificationCard(
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        commentId = commentId,
        commentEntry = commentEntry,
        forumThreadRoute = forumThreadRoute,
        forumThreadCommentRoute = forumThreadCommentRoute,
        threadRow = threadRow,
        commentRow = commentRow,
        userRoute = userRoute,
    )
}

@Composable
fun <CommentEntry> ThreadCommentSubscribedNotificationCard(
    notification: NotificationsQuery.Data.Page.ThreadCommentSubscribedNotificationNotification,
    commentId: String?,
    commentEntry: CommentEntry?,
    forumThreadRoute: ForumThreadRoute,
    forumThreadCommentRoute: ForumThreadCommentRoute,
    threadRow: @Composable (ForumThread?, Modifier) -> Unit,
    commentRow: @Composable (threadId: String, CommentEntry) -> Unit,
    userRoute: UserRoute,
) {
    ThreadAndCommentNotificationCard(
        user = notification.user,
        context = notification.context,
        createdAt = notification.createdAt,
        thread = notification.thread,
        commentId = commentId,
        commentEntry = commentEntry,
        forumThreadRoute = forumThreadRoute,
        forumThreadCommentRoute = forumThreadCommentRoute,
        threadRow = threadRow,
        commentRow = commentRow,
        userRoute = userRoute,
    )
}

@Composable
private fun <CommentEntry> ThreadAndCommentNotificationCard(
    user: UserNavigationData?,
    context: String?,
    createdAt: Int?,
    thread: ForumThread?,
    commentId: String?,
    commentEntry: CommentEntry?,
    forumThreadRoute: ForumThreadRoute,
    forumThreadCommentRoute: ForumThreadCommentRoute,
    threadRow: @Composable (ForumThread?, Modifier) -> Unit,
    commentRow: @Composable (threadId: String, CommentEntry) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val threadId = thread?.id?.toString()
    val imageState = rememberCoilImageState(user?.avatar?.large)
    val sharedTransitionKey = user?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        if (threadId != null && commentId != null) {
            navigationController.navigate(
                forumThreadCommentRoute(threadId, commentId, thread.title)
            )
        }
    }) {
        ContextHeader(
            user = user,
            sharedTransitionKey = sharedTransitionKey,
            imageState = imageState,
            context = context,
            createdAt = createdAt,
            userRoute = userRoute,
        )

        OutlinedCard(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            threadRow(
                thread,
                Modifier.clickable {
                    if (threadId != null) {
                        navigationController.navigate(forumThreadRoute(threadId, thread.title))
                    }
                }
            )

            HorizontalDivider()

            if (threadId != null && commentId != null && commentEntry != null) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState())
                        .clickable {
                            navigationController.navigate(
                                forumThreadCommentRoute(threadId, commentId, thread.title)
                            )
                        }
                ) {
                    commentRow(threadId, commentEntry)
                }
            }
        }
    }
}

@Composable
fun ThreadLikeNotificationCard(
    notification: NotificationsQuery.Data.Page.ThreadLikeNotificationNotification,
    forumThreadRoute: ForumThreadRoute,
    threadRow: @Composable (ForumThread?) -> Unit,
    userRoute: UserRoute,
) {
    val navigationController = LocalNavigationController.current
    val thread = notification.thread
    val imageState = rememberCoilImageState(notification.user?.avatar?.large)
    val sharedTransitionKey = notification.user?.id?.toString()
        ?.let { SharedTransitionKey.makeKeyForId(it) }
    ElevatedCard(onClick = {
        if (thread != null) {
            navigationController.navigate(
                forumThreadRoute(thread.id.toString(), thread.title)
            )
        }
    }) {
        ContextHeader(
            user = notification.user,
            sharedTransitionKey = sharedTransitionKey,
            imageState = imageState,
            context = notification.context,
            createdAt = notification.createdAt,
            userRoute = userRoute,
        )

        if (thread != null) {
            OutlinedCard(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                threadRow(thread)
            }
        }
    }
}

@Composable
private fun ContextHeader(
    user: UserNavigationData?,
    sharedTransitionKey: SharedTransitionKey?,
    imageState: CoilImageState,
    context: String?,
    createdAt: Int?,
    userRoute: UserRoute,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        val shape = RoundedCornerShape(12.dp)
        val navigationController = LocalNavigationController.current
        UserAvatarImage(
            imageState = imageState,
            image = imageState.request().build(),
            modifier = Modifier
                .size(40.dp)
                .sharedElement(sharedTransitionKey, "user_image")
                .clip(shape)
                .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                .clickable {
                    if (user != null) {
                        navigationController.navigate(
                            userRoute(
                                user.id.toString(),
                                sharedTransitionKey,
                                user.name,
                                imageState.toImageState(),
                            )
                        )
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
    activityEntry: NotificationActivityEntry?,
    sharedTransitionKey: SharedTransitionKey?,
    activityDetailsRoute: ActivityDetailsRoute,
    activityRow: @Composable (ActivityStatusAware, NotificationMediaAndActivityQuery.Data.Activity.Activity) -> Unit,
) {
    // TODO: Load activity manually if notification doesn't provide it
    val activity = activityEntry?.activity ?: return
    val navigationController = LocalNavigationController.current
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    OutlinedCard(
        onClick = {
            navigationController.navigate(
                activityDetailsRoute(activityEntry.id, sharedTransitionScopeKey)
            )
        },
        modifier = Modifier
            .sharedElement(sharedTransitionKey, "activity_card")
            .padding(8.dp)
    ) {
        activityRow(activityEntry, activity)
    }
}

@Composable
private fun Timestamp(createdAt: Int?, modifier: Modifier = Modifier) {
    if (createdAt == null) return
    val timestamp = remember(createdAt) {
        createdAt.let {
            HumanReadable.timeAgo(Instant.fromEpochSeconds(it.toLong()))
        }
    }

    Text(
        text = timestamp.toString(),
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier,
    )
}
