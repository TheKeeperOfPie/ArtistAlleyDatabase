@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.activities.generated.resources.Res
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_delete_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_like_icon_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_message_arrow_recipient_icon_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_open_in_browser_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_replies_icon_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_status_with_timestamp
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_subscribe_icon_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_media_details_activities_label
import artistalleydatabase.modules.anime.activities.generated.resources.anime_media_details_activities_no_results
import artistalleydatabase.modules.anime.activities.generated.resources.anime_media_details_activity_following
import artistalleydatabase.modules.anime.activities.generated.resources.anime_media_details_activity_global
import artistalleydatabase.modules.anime.ui.generated.resources.anime_generic_view_all_content_description
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.fragment.ActivityItem
import com.anilist.data.fragment.ListActivityActivityItem
import com.anilist.data.fragment.ListActivityWithoutMedia
import com.anilist.data.fragment.MessageActivityActivityItem
import com.anilist.data.fragment.MessageActivityFragment
import com.anilist.data.fragment.TextActivityActivityItem
import com.anilist.data.fragment.TextActivityFragment
import com.anilist.data.fragment.UserNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSectionWithoutHeader
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionPrefixKeys
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.anime.ui.generated.resources.Res as UiRes

object AnimeActivityComposables {
    const val ACTIVITIES_ABOVE_FOLD = 3
}

typealias MediaRow<MediaEntry> = @Composable (MediaEntry?, Modifier) -> Unit

