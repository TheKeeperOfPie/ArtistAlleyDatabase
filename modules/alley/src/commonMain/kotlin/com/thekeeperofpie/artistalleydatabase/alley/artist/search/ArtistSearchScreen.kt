package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.PagingData
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_summary
import artistalleydatabase.modules.alley.generated.resources.alley_expand_merch
import artistalleydatabase.modules.alley.generated.resources.alley_expand_series
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.text
import com.thekeeperofpie.artistalleydatabase.alley.links.tooltip
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.previewSeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object ArtistSearchScreen {

    @Composable
    operator fun invoke(
        viewModel: ArtistSearchViewModel,
        sortViewModel: ArtistSortFilterViewModel,
        onClickBack: (() -> Unit)?,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
    ) {
        val navigationController = LocalNavigationController.current
        ArtistSearchScreen(
            remember(viewModel, sortViewModel) {
                State(
                    lockedSeriesEntry = viewModel.lockedSeriesEntry,
                    lockedMerch = viewModel.lockedMerch,
                    lockedYear = viewModel.lockedYear,
                    randomSeed = viewModel.randomSeed,
                    year = viewModel.year,
                    query = viewModel.query,
                    results = viewModel.results,
                    onlyCatalogImages = sortViewModel.onlyCatalogImages,
                    sortOption = sortViewModel.sortOption,
                    sortAscending = sortViewModel.sortAscending,
                    searchState = viewModel.searchState,
                )
            },
            sortFilterState = sortViewModel.state,
            eventSink = { viewModel.onEvent(navigationController, it) },
            onClickBack,
            scaffoldState,
            scrollStateSaver,
            onClickMap,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        sortFilterState: SortFilterState<*>,
        eventSink: (Event) -> Unit,
        onClickBack: (() -> Unit)?,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        sortFilterState.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
            val showOnlyCatalogImages by state.onlyCatalogImages.collectAsStateWithLifecycle()
            val entries = state.results.collectAsLazyPagingItemsWithLifecycle()
            val query by state.query.collectAsStateWithLifecycle()
            val lockedSeriesEntry by state.lockedSeriesEntry.collectAsStateWithLifecycle()
            val shouldShowCount by remember {
                derivedStateOf {
                    query.isNotEmpty()
                            || showOnlyCatalogImages
                            || lockedSeriesEntry != null
                            || state.lockedMerch != null

                }
            }

            val year by state.year.collectAsState()
            val yearShortName = stringResource(year.shortName)
            val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
            val languageOption = LocalLanguageOptionMedia.current
            val seriesTitle = lockedSeriesEntry?.name(languageOption)
            SearchScreen(
                state = state.searchState,
                eventSink = {
                    eventSink(Event.SearchEvent(it))
                },
                query = state.query,
                title = { seriesTitle ?: state.lockedMerch },
                actions = if (onClickMap == null) {
                    null
                } else {
                    {
                        IconButton(onClick = onClickMap) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = stringResource(Res.string.alley_open_in_map),
                            )
                        }
                    }
                },
                onClickBack = onClickBack,
                entries = entries,
                scaffoldState = scaffoldState,
                bottomSheet = {
                    SortFilterOptionsPanel(
                        state = sortFilterState,
                        showClear = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 320.dp)
                    )
                },
                dataYearHeaderState = dataYearHeaderState,
                gridState = gridState,
                shouldShowCount = { shouldShowCount },
                itemToSharedElementId = { it.artist.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    ArtistListRow(
                        entry = entry,
                        onFavoriteToggle = onFavoriteToggle,
                        onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                        onMoreClick = {
                            eventSink(
                                Event.SearchEvent(
                                    SearchScreen.Event.OpenEntry(entry, 1)
                                )
                            )
                        },
                        modifier = modifier
                    )
                },
                columnHeader = { ColumnHeader(it, state.sortOption, state.sortAscending) },
                tableCell = { row, column ->
                    TableCell(
                        row = row,
                        column = column,
                        onEntryClick = { entry, imageIndex ->
                            eventSink(
                                Event.SearchEvent(SearchScreen.Event.OpenEntry(entry, imageIndex))
                            )
                        },
                        onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                        onMerchClick = { eventSink(Event.OpenMerch(it)) },
                    )
                },
            )
        }
    }

    @Composable
    fun ColumnHeader(
        column: ArtistColumn,
        sortOption: MutableStateFlow<ArtistSearchSortOption>,
        sortAscending: MutableStateFlow<Boolean>,
    ) {
        val columnSortOption = when (column) {
            ArtistColumn.BOOTH -> ArtistSearchSortOption.BOOTH
            ArtistColumn.NAME -> ArtistSearchSortOption.ARTIST
            else -> null
        }
        var sortOption by sortOption.collectAsMutableStateWithLifecycle()
        var sortAscending by sortAscending.collectAsMutableStateWithLifecycle()
        Row(
            modifier = Modifier.requiredWidth(column.size)
                .clickable(enabled = columnSortOption != null) {
                    if (columnSortOption != null) {
                        if (sortOption == columnSortOption) {
                            sortAscending = !sortAscending
                        } else {
                            sortOption = columnSortOption
                        }
                    }
                }
                .then(TwoWayGrid.modifierDefaultCellPadding)
        ) {
            AutoSizeText(
                text = stringResource(column.text),
                modifier = Modifier.weight(1f)
            )

            if (sortOption == columnSortOption) {
                Icon(
                    imageVector = if (sortAscending) {
                        Icons.Default.ArrowDropUp
                    } else {
                        Icons.Default.ArrowDropDown
                    },
                    contentDescription = null,
                )
            } else if (columnSortOption != null) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    tint = IconButtonDefaults.iconButtonColors().disabledContentColor,
                )
            }
        }
    }

    @Composable
    fun TableCell(
        row: ArtistEntryGridModel?, column: ArtistColumn,
        onEntryClick: (ArtistEntryGridModel, imageIndex: Int) -> Unit,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
    ) {
        val clickableCellModifier = Modifier.fillMaxSize()
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .conditionallyNonNull(row) { clickable { onEntryClick(it, 1) } }
            .then(TwoWayGrid.modifierDefaultCellPadding)
        when (column) {
            // TODO: Dynamic minimum column size by measuring header text
            ArtistColumn.BOOTH -> Box(clickableCellModifier) {
                AutoSizeText(text = row?.booth.orEmpty())
            }
            ArtistColumn.NAME -> Box(clickableCellModifier) {
                Text(text = row?.artist?.name.orEmpty())
            }
            ArtistColumn.SUMMARY -> Box(clickableCellModifier) {
                Text(text = row?.artist?.summary.orEmpty())
            }
            ArtistColumn.SERIES -> {
                val series = row?.series
                val randomSeed = LocalStableRandomSeed.current
                val languageOption = LocalLanguageOptionMedia.current
                val shuffledSeries = remember(series, randomSeed, languageOption) {
                    series?.map { it.name(languageOption) }
                        ?.shuffled(Random(randomSeed))
                }
                TagsFlowRow(
                    column = column,
                    tags = shuffledSeries,
                    contentDescription = Res.string.alley_expand_series,
                    onEntryClick = { if (row != null) onEntryClick(row, 1) },
                    onTagClick = onSeriesClick,
                )
            }
            ArtistColumn.MERCH -> TagsFlowRow(
                column = column,
                tags = row?.merch,
                contentDescription = Res.string.alley_expand_merch,
                onEntryClick = { if (row != null) onEntryClick(row, 1) },
                onTagClick = onMerchClick,
            )
            ArtistColumn.LINKS -> row?.artist?.linkModels?.let {
                FlowRow {
                    val uriHandler = LocalUriHandler.current
                    it.forEach {
                        IconButtonWithTooltip(
                            imageVector = it.logo?.icon ?: Icons.Default.Link,
                            tooltipText = it.link,
                            onClick = { uriHandler.openUri(it.link) },
                        )
                    }
                }
            }
            ArtistColumn.STORE -> row?.artist?.storeLinkModels?.let {
                FlowRow {
                    val uriHandler = LocalUriHandler.current
                    it.forEach {
                        IconButtonWithTooltip(
                            imageVector = it.logo?.icon ?: Icons.Default.Link,
                            tooltipText = it.link,
                            onClick = { uriHandler.openUri(it.link) },
                        )
                    }
                }
            }
            ArtistColumn.COMMISSIONS -> row?.artist?.commissionModels?.let {
                FlowRow {
                    val uriHandler = LocalUriHandler.current
                    it.forEach {
                        when (it) {
                            is CommissionModel.Link -> IconButtonWithTooltip(
                                imageVector = it.icon,
                                tooltipText = it.tooltip(),
                                onClick = { uriHandler.openUri(it.link) },
                            )
                            CommissionModel.OnSite -> Tooltip(it.tooltip()) {
                                CommissionChip(
                                    model = it,
                                    label = { Text(it.text()) },
                                )
                            }
                            CommissionModel.Online -> Tooltip(it.tooltip()) {
                                CommissionChip(
                                    model = it,
                                    label = { Text(it.text()) },
                                )
                            }
                            is CommissionModel.Unknown ->
                                CommissionChip(model = it, label = { Text(it.text()) })
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TagsFlowRow(
        column: ArtistColumn,
        tags: List<String>?,
        contentDescription: StringResource,
        onEntryClick: () -> Unit,
        onTagClick: (String) -> Unit,
    ) {
        if (tags.isNullOrEmpty()) return
        FlowRow(
            maxLines = 6,
            overflow = FlowRowOverflow.expandIndicator {
                IconButton(onClick = onEntryClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.OpenInNew,
                        contentDescription = stringResource(contentDescription),
                    )
                }
            },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            tags.forEach {
                SuggestionChip(
                    onClick = { onTagClick(it) },
                    label = { Text(text = it, modifier = Modifier.padding(vertical = 4.dp)) },
                    modifier = Modifier.widthIn(max = column.size - 16.dp)
                )
            }
        }
    }

    @Composable
    private fun CommissionChip(model: CommissionModel, label: @Composable () -> Unit) {
        Box(Modifier.padding(horizontal = 4.dp)) {
            SuggestionChip(
                onClick = {},
                icon = {
                    Icon(
                        imageVector = model.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = label,
            )
        }
    }

    enum class ArtistColumn(
        override val size: Dp,
        override val text: StringResource,
    ) : TwoWayGrid.Column {
        BOOTH(120.dp, Res.string.alley_artist_column_booth),
        NAME(160.dp, Res.string.alley_artist_column_name),
        SUMMARY(400.dp, Res.string.alley_artist_column_summary),
        SERIES(288.dp, Res.string.alley_artist_column_series),
        MERCH(144.dp, Res.string.alley_artist_column_merch),
        LINKS(144.dp, Res.string.alley_artist_column_links),
        STORE(96.dp, Res.string.alley_artist_column_store),
        COMMISSIONS(144.dp, Res.string.alley_artist_column_commissions),
    }

    @Stable
    class State(
        val lockedSeriesEntry: StateFlow<SeriesEntry?>,
        val lockedMerch: String?,
        val lockedYear: DataYear?,
        val randomSeed: Int,
        val year: MutableStateFlow<DataYear>,
        val query: MutableStateFlow<String>,
        val results: StateFlow<PagingData<ArtistEntryGridModel>>,
        val onlyCatalogImages: StateFlow<Boolean>,
        val sortOption: MutableStateFlow<ArtistSearchSortOption>,
        val sortAscending: MutableStateFlow<Boolean>,
        val searchState: SearchScreen.State<ArtistColumn>,
    )

    sealed interface Event {
        data class SearchEvent(val event: SearchScreen.Event<ArtistEntryGridModel>) : Event
        data class OpenSeries(val series: String) : Event
        data class OpenMerch(val merch: String) : Event
    }

    @Preview
    @Composable
    private fun Preview() = PreviewDark {
        val results = ArtistWithUserDataProvider.values.take(5)
            .toList()
            .map {
                ArtistEntryGridModel.buildFromEntry(
                    randomSeed = 1,
                    showOnlyConfirmedTags = false,
                    entry = it,
                    series = it.artist.seriesInferred.map { previewSeriesEntry(it) }
                )
            }
        val state = State(
            lockedSeriesEntry = MutableStateFlow(null),
            lockedMerch = null,
            lockedYear = null,
            randomSeed = 1,
            year = MutableStateFlow(DataYear.YEAR_2025),
            query = MutableStateFlow(""),
            results = MutableStateFlow(PagingData.from(results)),
            onlyCatalogImages = MutableStateFlow(false),
            sortOption = MutableStateFlow(ArtistSearchSortOption.RANDOM),
            sortAscending = MutableStateFlow(false),
            searchState = SearchScreen.State<ArtistColumn>(
                columns = ArtistColumn.entries,
                displayType = MutableStateFlow(DisplayType.CARD),
                showGridByDefault = MutableStateFlow(false),
                showRandomCatalogImage = MutableStateFlow(false),
                forceOneDisplayColumn = MutableStateFlow(false),
            ),
        )

        ArtistSearchScreen(
            state = state,
            sortFilterState = SortFilterState(
                emptyList(),
                MutableStateFlow(Unit),
                MutableStateFlow(false)
            ),
            eventSink = {},
            onClickBack = {},
            scrollStateSaver = ScrollStateSaver.STUB,
        )
    }
}
