package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText

object ArtistDetailsScreen {

    @Composable
    operator fun invoke(
        onClickBack: () -> Unit,
        onSeriesClick: (Series) -> Unit,
        onMerchClick: (String) -> Unit,
        onStampRallyClick: (StampRallyEntry) -> Unit,
        onArtistMapClick: () -> Unit,
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
            title = { ArtistTitle(artist) },
            sharedElementId = artist.id,
            favorite = { entry.favorite },
            onFavoriteToggle = viewModel::onFavoriteToggle,
            images = viewModel::images,
            onClickBack = onClickBack,
            initialImageIndex = viewModel.initialImageIndex,
        ) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoText(
                    stringResource(R.string.alley_artist_details_artist_name),
                    artist.name,
                    showDividerAbove = false,
                )
            }

            if (!artist.summary.isNullOrBlank()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
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

            if (entry.stampRallies.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = R.string.alley_artist_details_stamp_rallies,
                        contentDescriptionTextRes = null,
                        values = entry.stampRallies,
                        valueToText = { it.fandom },
                        onClick = onStampRallyClick,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            ConfirmedAndInferred(
                confirmed = entry.seriesConfirmed,
                inferred = entry.seriesInferred,
                headerTextRes = R.string.alley_artist_details_series,
                headerTextResUnconfirmed = R.string.alley_artist_details_series_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = R.string.alley_artist_details_series_unconfirmed_icon_content_description,
                expandTextRes = R.string.alley_artist_details_series_unconfirmed_expand,
                itemToText = { it.text },
                onClick = onSeriesClick,
            )

            ConfirmedAndInferred(
                confirmed = entry.artist.merchConfirmed,
                inferred = entry.artist.merchInferred,
                headerTextRes = R.string.alley_artist_details_merch,
                headerTextResUnconfirmed = R.string.alley_artist_details_merch_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = R.string.alley_artist_details_merch_unconfirmed_icon_content_description,
                expandTextRes = R.string.alley_artist_details_merch_unconfirmed_expand,
                itemToText = { it },
                onClick = onMerchClick,
            )

            FilledTonalButton(
                onClick = onArtistMapClick,
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
                        contentDescription = stringResource(R.string.alley_open_in_map),
                    )
                    Text(stringResource(R.string.alley_open_in_map))
                }
            }
        }
    }

    @Composable
    private fun <T> ConfirmedAndInferred(
        confirmed: List<T>,
        inferred: List<T>,
        @StringRes headerTextRes: Int,
        @StringRes headerTextResUnconfirmed: Int,
        @StringRes unconfirmedIconContentDescriptionTextRes: Int,
        @StringRes expandTextRes: Int,
        itemToText: @Composable (T) -> String,
        onClick: (T) -> Unit,
    ) {
        if (confirmed.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                expandableListInfoText(
                    labelTextRes = headerTextRes,
                    contentDescriptionTextRes = null,
                    values = confirmed,
                    valueToText = itemToText,
                    onClick = onClick,
                    allowExpand = false,
                    showDividerAbove = false,
                )
            }
        }

        if (inferred.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                val expandedByDefault = confirmed.isEmpty()
                var expanded by remember { mutableStateOf(expandedByDefault) }
                var showPopup by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .conditionally(!expandedByDefault) {
                            clickable { expanded = !expanded }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .clickable(interactionSource = null, indication = null) {
                                showPopup = !showPopup
                            }
                    ) {
                        Text(
                            text = stringResource(headerTextResUnconfirmed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 4.dp)
                        )

                        Box {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(
                                    unconfirmedIconContentDescriptionTextRes
                                ),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .heightIn(max = 20.dp)
                                    .padding(top = 6.dp)
                            )

                            if (showPopup) {
                                Popup(onDismissRequest = { showPopup = false }) {
                                    Text(
                                        text = stringResource(R.string.alley_artist_details_tags_unconfirmed_explanation),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 16.dp, vertical = 10.dp)
                                            .widthIn(max = 200.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!expandedByDefault) {
                        TrailingDropdownIconButton(
                            expanded = expanded,
                            contentDescription = stringResource(expandTextRes),
                            onClick = { expanded = !expanded },
                        )
                    }
                }
                if (expanded) {
                    expandableListInfoText(
                        labelTextRes = headerTextResUnconfirmed,
                        contentDescriptionTextRes = null,
                        values = inferred,
                        valueToText = itemToText,
                        onClick = onClick,
                        allowExpand = false,
                        showDividerAbove = false,
                        header = null,
                    )
                }
            }
        }
    }
}
