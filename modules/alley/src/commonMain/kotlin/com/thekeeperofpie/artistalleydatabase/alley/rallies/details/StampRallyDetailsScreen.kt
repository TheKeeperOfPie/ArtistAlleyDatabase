package com.thekeeperofpie.artistalleydatabase.alley.rallies.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_booth_and_table_name
import artistalleydatabase.modules.alley.generated.resources.alley_maintainer_notes
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_equation_any
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_equation_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_other
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_paid
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_cost_unknown
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_artists
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_cost
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_links
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_other_tables
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_prize
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_details_series
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.notes.UserNotesText
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyTitle
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSubsectionHeader
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.twoColumnInfoText
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

object StampRallyDetailsScreen {

    @Composable
    operator fun invoke(
        route: Destinations.StampRallyDetails,
        entry: () -> StampRallyDetailsViewModel.Entry?,
        series: () -> List<SeriesWithUserData>?,
        userNotesTextState: TextFieldState,
        images: () -> List<CatalogImage>,
        imagePagerState: PagerState,
        seriesImages: () -> Map<String, String>,
        eventSink: (Event) -> Unit,
    ) {
        val uriHandler = LocalUriHandler.current
        DetailsScreen(
            title = {
                val id = route.id
                val stampRally = entry()?.stampRally
                val hostTable = stampRally?.hostTable ?: route.hostTable
                val fandom = stampRally?.fandom ?: route.fandom
                StampRallyTitle(year = route.year, id = id, hostTable = hostTable, fandom = fandom)
            },
            sharedElementId = route.id,
            favorite = {
                // TODO: Show explanation for why favorites is disabled
                val entry = entry()
                entry?.favorite?.takeIf { entry.stampRally.confirmed }
            },
            images = images,
            imagePagerState = imagePagerState,
            eventSink = { eventSink(Event.DetailsEvent(it)) },
        ) {
            detailsContent(
                entry = entry,
                series = series,
                userNotesTextState = userNotesTextState,
                seriesImages = seriesImages,
                eventSink = eventSink,
                onClickOpenUri = {
                    try {
                        uriHandler.openUri(it)
                    } catch (_: Throwable) {
                    }
                },
            )
        }
    }

    private fun LazyListScope.detailsContent(
        entry: () -> StampRallyDetailsViewModel.Entry?,
        series: () -> List<SeriesWithUserData>?,
        userNotesTextState: TextFieldState,
        seriesImages: () -> Map<String, String>,
        eventSink: (Event) -> Unit,
        onClickOpenUri: (String) -> Unit,
    ) {
        item("stampRallyFandom") {
            Spacer(Modifier.height(16.dp))
            ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SelectionContainer {
                    Column {
                        InfoText(
                            stringResource(Res.string.alley_stamp_rally_details_fandom),
                            entry()?.stampRally?.fandom,
                            showDividerAbove = false,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        val links = entry()?.stampRally?.links
        if (links?.isNotEmpty() != false) {
            item("stampRallyLinks") {
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

        val prize = entry()?.stampRally?.prize
        if (prize != null) {
            item("stampRallyPrize") {
                Column(Modifier.animateItem()) {
                    ThemeAwareElevatedCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        SelectionContainer {
                            Column {
                                InfoText(
                                    label = stringResource(Res.string.alley_stamp_rally_details_prize),
                                    body = prize,
                                    showDividerAbove = false,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
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
                    when (tableMin) {
                        TableMin.Any ->
                            if (tableCount > 0) {
                                stringResource(
                                    Res.string.alley_stamp_rally_cost_equation_any,
                                    tableCount
                                )
                            } else {
                                stringResource(Res.string.alley_stamp_rally_cost_any)
                            }
                        TableMin.Free -> stringResource(Res.string.alley_stamp_rally_cost_free)
                        TableMin.Other -> stringResource(Res.string.alley_stamp_rally_cost_other)
                        TableMin.Paid -> stringResource(Res.string.alley_stamp_rally_cost_paid)
                        is TableMin.Price -> if (totalCost == null) {
                            stringResource(Res.string.alley_stamp_rally_cost_paid)
                        } else {
                            stringResource(
                                Res.string.alley_stamp_rally_cost_equation_paid,
                                tableMin.usd,
                                tableCount,
                                totalCost
                            )
                        }
                        null -> stringResource(Res.string.alley_stamp_rally_cost_unknown)
                    }
                }

                SelectionContainer {
                    twoColumnInfoText(
                        labelOne = stringResource(Res.string.alley_stamp_rally_details_cost),
                        bodyOne = body,
                        labelTwo = stringResource(Res.string.alley_stamp_rally_details_prize_limit),
                        bodyTwo = stampRally?.prizeLimitText(),
                        showDividerAbove = false,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        val artists = entry()?.artists
        if (artists?.isNotEmpty() != false) {
            item("stampRallyArtists") {
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

        val otherTables = entry()?.otherTables
        if (otherTables?.isNotEmpty() != false) {
            item("stampRallyOtherTables") {
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

        val series = series()
        if (series?.isNotEmpty() != false) {
            item("artistSeries") {
                Column(Modifier.animateItem()) {
                    val languageOption = LocalLanguageOptionMedia.current
                    val sorted = remember(series, languageOption) {
                        series?.sortedBy { it.series.name(languageOption) }
                    }
                    ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                        expandableListInfoText(
                            labelTextRes = Res.string.alley_stamp_rally_details_series,
                            contentDescriptionTextRes = null,
                            values = sorted,
                            allowExpand = true,
                            showDividerAbove = false,
                            dividerPadding = PaddingValues(start = 0.dp),
                            header = {
                                DetailsSubsectionHeader(
                                    text = stringResource(Res.string.alley_stamp_rally_details_series),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            },
                            item = { value, expanded, _ ->
                                SeriesRow(
                                    data = value,
                                    image = { value?.series?.id?.let { seriesImages()[it] } },
                                    onFavoriteToggle = {
                                        if (value != null) {
                                            eventSink(Event.SeriesFavoriteToggle(value, it))
                                        }
                                    },
                                    onClick = if (expanded) {
                                        {
                                            value?.series?.id?.let {
                                                eventSink(Event.OpenSeries(it))
                                            }
                                        }
                                    } else null,
                                )
                            },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        val notes = entry()?.stampRally?.notes
        if (!notes.isNullOrEmpty()) {
            item("stampRallyMaintainerNotes") {
                Column(Modifier.animateItem()) {
                    ThemeAwareElevatedCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        SelectionContainer {
                            Column {
                                InfoText(
                                    label = stringResource(Res.string.alley_maintainer_notes),
                                    body = notes,
                                    showDividerAbove = false
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        if (entry()?.stampRally?.confirmed == true) {
            item("stampRallyUserNotes") {
                UserNotesText(
                    state = userNotesTextState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
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

    sealed interface Event {
        data class DetailsEvent(val event: DetailsScreen.Event) : Event
        data class OpenArtist(val artist: ArtistEntry) : Event
        data class OpenSeries(val series: String) : Event
        data class SeriesFavoriteToggle(
            val series: SeriesWithUserData,
            val favorite: Boolean,
        ) : Event
    }
}

@Preview
@Composable
private fun PhoneLayout() {
    val stampRally = StampRallyWithUserDataProvider.values.first()
    val artists = ArtistWithUserDataProvider.values.take(3).map { it.artist }.toList()
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    PreviewDark {
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
            series = { emptyList() },
            userNotesTextState = rememberTextFieldState(),
            images = { images },
            imagePagerState = rememberImagePagerState(images, 1),
            seriesImages = { emptyMap() },
            eventSink = {},
        )
    }
}
