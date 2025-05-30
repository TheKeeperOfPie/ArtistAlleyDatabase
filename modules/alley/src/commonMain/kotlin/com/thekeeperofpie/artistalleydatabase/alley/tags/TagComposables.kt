package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.MerchEntry
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable

@Composable
fun SeriesRow(
    series: SeriesEntry?,
    image: () -> String?,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .optionalClickable(onClick)
            .height(IntrinsicSize.Min)
    ) {
        AsyncImage(
            model = image(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxHeight()
                .width(64.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        val languageOptionMedia = LocalLanguageOptionMedia.current
        val title = if (showAllTitles && series != null) {
            val colorScheme = MaterialTheme.colorScheme
            remember(series, languageOptionMedia, colorScheme) {
                val name = series.name(languageOptionMedia)
                val otherTitles = listOf(
                    series.titlePreferred,
                    series.titleEnglish,
                    series.titleRomaji,
                    series.titleNative,
                ).distinct() - name
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colorScheme.secondary)) {
                        append(name)
                    }
                    if (otherTitles.isNotEmpty()) {
                        otherTitles.forEach {
                            append(" / ")
                            append(it)
                        }
                    }
                }
            }
        } else {
            buildAnnotatedString {
                append(series?.name(languageOptionMedia))
            }
        }
        Text(
            text = title,
            style = textStyle,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .placeholder(
                    visible = series == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )

        val uriHandler = LocalUriHandler.current
        if (series?.aniListId != null) {
            val mediaType = when (series.aniListType) {
                "ANIME" -> MediaType.ANIME
                "MANGA" -> MediaType.MANGA
                else -> MediaType.UNKNOWN__
            }
            val icon = when (series.aniListType) {
                "ANIME" -> Icons.Default.Monitor
                "MANGA" -> Icons.Default.Book
                else -> Icons.Default.Monitor
            }
            val aniListUrl = AniListDataUtils.mediaUrl(mediaType, series.aniListId.toString())
            IconButtonWithTooltip(
                imageVector = icon,
                tooltipText = aniListUrl,
                onClick = { uriHandler.openUri(aniListUrl) },
                allowPopupHover = false,
            )
        }

        if (series?.link != null) {
            IconButtonWithTooltip(
                imageVector = Icons.Default.Link,
                tooltipText = series.link,
                onClick = { uriHandler.openUri(series.link) },
                allowPopupHover = false,
            )
        }
    }
}

@Composable
fun MerchRow(
    merch: String?,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = merch.orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .optionalClickable(onClick = onClick.takeIf { expanded })
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .placeholder(
                visible = merch == null,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

@Composable
fun MerchRow(
    merchEntry: MerchEntry?,
    onClick: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .optionalClickable(onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = merchEntry?.name.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .placeholder(
                    visible = merchEntry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
        if (merchEntry == null || !merchEntry.notes.isNullOrEmpty()) {
            Text(
                text = merchEntry?.notes.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = merchEntry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}
