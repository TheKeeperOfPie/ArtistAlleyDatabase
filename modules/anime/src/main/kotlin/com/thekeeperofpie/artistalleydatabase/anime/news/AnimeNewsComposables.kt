package com.thekeeperofpie.artistalleydatabase.anime.news

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.news.AnimeNewsEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimeNewsSmallCard(entry: AnimeNewsEntry<*>?) {
    val uriHandler = LocalUriHandler.current
    val onClick = entry?.link?.let { { uriHandler.openUri(it) } }
    val content: @Composable ColumnScope.() -> Unit = {
        Row(
            modifier = Modifier
                .conditionally(entry?.image != null) {
                    height(IntrinsicSize.Min)
                }
                .recomposeHighlighter()
        ) {
            if (entry == null || entry.image != null) {
                Box {
                    val fullscreenImageHandler = LocalFullscreenImageHandler.current
                    AsyncImage(
                        model = entry?.image,
                        contentDescription = stringResource(
                            R.string.anime_news_article_image_content_description
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            .width(80.dp)
                            .fillMaxHeight()
                            .combinedClickable(
                                onClick = { onClick?.invoke() },
                                onLongClick = { entry?.image?.let(fullscreenImageHandler::openImage) },
                            )
                            .blurForScreenshotMode()
                    )

                    entry?.icon?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = stringResource(
                                R.string.anime_news_site_logo_content_description
                            ),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .width(80.dp)
                                .align(Alignment.BottomCenter)
                                .blurForScreenshotMode()
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 8.dp,
                        bottom = if (entry != null && entry.copyright == null) 8.dp else 4.dp,
                    )
                    .weight(1f)
                    .wrapContentHeight()
            ) {
                Text(
                    text = entry?.title
                        ?: "Some really long placeholder news article title that fills 2 lines of space",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )

                CustomHtmlText(
                    text = entry?.description
                        ?: "Some really long placeholder news description that fills 3 lines of space; some really long placeholder news description that fills 3 lines of space.",
                    style = MaterialTheme.typography.bodySmall,
                    minLines = 3,
                    maxLines = 3,
                    overflow = TextOverflow.Clip,
                    detectTaps = false,
                    modifier = Modifier.placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (entry == null || entry.copyright != null) {
                        Text(
                            text = entry?.copyright ?: "Copyright placeholder",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .placeholder(
                                    visible = entry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }

                    val time = entry?.date?.toEpochMilliseconds()
                    if (entry == null || time != null) {
                        Text(
                            text = time?.let(DateUtils::getRelativeTimeSpanString)
                                ?.toString()
                                ?: "00 minutes ago",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .placeholder(
                                    visible = entry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                }
            }
        }
    }

    if (onClick != null) {
        ElevatedCard(onClick = onClick, content = content)
    } else {
        ElevatedCard(content = content)
    }
}
