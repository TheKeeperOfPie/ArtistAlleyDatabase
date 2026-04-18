package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_has_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import coil3.compose.AsyncImage
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.otherTitles
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistTitle(
    year: DataYear,
    id: String,
    booth: String?,
    profileImage: CatalogImage?,
    name: String?,
    useSharedElement: Boolean = true,
) {
    SelectionContainer {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            ArtistProfileImage(id, profileImage)
            Spacer(Modifier.width(12.dp))

            val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
            if (!isCurrentYear) {
                Text(text = "${stringResource(year.shortName)} - ")
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
}

@Composable
fun ArtistListRow(
    entry: ArtistEntryGridModel,
    series: () -> Map<String, GetSeriesTitles>,
    onFavoriteToggle: (Boolean) -> Unit,
    onSeriesClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSeries: Boolean = true,
    useSharedElements: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val artist = entry.artist
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .conditionally(useSharedElements, Modifier.sharedBounds("container", artist.id))
        ) {
            val profileImage = entry.profileImage
            Spacer(Modifier.width(10.dp))
            ArtistProfileImage(
                artistId = artist.id,
                image = profileImage,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            val booth = artist.booth
            if (!booth.isNullOrBlank()) {
                Text(
                    text = booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1,
                    modifier = Modifier
                        .conditionally(
                            useSharedElements,
                            Modifier.sharedElement("booth", artist.id)
                        )
                        .padding(12.dp)
                )
            }

            Text(
                text = artist.name,
                color = if (entry.artist.verifiedArtist) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
                    .conditionally(useSharedElements, Modifier.sharedElement("name", artist.id))
                    .padding(
                        start = if (artist.booth.isNullOrBlank()) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )

            if (entry.artist.verifiedArtist) {
                Spacer(Modifier.width(4.dp))
                IconWithTooltip(
                    imageVector = Icons.Default.Verified,
                    tooltipText = stringResource(Res.string.alley_artist_verified),
                    contentDescription = stringResource(Res.string.alley_artist_verified_content_description),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(16.dp))

            if (entry.artist.commissionModels.isNotEmpty()) {
                IconWithTooltip(
                    imageVector = Icons.Default.FormatPaint,
                    tooltipText = stringResource(Res.string.alley_artist_has_commissions),
                    contentDescription = stringResource(
                        Res.string.alley_artist_commission_icon_content_description
                    ),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .size(16.dp)
                        .align(Alignment.Top)
                )
            }

            var unfavoriteDialogEntry by remember {
                mutableStateOf<SearchScreen.SearchEntryModel?>(null)
            }

            val favorite = entry.favorite
            IconButton(
                onClick = {
                    if (favorite) {
                        unfavoriteDialogEntry = entry
                    } else {
                        onFavoriteToggle(true)
                    }
                },
                modifier = Modifier
                    .align(Alignment.Top)
                    .conditionally(useSharedElements, Modifier.sharedElement("favorite", artist.id))
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

            UnfavoriteDialog(
                entry = { unfavoriteDialogEntry },
                onClearEntry = { unfavoriteDialogEntry = null },
                onRemoveFavorite = { onFavoriteToggle(false) },
            )
        }

        if (showSeries && entry.series.isNotEmpty()) {
            val series = series()
            SeriesRow(
                series = entry.series.mapNotNull { series[it] },
                hasMoreSeries = entry.hasMoreSeries,
                onSeriesClick = onSeriesClick,
                onMoreClick = onMoreClick,
            )
        }
    }
}

@Composable
private fun ArtistProfileImage(
    artistId: String,
    image: CatalogImage?,
    modifier: Modifier = Modifier,
    useSharedElements: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
    ) {
        AsyncImage(
            model = image?.coilImageModel,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .conditionally(useSharedElements, Modifier.sharedElement("profile_image", artistId))
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
        )
    }
}

private val chipHeightModifier = Modifier.height(24.dp)

@Composable
internal fun SeriesRow(
    series: List<GetSeriesTitles>,
    onSeriesClick: (String) -> Unit,
    hasMoreSeries: Boolean,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (series.isEmpty()) return
    val languageOption = LocalLanguageOptionMedia.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .fadingEdgeEnd(
                startOpaque = 2.dp,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
            .then(modifier)
    ) {
        Spacer(Modifier.width(2.dp))
        val colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
        val border = AssistChipDefaults.assistChipBorder(false)
        series.forEach {
            AssistChip(
                colors = colors,
                border = border,
                onClick = { onSeriesClick(it.id) },
                label = { Text(text = it.name(languageOption)) },
                modifier = chipHeightModifier
            )
        }
        if (hasMoreSeries) {
            AssistChip(
                colors = colors,
                border = border,
                onClick = { onMoreClick() },
                label = { Text("...") },
                modifier = chipHeightModifier
            )
        }
    }
}

@Composable
fun SeriesPrediction(query: String, series: SeriesInfo) {
    Column {
        val languageOptionMedia = LocalLanguageOptionMedia.current
        val title = buildAnnotatedString {
            val name = series.name(languageOptionMedia)
            append(if (series.faked) "\"${name}\"" else name)
            if (!series.faked) {
                val startIndex = name.indexOf(query, ignoreCase = true)
                if (startIndex >= 0) {
                    addStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                        start = startIndex,
                        end = startIndex + query.length,
                    )
                }
            }
        }
        Text(text = title)

        if (!series.faked) {
            val otherTitles = series.otherTitles(languageOptionMedia)
            if (otherTitles.isNotEmpty()) {
                val text = buildAnnotatedString {
                    val value = otherTitles.joinToString(" / ")
                    append(value)
                    val startIndex = value.indexOf(query, ignoreCase = true)
                    if (startIndex >= 0) {
                        addStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                            start = startIndex,
                            end = startIndex + query.length,
                        )
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

