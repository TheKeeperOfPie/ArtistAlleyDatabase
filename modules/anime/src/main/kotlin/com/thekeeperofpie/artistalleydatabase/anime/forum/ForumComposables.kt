@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.forum

import android.text.Spanned
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Reply
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
import coil.compose.AsyncImage
import com.anilist.AuthedUserQuery
import com.anilist.fragment.ForumThread
import com.anilist.fragment.UserNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentChild
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.compose.openForceExternal

@Composable
fun ThreadCompactCard(thread: ForumThread, modifier: Modifier = Modifier) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(
        onClick = { navigationCallback.onForumThreadClick(thread.title, thread.id.toString()) },
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
    viewer: AuthedUserQuery.Data.Viewer?,
    entry: ForumThreadEntry,
    onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationCallback = LocalNavigationCallback.current
    val thread = entry.thread
    ElevatedCard(
        onClick = {
            navigationCallback.onForumThreadClick(thread.title, thread.id.toString())
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
fun ThreadCard(screenKey: String, thread: ForumThread?, modifier: Modifier = Modifier) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(
        onClick = {
            if (thread != null) {
                navigationCallback.onForumThreadClick(thread.title, thread.id.toString())
            }
        },
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .then(modifier),
    ) {
        ThreadCardContent(screenKey, thread)
    }
}

@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.ThreadCardContent(
    screenKey: String,
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
            if (thread == null || user?.avatar?.large != null) {
                UserImage(
                    screenKey = screenKey,
                    loading = thread == null,
                    user = user,
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
                    Icons.Filled.Comment
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
                onClick = { navigationCallback.onForumCategoryClick(it.name, it.id.toString()) },
                label = { Text(it.name) }
            )
        }
        items(mediaCategories, { "mediaCategory-${it.id}" }) {
            it.title?.primaryTitle()?.let { title ->
                SuggestionChip(
                    onClick = {
                        navigationCallback.onForumMediaCategoryClick(
                            title,
                            it.id.toString()
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
    screenKey: String,
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
        if (loading || image != null) {
            UserImage(
                screenKey = screenKey,
                loading = loading,
                user = user,
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
            .size(32.dp)
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

@Composable
fun ThreadHeader(
    screenKey: String,
    threadId: String,
    viewer: AuthedUserQuery.Data.Viewer?,
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
                    screenKey = screenKey,
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

        if (thread == null || !entry.bodyMarkdown.isNullOrBlank()) {
            var bodyShown by rememberSaveable(thread?.title) {
                val mayHaveSpoilers =
                    thread?.title?.contains("Spoilers", ignoreCase = true) == true
                mutableStateOf(!mayHaveSpoilers)
            }
            if (bodyShown) {
                MarkdownText(
                    text = entry?.bodyMarkdown,
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
            if (thread?.user != null && viewer?.id == thread.user?.id) {
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
    commentMarkdown: Spanned?,
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
                text = commentMarkdown,
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
    screenKey: String,
    threadId: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    entry: ForumCommentEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, Spanned?) -> Unit)? = null,
    onClickReplyComment: ((String, Spanned?) -> Unit)? = null,
) {
    // TODO: Child comments

    val comment = entry?.comment
    Column(modifier = Modifier.fillMaxWidth()) {
        ThreadCommentContent(
            screenKey = screenKey,
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
                    screenKey = screenKey,
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

@Composable
fun ThreadCommentContent(
    screenKey: String,
    threadId: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    loading: Boolean,
    commentId: String?,
    commentMarkdown: Spanned?,
    createdAt: Int?,
    liked: Boolean,
    likeCount: Int,
    user: UserNavigationData?,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, Spanned?) -> Unit)? = null,
    onClickReplyComment: ((String, Spanned?) -> Unit)? = null,
) {
    val navigationCallback = LocalNavigationCallback.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clickable {
                if (user != null) {
                    navigationCallback.onUserClick(user, 1f)
                }
            }
            .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 8.dp)
    ) {
        val image = user?.avatar?.large
        if (loading || image != null) {
            UserImage(
                screenKey = screenKey,
                loading = loading,
                user = user,
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
            if (onClickDelete != null && viewer != null && viewer.id == user?.id) {
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
                                    imageVector = Icons.Filled.Reply,
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
        text = commentMarkdown,
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
    screenKey: String,
    threadId: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    level: Int,
    child: ForumCommentChild,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: ((String, Spanned?) -> Unit)? = null,
    onClickReplyComment: ((String, Spanned?) -> Unit)? = null,
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
        ThreadCommentContent(
            screenKey = screenKey,
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

    child.childComments.forEach {
        ThreadCommentChild(
            screenKey = screenKey,
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
                imageVector = Icons.Filled.ArrowBack,
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
                imageVector = Icons.Filled.ArrowForward,
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
                imageVector = Icons.Filled.LastPage,
                contentDescription = stringResource(
                    R.string.anime_forum_thread_comments_page_last_content_description
                )
            )
        }
    }
}
