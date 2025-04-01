package com.thekeeperofpie.artistalleydatabase.alley.tags

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
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
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .optionalClickable(onClick = onClick.takeIf { expanded })
            .minimumInteractiveComponentSize()
    ) {
        Text(
            text = series?.name(LocalLanguageOptionMedia.current).orEmpty(),
            style = textStyle,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
