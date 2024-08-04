@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.activity

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRightAlt
import androidx.compose.material.icons.automirrored.filled.Comment
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.UserSocialActivityQuery
import com.anilist.fragment.ListActivityWithoutMedia
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MessageActivityFragment
import com.anilist.fragment.TextActivityFragment
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSectionWithoutHeader
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElement
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.ZoneOffset

object AnimeActivityComposables {
    const val ACTIVITIES_ABOVE_FOLD = 3
}

@Composable
fun ActivityList(
    editViewModel: MediaEditViewModel,
    viewer: AniListViewer?,
    activities: LazyPagingItems<ActivityEntry>,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    showMedia: Boolean,
    allowUserClick: Boolean = true,
    sortFilterController: ActivitySortFilterController,
) {
    when (val refreshState = activities.loadState.refresh) {
        is LoadState.Error -> AnimeMediaListScreen.Error(
            exception = refreshState.error,
        )
        else -> {
            if (activities.itemCount == 0
                && activities.loadState.refresh is LoadState.NotLoading
            ) {
                AnimeMediaListScreen.NoResults()
            } else {
                // TODO: Move this up a level
                val refreshing = activities.loadState.refresh is LoadState.Loading
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = refreshing,
                    onRefresh = { activities.refresh() },
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    val listState = rememberLazyListState()
                    sortFilterController.ImmediateScrollResetEffect(listState)
                    LazyColumn(
                        state = listState,
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
                            count = activities.itemCount,
                            key = activities.itemKey { it.activityId.scopedId },
                            contentType = activities.itemContentType { it.activityId.type }
                        ) {
                            val entry = activities[it]
                            SharedTransitionKeyScope("anime_user_activity_card_${entry?.activityId?.valueId}") {
                                when (val activity = entry?.activity) {
                                    is UserSocialActivityQuery.Data.Page.TextActivityActivity -> TextActivitySmallCard(
                                        viewer = viewer,
                                        activity = activity,
                                        entry = entry,
                                        onActivityStatusUpdate = onActivityStatusUpdate,
                                        modifier = Modifier.fillMaxWidth(),
                                        allowUserClick = allowUserClick,
                                        clickable = true
                                    )
                                    is UserSocialActivityQuery.Data.Page.ListActivityActivity -> ListActivitySmallCard(
                                        viewer = viewer,
                                        activity = activity,
                                        mediaEntry = entry.media,
                                        entry = entry,
                                        onActivityStatusUpdate = onActivityStatusUpdate,
                                        onClickListEdit = editViewModel::initialize,
                                        modifier = Modifier.fillMaxWidth(),
                                        allowUserClick = allowUserClick,
                                        clickable = true,
                                        showMedia = showMedia
                                    )
                                    is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> MessageActivitySmallCard(
                                        viewer = viewer,
                                        activity = activity,
                                        entry = entry,
                                        onActivityStatusUpdate = onActivityStatusUpdate,
                                        modifier = Modifier.fillMaxWidth(),
                                        allowUserClick = allowUserClick,
                                        clickable = true
                                    )
                                    is UserSocialActivityQuery.Data.Page.OtherActivity,
                                    null,
                                    -> TextActivitySmallCard(
                                        viewer = viewer,
                                        activity = null,
                                        entry = null,
                                        onActivityStatusUpdate = onActivityStatusUpdate,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        when (activities.loadState.append) {
                            is LoadState.Loading -> item("load_more_append") {
                                AnimeMediaListScreen.LoadingMore()
                            }
                            is LoadState.Error -> item("load_more_error") {
                                AnimeMediaListScreen.AppendError { activities.retry() }
                            }
                            is LoadState.NotLoading -> Unit
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
}

@Composable
fun TextActivitySmallCard(
    viewer: AniListViewer?,
    activity: TextActivityFragment?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
    allowUserClick: Boolean = true,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    val sharedTransitionKey = activity?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    val content: @Composable ColumnScope.() -> Unit = {
        TextActivityCardContent(
            viewer = viewer,
            activity = activity,
            user = activity?.user,
            entry = entry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            allowUserClick = allowUserClick,
            clickable = clickable,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
        )
    }

    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ElevatedCard(
            onClick = {
                navigationCallback.navigate(
                    AnimeDestination.ActivityDetails(
                        activityId = activity.id.toString(),
                        sharedTransitionScopeKey = sharedTransitionScopeKey,
                    )
                )
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
    viewer: AniListViewer?,
    activity: TextActivityFragment?,
    user: UserNavigationData?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    allowUserClick: Boolean = true,
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
                loading = activity == null,
                user = user,
                clickable = allowUserClick,
            )
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = user?.name ?: "USERNAME",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
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
            likes = activity?.likeCount,
            viewer = viewer,
            liked = entry?.liked ?: false,
            subscribed = entry?.subscribed ?: false,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    if (activity == null || activity.text != null) {
        val navigationCallback = LocalNavigationCallback.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ImageHtmlText(
            text = activity?.text ?: "Placeholder text",
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current },
            onClickFallback = {
                if (activity != null && clickable) {
                    navigationCallback.navigate(
                        AnimeDestination.ActivityDetails(
                            activityId = activity.id.toString(),
                            sharedTransitionScopeKey = sharedTransitionScopeKey,
                        )
                    )
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
    viewer: AniListViewer?,
    activity: MessageActivityFragment?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
    allowUserClick: Boolean = true,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    val sharedTransitionKey = activity?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    val content: @Composable ColumnScope.() -> Unit = {
        MessageActivityCardContent(
            viewer = viewer,
            activity = activity,
            sharedTransitionKey = sharedTransitionKey,
            messenger = activity?.messenger,
            entry = entry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            allowUserClick = allowUserClick,
            clickable = clickable,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
        )
    }

    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ElevatedCard(
            onClick = {
                navigationCallback.navigate(
                    AnimeDestination.ActivityDetails(
                        activityId = activity.id.toString(),
                        sharedTransitionScopeKey = sharedTransitionScopeKey,
                    )
                )
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
    viewer: AniListViewer?,
    activity: MessageActivityFragment?,
    sharedTransitionKey: SharedTransitionKey?,
    messenger: UserNavigationData?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    allowUserClick: Boolean = false,
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
                loading = activity == null,
                user = messenger,
                clickable = allowUserClick,
            )
        }

        Column(Modifier.weight(1f)) {
            val messengerName = if (activity == null) "USERNAME" else messenger?.name
            if (messengerName != null) {
                Text(
                    text = messengerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .placeholder(
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
            likes = activity?.likeCount,
            viewer = viewer,
            liked = entry?.liked ?: false,
            subscribed = entry?.subscribed ?: false,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    val navigationCallback = LocalNavigationCallback.current
    val userImage = activity?.recipient?.avatar?.large
    val userImageState = rememberCoilImageState(userImage)
    val userSharedTransitionKey = activity?.recipient?.id?.toString()
        ?.let { SharedTransitionKey.makeKeyForId(it) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .align(Alignment.End)
            .clickable {
                activity?.recipient?.let {
                    navigationCallback.navigate(
                        AnimeDestination.User(
                            userId = it.id.toString(),
                            sharedTransitionKey = userSharedTransitionKey,
                            headerParams = UserHeaderParams(
                                name = it.name,
                                bannerImage = null,
                                coverImage = userImageState.toImageState(),
                            )
                        )
                    )
                }
            }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
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
        if (activity == null || userImage != null) {
            UserAvatarImage(
                imageState = userImageState,
                image = userImageState.request().build(),
                modifier = Modifier
                    .size(32.dp)
                    .sharedElement(userSharedTransitionKey, "userImage")
                    .clip(RoundedCornerShape(12.dp))
                    .placeholder(
                        visible = activity == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }

    if (activity == null || activity.message != null) {
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ImageHtmlText(
            text = activity?.message ?: "Placeholder text",
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current },
            onClickFallback = {
                if (activity != null && clickable) {
                    navigationCallback.navigate(
                        AnimeDestination.ActivityDetails(
                            activityId = activity.id.toString(),
                            sharedTransitionScopeKey = sharedTransitionScopeKey,
                        )
                    )
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
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
    modifier: Modifier = Modifier,
    allowUserClick: Boolean = true,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    onClickDelete: (String) -> Unit = {},
) {
    ListActivitySmallCard(
        viewer = viewer,
        activity = activity,
        showMedia = false,
        entry = null,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        onClickListEdit = onClickListEdit,
        allowUserClick = allowUserClick,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        modifier = modifier,
    )
}

@Composable
fun ListActivitySmallCard(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    mediaEntry: AnimeMediaCompactListRow.Entry?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
    modifier: Modifier = Modifier,
    allowUserClick: Boolean = true,
    clickable: Boolean = false,
    showMedia: Boolean = true,
    showActionsRow: Boolean = false,
    onClickDelete: ((String) -> Unit)? = null,
) {
    ListActivitySmallCard(
        viewer = viewer,
        activity = activity,
        showMedia = showMedia,
        entry = mediaEntry,
        liked = entry?.liked ?: false,
        subscribed = entry?.subscribed ?: false,
        onActivityStatusUpdate = onActivityStatusUpdate,
        onClickListEdit = onClickListEdit,
        allowUserClick = allowUserClick,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        modifier = modifier,
    )
}

@Composable
private fun ListActivitySmallCard(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    showMedia: Boolean,
    entry: AnimeMediaCompactListRow.Entry?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
    allowUserClick: Boolean,
    clickable: Boolean,
    showActionsRow: Boolean,
    onClickDelete: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val sharedTransitionKey = activity?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
    val content: @Composable ColumnScope.() -> Unit = {
        ListActivityCardContent(
            viewer = viewer,
            activity = activity,
            user = activity?.user,
            entry = entry,
            liked = liked,
            subscribed = subscribed,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
            showMedia = showMedia,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
            allowUserClick = allowUserClick,
        )
    }
    if (clickable && activity != null) {
        val navigationCallback = LocalNavigationCallback.current
        ElevatedCard(
            onClick = {
                navigationCallback.navigate(
                    AnimeDestination.ActivityDetails(
                        activityId = activity.id.toString(),
                        sharedTransitionScopeKey = sharedTransitionScopeKey,
                    )
                )
            },
            modifier = modifier.sharedElement(sharedTransitionKey, "activity_card"),
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier.sharedElement(sharedTransitionKey, "activity_card"),
            content = content,
        )
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ListActivityCardContent(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    user: UserNavigationData?,
    entry: AnimeMediaCompactListRow.Entry?,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
    showMedia: Boolean = entry != null,
    showUser: Boolean = true,
    showActionsRow: Boolean = false,
    onClickDelete: ((String) -> Unit)? = null,
    allowUserClick: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        val image = user?.avatar?.large
        if (showUser && (activity == null || image != null)) {
            UserImage(
                loading = activity == null,
                user = user,
                clickable = allowUserClick,
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
            likes = activity?.likeCount,
            viewer = viewer,
            liked = liked,
            subscribed = subscribed,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    if (showMedia) {
        AnimeMediaCompactListRow(
            viewer = viewer,
            entry = entry,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            onClickListEdit = onClickListEdit
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
    onClickDelete: ((String) -> Unit)?,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isViewer) {
            IconButton(onClick = { if (activityId != null) onClickDelete?.invoke(activityId) }) {
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
    likes: Int?,
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
                modifier = Modifier
                    .placeholder(
                        visible = activityId == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .padding(start = 4.dp)
            )
        }
        Icon(
            imageVector = if (replyCount == 0) {
                Icons.Outlined.ModeComment
            } else {
                Icons.AutoMirrored.Filled.Comment
            },
            contentDescription = stringResource(
                R.string.anime_activity_replies_icon_content_description
            ),
            modifier = Modifier
                .size(36.dp)
                .padding(6.dp)
        )

        val likeCount = likes ?: 0
        if (activityId == null || likeCount > 0) {
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.labelMedium.copy(),
                modifier = Modifier
                    .placeholder(
                        visible = activityId == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .padding(start = 4.dp)
            )
        }
        IconButton(
            enabled = viewer != null,
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
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = stringResource(
                    R.string.anime_activity_like_icon_content_description
                ),
                modifier = Modifier.size(24.dp)
            )
        }

        if (viewer != null) {
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
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun UserImage(
    loading: Boolean,
    user: UserNavigationData?,
    clickable: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val navigationCallback = LocalNavigationCallback.current
    val imageState = rememberCoilImageState(user?.avatar?.large)
    val sharedTransitionKey = user?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    UserAvatarImage(
        imageState = imageState,
        image = imageState.request().build(),
        modifier = Modifier
            .size(40.dp)
            .sharedElement(sharedTransitionKey, "user_image")
            .clip(shape)
            .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
            .clickable(enabled = clickable) {
                if (user != null) {
                    navigationCallback.navigate(
                        AnimeDestination.User(
                            userId = user.id.toString(),
                            sharedTransitionKey = sharedTransitionKey,
                            headerParams = UserHeaderParams(
                                name = user.name,
                                bannerImage = null,
                                coverImage = imageState.toImageState(),
                            )
                        )
                    )
                }
            }
            .placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

fun LazyListScope.activitiesSection(
    viewer: AniListViewer?,
    activityTab: AnimeMediaDetailsActivityViewModel.ActivityTab,
    activities: ImmutableList<AnimeMediaDetailsActivityViewModel.ActivityEntry>?,
    onActivityTabChange: (AnimeMediaDetailsActivityViewModel.ActivityTab) -> Unit,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClickViewAll: (AnimeNavigator.NavigationCallback) -> Unit,
    onClickListEdit: (MediaNavigationData) -> Unit,
) {
    item("activitiesHeader") {
        val navigationCallback = LocalNavigationCallback.current
        DetailsSectionHeader(
            text = stringResource(R.string.anime_media_details_activities_label),
            modifier = Modifier.clickable { onClickViewAll(navigationCallback) },
            onClickViewAll = { onClickViewAll(navigationCallback) },
            viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description
        )
    }

    if (viewer != null) {
        item("activitiesTabHeader") {
            TabRow(
                selectedTabIndex = if (activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) 0 else 1,
                modifier = Modifier
                    .padding(bottom = if (activities.isNullOrEmpty()) 0.dp else 16.dp)
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING,
                    onClick = { onActivityTabChange(AnimeMediaDetailsActivityViewModel.ActivityTab.FOLLOWING) },
                    text = {
                        Text(stringResource(R.string.anime_media_details_activity_following))
                    },
                )
                Tab(
                    selected = activityTab == AnimeMediaDetailsActivityViewModel.ActivityTab.GLOBAL,
                    onClick = { onActivityTabChange(AnimeMediaDetailsActivityViewModel.ActivityTab.GLOBAL) },
                    text = {
                        Text(stringResource(R.string.anime_media_details_activity_global))
                    },
                )
            }
        }
    }

    listSectionWithoutHeader(
        titleRes = R.string.anime_media_details_activities_label,
        values = activities,
        valueToId = { it.activityId },
        aboveFold = AnimeActivityComposables.ACTIVITIES_ABOVE_FOLD,
        hasMoreValues = true,
        noResultsTextRes = R.string.anime_media_details_activities_no_results,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onClickViewAll = onClickViewAll,
    ) { item, paddingBottom ->
        ListActivitySmallCard(
            viewer = viewer,
            activity = item.activity,
            entry = item,
            onActivityStatusUpdate = onActivityStatusUpdate,
            onClickListEdit = onClickListEdit,
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom),
            clickable = true
        )
    }
}
