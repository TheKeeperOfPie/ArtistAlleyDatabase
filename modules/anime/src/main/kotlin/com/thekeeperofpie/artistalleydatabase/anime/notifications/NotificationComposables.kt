@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.notifications

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import coil.compose.AsyncImage
import com.anilist.AuthedUserQuery
import com.anilist.NotificationMediaAndActivityQuery
import com.anilist.NotificationsQuery
import com.anilist.NotificationsQuery.Data.Page.ActivityLikeNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityMentionNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplyLikeNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplyNotificationNotification
import com.anilist.NotificationsQuery.Data.Page.ActivityReplySubscribedNotificationNotification
import com.anilist.fragment.UserNavigationData
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.activity.MessageActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivityCardContent
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun ActivityMentionNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: ActivityMentionNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey =screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun ActivityMessageNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: NotificationsQuery.Data.Page.ActivityMessageNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey =screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = null,
            onActivityStatusUpdate = onActivityStatusUpdate,
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun ActivityReplyNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: ActivityReplyNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        ContextHeader(
            screenKey = screenKey,
            user = notification.user,
            context = notification.context,
            createdAt = notification.createdAt,
        )

        ActivityCard(
            screenKey =screenKey,
            viewer = viewer,
            activityEntry = activityEntry,
            mediaEntry = mediaEntry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun ActivityReplySubscribedNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: ActivityReplySubscribedNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
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
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun ActivityLikedNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: ActivityLikeNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
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
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun ActivityReplyLikedNotificationCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    notification: ActivityReplyLikeNotificationNotification,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
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
            colorCalculationState = colorCalculationState,
        )
    }
}

@Composable
fun AiringNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.AiringNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    colorCalculationState: ColorCalculationState,
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
                entry = mediaEntry,
                onLongClick = { /* TODO */ },
                onLongPressImage = { /* TODO */ },
                colorCalculationState = colorCalculationState,
                navigationCallback = LocalNavigationCallback.current,
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
            navigationCallback?.onUserClick(it, 1f)
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
    notification: NotificationsQuery.Data.Page.RelatedMediaAdditionNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    colorCalculationState: ColorCalculationState,
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
                entry = mediaEntry,
                onLongClick = { /* TODO */ },
                onLongPressImage = { /* TODO */ },
                colorCalculationState = colorCalculationState,
                navigationCallback = LocalNavigationCallback.current,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaDataChangeNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.MediaDataChangeNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
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
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                entry = mediaEntry,
                onLongClick = { /* TODO */ },
                onLongPressImage = { /* TODO */ },
                colorCalculationState = colorCalculationState,
                navigationCallback = LocalNavigationCallback.current,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaDeletionNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.MediaDeletionNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
            Text(
                text = "${notification.deletedMediaTitle} ${notification.context}",
                style = MaterialTheme.typography.bodyMedium,
            )
            notification.reason?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                entry = mediaEntry,
                onLongClick = { /* TODO */ },
                onLongPressImage = { /* TODO */ },
                colorCalculationState = colorCalculationState,
                navigationCallback = LocalNavigationCallback.current,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MediaMergeNotificationCard(
    screenKey: String,
    notification: NotificationsQuery.Data.Page.MediaMergeNotificationNotification,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    colorCalculationState: ColorCalculationState,
) {
    ElevatedCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 10.dp)
        ) {
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
            Timestamp(createdAt = notification.createdAt, modifier = Modifier.padding(top = 4.dp))
        }
        mediaEntry?.let {
            AnimeMediaCompactListRow(
                screenKey = screenKey,
                entry = mediaEntry,
                onLongClick = { /* TODO */ },
                onLongPressImage = { /* TODO */ },
                colorCalculationState = colorCalculationState,
                navigationCallback = LocalNavigationCallback.current,
                modifier = Modifier.padding(8.dp)
            )
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
        SharedElement(key = "anime_user_${user?.id}_image", screenKey = screenKey) {
            AsyncImage(
                model = user?.avatar?.large,
                contentDescription = stringResource(R.string.anime_user_image),
                modifier = Modifier
                    .size(40.dp)
                    .clip(shape)
                    .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                    .clickable {
                        if (user != null) {
                            navigationCallback?.onUserClick(user, 1f)
                        }
                    }
            )
        }

        Column(Modifier.weight(1f)) {
            user?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = context.orEmpty().trim().removeSuffix("."),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Timestamp(createdAt = createdAt, modifier = Modifier.padding(top = 4.dp))
    }
}


@Composable
private fun ActivityCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    activityEntry: NotificationsViewModel.NotificationEntry.ActivityEntry?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
) {
    val navigationCallback = LocalNavigationCallback.current
    OutlinedCard(
        onClick = {
            activityEntry?.id?.let { navigationCallback?.onActivityDetailsClick(it) }
        },
        modifier = Modifier.padding(8.dp)
    ) {
        when (val activity = activityEntry?.activity) {
            is NotificationMediaAndActivityQuery.Data.Activity.ListActivityActivity -> ListActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                user = activity.user,
                activity = activity,
                entry = mediaEntry,
                liked = activityEntry.liked,
                subscribed = activityEntry.subscribed,
                onActivityStatusUpdate = onActivityStatusUpdate,
                colorCalculationState = colorCalculationState,
                navigationCallback = navigationCallback,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.MessageActivityActivity -> MessageActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                activity = activity,
                messenger = activity.messenger,
                entry = activityEntry,
                onActivityStatusUpdate = onActivityStatusUpdate,
                navigationCallback = navigationCallback,
                clickable = true,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.TextActivityActivity -> TextActivityCardContent(
                screenKey = screenKey,
                viewer = viewer,
                activity = activity,
                entry = activityEntry,
                user = activity.user,
                onActivityStatusUpdate = onActivityStatusUpdate,
                navigationCallback = navigationCallback,
                clickable = true,
            )
            is NotificationMediaAndActivityQuery.Data.Activity.OtherActivity,
            null,
            -> TextActivityCardContent(
                screenKey = screenKey,
                activity = null,
                viewer = viewer,
                entry = null,
                user = null,
                onActivityStatusUpdate = onActivityStatusUpdate,
                navigationCallback = navigationCallback,
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
