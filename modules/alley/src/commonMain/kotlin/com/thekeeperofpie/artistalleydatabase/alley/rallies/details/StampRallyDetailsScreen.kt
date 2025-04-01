package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.notes.NotesText
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.skipToLookaheadSize
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object StampRallyDetailsScreen {

    @Composable
    operator fun invoke(
        route: Destinations.StampRallyDetails,
        entry: () -> StampRallyDetailsViewModel.Entry?,
        notesTextState: TextFieldState,
        initialImageIndex: Int,
        images: () -> List<CatalogImage>,
        eventSink: (Event) -> Unit,
    ) {
        val uriHandler = LocalUriHandler.current
        DetailsScreen(
            title = {
                val id = route.id
                val stampRally = entry()?.stampRally
                val hostTable = stampRally?.hostTable ?: route.hostTable
                val fandom = stampRally?.fandom ?: route.fandom
                StampRallyTitle(id = id, hostTable = hostTable, fandom = fandom)
            },
            sharedElementId = route.id,
            favorite = { entry()?.favorite },
            images = images,
            initialImageIndex = initialImageIndex,
            eventSink = { eventSink(Event.DetailsEvent(it)) },
        ) {
            detailsContent(entry, notesTextState, eventSink, onClickOpenUri = {
                try {
                    uriHandler.openUri(it)
                } catch (_: Throwable) {
                }
            })
        }
    }

    private fun LazyListScope.detailsContent(
        entry: () -> StampRallyDetailsViewModel.Entry?,
        notesTextState: TextFieldState,
        eventSink: (Event) -> Unit,
        onClickOpenUri: (String) -> Unit,
    ) {
        item("stampRallyFandom") {
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoText(
                    stringResource(Res.string.alley_stamp_rally_details_fandom),
                    entry()?.stampRally?.fandom,
                    showDividerAbove = false,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item("stampRallyLinks") {
            val links = entry()?.stampRally?.links
            if (links?.isNotEmpty() != false) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_links,
                        contentDescriptionTextRes = null,
                        values = links,
                        valueToText = { it },
                        onClick = onClickOpenUri,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item("stampRallyCostAndPrizes") {
            val stampRally = entry()?.stampRally
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                val body = if (stampRally == null) {
                    null
                } else {
                    val tableMin = stampRally.tableMin
                    val totalCost = stampRally.totalCost
                    val tableCount = stampRally.tables.count()
                    if (tableMin == null) {
                        stringResource(Res.string.alley_stamp_rally_cost_unknown)
                    } else if (tableMin == 0L) {
                        stringResource(Res.string.alley_stamp_rally_cost_free)
                    } else if (tableMin == 1L) {
                        if (tableCount > 0) {
                            stringResource(
                                Res.string.alley_stamp_rally_cost_equation_any,
                                tableCount
                            )
                        } else {
                            stringResource(Res.string.alley_stamp_rally_cost_any)
                        }
                    } else if (totalCost != null && tableCount > 0) {
                        stringResource(
                            Res.string.alley_stamp_rally_cost_equation_paid,
                            tableMin,
                            tableCount,
                            totalCost
                        )
                    } else {
                        stringResource(Res.string.alley_stamp_rally_cost_unknown)
                    }
                }

                twoColumnInfoText(
                    labelOne = stringResource(Res.string.alley_stamp_rally_details_cost),
                    bodyOne = body,
                    labelTwo = stringResource(Res.string.alley_stamp_rally_details_prize_limit),
                    bodyTwo = stampRally?.prizeLimitText(),
                    showDividerAbove = false,
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item("stampRallyArtists") {
            val artists = entry()?.artists
            if (artists?.isNotEmpty() != false) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_artists,
                        contentDescriptionTextRes = null,
                        values = artists,
                        valueToText = {
                            if (it.booth == null) {
                                it.name
                            } else {
                                stringResource(
                                    Res.string.alley_artist_details_booth_and_table_name,
                                    it.booth,
                                    it.name
                                )
                            }
                        },
                        onClick = { eventSink(Event.OpenArtist(it)) },
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item("stampRallyOtherTables") {
            val otherTables = entry()?.otherTables
            if (otherTables?.isNotEmpty() != false) {
                ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    expandableListInfoText(
                        labelTextRes = Res.string.alley_stamp_rally_details_other_tables,
                        contentDescriptionTextRes = null,
                        values = otherTables,
                        valueToText = { it },
                        onClick = null,
                        allowExpand = false,
                        showDividerAbove = false,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        item("stampRallyNotes") {
            NotesText(
                state = notesTextState,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        item("stampRallyButtons") {
            FilledTonalButton(
                onClick = { eventSink(Event.DetailsEvent(DetailsScreen.Event.OpenMap)) },
                modifier = Modifier.fillMaxWidth()
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

    @Composable
    private fun StampRallyTitle(id: String, hostTable: String?, fandom: String?) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = hostTable.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .sharedElement(
                        "hostTable",
                        id,
                        zIndexInOverlay = 1f,
                    )
            )

            Text(text = " - ", modifier = Modifier.skipToLookaheadSize())

            Text(
                text = fandom.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .placeholder(
                        visible = hostTable == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .sharedElement("fandom", id, zIndexInOverlay = 1f)
            )
        }
    }

    sealed interface Event {
        data class DetailsEvent(val event: DetailsScreen.Event) : Event
        data class OpenArtist(val artist: ArtistEntry) : Event
    }
}

@Preview
@Composable
private fun PhoneLayout() {
    val stampRally = StampRallyWithUserDataProvider.values.first()
    val artists = ArtistWithUserDataProvider.values.take(3).map { it.artist }.toList()
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    PreviewDark {
        var notes by remember { mutableStateOf("") }
        StampRallyDetailsScreen(
            route = Destinations.StampRallyDetails(stampRally.stampRally),
            entry = {
                StampRallyDetailsViewModel.Entry(
                    stampRally = stampRally.stampRally,
                    userEntry = stampRally.userEntry,
                    artists = artists,
                    otherTables = listOf("ANX-101"),
                )
            },
            notesTextState = rememberTextFieldState(),
            initialImageIndex = 1,
            images = { images },
            eventSink = {},
        )
    }
}