@Composable
fun <MediaEntry> ActivityList(
    viewer: AniListViewer?,
    activities: LazyPagingItems<ActivityEntry<MediaEntry>>,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    showMedia: Boolean,
    allowUserClick: Boolean = true,
    sortFilterState: () -> SortFilterController<*>.State?,
    userRoute: UserRoute,
    mediaRow: @Composable (
        MediaEntry?,
        Modifier,
    ) -> Unit,
) {
    val gridState = rememberLazyGridState()
    // TODO: Move this further up
    sortFilterState()?.ImmediateScrollResetEffect(gridState)
    VerticalList(
        gridState = gridState,
        onRefresh = activities::refresh,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 72.dp),
        itemHeaderText = null,
        itemKey = { it.activityId.scopedId },
        itemContentType = { it.activityId.type },
        items = activities,
        item = { entry ->
            SharedTransitionKeyScope("anime_user_activity_card_${entry?.activityId?.valueId}") {
                when (val activity = entry?.activity) {
                    is UserSocialActivityQuery.Data.Page.TextActivityActivity -> TextActivitySmallCard(
                        viewer = viewer,
                        activity = activity,
                        entry = entry,
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        allowUserClick = allowUserClick,
                        clickable = true,
                        userRoute = userRoute,
                    )
                    is UserSocialActivityQuery.Data.Page.ListActivityActivity -> ListActivitySmallCard<MediaEntry>(
                        viewer = viewer,
                        activity = activity,
                        mediaEntry = entry.media,
                        mediaRow = { entry, modifier -> mediaRow(entry, modifier) },
                        entry = entry,
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        allowUserClick = allowUserClick,
                        clickable = true,
                        showMedia = showMedia,
                        userRoute = userRoute,
                    )
                    is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> MessageActivitySmallCard(
                        viewer = viewer,
                        activity = activity,
                        entry = entry,
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        modifier = Modifier.fillMaxWidth(),
                        allowUserClick = allowUserClick,
                        userRoute = userRoute,
                        clickable = true
                    )
                    is UserSocialActivityQuery.Data.Page.OtherActivity,
                    null,
                        -> TextActivitySmallCard(
                        viewer = viewer,
                        activity = null,
                        entry = null,
                        onActivityStatusUpdate = onActivityStatusUpdate,
                        userRoute = userRoute,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

@Composable
fun <MediaEntry> ActivitySmallCard(
    viewer: AniListViewer?,
    activity: ActivityItem?,
    entry: ActivityStatusAware?,
    mediaEntry: MediaEntry?,
    mediaRow: MediaRow<MediaEntry>,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    userRoute: UserRoute,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .recomposeHighlighter()
    when (activity) {
        is TextActivityActivityItem ->
            TextActivitySmallCard(
                viewer = viewer,
                activity = activity,
                entry = entry,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
                userRoute = userRoute,
                modifier = modifier,
            )
        is ListActivityActivityItem ->
            ListActivitySmallCard(
                viewer = viewer,
                activity = activity,
                mediaEntry = mediaEntry,
                mediaRow = mediaRow,
                entry = entry,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
                userRoute = userRoute,
                modifier = modifier,
            )
        is MessageActivityActivityItem ->
            MessageActivitySmallCard(
                viewer = viewer,
                activity = activity,
                entry = entry,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
                userRoute = userRoute,
                modifier = modifier,
            )
        else -> ListActivitySmallCard(
            viewer = viewer,
            activity = null,
            mediaEntry = null,
            mediaRow = mediaRow,
            entry = null,
            onActivityStatusUpdate = onActivityStatusUpdate,
            clickable = false,
            userRoute = userRoute,
            modifier = modifier,
        )
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
    userRoute: UserRoute,
) {
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
            userRoute = userRoute,
        )
    }

    if (clickable && activity != null) {
        val navigationController = LocalNavigationController.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ElevatedCard(
            onClick = {
                navigationController.navigate(
                    ActivityDestinations.ActivityDetails(
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
    userRoute: UserRoute,
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
                userRoute = userRoute,
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
                    HumanReadable.timeAgo(Instant.fromEpochSeconds(it.createdAt.toLong()))
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
            liked = entry?.liked == true,
            subscribed = entry?.subscribed == true,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    if (activity == null || activity.text != null) {
        val navigationController = LocalNavigationController.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ImageHtmlText(
            text = activity?.text ?: "Placeholder text",
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current },
            onClickFallback = {
                if (activity != null && clickable) {
                    navigationController.navigate(
                        ActivityDestinations.ActivityDetails(
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
    userRoute: UserRoute,
    onClickDelete: (String) -> Unit = {},
) {
    val content: @Composable ColumnScope.() -> Unit = {
        MessageActivityCardContent(
            viewer = viewer,
            activity = activity,
            messenger = activity?.messenger,
            entry = entry,
            onActivityStatusUpdate = onActivityStatusUpdate,
            allowUserClick = allowUserClick,
            clickable = clickable,
            showActionsRow = showActionsRow,
            userRoute = userRoute,
            onClickDelete = onClickDelete,
        )
    }

    if (clickable && activity != null) {
        val navigationController = LocalNavigationController.current
        val sharedTransitionScopeKey = LocalSharedTransitionPrefixKeys.current
        ElevatedCard(
            onClick = {
                navigationController.navigate(
                    ActivityDestinations.ActivityDetails(
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
    messenger: UserNavigationData?,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    allowUserClick: Boolean = false,
    clickable: Boolean = false,
    showActionsRow: Boolean = false,
    userRoute: UserRoute,
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
                userRoute = userRoute,
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
                    HumanReadable.timeAgo(Instant.fromEpochSeconds(it.createdAt.toLong()))
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
            liked = entry?.liked == true,
            subscribed = entry?.subscribed == true,
            onActivityStatusUpdate = onActivityStatusUpdate,
        )
    }

    val navigationController = LocalNavigationController.current
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
                    navigationController.navigate(
                        userRoute(
                            it.id.toString(),
                            userSharedTransitionKey,
                            it.name,
                            userImageState.toImageState()
                        )
                    )
                }
            }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowRightAlt,
            contentDescription = stringResource(
                Res.string.anime_activity_message_arrow_recipient_icon_content_description
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
                    navigationController.navigate(
                        ActivityDestinations.ActivityDetails(
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
    userRoute: UserRoute,
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
        mediaRow = { _, _ -> },
        liked = entry?.liked == true,
        subscribed = entry?.subscribed == true,
        onActivityStatusUpdate = onActivityStatusUpdate,
        allowUserClick = allowUserClick,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        userRoute = userRoute,
        modifier = modifier,
    )
}

@Composable
fun <MediaEntry >ListActivitySmallCard(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    mediaEntry: MediaEntry?,
    mediaRow: MediaRow<MediaEntry>,
    entry: ActivityStatusAware?,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    userRoute: UserRoute,
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
        mediaRow = mediaRow,
        liked = entry?.liked == true,
        subscribed = entry?.subscribed == true,
        onActivityStatusUpdate = onActivityStatusUpdate,
        allowUserClick = allowUserClick,
        clickable = clickable,
        showActionsRow = showActionsRow,
        onClickDelete = onClickDelete,
        userRoute = userRoute,
        modifier = modifier,
    )
}

@Composable
private fun <MediaEntry> ListActivitySmallCard(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    showMedia: Boolean,
    entry: MediaEntry?,
    mediaRow: MediaRow<MediaEntry>,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    allowUserClick: Boolean,
    clickable: Boolean,
    showActionsRow: Boolean,
    onClickDelete: ((String) -> Unit)?,
    userRoute: UserRoute,
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
            mediaRow = mediaRow,
            liked = liked,
            subscribed = subscribed,
            onActivityStatusUpdate = onActivityStatusUpdate,
            showMedia = showMedia,
            showActionsRow = showActionsRow,
            onClickDelete = onClickDelete,
            allowUserClick = allowUserClick,
            userRoute = userRoute,
        )
    }
    if (clickable && activity != null) {
        val navigationController = LocalNavigationController.current
        ElevatedCard(
            onClick = {
                navigationController.navigate(
                    ActivityDestinations.ActivityDetails(
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
fun <MediaEntry> ColumnScope.ListActivityCardContent(
    viewer: AniListViewer?,
    activity: ListActivityWithoutMedia?,
    user: UserNavigationData?,
    entry: MediaEntry?,
    mediaRow: MediaRow<MediaEntry>,
    liked: Boolean,
    subscribed: Boolean,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    showMedia: Boolean = entry != null,
    showUser: Boolean = true,
    showActionsRow: Boolean = false,
    onClickDelete: ((String) -> Unit)? = null,
    allowUserClick: Boolean = true,
    userRoute: UserRoute,
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
                userRoute = userRoute,
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
                    HumanReadable.timeAgo(Instant.fromEpochSeconds(it.createdAt.toLong()))
                }
            }
            val summaryText = if (status.isNotBlank()) {
                stringResource(
                    Res.string.anime_activity_status_with_timestamp,
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
        mediaRow(entry, Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
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
                        Res.string.anime_activity_delete_content_description
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
                    Res.string.anime_activity_open_in_browser_content_description
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
                Res.string.anime_activity_replies_icon_content_description
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
                    Res.string.anime_activity_like_icon_content_description
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
                        Res.string.anime_activity_subscribe_icon_content_description
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
    userRoute: UserRoute,
) {
    val shape = RoundedCornerShape(12.dp)
    val navigationController = LocalNavigationController.current
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
            .placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

fun LazyGridScope.activitiesSection(
    viewer: AniListViewer?,
    activityTab: ActivityTab,
    activities: List<MediaActivityEntry>?,
    onActivityTabChange: (ActivityTab) -> Unit,
    onActivityStatusUpdate: (ActivityToggleUpdate) -> Unit,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClickViewAll: () -> Unit,
    userRoute: UserRoute,
) {
    item(
        key = "activitiesHeader",
        span = GridUtils.maxSpanFunction,
        contentType = "activitiesHeader",
    ) {
        DetailsSectionHeader(
            text = stringResource(Res.string.anime_media_details_activities_label),
            onClickViewAll = onClickViewAll,
            viewAllContentDescriptionTextRes = UiRes.string.anime_generic_view_all_content_description
        )
    }

    if (viewer != null) {
        item(
            key = "activitiesTabHeader",
            span = GridUtils.maxSpanFunction,
            contentType = "activitiesTabHeader",
        ) {
            TabRow(
                selectedTabIndex = if (activityTab == ActivityTab.FOLLOWING) 0 else 1,
                modifier = Modifier
                    .padding(bottom = if (activities.isNullOrEmpty()) 0.dp else 16.dp)
                    .fillMaxWidth()
            ) {
                Tab(
                    selected = activityTab == ActivityTab.FOLLOWING,
                    onClick = { onActivityTabChange(ActivityTab.FOLLOWING) },
                    text = {
                        Text(stringResource(Res.string.anime_media_details_activity_following))
                    },
                )
                Tab(
                    selected = activityTab == ActivityTab.GLOBAL,
                    onClick = { onActivityTabChange(ActivityTab.GLOBAL) },
                    text = {
                        Text(stringResource(Res.string.anime_media_details_activity_global))
                    },
                )
            }
        }
    }

    listSectionWithoutHeader(
        titleRes = Res.string.anime_media_details_activities_label,
        values = activities,
        valueToId = { it.activityId },
        aboveFold = AnimeActivityComposables.ACTIVITIES_ABOVE_FOLD,
        hasMoreValues = true,
        noResultsTextRes = Res.string.anime_media_details_activities_no_results,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onClickViewAll = onClickViewAll,
    ) { item, paddingBottom ->
        SharedTransitionKeyScope("anime_activity_card_${item.activityId}") {
            ListActivitySmallCard(
                viewer = viewer,
                activity = item.activity,
                entry = item,
                onActivityStatusUpdate = onActivityStatusUpdate,
                clickable = true,
                userRoute = userRoute,
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
            )
        }
    }
}
