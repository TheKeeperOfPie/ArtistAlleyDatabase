@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.forum

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anilist.fragment.ForumThread
import com.anilist.fragment.UserNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import java.time.Instant
import java.time.ZoneOffset

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
fun ThreadCard(screenKey: String, thread: ForumThread?, modifier: Modifier = Modifier) {
    val navigationCallback = LocalNavigationCallback.current
    ElevatedCard(
        onClick = {
            if (thread != null) {
                navigationCallback.onForumThreadClick(thread.title, thread.id.toString())
            }
        },
        modifier = modifier,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = thread?.title ?: "Placeholder title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.placeholder(
                        visible = thread == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )

                val userName = thread?.user?.name
                val timestamp = thread?.createdAt?.let(AniListUtils::relativeTimestamp)
                if (thread == null || userName != null || timestamp != null) {
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
                        modifier = Modifier.placeholder(
                            visible = thread == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Reply,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_last_reply_icon_content_description
                        ),
                    )

                    ThreadAuthor(
                        screenKey = screenKey,
                        loading = thread == null,
                        user = thread?.replyUser,
                        aniListTimestamp = thread?.repliedAt,
                    )
                }
            }

            ThreadViewReplyCountIcons(
                viewCount = thread?.viewCount,
                replyCount = thread?.replyCount,
            )
        }

        if (thread != null) {
            ThreadCategoryRow(thread)
        }
    }
}

@Composable
fun ThreadViewReplyCountIcons(viewCount: Int?, replyCount: Int?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(top = 12.dp, end = 12.dp)
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
fun ThreadCategoryRow(thread: ForumThread) {
    val categories = thread.categories?.filterNotNull().orEmpty()
    val mediaCategories = thread.mediaCategories?.filterNotNull().orEmpty()
    if (categories.isEmpty() && mediaCategories.isEmpty()) return

    val navigationCallback = LocalNavigationCallback.current
    LazyRow(
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(top = 4.dp, bottom = 10.dp)
            .fillMaxWidth()
            // SubcomposeLayout doesn't support fill max width, so use a really large number.
            // The parent will clamp the actual width so all content still fits on screen.
            .size(width = LocalConfiguration.current.screenWidthDp.dp, height = 24.dp)
            .fadingEdgeEnd(
                startOpaque = 12.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
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
        aniListTimestamp?.let {
            DateUtils.getRelativeTimeSpanString(
                it * 1000L,
                Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                0,
                DateUtils.FORMAT_ABBREV_ALL,
            )
        }
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
    SharedElement(key = "anime_user_${user?.id}_image", screenKey = screenKey) {
        val navigationCallback = LocalNavigationCallback.current
        AsyncImage(
            model = user?.avatar?.large,
            contentDescription = stringResource(R.string.anime_user_image),
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
}
