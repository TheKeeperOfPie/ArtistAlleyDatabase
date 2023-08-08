@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anilist.ForumRootQuery
import com.anilist.fragment.ForumThread
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd

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
fun ThreadCard(thread: ForumThread?, modifier: Modifier = Modifier) {
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
                verticalArrangement = Arrangement.spacedBy(4.dp),
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

                val mayHaveSpoilers = thread?.title?.contains("Spoilers", ignoreCase = true) == true
                if (!mayHaveSpoilers) {
                    CustomHtmlText(
                        text = thread?.body ?: "Some forum thread body text",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        style = MaterialTheme.typography.bodySmall,
                        onFallbackClick = {
                            if (thread != null) {
                                navigationCallback.onForumThreadClick(
                                    thread.title,
                                    thread.id.toString(),
                                )
                            }
                        },
                        modifier = Modifier.placeholder(
                            visible = thread == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
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
        modifier = Modifier.padding(top = 8.dp, end = 8.dp)
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

@Preview
@Composable
private fun Thread() {
    ThreadCard(
        thread = ForumRootQuery.Data.Active.Thread(
            title = "Some interesting forum thread title",
            body = "Sample test thread body, sample test thread body, sample test thread body, sample test thread body",
            viewCount = 12345,
            replyCount = 987,
        )
    )
}
