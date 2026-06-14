@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.rallies

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_other
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_cost
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistProfileImage
import com.thekeeperofpie.artistalleydatabase.alley.favorite.UnfavoriteDialog
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Favorite
import com.thekeeperofpie.artistalleydatabase.icons.filled.FavoriteBorder
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyTitle(
    year: DataYear,
    id: String,
    hostTable: String?,
    fandom: String?,
    useSharedElement: Boolean = true,
) {
    SelectionContainer {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isCurrentYear = remember(year) { AlleyUtils.isCurrentYear(year) }
            if (!isCurrentYear) {
                Text(text = "${stringResource(year.shortName)} - ")
            }

            Text(
                text = hostTable.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("hostTable", id)
                    }
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

            Text(
                text = fandom.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .conditionally(useSharedElement) {
                        sharedElement("fandom", id)
                    }
                    .weight(1f)
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun StampRallySeriesImage(
    stampRallyId: String,
    seriesId: String?,
    startTable: String?,
    image: () -> String?,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxHeight()
            .width(72.dp)
            .heightIn(min = 80.dp)
    ) {
        val image = image()
        if (image != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(image)
                    .build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .sharedElement("seriesImage", seriesId)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            val textStyle = MaterialTheme.typography.titleLarge
            Text(
                text = startTable.orEmpty(),
                style = textStyle.copy(fontFamily = FontFamily.Monospace),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 12.sp,
                    maxFontSize = textStyle.fontSize,
                ),
                modifier = Modifier
                    .sharedElement("hostTable", stampRallyId, zIndexInOverlay = 1f)
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun StampRallyListRow(
    entry: StampRallyEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    seriesImage: (SeriesImageInfo) -> String?,
    modifier: Modifier = Modifier,
) {
    val stampRally = entry.stampRally
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        val series = entry.seriesImageInfo.firstOrNull()
        StampRallySeriesImage(
            stampRallyId = stampRally.id,
            seriesId = series?.id,
            startTable = stampRally.startTableOrDefault,
            image = { series?.let(seriesImage) }
        )

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .sharedBounds("container", stampRally.id, zIndexInOverlay = 1f)
            ) {
                Text(
                    text = stampRally.fandom,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )

                Spacer(Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    if (stampRally.prizeLimit != null) {
                        Text(
                            text = stringResource(
                                Res.string.alley_stamp_rally_prize_limit,
                                stampRally.prizeLimitText(),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    val totalCost = stampRally.totalCost
                    val text = when (val tableMin = stampRally.tableMin) {
                        TableMin.Free -> stringResource(Res.string.alley_stamp_rally_cost_free)
                        TableMin.Other -> stringResource(Res.string.alley_stamp_rally_cost_other)
                        TableMin.Any -> stringResource(Res.string.alley_stamp_rally_cost_any)
                        TableMin.Paid -> stringResource(Res.string.alley_stamp_rally_cost_paid)
                        is TableMin.Price -> when (totalCost) {
                            null -> {
                                val totalCostUsd = tableMin.totalCost(stampRally.tables.size)
                                if (totalCostUsd != null) {
                                    stringResource(
                                        Res.string.alley_stamp_rally_total_cost,
                                        totalCostUsd,
                                    )
                                } else {
                                    stringResource(Res.string.alley_stamp_rally_cost_paid)
                                }
                            }
                            0L -> stringResource(Res.string.alley_stamp_rally_cost_free)
                            else -> stringResource(
                                Res.string.alley_stamp_rally_total_cost,
                                totalCost,
                            )
                        }
                        null -> null
                    }
                    if (text != null) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
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
                        .sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                        .align(Alignment.Top)
                ) {
                    Icon(
                        imageVector = if (favorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = stringResource(
                            Res.string.alley_stamp_rally_favorite_icon_content_description
                        ),
                    )
                }

                UnfavoriteDialog(
                    entry = { unfavoriteDialogEntry },
                    onClearEntry = { unfavoriteDialogEntry = null },
                    onRemoveFavorite = { onFavoriteToggle(false) },
                )
            }

            val artistBoothsToProfileImages = entry.artistBoothsToProfileImages
            val tables = entry.stampRally.tables
            if (artistBoothsToProfileImages.isNotEmpty() || tables.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .fadingEdgeEnd(
                            startTransparent = 0.dp,
                            startOpaque = 0.dp,
                            endOpaque = 32.dp,
                            endTransparent = 16.dp,
                        )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.wrapContentWidth(
                            unbounded = true,
                            align = Alignment.Start,
                        )
                    ) {
                        if (artistBoothsToProfileImages.isNotEmpty()) {
                            artistBoothsToProfileImages.forEach {
                                ArtistProfileImage(
                                    booth = it.first,
                                    image = it.second,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        } else {
                            tables.forEach {
                                ArtistProfileImage(
                                    booth = it,
                                    image = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
