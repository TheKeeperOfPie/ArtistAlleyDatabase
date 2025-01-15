package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_booth_and_table_name
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_equation_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_equation_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_unknown
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_artists
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_cost
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_links
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_other_tables
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_prize_limit
import com.thekeeperofpie.artistalleydatabase.alley.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import org.jetbrains.compose.resources.stringResource

object StampRallyDetailsScreen {

    @Composable
    operator fun invoke(
        viewModel: StampRallyDetailsViewModel,
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntry) -> Unit,
        onStampRallyMapClick: () -> Unit,
    ) {
        val entry = viewModel.entry
        if (entry == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                CircularProgressIndicator()
            }
            return
        }

        var showFullImagesIndex by rememberSaveable { mutableStateOf<Int?>(null) }
        val stampRally = entry.stampRally
        DetailsScreen(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stampRally.hostTable,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedElement(
                            "hostTable",
                            stampRally.id,
                            zIndexInOverlay = 1f,
                        )
                    )

                    Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

                    Text(
                        text = stampRally.fandom,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                    )
                }
            },
            sharedElementId = stampRally.id,
            favorite = { entry.favorite },
            onFavoriteToggle = viewModel::onFavoriteToggle,
            images = viewModel::images,
            onClickBack = onClickBack,
            initialImageIndex = viewModel.initialImageIndex,
            onClickOpenInMap = onStampRallyMapClick,
        ) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoText(
                    stringResource(Res.string.alley_stamp_rally_details_fandom),
                    stampRally.fandom,
                    showDividerAbove = false,
                )
            }

            val uriHandler = LocalUriHandler.current
            val onClickOpenUri: (String) -> Unit = {
                try {
                    uriHandler.openUri(it)
                } catch (ignored: Throwable) {
                }
            }

            if (stampRally.links.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_links,
                        contentDescriptionTextRes = null,
                        values = stampRally.links,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                val tableMin = entry.stampRally.tableMin
                val totalCost = entry.stampRally.totalCost
                val tableCount = entry.stampRally.tables.count()

                val body = if (tableMin == null) {
                    stringResource(Res.string.alley_stamp_rally_cost_unknown)
                } else if (tableMin == 0L) {
                    stringResource(Res.string.alley_stamp_rally_cost_free)
                } else if (tableMin == 1L) {
                    if (tableCount > 0) {
                        stringResource(Res.string.alley_stamp_rally_cost_equation_any, tableCount)
                    } else {
                        stringResource(Res.string.alley_stamp_rally_cost_any)
                    }
                } else if (totalCost != null && tableCount > 0) {
                    stringResource(Res.string.alley_stamp_rally_cost_equation_paid, tableMin, tableCount, totalCost)
                } else {
                    stringResource(Res.string.alley_stamp_rally_cost_unknown)
                }

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.alley_stamp_rally_details_cost),
                    bodyOne = body,
                    labelTwo = stringResource(Res.string.alley_stamp_rally_details_prize_limit),
                    bodyTwo = entry.stampRally.prizeLimitText(),
                    showDividerAbove = false,
                )
            }

            if (entry.artists.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_artists,
                        contentDescriptionTextRes = null,
                        values = entry.artists,
                        valueToText = {
                            stringResource(
                                Res.string.alley_artist_details_booth_and_table_name,
                                it.booth,
                                it.name
                            )
                        },
                        onClick = onArtistClick,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            if (entry.otherTables.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_other_tables,
                        contentDescriptionTextRes = null,
                        values = entry.otherTables,
                        valueToText = { it },
                        onClick = null,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            FilledTonalButton(
                onClick = onStampRallyMapClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = stringResource(Res.string.alley_open_in_map),
                    )
                    Text(stringResource(Res.string.alley_open_in_map))
                }
            }
        }
    }
}
