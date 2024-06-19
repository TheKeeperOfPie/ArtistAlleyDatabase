package com.thekeeperofpie.artistalleydatabase.alley.artist.details

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
import com.thekeeperofpie.artistalleydatabase.compose.InfoText
import com.thekeeperofpie.artistalleydatabase.compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.data.Series

object ArtistDetailsScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onSeriesClick: (Series) -> Unit,
    ) {
        val viewModel = hiltViewModel<ArtistDetailsViewModel>()
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
        val artist = entry.artist
        DetailsScreen(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = artist.booth,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedBounds(
                            "booth",
                            artist.id,
                            zIndexInOverlay = 1f,
                        )
                    )

                    Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

                    Text(
                        text = artist.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .sharedBounds("name", artist.id, zIndexInOverlay = 1f)
                    )
                }
            },
            sharedElementId = artist.id,
            favorite = { artist.favorite },
            onFavoriteToggle = viewModel::onFavoriteToggle,
            images = viewModel::images,
            onClickBack = onClickBack,
            initialImageIndex = viewModel.initialImageIndex,
        ) {
            ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                InfoText(
                    stringResource(R.string.alley_artist_details_artist_name),
                    artist.name,
                    showDividerAbove = false,
                )
            }

            if (!artist.summary.isNullOrBlank()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    InfoText(
                        stringResource(R.string.alley_artist_details_description),
                        artist.summary,
                        showDividerAbove = false
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            val onClickOpenUri: (String) -> Unit = {
                try {
                    uriHandler.openUri(it)
                } catch (ignored: Throwable) {
                }
            }

            if (artist.links.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_links,
                        contentDescriptionTextRes = R.string.alley_artist_details_links_expand_content_description,
                        values = artist.links,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }
            if (artist.storeLinks.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_store,
                        contentDescriptionTextRes = null,
                        values = artist.storeLinks,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            if (artist.catalogLinks.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_catalog,
                        contentDescriptionTextRes = null,
                        values = artist.catalogLinks,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            if (entry.series.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_series,
                        contentDescriptionTextRes = null,
                        values = entry.series,
                        valueToText = { it.text },
                        onClick = onSeriesClick,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            val merch = artist.merchConfirmed.ifEmpty { artist.merchInferred }
            if (merch.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_merch,
                        contentDescriptionTextRes = null,
                        values = merch,
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
