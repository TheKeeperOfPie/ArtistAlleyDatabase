package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
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

        Text(
            text = series?.name(LocalLanguageOptionMedia.current).orEmpty(),
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
