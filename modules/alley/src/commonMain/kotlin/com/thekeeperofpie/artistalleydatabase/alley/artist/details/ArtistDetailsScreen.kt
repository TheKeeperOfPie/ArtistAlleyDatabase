package com.thekeeperofpie.artistalleydatabase.alley.artist.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
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
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_commissions
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
import artistalleydatabase.modules.alley.generated.resources.alley_maintainer_notes
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_year
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.alley.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.text
import com.thekeeperofpie.artistalleydatabase.alley.notes.UserNotesText
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.previewSeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.DetailsSubsectionHeader
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
        route: Destinations.ArtistDetails,
        entry: () -> ArtistDetailsViewModel.Entry?,
        userNotesTextState: TextFieldState,
        imagePagerState: PagerState,
        catalogImages: () -> List<CatalogImage>,
        seriesImages: () -> Map<String, String>,
        otherYears: () -> List<DataYear>,
        eventSink: (Event) -> Unit,
    ) {
        val uriHandler = LocalUriHandler.current
        val onClickOpenUri: (String) -> Unit = {
            try {
                uriHandler.openUri(it)
            } catch (_: Throwable) {
            }
        }
        DetailsScreen(
            title = {
                val artist = entry()?.artist
                val id = artist?.id ?: route.id
                val booth = artist?.booth ?: route.booth
                val name = artist?.name ?: route.name
                ArtistTitle(year = route.year, id = id, booth = booth, name = name)
            },
            sharedElementId = route.id,
            favorite = { entry()?.favorite },
            images = catalogImages,
            imagePagerState = imagePagerState,
            eventSink = { eventSink(Event.DetailsEvent(it)) }
        ) {
            item("artistName") {
                Column(Modifier.animateItem()) {
                    Spacer(Modifier.height(16.dp))
                    ThemeAwareElevatedCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        SelectableInfoText(
                            stringResource(Res.string.alley_artist_details_artist_name),
                            entry()?.artist?.name,
                            showDividerAbove = false,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            val artist = entry()?.artist
            val summary = artist?.summary
            if (artist == null || !summary.isNullOrBlank()) {
                item("artistDescription") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            SelectableInfoText(
                                label = stringResource(Res.string.alley_artist_details_description),
                                body = summary,
                                showDividerAbove = false
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val linkModels = entry()?.artist?.linkModels
            if (linkModels?.isNotEmpty() != false) {
                item("artistLinks") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_links,
                                contentDescriptionTextRes = Res.string.alley_artist_details_links_expand_content_description,
                                values = linkModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { link, _, isLast -> LinkRow(link, isLast) },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val storeLinkModels = entry()?.artist?.storeLinkModels
            if (storeLinkModels?.isNotEmpty() != false) {
                item("artistStoreLinks") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_store,
                                contentDescriptionTextRes = null,
                                values = storeLinkModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { link, _, isLast -> LinkRow(link, isLast) },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val catalogLinks = entry()?.artist?.catalogLinks
            if (catalogLinks?.isNotEmpty() != false) {
                item("artistCatalogLinks") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_catalog,
                                contentDescriptionTextRes = null,
                                values = catalogLinks,
                                valueToText = { it },
                                onClick = onClickOpenUri,
                                allowExpand = false,
                                showDividerAbove = false,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val stampRallies = entry()?.stampRallies
            if (stampRallies?.isNotEmpty() == true) {
                item("artistStampRallies") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_stamp_rallies,
                                contentDescriptionTextRes = null,
                                values = stampRallies,
                                valueToText = { it.fandom },
                                onClick = { eventSink(Event.OpenStampRally(it)) },
                                allowExpand = false,
                                showDividerAbove = false,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val seriesConfirmed = entry()?.seriesConfirmed
            if (seriesConfirmed?.isNotEmpty() != false) {
                item("artistSeriesConfirmed") {
                    Column(Modifier.animateItem()) {
                        val languageOption = LocalLanguageOptionMedia.current
                        val sorted = remember(seriesConfirmed, languageOption) {
                            seriesConfirmed?.sortedBy { it.name(languageOption) }
                        }
                        Confirmed(
                            confirmed = sorted,
                            headerTextRes = Res.string.alley_artist_details_series,
                            forSeries = true,
                        ) { value, expanded ->
                            SeriesRow(
                                series = value,
                                image = { value?.id?.let { seriesImages()[it] } },
                                onClick = if (expanded) {
                                    { value?.id?.let { eventSink(Event.OpenSeries(it)) } }
                                } else null,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val seriesInferred = entry()?.seriesInferred
            if (seriesInferred?.isNotEmpty() != false) {
                item("artistSeriesInferred") {
                    Column(Modifier.animateItem()) {
                        val languageOption = LocalLanguageOptionMedia.current
                        val sorted = remember(seriesInferred, languageOption) {
                            seriesInferred?.sortedBy { it.name(languageOption) }
                        }
                        Inferred(
                            inferred = sorted,
                            headerTextRes = Res.string.alley_artist_details_series_unconfirmed,
                            iconContentDescriptionTextRes = Res.string.alley_artist_details_series_unconfirmed_icon_content_description,
                            forSeries = true,
                        ) { value, expanded ->
                            SeriesRow(
                                series = value,
                                image = { value?.id?.let { seriesImages()[it] } },
                                onClick = if (expanded) {
                                    { value?.id?.let { eventSink(Event.OpenSeries(it)) } }
                                } else null,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val merchConfirmed = entry()?.artist?.merchConfirmed
            if (merchConfirmed?.isNotEmpty() != false) {
                item("artistMerchConfirmed") {
                    Column(Modifier.animateItem()) {
                        Confirmed(
                            confirmed = merchConfirmed,
                            headerTextRes = Res.string.alley_artist_details_merch,
                        ) { value, expanded ->
                            MerchRow(
                                merch = value,
                                expanded = expanded,
                                onClick = { value?.let { eventSink(Event.OpenMerch(it)) } },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val merchInferred = entry()?.artist?.merchInferred
            if (merchInferred?.isNotEmpty() != false) {
                item("artistMerchInferred") {
                    Column(Modifier.animateItem()) {
                        Inferred(
                            inferred = merchInferred,
                            headerTextRes = Res.string.alley_artist_details_merch_unconfirmed,
                            iconContentDescriptionTextRes = Res.string.alley_artist_details_merch_unconfirmed_icon_content_description,
                        ) { value, expanded ->
                            MerchRow(
                                merch = value,
                                expanded = expanded,
                                onClick = { value?.let { eventSink(Event.OpenMerch(it)) } },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }


            val commissionModels = entry()?.artist?.commissionModels
            if (commissionModels?.isNotEmpty() != false) {
                item("artistCommissions") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_commissions,
                                contentDescriptionTextRes = null,
                                values = commissionModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { model, _, isLast -> CommissionRow(model, isLast) },
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            val notes = artist?.notes
            if (!notes.isNullOrEmpty()) {
                item("artistMaintainerNotes") {
                    Column(Modifier.animateItem()) {
                        ThemeAwareElevatedCard(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            SelectableInfoText(
                                label = stringResource(Res.string.alley_maintainer_notes),
                                body = notes,
                                showDividerAbove = false
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            item("artistUserNotes") {
                Column(Modifier.animateItem()) {
                    UserNotesText(
                        state = userNotesTextState,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            item("artistButtons") {
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.animateItem()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
    }

    @Composable
    private fun SelectableInfoText(
        label: String,
        body: String?,
        showDividerAbove: Boolean = true,
    ) {
        SelectionContainer {
            Column {
                InfoText(label, body, showDividerAbove)
            }
        }
    }

    @Composable
    private fun <T> Confirmed(
        confirmed: List<T>?,
        headerTextRes: StringResource,
        modifier: Modifier = Modifier,
        forSeries: Boolean = false,
        item: @Composable (T?, expanded: Boolean) -> Unit,
    ) {
        ThemeAwareElevatedCard(modifier = modifier.padding(horizontal = 16.dp)) {
            expandableListInfoText(
                labelTextRes = headerTextRes,
                contentDescriptionTextRes = null,
                values = confirmed,
                allowExpand = true,
                showDividerAbove = false,
                dividerPadding = PaddingValues(start = if (forSeries) 0.dp else 16.dp),
                header = {
                    DetailsSubsectionHeader(
                        text = stringResource(headerTextRes),
                        modifier = Modifier.padding(bottom = if (forSeries) 6.dp else 0.dp)
                    )
                },
                item = { value, expanded, _ -> item(value, expanded) },
            )
        }
    }

    @Composable
    private fun <T> Inferred(
        inferred: List<T>?,
        headerTextRes: StringResource,
        iconContentDescriptionTextRes: StringResource,
        modifier: Modifier = Modifier,
        forSeries: Boolean = false,
        item: @Composable (T?, expanded: Boolean) -> Unit,
    ) {
        ThemeAwareElevatedCard(modifier = modifier.padding(horizontal = 16.dp)) {
            var showPopup by remember { mutableStateOf(false) }
            expandableListInfoText(
                labelTextRes = headerTextRes,
                contentDescriptionTextRes = null,
                values = inferred,
                allowExpand = true,
                showDividerAbove = false,
                dividerPadding = PaddingValues(start = if (forSeries) 0.dp else 16.dp),
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
                            text = stringResource(headerTextRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    end = 8.dp,
                                    top = 10.dp,
                                    bottom = if (forSeries) 10.dp else 4.dp,
                                )
                        )

                        Box {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(
                                    iconContentDescriptionTextRes
                                ),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .heightIn(max = 20.dp)
                                    .padding(top = 6.dp, bottom = if (forSeries) 6.dp else 0.dp)
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
                item = { value, expanded, _ -> item(value, expanded) },
            )
        }
    }

    @Composable
    private fun LinkRow(link: LinkModel?, isLast: Boolean) {
        val uriHandler = LocalUriHandler.current
        val bottomPadding = if (isLast) 12.dp else 8.dp
        Tooltip(
            link?.link,
            Alignment.BottomEnd,
            onClick = { link?.link?.let { uriHandler.openUri(it) } }) {
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(20.dp)
                        .widthIn(min = 20.dp)
                ) {
                    Icon(
                        imageVector = link?.logo?.icon ?: Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier
                            .height(16.dp)
                            .placeholder(
                                visible = link == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }

                val outlineVariant = MaterialTheme.colorScheme.outline
                val label = link?.logo?.label?.let { stringResource(it) }
                val text = remember(link, label, outlineVariant) {
                    buildAnnotatedString {
                        // TODO: This doesn't support localization
                        if (label != null) {
                            withStyle(SpanStyle(color = outlineVariant)) {
                                append(label)
                                append(" - ")
                            }
                        }
                        if (link != null) {
                            append(link.identifier)
                        }
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .placeholder(
                            visible = link == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }
        }
    }

    @Composable
    private fun CommissionRow(model: CommissionModel?, isLast: Boolean) {
        val uriHandler = LocalUriHandler.current
        val bottomPadding = if (isLast) 12.dp else 8.dp
        val link = (model as? CommissionModel.Link)?.link
        Tooltip(
            text = link,
            popupAlignment = Alignment.BottomEnd,
            onClick = if (link == null) {
                null
            } else {
                { uriHandler.openUri(link) }
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = bottomPadding,
                    )
                    .fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(20.dp)
                        .widthIn(min = 20.dp)
                ) {
                    Icon(
                        imageVector = model?.icon ?: Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .placeholder(
                                visible = model == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }

                val text = model?.text()
                Text(
                    text = text.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .placeholder(
                            visible = model == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
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
private fun PhoneLayout() = PreviewDark {
    val artist = ArtistWithUserDataProvider.values.first()
    val images = CatalogImagePreviewProvider.values.take(4).toList()
    val entry = ArtistDetailsViewModel.Entry(
        artist = artist.artist,
        userEntry = artist.userEntry,
        seriesInferred = (artist.artist.seriesInferred - artist.artist.seriesConfirmed)
            .map { previewSeriesEntry(it) },
        seriesConfirmed = artist.artist.seriesConfirmed.map { previewSeriesEntry(it) },
        stampRallies = emptyList(),
    )
    ArtistDetailsScreen(
        route = Destinations.ArtistDetails(artist.artist),
        entry = { entry },
        userNotesTextState = rememberTextFieldState(),
        imagePagerState = rememberImagePagerState(images, 1),
        eventSink = {},
        catalogImages = { images },
        seriesImages = { emptyMap() },
        otherYears = { listOf(DataYear.YEAR_2024) },
    )
}
