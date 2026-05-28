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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_artist_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_catalog
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_last_updated
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_merch_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_other_artists
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_portfolio
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_series_unconfirmed_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_social_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_social_links_expand_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_stamp_rallies
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_details_tags_unconfirmed_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_artist_new_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_new_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verification_action_is_this_you
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_verified_explanation
import artistalleydatabase.modules.alley.generated.resources.alley_maintainer_notes
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_year
import artistalleydatabase.modules.alley.generated.resources.alley_show_unconfirmed_tags
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTitle
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.details.DetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.details.DetailsScreenCatalog
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImagePreviewProvider
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.links.text
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.notes.UserNotesText
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchChips
import com.thekeeperofpie.artistalleydatabase.alley.tags.previewSeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.tags.series
import com.thekeeperofpie.artistalleydatabase.alley.ui.ClickableIconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.InfiniteProgressIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.utils.isOver
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FiberNew
import com.thekeeperofpie.artistalleydatabase.icons.filled.Info
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.Map
import com.thekeeperofpie.artistalleydatabase.icons.filled.Verified
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.FilledTonalButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.GridUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.InfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.expandableListInfoText
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.optionalClickable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
object ArtistDetailsScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        route: AlleyDestination.ArtistDetails,
        onOpenArtist: (DataYear, artistId: String) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenStampRally: (StampRallyDatabaseEntry) -> Unit,
        onOpenOtherYear: (DataYear) -> Unit,
        onOpenMap: () -> Unit,
        onOpenImages: (DataYear, artistId: String, booth: String, name: String, showingFallback: Boolean, images: List<CatalogImage>, imageIndex: Int, profileImage: CatalogImage?) -> Unit,
        onNavigateUp: () -> Unit,
        viewModel: ArtistDetailsViewModel = viewModel {
            graph.artistDetailsViewModelFactory.create(
                route = route,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val catalog by viewModel.catalog.collectAsStateWithLifecycle()
        val images = catalog.result?.images.orEmpty()
        val imagePagerState = rememberImagePagerState(
            images,
            viewModel.initialImageIndex
        )

        NavigationResultEffect(ImagesScreen.REQUEST_KEY) {
            if (it in (0 until imagePagerState.pageCount)) {
                imagePagerState.scrollToPage(it)
            }
        }
        val entry by viewModel.entry.collectAsStateWithLifecycle()
        val otherArtists by viewModel.otherArtists.collectAsStateWithLifecycle()
        val seriesInferred by viewModel.seriesInferred.collectAsStateWithLifecycle()
        val seriesConfirmed by viewModel.seriesConfirmed.collectAsStateWithLifecycle()
        val seriesImages by viewModel.seriesImages.collectAsStateWithLifecycle()
        ArtistDetailsScreen(
            route = route,
            entry = { entry },
            otherArtists = { otherArtists },
            seriesInferred = { seriesInferred },
            seriesConfirmed = { seriesConfirmed },
            userNotesTextState = viewModel.userNotes,
            imagePagerState = imagePagerState,
            catalog = { catalog },
            seriesImages = { seriesImages },
            otherYears = viewModel::otherYears,
            eventSink = {
                when (it) {
                    is Event.OpenArtist -> onOpenArtist(route.year, it.artistId)
                    is Event.OpenMerch -> onOpenMerch(route.year, it.merch)
                    is Event.OpenSeries -> onOpenSeries(route.year, it.series)
                    is Event.OpenStampRally -> onOpenStampRally(it.entry)
                    is Event.OpenOtherYear -> onOpenOtherYear(it.year)
                    is Event.SeriesFavoriteToggle ->
                        viewModel.onSeriesFavoriteToggle(
                            data = it.series,
                            favorite = it.favorite,
                        )
                    is Event.DetailsEvent ->
                        when (val event = it.event) {
                            is DetailsScreen.Event.FavoriteToggle ->
                                viewModel.onFavoriteToggle(event.favorite)
                            DetailsScreen.Event.NavigateUp -> onNavigateUp()
                            is DetailsScreen.Event.OpenImage -> {
                                val artist = viewModel.entry.value?.artist
                                val booth = artist?.booth
                                if (artist != null) {
                                    val showingOutdatedCatalogs =
                                        catalog.result?.showOutdatedCatalogs == true
                                    val year = catalog.result?.fallbackYear
                                        ?.takeIf { showingOutdatedCatalogs }
                                        ?: route.year
                                    val profileImage =
                                        AlleyImageUtils.getProfileImage(artist.profileImage)
                                    onOpenImages(
                                        year,
                                        artist.id,
                                        booth
                                            .takeUnless { showingOutdatedCatalogs }
                                            .orEmpty(),
                                        artist.name,
                                        showingOutdatedCatalogs,
                                        catalog.result?.images.orEmpty(),
                                        event.imageIndex,
                                        profileImage,
                                    )
                                }
                            }
                            DetailsScreen.Event.OpenMap -> onOpenMap()
                            DetailsScreen.Event.ShowFallback ->
                                viewModel.onShowFallback()
                            DetailsScreen.Event.AlwaysShowFallback ->
                                viewModel.onAlwaysShowFallback()
                        }
                }
            },
        )
    }

    @Composable
    operator fun invoke(
        route: AlleyDestination.ArtistDetails,
        entry: () -> ArtistDetailsViewModel.Entry?,
        otherArtists: () -> List<ArtistEntry>,
        seriesInferred: () -> List<SeriesWithUserData>?,
        seriesConfirmed: () -> List<SeriesWithUserData>?,
        userNotesTextState: TextFieldState,
        imagePagerState: PagerState,
        catalog: () -> LoadingResult<DetailsScreenCatalog>,
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

        val randomSeed = LocalStableRandomSeed.current

        var seriesConfirmedExpanded by rememberSaveable { mutableStateOf(false) }
        val seriesConfirmed = seriesConfirmed()
        val seriesConfirmedRandomizedIndexes = remember(seriesConfirmed) {
            seriesConfirmed.orEmpty().indices.shuffled(Random(randomSeed))
        }

        var seriesInferredExpanded by rememberSaveable { mutableStateOf(false) }
        val seriesInferred = seriesInferred()
        val seriesInferredRandomizedIndexes = remember(seriesInferred) {
            seriesInferred.orEmpty().indices.shuffled(Random(randomSeed))
        }

        val merchConfirmed = entry()?.artist?.merchConfirmed.orEmpty()
        var showInferred by rememberSaveable(seriesConfirmed, merchConfirmed) {
            mutableStateOf(seriesConfirmed.isNullOrEmpty() && merchConfirmed.isEmpty())
        }

        DetailsScreen(
            title = {
                val artist = entry()?.artist
                val id = artist?.id ?: route.id
                val booth = artist?.booth ?: route.booth
                val name = artist?.name ?: route.name
                val profileImage = remember(artist) {
                    artist?.let {
                        AlleyImageUtils.getProfileImage(artist.profileImage)
                    }
                }
                ArtistTitle(
                    year = route.year,
                    id = id,
                    booth = booth,
                    profileImage = profileImage,
                    name = name,
                )
            },
            sharedElementId = route.id,
            favorite = { entry()?.favorite },
            catalog = catalog,
            imagePagerState = imagePagerState,
            eventSink = { eventSink(Event.DetailsEvent(it)) }
        ) { columnCount ->
            val artist = entry()?.artist
            val summary = artist?.summary
            if (artist == null) {
                item("loading", GridUtils.maxSpanFunction) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfiniteProgressIndicator()
                    }
                }
            } else {
                item("artistName", GridUtils.maxSpanFunction) {
                    ThemeAwareElevatedCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {
                            SelectableInfoText(
                                stringResource(Res.string.alley_artist_details_artist_name),
                                artist.name,
                                showDividerAbove = false,
                                modifier = Modifier.weight(1f)
                            )

                            if (artist.newArtist) {
                                ClickableIconWithTooltip(
                                    imageVector = Icons.Default.FiberNew,
                                    tooltipText = stringResource(
                                        Res.string.alley_artist_new_explanation,
                                        stringResource(artist.year.convention.fullName),
                                        artist.year.convention.firstRecordedYear,
                                    ),
                                    contentDescription = stringResource(Res.string.alley_artist_new_content_description),
                                    iconModifier = Modifier.fillMaxHeight()
                                )
                            }

                            if (artist.verifiedArtist) {
                                ClickableIconWithTooltip(
                                    imageVector = Icons.Default.Verified,
                                    tooltipText = stringResource(Res.string.alley_artist_verified_explanation),
                                    contentDescription = stringResource(Res.string.alley_artist_verified_content_description),
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    iconModifier = Modifier.fillMaxHeight()
                                )
                            }
                        }
                    }
                }

                if (!summary.isNullOrBlank()) {
                    item("artistDescription", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            SelectableInfoText(
                                label = stringResource(Res.string.alley_artist_details_description),
                                body = summary,
                                showDividerAbove = false
                            )
                        }
                    }
                }

                val socialLinkModels = artist.socialLinkModels
                if (socialLinkModels.isNotEmpty()) {
                    item("artistLinks", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_social_links,
                                contentDescriptionTextRes = Res.string.alley_artist_details_social_links_expand_content_description,
                                values = socialLinkModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { link, _, isLast -> LinkRow(link, isLast) },
                            )
                        }
                    }
                }

                val storeLinkModels = artist.storeLinkModels
                if (storeLinkModels.isNotEmpty()) {
                    item("artistStoreLinks", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_store,
                                contentDescriptionTextRes = null,
                                values = storeLinkModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { link, _, isLast -> LinkRow(link, isLast) },
                            )
                        }
                    }
                }

                val portfolioLinks = artist.portfolioLinks
                if (portfolioLinks.isNotEmpty()) {
                    item("artistPortfolioLinks", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_portfolio,
                                contentDescriptionTextRes = null,
                                values = portfolioLinks,
                                valueToText = { it },
                                onClick = onClickOpenUri,
                                allowExpand = false,
                                showDividerAbove = false,
                            )
                        }
                    }
                }

                val catalogLinks = artist.catalogLinks
                if (catalogLinks.isNotEmpty()) {
                    item("artistCatalogLinks", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
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
                    }
                }

                val stampRallies = entry()?.stampRallies
                if (stampRallies?.isNotEmpty() == true) {
                    item("artistStampRallies", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
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
                    }
                }

                val commissionModels = artist.commissionModels
                if (commissionModels.isNotEmpty()) {
                    item("artistCommissions", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_commissions,
                                contentDescriptionTextRes = null,
                                values = commissionModels,
                                allowExpand = false,
                                showDividerAbove = false,
                                item = { model, _, isLast -> CommissionRow(model, isLast) },
                            )
                        }
                    }
                }

                val otherArtists = otherArtists()
                if (otherArtists.isNotEmpty()) {
                    item("artistOtherArtists", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            expandableListInfoText(
                                labelTextRes = Res.string.alley_artist_details_other_artists,
                                contentDescriptionTextRes = null,
                                values = otherArtists,
                                valueToText = { it.name },
                                onClick = { eventSink(Event.OpenArtist(it.id)) },
                                allowExpand = false,
                                showDividerAbove = false,
                            )
                        }
                    }
                }

                if (seriesConfirmed?.isNotEmpty() != false) {
                    item(
                        "artistSeriesConfirmedHeader",
                        contentType = "ConfirmedHeader",
                        span = GridUtils.maxSpanFunction,
                    ) {
                        Text(
                            text = stringResource(Res.string.alley_artist_details_series),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }
                    series(
                        key = "artistSeriesConfirmed",
                        series = seriesConfirmed.orEmpty(),
                        image = { seriesImages()[it.series.id] },
                        columnCount = columnCount,
                        randomizedIndexes = seriesConfirmedRandomizedIndexes,
                        expanded = { seriesConfirmedExpanded },
                        onExpanded = { seriesConfirmedExpanded = true },
                        onClick = { eventSink(Event.OpenSeries(it.series.id)) },
                    )
                }

                if (merchConfirmed.isNotEmpty()) {
                    item(
                        "artistMerchConfirmedHeader",
                        contentType = "ConfirmedHeader",
                        span = GridUtils.maxSpanFunction,
                    ) {
                        Text(
                            text = stringResource(Res.string.alley_artist_details_merch),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.surfaceTint,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }
                    item("artistMerchConfirmed", GridUtils.maxSpanFunction) {
                        MerchChips(merchConfirmed, onClick = { eventSink(Event.OpenMerch(it)) })
                    }
                }

                if (showInferred) {
                    if (seriesInferred?.isNotEmpty() != false) {
                        item(
                            "artistSeriesInferredHeader",
                            contentType = "InferredHeader",
                            span = GridUtils.maxSpanFunction,
                        ) {
                            InferredHeader(
                                headerTextRes = Res.string.alley_artist_details_series_unconfirmed,
                                iconContentDescriptionTextRes = Res.string.alley_artist_details_series_unconfirmed_icon_content_description,
                            )
                        }
                        series(
                            key = "artistSeriesInferred",
                            series = seriesInferred.orEmpty(),
                            image = { seriesImages()[it.series.id] },
                            columnCount = columnCount,
                            randomizedIndexes = seriesInferredRandomizedIndexes,
                            expanded = { seriesInferredExpanded },
                            onExpanded = { seriesInferredExpanded = true },
                            onClick = { eventSink(Event.OpenSeries(it.series.id)) },
                        )
                    }

                    val merchInferred = artist.merchInferred
                    if (merchInferred.isNotEmpty()) {
                        item(
                            "artistMerchInferredHeader",
                            contentType = "InferredHeader",
                            span = GridUtils.maxSpanFunction,
                        ) {
                            InferredHeader(
                                headerTextRes = Res.string.alley_artist_details_merch_unconfirmed,
                                iconContentDescriptionTextRes = Res.string.alley_artist_details_merch_unconfirmed,
                            )
                        }
                        item("artistMerchInferred", GridUtils.maxSpanFunction) {
                            MerchChips(merchInferred, onClick = { eventSink(Event.OpenMerch(it)) })
                        }
                    }
                } else {
                    item("artistShowInferredButton", GridUtils.maxSpanFunction) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledTonalButton(onClick = {
                                seriesInferredExpanded = true
                                showInferred = true
                            }) {
                                Text(stringResource(Res.string.alley_show_unconfirmed_tags))
                            }
                        }
                    }
                }

                val notes = artist.notes
                if (!notes.isNullOrEmpty()) {
                    item("artistMaintainerNotes", GridUtils.maxSpanFunction) {
                        ThemeAwareElevatedCard {
                            SelectableInfoText(
                                label = stringResource(Res.string.alley_maintainer_notes),
                                body = notes,
                                showDividerAbove = false
                            )
                        }
                    }
                }

                item("artistUserNotes", GridUtils.maxSpanFunction) {
                    UserNotesText(state = userNotesTextState)
                }

                item("artistFooter", GridUtils.maxSpanFunction) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.animateItem()
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        val lastEditTime = artist.lastEditTime
                        if (lastEditTime != null) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                    TooltipAnchorPosition.Above
                                ),
                                state = rememberTooltipState(),
                                tooltip = {
                                    Text(LocalDateTimeFormatter.current.formatDateTime(lastEditTime))
                                },
                            ) {
                                Text(
                                    text = stringResource(
                                        Res.string.alley_artist_details_last_updated,
                                        LocalDateTimeFormatter.current.formatRemainingTime(
                                            lastEditTime
                                        ),
                                    ),
                                    style = MaterialTheme.typography.labelSmallEmphasized,
                                )
                            }
                        }
                    }
                }

                item("artistButtons", GridUtils.maxSpanFunction) {
                    FlowRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        modifier = Modifier.animateItem().fillMaxWidth()
                    ) {
                        // TODO: verifiedArtist check was removed here, re-add as a badge?
                        val entry = entry()
                        if (entry != null && !entry.artist.year.dates.isOver) {
                            var showVerificationDialog by remember { mutableStateOf(false) }
                            FilledTonalButton(
                                icon = Icons.Default.Verified,
                                text = stringResource(Res.string.alley_artist_verification_action_is_this_you),
                                onClick = { showVerificationDialog = true },
                            )
                            if (showVerificationDialog) {
                                ArtistVerificationDialog(
                                    artist = entry.artist,
                                    onDismiss = { showVerificationDialog = false },
                                )
                            }
                        }

                        FilledTonalButton(
                            icon = Icons.Default.Map,
                            text = stringResource(Res.string.alley_open_in_map),
                            onClick = { eventSink(Event.DetailsEvent(DetailsScreen.Event.OpenMap)) },
                        )

                        otherYears().forEach {
                            FilledTonalButton(onClick = { eventSink(Event.OpenOtherYear(it)) }) {
                                Text(
                                    stringResource(
                                        Res.string.alley_open_year,
                                        stringResource(it.shortName)
                                    )
                                )
                            }
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
        modifier: Modifier = Modifier,
        showDividerAbove: Boolean = true,
    ) {
        SelectionContainer(modifier = modifier) {
            Column {
                InfoText(label, body, showDividerAbove)
            }
        }
    }

    @Composable
    private fun InferredHeader(
        headerTextRes: StringResource,
        iconContentDescriptionTextRes: StringResource,
    ) {
        var showPopup by remember { mutableStateOf(false) }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                modifier = Modifier.padding(vertical = 4.dp)
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
                        .padding(vertical = 4.dp)
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
    }

    @Composable
    private fun CommissionRow(model: CommissionModel?, isLast: Boolean) {
        val uriHandler = LocalUriHandler.current
        val bottomPadding = if (isLast) 12.dp else 8.dp
        val link = (model as? CommissionModel.Link)?.link

        val content = remember {
            movableContentOf {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .optionalClickable(
                            if (link == null) {
                                null
                            } else {
                                {
                                    uriHandler.openUri(link)
                                }
                            }
                        )
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
        if (link == null) {
            content()
        } else {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Below,
                    spacingBetweenTooltipAndAnchor = 0.dp,
                ),
                tooltip = {
                    PlainTooltip(Modifier.clickable { uriHandler.openUri(link) }) {
                        Text(link)
                    }
                },
                state = rememberTooltipState(),
                content = content,
            )
        }
    }

    sealed interface Event {
        data class DetailsEvent(val event: DetailsScreen.Event) : Event
        data class OpenArtist(val artistId: String) : Event
        data class OpenMerch(val merch: String) : Event
        data class OpenOtherYear(val year: DataYear) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenStampRally(val entry: StampRallyDatabaseEntry) : Event
        data class SeriesFavoriteToggle(
            val series: SeriesWithUserData,
            val favorite: Boolean,
        ) : Event
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
        stampRallies = emptyList(),
    )
    val seriesInferred = (artist.artist.seriesInferred - artist.artist.seriesConfirmed)
        .map { previewSeriesWithUserData(it) }
    val seriesConfirmed = artist.artist.seriesConfirmed.map { previewSeriesWithUserData(it) }
    ArtistDetailsScreen(
        route = AlleyDestination.ArtistDetails(artist.artist),
        entry = { entry },
        otherArtists = { emptyList() },
        seriesInferred = { seriesInferred },
        seriesConfirmed = { seriesConfirmed },
        userNotesTextState = rememberTextFieldState(),
        imagePagerState = rememberImagePagerState(images, 1),
        eventSink = {},
        catalog = { LoadingResult.success(DetailsScreenCatalog(images, null, null)) },
        seriesImages = { emptyMap() },
        otherYears = { listOf(DataYear.ANIME_EXPO_2024) },
    )
}
