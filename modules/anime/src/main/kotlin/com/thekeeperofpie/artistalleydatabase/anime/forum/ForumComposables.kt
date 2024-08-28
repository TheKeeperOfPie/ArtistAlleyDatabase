@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anilist.fragment.ForumThread
import com.anilist.fragment.UserNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentChild
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.anime.user.UserHeaderParams
import com.thekeeperofpie.artistalleydatabase.compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.compose.image.CoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.compose.image.request
import com.thekeeperofpie.artistalleydatabase.compose.openForceExternal
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd

object ForumComposables {
    const val FORUM_THREADS_ABOVE_FOLD = 3
}

@Composable
fun ThreadCompactCard(thread: ForumThread, modifier: Modifier = Modifier) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(
        onClick = {
            navigationCallback.navigate(
                AnimeDestination.ForumThread(
                    threadId = thread.id.toString(),
                    title = thread.title,
                )
            )
        },
        modifier = modifier,
    ) {
        Text(
            text = thread.title.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ThreadSmallCard(
    viewer: AniListViewer?,
    entry: ForumThreadEntry,
    onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationCallback = LocalNavigationCallback.current
    val thread = entry.thread
    ElevatedCard(
        onClick = {
            navigationCallback.navigate(
                AnimeDestination.ForumThread(
                    threadId = thread.id.toString(),
                    title = thread.title,
                )
            )
        },
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .then(modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = thread.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                val userName = thread.replyUser?.name ?: thread.user?.name
                val timestamp = (thread.repliedAt ?: thread.createdAt)
                    .let(AniListUtils::relativeTimestamp)
                if (userName != null || timestamp != null) {
                    Text(
                        text = if (userName != null && timestamp != null) {
                            stringResource(
                                R.string.anime_forum_thread_author_and_timestamp,
                                userName,
                                timestamp,
                            )
                        } else {
                            userName ?: timestamp.toString()
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            if (viewer != null) {
                val liked = entry.liked
                val subscribed = entry.subscribed
                IconButton(
                    onClick = {
                        onStatusUpdate(
                            ForumThreadToggleUpdate.Liked(
                                id = thread.id.toString(),
                                liked = !liked,
                                subscribed = subscribed,
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_like_icon_content_description
                        ),
                    )
                }
                IconButton(
                    onClick = {
                        onStatusUpdate(
                            ForumThreadToggleUpdate.Subscribe(
                                id = thread.id.toString(),
                                liked = liked,
                                subscribed = !subscribed,
                            )
                        )
                    }
                ) {
                    Icon(
                        imageVector = if (subscribed) {
                            Icons.Filled.NotificationsActive
                        } else {
                            Icons.Filled.NotificationsNone
                        },
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_subscribe_icon_content_description
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun ThreadCard(thread: ForumThread?, modifier: Modifier = Modifier) {
    val navigationCallback = LocalNavigationCallback.current
    SharedTransitionKeyScope("forum_thread_${thread?.id}") {
        ElevatedCard(
            onClick = {
                if (thread != null) {
                    navigationCallback.navigate(
                        AnimeDestination.ForumThread(
                            threadId = thread.id.toString(),
                            title = thread.title,
                        )
                    )
                }
            },
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .then(modifier),
        ) {
            ThreadCardContent(thread)
        }
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ThreadCardContent(
    thread: ForumThread?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(modifier)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            Text(
                text = thread?.title ?: "Placeholder title",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .placeholder(
                        visible = thread == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            val userName = thread?.user?.name
            val createdAt = thread?.createdAt?.let(AniListUtils::relativeTimestamp)
            if (thread == null || userName != null || createdAt != null) {
                Text(
                    text = if (userName != null && createdAt != null) {
                        stringResource(
                            R.string.anime_forum_thread_author_and_timestamp,
                            userName,
                            createdAt,
                        )
                    } else {
                        userName ?: createdAt.toString()
                    },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .placeholder(
                            visible = thread == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(12.dp))

            if (thread != null) {
                ThreadCategoryRow(thread)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            ThreadViewReplyCountIcons(
                viewCount = thread?.viewCount,
                replyCount = thread?.replyCount,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(Modifier.height(12.dp))

            val user = thread?.replyUser ?: thread?.user
            val image = user?.avatar?.large
            val imageState = rememberCoilImageState(image)
            val sharedTransitionKey = user?.id?.toString()
                ?.let { SharedTransitionKey.makeKeyForId(it) }
            if (thread == null || image != null) {
                UserImage(
                    loading = thread == null,
                    user = user,
                    sharedTransitionKey = sharedTransitionKey,
                    imageState = imageState,
                )

                Spacer(Modifier.height(4.dp))
            }

            val timestamp = (thread?.repliedAt ?: thread?.createdAt)
                ?.let(AniListUtils::relativeTimestamp)
            if (thread == null || timestamp != null) {
                Text(
                    text = timestamp.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .widthIn(max = 180.dp)
                        .placeholder(
                            visible = thread == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }
    }
}

@Composable
fun ThreadViewReplyCountIcons(viewCount: Int?, replyCount: Int?, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.placeholder(
                visible = viewCount == null,
                highlight = PlaceholderHighlight.shimmer(),
            )
        ) {
            Text(
                text = viewCount.toString(),
                style = MaterialTheme.typography.labelMedium,
            )

            Icon(
                imageVector = Icons.Filled.Visibility,
                contentDescription = stringResource(
                    R.string.anime_forum_view_count_content_description
                ),
                modifier = Modifier.size(16.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.placeholder(
                visible = replyCount == null,
                highlight = PlaceholderHighlight.shimmer(),
            )
        ) {
            Text(
                text = replyCount.toString(),
                style = MaterialTheme.typography.labelSmall,
            )

            Icon(
                imageVector = if (replyCount == 0) {
                    Icons.Outlined.ModeComment
                } else {
                    Icons.AutoMirrored.Filled.Comment
                },
                contentDescription = stringResource(
                    R.string.anime_forum_reply_count_icon_content_description
                ),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ThreadCategoryRow(thread: ForumThread, modifier: Modifier = Modifier) {
    val categories = thread.categories?.filterNotNull().orEmpty()
    val mediaCategories = thread.mediaCategories?.filterNotNull().orEmpty()
    if (categories.isEmpty() && mediaCategories.isEmpty()) return

    val navigationCallback = LocalNavigationCallback.current
    LazyRow(
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            // SubcomposeLayout doesn't support fill max width, so use a really large number.
            // The parent will clamp the actual width so all content still fits on screen.
            .size(width = LocalConfiguration.current.screenWidthDp.dp, height = 24.dp)
            .fadingEdgeEnd(
                startOpaque = 12.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
            .then(modifier)
    ) {
        // TODO: Enforce unique IDs
        items(categories, { "category-${it.id}" }) {
            SuggestionChip(
                onClick = {
                    navigationCallback.navigate(
                        AnimeDestination.ForumSearch(
                            title = it.name,
                            categoryId = it.id.toString(),
                        )
                    )
                },
                label = { Text(it.name) }
            )
        }
        items(mediaCategories, { "mediaCategory-${it.id}" }) {
            it.title?.primaryTitle()?.let { title ->
                SuggestionChip(
                    onClick = {
                        navigationCallback.navigate(
                            AnimeDestination.ForumSearch(
                                title = title,
                                mediaCategoryId = it.id.toString(),
                            )
                        )
                    },
                    label = { Text(text = title) }
                )
            }
        }
    }
}

@Composable
fun ThreadCommentTimestamp(loading: Boolean, aniListTimestamp: Int?) {
    val timestamp = remember(aniListTimestamp) {
        aniListTimestamp?.let(AniListUtils::relativeTimestamp)
    }

    if (loading || timestamp != null) {
        Text(
            text = timestamp.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
        )
    }
}

@Composable
fun ThreadAuthor(
    loading: Boolean,
    user: UserNavigationData?,
    aniListTimestamp: Int?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val image = user?.avatar?.large
        val imageState = rememberCoilImageState(image)
        val sharedTransitionKey = user?.id?.toString()
            ?.let { SharedTransitionKey.makeKeyForId(it) }
        if (loading || image != null) {
            UserImage(
                loading = loading,
                user = user,
                sharedTransitionKey = sharedTransitionKey,
                imageState = imageState,
            )
        }

        Column {
            Text(
                text = user?.name ?: "USERNAME",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.placeholder(
                    visible = loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
            )

            ThreadCommentTimestamp(loading = loading, aniListTimestamp = aniListTimestamp)
        }
    }
}

@Composable
private fun UserImage(
    loading: Boolean,
    user: UserNavigationData?,
    sharedTransitionKey: SharedTransitionKey?,
    imageState: CoilImageState,
) {
    val shape = RoundedCornerShape(12.dp)
    val navigationCallback = LocalNavigationCallback.current
    UserAvatarImage(
        imageState = imageState,
        image = imageState.request().build(),
        modifier = Modifier
            .size(32.dp)
            .sharedElement(sharedTransitionKey, "user_image")
            .clip(shape)
            .border(width = Dp.Hairline, MaterialTheme.colorScheme.primary, shape)
            .clickable {
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

@Composable
fun ThreadHeader(
    threadId: String,
    viewer: AniListViewer?,
    entry: ForumThreadEntry?,
    onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
) {
    val thread = entry?.thread
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread?.title ?: "Placeholder title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .placeholder(
                            visible = entry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

                ThreadAuthor(
                    loading = entry == null,
                    user = thread?.user,
                    aniListTimestamp = thread?.createdAt,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                ThreadViewReplyCountIcons(
                    viewCount = thread?.viewCount,
                    replyCount = thread?.replyCount,
                    modifier = Modifier.padding(top = 12.dp, end = 12.dp)
                )

                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_more_actions_content_description,
                            ),
                        )
                    }

                    val uriHandler = LocalUriHandler.current
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.anime_forum_thread_open_in_browser)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.OpenInBrowser,
                                    contentDescription = stringResource(
                                        R.string.anime_forum_thread_open_in_browser_content_description
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                uriHandler.openForceExternal(
                                    AniListUtils.forumThreadUrl(threadId)
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (thread != null) {
            ThreadCategoryRow(thread)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (thread == null || !entry.bodyMarkdown?.value.isNullOrBlank()) {
            var bodyShown by rememberSaveable(thread?.title) {
                val mayHaveSpoilers =
                    thread?.title?.contains("Spoilers", ignoreCase = true) == true
                mutableStateOf(!mayHaveSpoilers)
            }
            if (bodyShown) {
                MarkdownText(
                    markdownText = entry?.bodyMarkdown,
                    textColor = MaterialTheme.typography.bodySmall.color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .placeholder(
                            visible = entry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            } else {
                Button(
                    onClick = { bodyShown = true },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(R.string.anime_forum_thread_show_body_spoilers))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (thread?.user != null && viewer?.id == thread.user?.id?.toString()) {
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_delete_content_description
                        ),
                    )
                }
            }

            if (viewer != null) {
                val liked = entry?.liked ?: false
                val subscribed = entry?.subscribed ?: false
                IconButton(
                    onClick = {
                        if (thread != null) {
                            onStatusUpdate(
                                ForumThreadToggleUpdate.Liked(
                                    id = thread.id.toString(),
                                    liked = !liked,
                                    subscribed = subscribed,
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_like_icon_content_description
                        ),
                    )
                }
                IconButton(
                    onClick = {
                        if (thread != null) {
                            onStatusUpdate(
                                ForumThreadToggleUpdate.Subscribe(
                                    id = thread.id.toString(),
                                    liked = liked,
                                    subscribed = !subscribed,
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (subscribed) {
                            Icons.Filled.NotificationsActive
                        } else {
                            Icons.Filled.NotificationsNone
                        },
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_subscribe_icon_content_description
                        ),
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            IconButton(onClick = {
                if (thread != null) {
                    uriHandler.openUri(
                        AniListUtils.forumThreadUrl(thread.id.toString()) +
                                "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                    )
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_open_in_browser_content_description
                    ),
                )
            }
        }
    }
}

@Composable
fun ThreadDeleteCommentPrompt(
    commentId: String,
    commentMarkdown: MarkdownText?,
    deleting: () -> Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    R.string.anime_forum_thread_comment_delete_confirmation
                )
            )
        },
        text = {
            MarkdownText(
                markdownText = commentMarkdown,
                textColor = MaterialTheme.typography.bodySmall.color,
            )
        },
        confirmButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                val loadingAlpha by animateFloatAsState(
                    targetValue = if (deleting()) 1f else 0f,
                    label = "Forum thread comment deleting crossfade",
                )
                TextButton(
                    onClick = { onConfirmDelete(commentId) },
                    modifier = Modifier.alpha(1f - loadingAlpha)
                ) {
                    Text(
                        text = stringResource(
                            R.string.anime_forum_thread_comment_delete
                        )
                    )
                }
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxHeight()
                        .alpha(loadingAlpha)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(
                        R.string.anime_forum_thread_comment_cancel
                    )
                )
            }
        }
    )
}

// TODO: Add updatedAt timestamp
@Composable
fun ThreadComment(
    threadId: String,
    viewer: AniListViewer?,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, MarkdownText?) -> Unit)? = null,
    onClickReplyComment: ((String, MarkdownText?) -> Unit)? = null,
) {
    // TODO: Child comments

    SharedTransitionKeyScope("forum_thread_comment_${entry?.comment?.id}") {
        val comment = entry?.comment
        Column(modifier = Modifier.fillMaxWidth()) {
            ThreadCommentContent(
                threadId = threadId,
                viewer = viewer,
                loading = entry == null,
                commentId = comment?.id?.toString(),
                commentMarkdown = entry?.commentMarkdown,
                createdAt = comment?.createdAt,
                liked = entry?.liked ?: false,
                likeCount = comment?.likeCount ?: 0,
                user = entry?.user,
                onStatusUpdate = onStatusUpdate,
                onClickDelete = onClickDelete,
                onClickReplyComment = onClickReplyComment,
            )

            val children = entry?.children
            if (!children.isNullOrEmpty()) {
                children.forEach {
                    ThreadCommentChild(
                        threadId = threadId,
                        viewer = viewer,
                        level = 1,
                        child = it,
                        onStatusUpdate = onStatusUpdate,
                        onClickDelete = onClickDelete,
                        onClickReplyComment = onClickReplyComment,
                    )
                }
            }

            HorizontalDivider()
        }
    }
}

@Composable
fun ThreadCommentContent(
    threadId: String,
    viewer: AniListViewer?,
    loading: Boolean,
    commentId: String?,
    commentMarkdown: MarkdownText?,
    createdAt: Int?,
    liked: Boolean,
    likeCount: Int,
    user: UserNavigationData?,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, MarkdownText?) -> Unit)? = null,
    onClickReplyComment: ((String, MarkdownText?) -> Unit)? = null,
) {
    val navigationCallback = LocalNavigationCallback.current
    val imageState = rememberCoilImageState(user?.avatar?.large)
    val sharedTransitionKey = user?.id?.toString()?.let { SharedTransitionKey.makeKeyForId(it) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable {
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
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 8.dp)
    ) {
        val image = user?.avatar?.large
        if (loading || image != null) {
            UserImage(
                loading = loading,
                user = user,
                sharedTransitionKey = sharedTransitionKey,
                imageState = imageState,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user?.name ?: "USERNAME",
                modifier = Modifier.placeholder(
                    visible = loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
            )

            ThreadCommentTimestamp(
                loading = loading,
                aniListTimestamp = createdAt,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClickDelete != null && viewer != null && viewer.id == user?.id?.toString()) {
                IconButton(onClick = {
                    if (commentId != null) {
                        onClickDelete(commentId, commentMarkdown)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_comment_delete_icon_content_description
                        ),
                    )
                }
            }

            if (loading || likeCount > 0) {
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
            IconButton(
                enabled = viewer != null,
                onClick = {
                    if (commentId != null) {
                        onStatusUpdate(commentId, !liked)
                    }
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (liked) {
                        Icons.Filled.ThumbUp
                    } else {
                        Icons.Outlined.ThumbUp
                    },
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_comment_like_icon_content_description
                    ),
                )
            }

            Box {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_more_actions_content_description,
                        ),
                    )
                }

                val uriHandler = LocalUriHandler.current
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(R.string.anime_forum_thread_open_in_browser)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.OpenInBrowser,
                                contentDescription = stringResource(
                                    R.string.anime_forum_thread_open_in_browser_content_description
                                )
                            )
                        },
                        onClick = {
                            showMenu = false
                            if (commentId != null) {
                                uriHandler.openForceExternal(
                                    AniListUtils.forumThreadCommentUrl(threadId, commentId)
                                )
                            }
                        }
                    )
                    if (onClickReplyComment != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.anime_forum_thread_comment_reply)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = stringResource(
                                        R.string.anime_forum_thread_comment_reply_content_description
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                if (commentId != null) {
                                    onClickReplyComment(commentId, commentMarkdown)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    MarkdownText(
        markdownText = commentMarkdown,
        modifier = Modifier
            .conditionally(loading) { fillMaxWidth() }
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            .placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

@Composable
fun ColumnScope.ThreadCommentChild(
    threadId: String,
    viewer: AniListViewer?,
    level: Int,
    child: ForumCommentChild,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, MarkdownText?) -> Unit)? = null,
    onClickReplyComment: ((String, MarkdownText?) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(start = 16.dp + (8.dp * (level - 1)))
            // TODO: Better border drawing, currently very primitive
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RectangleShape,
            )
    ) {
        SharedTransitionKeyScope("forum_thread_comment_child_${level}_${child.id}") {
            ThreadCommentContent(
                threadId = threadId,
                viewer = viewer,
                loading = false,
                commentId = child.id,
                commentMarkdown = child.commentMarkdown,
                createdAt = child.createdAt,
                liked = child.liked,
                likeCount = child.likeCount ?: 0,
                user = child.user,
                onStatusUpdate = onStatusUpdate,
                onClickDelete = onClickDelete,
                onClickReplyComment = onClickReplyComment,
            )
        }
    }

    child.childComments.forEach {
        ThreadCommentChild(
            threadId = threadId,
            viewer = viewer,
            level = level + 1,
            child = it,
            onStatusUpdate = onStatusUpdate,
            onClickDelete = onClickDelete,
            onClickReplyComment = onClickReplyComment,
        )
    }
}

@Composable
fun ThreadPageIndicator(
    page: Int,
    pageString: String,
    maxPage: Int,
    onPageChange: (Int) -> Unit,
    onPageStringChange: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                onPageChange(1)
                onPageStringChange("1")
            },
            modifier = Modifier.alpha(if (page > 1) 1f else 0f)
        ) {
            Icon(
                imageVector = Icons.Filled.FirstPage,
                contentDescription = stringResource(
                    R.string.anime_forum_thread_comments_page_first_content_description
                )
            )
        }
        IconButton(
            onClick = {
                onPageChange(page - 1)
                onPageStringChange("${page - 1}")
            },
            modifier = Modifier.alpha(if (page > 1) 1f else 0f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(
                    R.string.anime_forum_thread_comments_page_back_content_description
                )
            )
        }

        MinWidthTextField(
            value = pageString,
            onValueChange = {
                onPageStringChange(it)
                val asInt = it.trim().toIntOrNull()
                if (asInt != null && asInt >= 1 && asInt <= maxPage) {
                    onPageChange(asInt)
                }
            },
            suffix = { Text(text = "/$maxPage") },
            textStyle = LocalTextStyle.current
                .copy(textAlign = TextAlign.Center),
            minWidth = 80.dp,
            modifier = Modifier
                .widthIn(min = 80.dp)
                .heightIn(min = 0.dp)
        )

        IconButton(
            onClick = {
                onPageChange(page + 1)
                onPageStringChange("${page + 1}")
            },
            modifier = Modifier.alpha(if (page < maxPage) 1f else 0f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(
                    R.string.anime_forum_thread_comments_page_forward_content_description
                )
            )
        }
        IconButton(
            onClick = {
                onPageChange(maxPage)
                onPageStringChange("$maxPage")
            },
            modifier = Modifier.alpha(if (page < maxPage) 1f else 0f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LastPage,
                contentDescription = stringResource(
                    R.string.anime_forum_thread_comments_page_last_content_description
                )
            )
        }
    }
}

fun LazyListScope.forumThreadsSection(
    viewer: AniListViewer?,
    forumThreads: List<ForumThreadEntry>?,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClickViewAll: (AnimeNavigator.NavigationCallback) -> Unit,
    onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
    loading: Boolean,
    requestLoad: () -> Unit,
) {
    listSection(
        headerSideEffect = requestLoad,
        loading = loading,
        titleRes = R.string.anime_media_details_forum_threads_label,
        values = forumThreads,
        valueToId = { it.thread.id.toString() },
        aboveFold = ForumComposables.FORUM_THREADS_ABOVE_FOLD,
        hasMoreValues = true,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        onClickViewAll = onClickViewAll,
        viewAllContentDescriptionTextRes = R.string.anime_media_details_view_all_content_description,
    ) { item, paddingBottom ->
        ThreadSmallCard(
            viewer = viewer,
            entry = item,
            onStatusUpdate = onStatusUpdate,
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
        )
    }
}
