package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_article_image_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_row_title
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_row_view_all_content_description
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_site_logo_content_description
import coil3.compose.AsyncImage
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.recomposeHighlighter
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewsRow(
    result: () -> LoadingResult<List<AnimeNewsEntry<*>>>,
    pageSize: PageSize,
) {
    NavigationHeader(
        titleRes = Res.string.anime_news_row_title,
        viewAllRoute = AnimeNewsDestinations.News,
        viewAllContentDescriptionTextRes = Res.string.anime_news_row_view_all_content_description,
    )

    val news = result().result
    val itemCount = news?.size ?: 3
    if (itemCount == 0) return
    val pagerState = rememberPagerState(pageCount = { itemCount })
    val fullscreenImageHandler = LocalFullscreenImageHandler.current
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        pageSpacing = 16.dp,
        pageSize = pageSize,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.recomposeHighlighter()
    ) {
        AnimeNewsSmallCard(
            entry = news?.get(it),
            onOpenImage = fullscreenImageHandler::openImage,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimeNewsSmallCard(entry: AnimeNewsEntry<*>?, onOpenImage: (url: String) -> Unit) {
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
            val image = entry?.image
            if (entry == null || image != null) {
                Box {
                    AsyncImage(
                        model = image,
                        contentDescription = stringResource(
                            Res.string.anime_news_article_image_content_description
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                            .width(80.dp)
                            .fillMaxHeight()
                            .combinedClickable(
                                onClick = { onClick?.invoke() },
                                onLongClick = if (image == null) null else {
                                    { onOpenImage(image) }
                                },
                            )
                            .blurForScreenshotMode()
                    )

                    entry?.icon?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = stringResource(
                                Res.string.anime_news_site_logo_content_description
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

                    val time = entry?.date
                    if (entry == null || time != null) {
                        Text(
                            text = time?.let { HumanReadable.timeAgo(time) }
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
