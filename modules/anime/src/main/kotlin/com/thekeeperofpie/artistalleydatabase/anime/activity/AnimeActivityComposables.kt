@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anilist.AuthedUserQuery
import com.anilist.fragment.ListActivityWithoutMedia
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.fragment.TextActivityFragment
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun TextActivitySmallCard(
    viewer: AuthedUserQuery.Data.Viewer?,
    activity: TextActivityFragment?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    navigationCallback: AnimeNavigator.NavigationCallback,
    modifier: Modifier = Modifier,
    clickable: Boolean = false,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            val image = activity?.user?.avatar?.large
            if (activity == null || image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = activity?.user?.name ?: "USERNAME",
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
                        style = MaterialTheme.typography.labelSmall,
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
            ImageHtmlText(
                text = activity?.text ?: "Placeholder text",
                color = MaterialTheme.typography.bodySmall.color
                    .takeOrElse { LocalContentColor.current },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .conditionally(activity == null) { fillMaxWidth() }
                    .placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    if (clickable && activity != null) {
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
fun ListActivitySmallCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    activity: ListActivityWithoutMedia?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    modifier: Modifier = Modifier,
    clickable: Boolean = false,
) {
    ListActivitySmallCard(
        screenKey = screenKey,
        viewer = viewer,
        activity = activity,
        showMedia = false,
        media = null,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        colorCalculationState = colorCalculationState,
        navigationCallback = navigationCallback,
        clickable = clickable,
        modifier = modifier,
    )
}

@Composable
fun ListActivitySmallCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    activity: ListActivityWithoutMedia?,
    media: MediaCompactWithTags?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    clickable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    ListActivitySmallCard(
        screenKey = screenKey,
        viewer = viewer,
        activity = activity,
        showMedia = true,
        media = media,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        colorCalculationState = colorCalculationState,
        navigationCallback = navigationCallback,
        clickable = clickable,
        modifier = modifier,
    )
}

@Composable
private fun ListActivitySmallCard(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    activity: ListActivityWithoutMedia?,
    showMedia: Boolean,
    media: MediaCompactWithTags?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    clickable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            val image = activity?.user?.avatar?.large
            if (activity == null || image != null) {
                val shape = RoundedCornerShape(12.dp)
                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(shape)
                        .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity?.user?.name ?: "USERNAME",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .placeholder(
                            visible = activity == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

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
                        style = MaterialTheme.typography.labelSmall,
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
                entry = media?.let {
                    // TODO: Ignored
                    AnimeMediaCompactListRow.Entry(it, false)
                },
                onLongClick = {
                    // TODO: Ignored
                },
                onTagLongClick = {
                    // TODO: Tag long click
                },
                onLongPressImage = {
                    // TODO: Image long click
                },
                colorCalculationState = colorCalculationState,
                navigationCallback = navigationCallback,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )
        }
    }
    if (clickable && activity != null) {
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
fun ActivityStatusIcons(
    activityId: String?,
    replies: Int?,
    viewer: AuthedUserQuery.Data.Viewer?,
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
                style = MaterialTheme.typography.labelSmall,
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
