package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry.Different.id
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
fun ArtistTitle(year: DataYear, booth: String?, name: String?) {
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
                    .placeholder(
                        visible = name == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .sharedElement(
                        "booth",
                        id,
                        zIndexInOverlay = 1f,
                    )
            )
            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())
        }

        Text(
            text = name.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .sharedElement("name", id, zIndexInOverlay = 1f)
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
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val artist = entry.artist
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.sharedBounds("container", artist.id, zIndexInOverlay = 1f)
        ) {
            if (artist.booth != null) {
                Text(
                    text = artist.booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                        .sharedElement("booth", artist.id, zIndexInOverlay = 1f)
                )
            }

            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("name", artist.id, zIndexInOverlay = 1f)
                    .weight(1f)
                    .padding(
                        start = if (artist.booth == null) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )

            val favorite = entry.favorite
            IconButton(
                onClick = { onFavoriteToggle(!favorite) },
                modifier = Modifier
                    .align(Alignment.Top)
                    .sharedElement(
                        "favorite",
                        artist.id,
                        zIndexInOverlay = 1f,
                    )
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
            SeriesRow(series = entry.series, onSeriesClick = onSeriesClick)
        }
    }
}

@Composable
private fun SeriesRow(
    series: List<SeriesEntry>,
    onSeriesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (series.isEmpty()) return
    val listState = rememberLazyListState()
    LaunchedEffect(series) {
        listState.scrollToItem(0, 0)
    }
    val randomSeed = LocalStableRandomSeed.current
    val shuffledSeries = remember(series, randomSeed) {
        series.shuffled(Random(randomSeed))
    }
    val languageOption = LocalLanguageOptionMedia.current
    LazyRow(
        state = listState,
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp),
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
    ) {
        items(items = shuffledSeries, key = { it }) {
            AssistChip(
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
                border = AssistChipDefaults.assistChipBorder(false),
                onClick = { onSeriesClick(it.id) },
                label = { Text(text = it.name(languageOption)) },
                modifier = Modifier.height(24.dp)
            )
        }
    }
}
