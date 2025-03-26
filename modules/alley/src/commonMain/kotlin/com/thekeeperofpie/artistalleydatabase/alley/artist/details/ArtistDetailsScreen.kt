package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_artist_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_catalog
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_links_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_tags_unconfirmed_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_year
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.notes.NotesText
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
object ArtistDetailsScreen {

    @Composable
    operator fun invoke(
        entry: ArtistDetailsViewModel.Entry?,
        notesTextState: TextFieldState,
        initialImageIndex: Int,
        images: () -> List<CatalogImage>,
        otherYears: () -> List<DataYear>,
        eventSink: (Event) -> Unit,
    ) {
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

        val artist = entry.artist
        DetailsScreen(
            title = { ArtistTitle(artist) },
            sharedElementId = artist.id,
            favorite = { entry.favorite },
            images = images,
            initialImageIndex = initialImageIndex,
            eventSink = { eventSink(Event.DetailsEvent(it)) }
        ) {
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoText(
                    stringResource(Res.string.alley_artist_details_artist_name),
                    artist.name,
                    showDividerAbove = false,
                )
            }

            val summary = artist.summary
            if (!summary.isNullOrBlank()) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    InfoText(
                        label = stringResource(Res.string.alley_artist_details_description),
                        body = summary,
                        showDividerAbove = false
                    )
                }
            }

            val uriHandler = LocalUriHandler.current
            val onClickOpenUri: (String) -> Unit = {
                try {
                    uriHandler.openUri(it)
                } catch (_: Throwable) {
                }
            }

            if (artist.linkModels.isNotEmpty()) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_links,
                        contentDescriptionTextRes = Res.string.alley_artist_details_links_expand_content_description,
                        values = artist.linkModels,
                        allowExpand = false,
                        showDividerAbove = false,
                        item = { link, _, isLast -> LinkRow(link, isLast) },
                    )
                }
            }
            if (artist.storeLinkModels.isNotEmpty()) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_store,
                        contentDescriptionTextRes = null,
                        values = artist.storeLinkModels,
                        allowExpand = false,
                        showDividerAbove = false,
                        item = { link, _, isLast -> LinkRow(link, isLast) },
                    )
                }
            }

            if (artist.catalogLinks.isNotEmpty()) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_catalog,
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
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_artist_details_stamp_rallies,
                        contentDescriptionTextRes = null,
                        values = entry.stampRallies,
                        valueToText = { it.fandom },
                        onClick = { eventSink(Event.OpenStampRally(it)) },
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
            }

            ConfirmedAndInferred(
                confirmed = entry.seriesConfirmed,
                inferred = entry.seriesInferred,
                headerTextRes = Res.string.alley_artist_details_series,
                headerTextResUnconfirmed = Res.string.alley_artist_details_series_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = Res.string.alley_artist_details_series_unconfirmed_icon_content_description,
                itemToText = { it },
                onClick = { eventSink(Event.OpenSeries(it)) },
            )

            ConfirmedAndInferred(
                confirmed = entry.artist.merchConfirmed,
                inferred = entry.artist.merchInferred,
                headerTextRes = Res.string.alley_artist_details_merch,
                headerTextResUnconfirmed = Res.string.alley_artist_details_merch_unconfirmed,
                unconfirmedIconContentDescriptionTextRes = Res.string.alley_artist_details_merch_unconfirmed_icon_content_description,
                itemToText = { it },
                onClick = { eventSink(Event.OpenMerch(it)) },
            )

            NotesText(
                state = notesTextState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                FilledTonalButton(
                    onClick = { eventSink(Event.DetailsEvent(DetailsScreen.Event.OpenMap)) },
                    modifier = Modifier.padding(horizontal = 16.dp)
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

                otherYears().forEach {
                    FilledTonalButton(
                        onClick = { eventSink(Event.OpenOtherYear(it)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(stringResource(Res.string.alley_open_year, it.year))
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> ConfirmedAndInferred(
        confirmed: List<T>,
        inferred: List<T>,
        headerTextRes: StringResource,
        headerTextResUnconfirmed: StringResource,
        unconfirmedIconContentDescriptionTextRes: StringResource,
        itemToText: @Composable (T) -> String,
        onClick: (T) -> Unit,
    ) {
        if (confirmed.isNotEmpty()) {
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                expandableListInfoText(
                    labelTextRes = headerTextRes,
                    contentDescriptionTextRes = null,
                    values = confirmed,
                    valueToText = itemToText,
                    onClick = onClick,
                    allowExpand = true,
                    showDividerAbove = false,
                )
            }
        }

        if (inferred.isNotEmpty()) {
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                var showPopup by remember { mutableStateOf(false) }
                expandableListInfoText(
                    labelTextRes = headerTextResUnconfirmed,
                    contentDescriptionTextRes = null,
                    values = inferred,
                    valueToText = itemToText,
                    onClick = onClick,
                    allowExpand = true,
                    showDividerAbove = false,
                    header = {
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
                                            text = stringResource(Res.string.alley_artist_details_tags_unconfirmed_explanation),
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
                    },
                )
            }
        }
    }

    @Composable
    private fun LinkRow(link: LinkModel, isLast: Boolean) {
        val uriHandler = LocalUriHandler.current
        val bottomPadding = if (isLast) 12.dp else 8.dp
        Tooltip(link.link, Alignment.BottomEnd, onClick = { uriHandler.openUri(link.link) }) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = bottomPadding,
                    )
                    .fillMaxWidth()
            ) {
                if (link.logo?.icon != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.height(20.dp)
                            .widthIn(min = 20.dp)
                    ) {
                        Icon(
                            imageVector = link.logo.icon,
                            contentDescription = null,
                            modifier = Modifier.height(16.dp)
                        )
                    }
                }

                val outlineVariant = MaterialTheme.colorScheme.outline
                val label = link.logo?.label?.let { stringResource(it) }
                val text = remember(link, label, outlineVariant) {
                    buildAnnotatedString {
                        // TODO: This doesn't support localization
                        if (label != null) {
                            withStyle(SpanStyle(color = outlineVariant)) {
                                append(label)
                                append(" - ")
                            }
                        }
                        append(link.title)
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    sealed interface Event {
        data class DetailsEvent(val event: DetailsScreen.Event) : Event
        data class OpenMerch(val merch: String) : Event
        data class OpenOtherYear(val year: DataYear) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenStampRally(val entry: StampRallyEntry) : Event
    }
}

@Preview
@Composable
private fun PhoneLayout() {
    val artist = ArtistWithUserDataProvider.values.first()
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    PreviewDark {
        var notes by remember { mutableStateOf("") }
        ArtistDetailsScreen(
            entry = ArtistDetailsViewModel.Entry(
                artist = artist.artist,
                userEntry = artist.userEntry,
                seriesInferred = artist.artist.seriesInferred - artist.artist.seriesConfirmed,
                seriesConfirmed = artist.artist.seriesConfirmed,
                stampRallies = emptyList(),
            ),
            notesTextState = rememberTextFieldState(),
            initialImageIndex = 1,
            eventSink = {},
            images = { images },
            otherYears = { listOf(DataYear.YEAR_2024) },
        )
    }
}
