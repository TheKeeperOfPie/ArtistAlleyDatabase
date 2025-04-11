package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_has_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
fun ArtistTitle(
    year: DataYear,
    id: String,
    booth: String?,
    name: String?,
    useSharedElement: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
        if (!isCurrentYear) {
            Text(text = "${year.year} - ")
        }

        if (name == null || booth != null) {
            Text(
                text = if (name == null) "" else booth!!,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("booth", id)
                    }
                    .placeholder(
                        visible = name == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )

            )
            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())
        }

        Text(
            text = name.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .conditionally(useSharedElement) {
                    sharedElement("name", id)
                }
                .weight(1f)
                .placeholder(
                    visible = name == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }
}

@Composable
fun ArtistListRow(
    entry: ArtistEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    onSeriesClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val artist = entry.artist
        Row(
            modifier = Modifier.sharedBounds("container", artist.id)
        ) {
            if (artist.booth != null) {
                Text(
                    text = artist.booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                        .sharedElement("booth", artist.id)
                )
                Spacer(Modifier.width(16.dp))
            }

            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("name", artist.id)
                    .weight(1f)
                    .padding(
                        start = if (artist.booth == null) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )
            Spacer(Modifier.width(16.dp))

            if (entry.artist.commissionModels.isNotEmpty()) {
                IconWithTooltip(
                    imageVector = Icons.Default.FormatPaint,
                    tooltipText = stringResource(Res.string.alley_artist_has_commissions),
                    contentDescription = stringResource(
                        Res.string.alley_artist_commission_icon_content_description
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                        .size(16.dp)
                )
            }

            val favorite = entry.favorite
            IconButton(
                onClick = { onFavoriteToggle(!favorite) },
                modifier = Modifier
                    .align(Alignment.Top)
                    .sharedElement("favorite", artist.id)
            ) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        Res.string.alley_favorite_icon_content_description
                    ),
                )
            }
        }

        if (entry.series.isNotEmpty()) {
            SeriesRow(
                series = entry.series,
                onSeriesClick = onSeriesClick,
                onMoreClick = onMoreClick,
            )
        }
    }
}

private val chipHeightModifier = Modifier.height(24.dp)

@Composable
private fun SeriesRow(
    series: List<SeriesEntry>,
    onSeriesClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (series.isEmpty()) return
    val scrollState = rememberScrollState()
    LaunchedEffect(series) {
        scrollState.scrollTo(0)
    }
    val randomSeed = LocalStableRandomSeed.current
    val shuffledSeries = remember(series, randomSeed) {
        series.shuffled(Random(randomSeed))
    }
    val languageOption = LocalLanguageOptionMedia.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .fadingEdgeEnd(
                startOpaque = 12.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
            .then(modifier)
            .horizontalScroll(scrollState)
    ) {
        Spacer(Modifier.width(12.dp))
        val colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        val border = AssistChipDefaults.assistChipBorder(false)
        shuffledSeries.take(5).forEach {
            AssistChip(
                colors = colors,
                border = border,
                onClick = { onSeriesClick(it.id) },
                label = { Text(text = it.name(languageOption)) },
                modifier = chipHeightModifier
            )
        }
        if (shuffledSeries.size > 5) {
            AssistChip(
                colors = colors,
                border = border,
                onClick = { onMoreClick() },
                label = { Text("...") },
                modifier = chipHeightModifier
            )
        }
        Spacer(Modifier.width(32.dp))
    }
}
