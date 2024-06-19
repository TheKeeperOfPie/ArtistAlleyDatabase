package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.skipToLookaheadSize

object StampRallyDetailsScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onArtistClick: (ArtistEntry) -> Unit,
    ) {
        val viewModel = hiltViewModel<StampRallyDetailsViewModel>()
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
                        modifier = Modifier.sharedBounds(
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
                            .sharedBounds("fandom", stampRally.id, zIndexInOverlay = 1f)
                    )
                }
            },
            sharedElementId = stampRally.id,
            favorite = { entry.favorite },
            onFavoriteToggle = viewModel::onFavoriteToggle,
            images = viewModel::images,
            onClickBack = onClickBack,
            initialImageIndex = viewModel.initialImageIndex,
        ) {
            ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                InfoText(
                    stringResource(R.string.alley_stamp_rally_details_fandom),
                    stampRally.fandom,
                    showDividerAbove = false,
                )
            }

            // TODO: Tables

            val uriHandler = LocalUriHandler.current
            val onClickOpenUri: (String) -> Unit = {
                try {
                    uriHandler.openUri(it)
                } catch (ignored: Throwable) {
                }
            }

            if (stampRally.links.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_stamp_rally_details_links,
                        contentDescriptionTextRes = null,
                        values = stampRally.links,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            if (entry.artists.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_stamp_rally_details_artists,
                        contentDescriptionTextRes = null,
                        values = entry.artists,
                        valueToText = {
                            stringResource(
                                R.string.alley_artist_details_booth_and_table_name,
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
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_stamp_rally_details_other_tables,
                        contentDescriptionTextRes = null,
                        values = entry.otherTables,
                        valueToText = { it },
                        onClick = null,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }
        }
    }
}
