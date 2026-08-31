package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.anilist.data.type.MediaType
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.models.AniListType
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.UnrecognizedTagIcon
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Book
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.Monitor
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable

private val ChipHeightModifier = Modifier.height(24.dp)

@Composable
internal fun SeriesRow(
    series: List<GetSeriesTitles>,
    onSeriesClick: (String) -> Unit,
    hasMoreSeries: Boolean,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) = SeriesRow(
    seriesHighlighted = emptyList(),
    seriesRemaining = series,
    onSeriesClick = onSeriesClick,
    hasMoreSeries = hasMoreSeries,
    onMoreClick = onMoreClick,
    modifier = modifier,
)

@Composable
internal fun SeriesRow(
    seriesHighlighted: List<GetSeriesTitles>,
    seriesRemaining: List<GetSeriesTitles>,
    onSeriesClick: (String) -> Unit,
    hasMoreSeries: Boolean,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (seriesHighlighted.isEmpty() && seriesRemaining.isEmpty()) return
    val languageOption = LocalLanguageOptionMedia.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .fadingEdgeEnd(
                startTransparent = 0.dp,
                startOpaque = 0.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
            .then(modifier)
    ) {
        val highlightedColors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onPrimary,
        )
        val remainingColors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        val highlightedBorder = AssistChipDefaults.assistChipBorder(enabled = true)
        val remainingBorder = AssistChipDefaults.assistChipBorder(enabled = false)
        seriesHighlighted.take(TagUtils.TAGS_TO_SHOW).forEach {
            AssistChip(
                colors = highlightedColors,
                border = highlightedBorder,
                onClick = { onSeriesClick(it.id) },
                label = { Text(text = it.name(languageOption)) },
                modifier = ChipHeightModifier
            )
        }
        seriesRemaining.take((TagUtils.TAGS_TO_SHOW - seriesHighlighted.size).coerceAtLeast(0))
            .forEach {
                AssistChip(
                    colors = remainingColors,
                    border = remainingBorder,
                    onClick = { onSeriesClick(it.id) },
                    label = { Text(text = it.name(languageOption)) },
                    modifier = ChipHeightModifier
                )
            }
        if (hasMoreSeries) {
            AssistChip(
                colors = remainingColors,
                border = remainingBorder,
                onClick = { onMoreClick() },
                label = { Text("...") },
                modifier = ChipHeightModifier
            )
        }
    }
}

@Composable
fun SeriesRow(
    data: SeriesWithUserData?,
    image: () -> String?,
    onFavoriteToggle: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    showNotes: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    SeriesRow(
        series = data?.series,
        image = image,
        favoritesButton = {
            val languageOptionMedia = LocalLanguageOptionMedia.current
            FavoriteIconButton(
                entryText = { data?.series?.name(languageOptionMedia) },
                favorite = { data?.userEntry?.favorite },
                onFavoriteToggle = onFavoriteToggle,
            )
        },
        onClick = onClick,
        showAllTitles = showAllTitles,
        showNotes = showNotes,
        textStyle = textStyle,
        modifier = modifier,
    )
}

@Composable
fun SeriesRow(
    series: SeriesInfo?,
    image: () -> String?,
    favoritesButton: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showAllTitles: Boolean = false,
    showNotes: Boolean = false,
    showUnknownIndicator: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .optionalClickable(onClick)
            .height(IntrinsicSize.Min)
    ) {
        favoritesButton?.invoke()

        Box {
            AsyncImage(
                model = image(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxHeight()
                    .width(56.dp)
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .sharedElement("seriesImage", series?.id)
            )

            // TODO: Show on other series image usages
            if (series?.tmdbId != null && series.tmdbType != null) {
                Icon(
                    imageVector = Logo.TMDB.icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(2.dp)
                )
            }
        }

        // For highlighting tag that needs resolution in edit app
        if (series?.faked == true && showUnknownIndicator) {
            UnrecognizedTagIcon()
        }

        Column(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .placeholder(
                    visible = series == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        ) {
            val languageOptionMedia = LocalLanguageOptionMedia.current
            val name = series?.name(languageOptionMedia).orEmpty()
            Text(
                text = name,
                color = textStyle.color.takeOrElse { MaterialTheme.colorScheme.secondary },
                style = textStyle,
            )

            if (showAllTitles) {
                val otherTitles = series?.otherTitles(languageOptionMedia)
                if (!otherTitles.isNullOrEmpty()) {
                    Text(
                        text = otherTitles.joinToString(separator = " / "),
                        style = textStyle,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            }

            if (showNotes && !series?.notes.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = series.notes.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        val uriHandler = LocalUriHandler.current
        if (series?.aniListId != null) {
            val mediaType = when (series.aniListType) {
                AniListType.NONE -> MediaType.UNKNOWN__
                AniListType.ANIME -> MediaType.ANIME
                AniListType.MANGA -> MediaType.MANGA
            }
            val icon = when (series.aniListType) {
                AniListType.NONE -> Icons.Default.Monitor
                AniListType.ANIME -> Icons.Default.Monitor
                AniListType.MANGA -> Icons.Default.Book
            }
            val aniListUrl = AniListDataUtils.mediaUrl(mediaType, series.aniListId.toString())
            TooltipIconButton(
                icon = icon,
                tooltipText = aniListUrl,
                onClick = { uriHandler.openUri(aniListUrl) },
            )
        }

        val link = series?.resolvedLink
        if (link != null) {
            TooltipIconButton(
                icon = Icons.Default.Link,
                tooltipText = link,
                onClick = { uriHandler.openUri(link) },
            )
        }
    }
}
