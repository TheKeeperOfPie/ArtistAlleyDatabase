package com.thekeeperofpie.artistalleydatabase.alley.rallies.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_other
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_cost
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistProfileImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallySeriesImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.rallies.startTableOrDefault
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallyListRow(
    stampRallyWithUserData: StampRallyWithUserData,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stampRally = stampRallyWithUserData.stampRally
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        val series = stampRallyWithUserData.seriesImageInfo.firstOrNull()
        StampRallySeriesImage(
            stampRallyId = stampRally.id,
            seriesId = series?.id,
            startTable = stampRally.startTableOrDefault,
            image = series,
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

                FavoriteIconButton(
                    entryText = { "${stampRally.hostTable}-${stampRally.fandom}" },
                    favorite = { stampRallyWithUserData.userEntry.favorite },
                    onFavoriteToggle = onFavoriteToggle,
                    modifier = Modifier
                        .sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                        .align(Alignment.Top)
                )
            }

            val artistBoothsToProfileImages = stampRallyWithUserData.artistBoothsToProfileImages
            val tables = stampRally.tables
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

                            // Other tables, may include other halls like KH-1000
                            (tables - artistBoothsToProfileImages.map { it.first }
                                .toSet()).forEach {
                                ArtistProfileImage(
                                    booth = it,
                                    image = null,
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

@AlleyPreview
@Composable
private fun StampRallyListRowPreview() {
    val stampRally = StampRallyWithUserDataProvider.values.first()
    StampRallyListRow(
        stampRallyWithUserData = stampRally,
        onFavoriteToggle = {},
    )
}
