@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRightAlt
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.fragment.ListActivityWithoutMedia
import com.anilist.fragment.MessageActivityFragment
import com.anilist.fragment.TextActivityFragment
import com.anilist.fragment.UserNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun TextActivitySmallCard(
    screenKey: String,
    viewer: AniListViewer?,
    activity: TextActivityFragment?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    val content: @Composable ColumnScope.() -> Unit = {
        TextActivityCardContent(
            screenKey = screenKey,
            viewer = viewer,
            activity = activity,
            user = activity?.user,
            entry = entry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            clickable = clickable,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
        )
    }

    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                navigationCallback.onActivityDetailsClick(activity.id.toString())
            },
            modifier = modifier,
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            content = content,
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.TextActivityCardContent(
    screenKey: String,
    viewer: AniListViewer?,
    activity: TextActivityFragment?,
    user: UserNavigationData?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val image = user?.avatar?.large
        if (activity == null || image != null) {
            UserImage(
                screenKey = screenKey,
                loading = activity == null,
                user = user,
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = user?.name ?: "USERNAME",
                modifier = Modifier.placeholder(
                    visible = activity == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
            )

            val timestamp = remember(activity) {
                activity?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.createdAt * 1000L,
                        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                        0,
                        DateUtils.FORMAT_ABBREV_ALL,
                    )
                }
            }

            if (activity == null || timestamp != null) {
                Text(
                    text = timestamp.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
            }
        }

        ActivityStatusIcons(
            activityId = activity?.id?.toString(),
            replies = activity?.replyCount,
            viewer = viewer,
            liked = entry?.liked ?: false,
            subscribed = entry?.subscribed ?: false,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    if (activity == null || activity.text != null) {
        val navigationCallback = LocalNavigationCallback.current
        ImageHtmlText(
            text = activity?.text ?: "Placeholder text",
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current },
            onClickFallback = {
                if (activity != null && clickable) {
                    navigationCallback.onActivityDetailsClick(activity.id.toString())
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .conditionally(activity == null) { fillMaxWidth() }
                .placeholder(
                    visible = activity == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    if (showActionsRow) {
        ActivityDetailsActionRow(
            activityId = activity?.id?.toString(),
            isViewer = viewer != null && user?.id?.toString() == viewer.id,
            onClickDelete = onClickDelete,
        )
    }
}

@Composable
fun MessageActivitySmallCard(
    screenKey: String,
    viewer: AniListViewer?,
    activity: MessageActivityFragment?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    val content: @Composable ColumnScope.() -> Unit = {
        MessageActivityCardContent(
            screenKey = screenKey,
            viewer = viewer,
            activity = activity,
            messenger = activity?.messenger,
            entry = entry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            clickable = clickable,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
        )
    }

    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                navigationCallback.onActivityDetailsClick(activity.id.toString())
            },
            modifier = modifier,
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
fun ColumnScope.MessageActivityCardContent(
    screenKey: String,
    viewer: AniListViewer?,
    activity: MessageActivityFragment?,
    messenger: UserNavigationData?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
    ) {
        val image = messenger?.avatar?.large
        if (activity == null || image != null) {
            UserImage(
                screenKey = screenKey,
                loading = activity == null,
                user = messenger,
            )
        }

        Column(Modifier.weight(1f)) {
            val messengerName = if (activity == null) "USERNAME" else messenger?.name
            if (messengerName != null) {
                Text(
                    text = messengerName,
                    modifier = Modifier.placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
            }

            val timestamp = remember(activity) {
                activity?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.createdAt * 1000L,
                        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                        0,
                        DateUtils.FORMAT_ABBREV_ALL,
                    )
                }
            }

            if (activity == null || timestamp != null) {
                Text(
                    text = timestamp.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )
            }
        }

        ActivityStatusIcons(
            activityId = activity?.id?.toString(),
            replies = activity?.replyCount,
            viewer = viewer,
            liked = entry?.liked ?: false,
            subscribed = entry?.subscribed ?: false,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    val navigationCallback = LocalNavigationCallback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .align(Alignment.End)
            .clickable {
                activity?.recipient?.let { navigationCallback.onUserClick(it, 1f) }
            }
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowRightAlt,
            contentDescription = stringResource(
                R.string.anime_activity_message_arrow_recipient_icon_content_description
            ),
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = activity?.recipient?.name ?: "USERNAME",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .placeholder(
                    visible = activity == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
        val image = activity?.recipient?.avatar?.large
        if (activity == null || image != null) {
            UserAvatarImage(
                screenKey = screenKey,
                userId = activity?.recipient?.id?.toString(),
                image = image,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    if (activity == null || activity.message != null) {
        ImageHtmlText(
            text = activity?.message ?: "Placeholder text",
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current },
            onClickFallback = {
                if (activity != null && clickable) {
                    navigationCallback.onActivityDetailsClick(activity.id.toString())
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .conditionally(activity == null) { fillMaxWidth() }
                .placeholder(
                    visible = activity == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    if (showActionsRow) {
        ActivityDetailsActionRow(
            activityId = activity?.id?.toString(),
            isViewer = viewer != null && messenger?.id?.toString() == viewer.id,
            onClickDelete = onClickDelete,
        )
    }
}

@Composable
fun ListActivitySmallCard(
    screenKey: String,
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (AnimeMediaCompactListRow.Entry) -> Unit,
    colorCalculationState: ColorCalculationState,
    modifier: Modifier = Modifier,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    ListActivitySmallCard(
        screenKey = screenKey,
        viewer = viewer,
        activity = activity,
        showMedia = false,
        entry = null,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        onClickListEdit = onClickListEdit,
        colorCalculationState = colorCalculationState,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        modifier = modifier,
    )
}

@Composable
fun ListActivitySmallCard(
    screenKey: String,
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (AnimeMediaCompactListRow.Entry) -> Unit,
    colorCalculationState: ColorCalculationState,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ListActivitySmallCard(
        screenKey = screenKey,
        viewer = viewer,
        activity = activity,
        showMedia = true,
        entry = mediaEntry,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        onClickListEdit = onClickListEdit,
        colorCalculationState = colorCalculationState,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        modifier = modifier,
    )
}

@Composable
private fun ListActivitySmallCard(
    screenKey: String,
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    showMedia: Boolean,
    entry: AnimeMediaCompactListRow.Entry?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (AnimeMediaCompactListRow.Entry) -> Unit,
    colorCalculationState: ColorCalculationState,
    clickable: Boolean,
    showActionsRow: Boolean,
    onClickDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        ListActivityCardContent(
            screenKey = screenKey,
            viewer = viewer,
            activity = activity,
            showMedia = showMedia,
            user = activity?.user,
            entry = entry,
            liked = liked,
            subscribed = subscribed,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
            colorCalculationState = colorCalculationState,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
        )
    }
    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                navigationCallback.onActivityDetailsClick(activity.id.toString())
            },
            modifier = modifier,
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            content = content,
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ListActivityCardContent(
    screenKey: String,
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    user: UserNavigationData?,
    entry: AnimeMediaCompactListRow.Entry?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (AnimeMediaCompactListRow.Entry) -> Unit,
    colorCalculationState: ColorCalculationState,
    showMedia: Boolean = entry != null,
    showUser: Boolean = true,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        val image = user?.avatar?.large
        if (showUser && (activity == null || image != null)) {
            UserImage(
                screenKey = screenKey,
                loading = activity == null,
                user = user,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            val userName = if (activity == null) "USERNAME" else user?.name
            if (userName != null) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            // API returns "1" if the status is "plans to watch", which is redundant, strip it
            val progress =
                if (activity?.status == "plans to watch") null else activity?.progress
            val status = listOfNotNull(activity?.status, progress).joinToString(separator = " ")
            val timestamp = remember(activity) {
                activity?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.createdAt * 1000L,
                        Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                        0,
                        DateUtils.FORMAT_ABBREV_ALL,
                    )
                }
            }
            val summaryText = if (status.isNotBlank()) {
                stringResource(
                    R.string.anime_activity_status_with_timestamp,
                    status,
                    timestamp.toString()
                )
            } else {
                timestamp
            }
            if (activity == null || summaryText != null) {
                Text(
                    text = summaryText.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }

        ActivityStatusIcons(
            activityId = activity?.id?.toString(),
            replies = activity?.replyCount,
            viewer = viewer,
            liked = liked,
            subscribed = subscribed,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    if (showMedia) {
        AnimeMediaCompactListRow(
            screenKey = screenKey,
            viewer = viewer,
            entry = entry,
            onLongClick = {
                // TODO: Ignored
            },
            onClickListEdit = onClickListEdit,
            colorCalculationState = colorCalculationState,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
    }

    if (showActionsRow) {
        ActivityDetailsActionRow(
            activityId = activity?.id?.toString(),
            isViewer = viewer != null && user?.id?.toString() == viewer.id,
            onClickDelete = onClickDelete,
        )
    }
}

@Composable
fun ActivityDetailsActionRow(
    activityId: String?,
    isViewer: Boolean,
    onClickDelete: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isViewer) {
            IconButton(onClick = { if (activityId != null) onClickDelete(activityId) }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(
                        R.string.anime_activity_delete_content_description
                    ),
                )
            }
        }
        val uriHandler = LocalUriHandler.current
        IconButton(onClick = {
            if (activityId != null) {
                uriHandler.openUri(
                    AniListUtils.activityUrl(activityId) +
                            "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                )
            }
        }) {
            Icon(
                imageVector = Icons.Filled.OpenInBrowser,
                contentDescription = stringResource(
                    R.string.anime_activity_open_in_browser_content_description
                ),
            )
        }
    }
}

@Composable
fun ActivityStatusIcons(
    activityId: String?,
    replies: Int?,
    viewer: AniListViewer?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        val replyCount = replies ?: 0
        if (activityId == null || replyCount > 0) {
            Text(
                text = replyCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.placeholder(
                    visible = activityId == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
            )
        }
        Icon(
            imageVector = if (replyCount == 0) {
                Icons.Outlined.ModeComment
            } else {
                Icons.Filled.Comment
            },
            contentDescription = stringResource(
                R.string.anime_activity_replies_icon_content_description
            ),
            modifier = Modifier
                .size(36.dp)
                .padding(8.dp)
        )

        if (viewer != null) {
            IconButton(
                onClick = {
                    if (activityId != null) {
                        onActivityStatusUpdate(
                            ActivityToggleUpdate.Liked(
                                id = activityId.toString(),
                                liked = !liked,
                                subscribed = subscribed,
                            )
                        )
                    }
                }, modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                    contentDescription = stringResource(
                        R.string.anime_activity_like_icon_content_description
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = {
                    if (activityId != null) {
                        onActivityStatusUpdate(
                            ActivityToggleUpdate.Subscribe(
                                id = activityId.toString(),
                                liked = liked,
                                subscribed = !subscribed,
                            )
                        )
                    }
                }, modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (subscribed) {
                        Icons.Filled.NotificationsActive
                    } else {
                        Icons.Filled.NotificationsNone
                    },
                    contentDescription = stringResource(
                        R.string.anime_activity_subscribe_icon_content_description
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UserImage(
    screenKey: String,
    loading: Boolean,
    user: UserNavigationData?,
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
            .placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}
