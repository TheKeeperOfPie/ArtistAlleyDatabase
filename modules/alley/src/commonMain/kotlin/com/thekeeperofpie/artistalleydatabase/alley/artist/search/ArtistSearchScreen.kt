package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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
import artistalleydatabase.modules.alley.generated.resources.alley_search_title_results_suffix
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.text
import com.thekeeperofpie.artistalleydatabase.alley.links.tooltip
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen.DisplayType
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.tags.previewSeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.ui.PreviewDark
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object ArtistSearchScreen {

    @Composable
    operator fun invoke(
        viewModel: ArtistSearchViewModel,
        sortFilterController: ArtistSortFilterController,
        onClickBack: (() -> Unit)?,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
    ) {
        val state = remember(viewModel, sortFilterController) {
            State(viewModel, sortFilterController)
        }
        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        val navigationController = LocalNavigationController.current
        ArtistSearchScreen(
            state = state,
            sortFilterState = sortFilterController.state,
            eventSink = { viewModel.onEvent(navigationController, it) },
            onClickBack,
            header = { BottomSheetFilterDataYearHeader(dataYearHeaderState, scaffoldState) },
            scaffoldState,
            scrollStateSaver,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        sortFilterState: SortFilterState<*>,
        eventSink: (Event) -> Unit,
        onClickBack: (() -> Unit)?,
        header: @Composable () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        actions: (@Composable RowScope.() -> Unit)? = null,
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        sortFilterState.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
            val lockedSeriesEntry by state.lockedSeriesEntry.collectAsStateWithLifecycle()
            val entries = state.results.collectAsLazyPagingItems()
            val unfilteredCount by state.unfilteredCount.collectAsStateWithLifecycle()
            val count = entries.itemCount
            val title = (lockedSeriesEntry?.name(LocalLanguageOptionMedia.current)
                ?: state.lockedMerch)
                ?.let {
                    @Suppress("USELESS_IS_CHECK")
                    if (entries.loadState.refresh is LoadState.Loading) {
                        it
                    } else {
                        pluralStringResource(
                            Res.plurals.alley_search_title_results_suffix,
                            count,
                            it,
                            count,
                        )
                    }
                }

            SearchScreen(
                state = state.searchState,
                eventSink = {
                    eventSink(Event.SearchEvent(it))
                },
                query = state.query,
                onClickBack = onClickBack,
                entries = entries,
                unfilteredCount = { unfilteredCount },
                scaffoldState = scaffoldState,
                sortFilterState = sortFilterState,
                gridState = gridState,
                title = { title },
                header = header,
                itemToSharedElementId = { it.artist.id },
                actions = actions,
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
                val shuffledSeries = row?.series
                val languageOption = LocalLanguageOptionMedia.current
                val seriesNames = remember(row?.series, languageOption) {
                    shuffledSeries?.map { it.name(languageOption) }
                }
                TagsCell(
                    column = column,
                    tags = seriesNames,
                    hasMoreTags = row?.hasMoreSeries == true,
                    moreContentDescription = Res.string.alley_expand_series,
                    onTagClick = onSeriesClick,
                    onMoreClick = { if (row != null) onEntryClick(row, 1) },
                )
            }
            ArtistColumn.MERCH -> TagsCell(
                column = column,
                tags = row?.merch,
                hasMoreTags = row?.hasMoreMerch == true,
                moreContentDescription = Res.string.alley_expand_merch,
                onTagClick = onMerchClick,
                onMoreClick = { if (row != null) onEntryClick(row, 1) },
            )
            ArtistColumn.LINKS -> row?.artist?.linkModels?.let { linkModels ->
                val uriHandler = LocalUriHandler.current
                Grid(linkModels.size) {
                    val linkModel = linkModels[it]
                    TooltipIconButton(
                        icon = linkModel.logo?.icon ?: Icons.Default.Link,
                        tooltipText = linkModel.link,
                        positioning = TooltipAnchorPosition.Above,
                        onClick = { uriHandler.openUri(linkModel.link) },
                    )
                }
            }
            ArtistColumn.STORE -> row?.artist?.storeLinkModels?.let { storeLinkModels ->
                val uriHandler = LocalUriHandler.current
                Grid(count = storeLinkModels.size, columnCount = 2) {
                    val linkModel = storeLinkModels[it]
                    TooltipIconButton(
                        icon = linkModel.logo?.icon ?: Icons.Default.Link,
                        tooltipText = linkModel.link,
                        positioning = TooltipAnchorPosition.Above,
                        onClick = { uriHandler.openUri(linkModel.link) },
                    )
                }
            }
            ArtistColumn.COMMISSIONS -> row?.artist?.commissionModels?.let {
                Column {
                    val uriHandler = LocalUriHandler.current
                    it.forEach {
                        when (it) {
                            is CommissionModel.Link -> TooltipIconButton(
                                icon = it.icon,
                                tooltipText = it.tooltip(),
                                positioning = TooltipAnchorPosition.Above,
                                onClick = { uriHandler.openUri(it.link) },
                            )
                            CommissionModel.Online,
                            CommissionModel.OnSite,
                            is CommissionModel.Unknown-> TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        positioning = TooltipAnchorPosition.Below,
                                        spacingBetweenTooltipAndAnchor = 0.dp,
                                    ),
                                    tooltip = { PlainTooltip { Text(it.tooltip()) } },
                                    state = rememberTooltipState(),
                                ) {
                                    CommissionChip(
                                        model = it,
                                        label = { Text(it.text()) },
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TagsCell(
        column: ArtistColumn,
        tags: List<String>?,
        hasMoreTags: Boolean,
        moreContentDescription: StringResource,
        onTagClick: (String) -> Unit,
        onMoreClick: () -> Unit,
    ) {
        if (tags.isNullOrEmpty()) return
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            tags.take(ArtistEntryGridModel.TAGS_TO_SHOW)
                .forEach {
                    SuggestionChip(
                        onClick = { onTagClick(it) },
                        label = { Text(text = it, modifier = Modifier.padding(vertical = 4.dp)) },
                        modifier = Modifier.widthIn(max = column.size - 16.dp)
                    )
                }
            if (hasMoreTags) {
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.OpenInNew,
                        contentDescription = stringResource(moreContentDescription),
                    )
                }
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

    @Composable
    private fun Grid(count: Int, columnCount: Int = 3, item: @Composable (index: Int) -> Unit) {
        Column {
            val rowCount = (count + columnCount - 1) / columnCount
            var index = 0
            repeat(rowCount) {
                Row {
                    repeat(columnCount) {
                        if (index < count) {
                            item(index++)
                        }
                    }
                }
            }
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
        val lockedSeriesEntry: StateFlow<SeriesInfo?>,
        val lockedMerch: String?,
        val lockedYear: DataYear?,
        val randomSeed: Int,
        val year: MutableStateFlow<DataYear>,
        val query: MutableStateFlow<String>,
        val results: StateFlow<PagingData<ArtistEntryGridModel>>,
        val unfilteredCount: StateFlow<Int>,
        val showOnlyWithCatalog: StateFlow<Boolean>,
        val sortOption: MutableStateFlow<ArtistSearchSortOption>,
        val sortAscending: MutableStateFlow<Boolean>,
        val searchState: SearchScreen.State<ArtistColumn>,
    ) {
        constructor(
            viewModel: ArtistSearchViewModel,
            sortFilterController: ArtistSortFilterController,
        ) : this(
            lockedSeriesEntry = viewModel.lockedSeriesEntry,
            lockedMerch = viewModel.lockedMerch,
            lockedYear = viewModel.lockedYear,
            randomSeed = viewModel.randomSeed,
            year = viewModel.year,
            query = viewModel.query,
            results = viewModel.results,
            unfilteredCount = viewModel.unfilteredCount,
            showOnlyWithCatalog = sortFilterController.showOnlyWithCatalog,
            sortOption = sortFilterController.sortOption,
            sortAscending = sortFilterController.sortAscending,
            searchState = viewModel.searchState,
        )
    }

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
                    series = it.artist.seriesInferred.take(ArtistEntryGridModel.TAGS_TO_SHOW)
                        .map { previewSeriesWithUserData(it).series },
                    hasMoreSeries = true,
                    showOutdatedCatalogs = true,
                    fallbackCatalog = null,
                )
            }
        val state = State(
            lockedSeriesEntry = MutableStateFlow(null),
            lockedMerch = null,
            lockedYear = null,
            randomSeed = 1,
            year = MutableStateFlow(DataYear.ANIME_EXPO_2025),
            query = MutableStateFlow(""),
            results = MutableStateFlow(PagingData.from(results)),
            unfilteredCount = MutableStateFlow(1000),
            showOnlyWithCatalog = MutableStateFlow(false),
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

        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        val scaffoldState = rememberBottomSheetScaffoldState()
        ArtistSearchScreen(
            state = state,
            sortFilterState = SortFilterState(
                emptyList(),
                MutableStateFlow(Unit),
                MutableStateFlow(false)
            ),
            eventSink = {},
            onClickBack = {},
            header = { BottomSheetFilterDataYearHeader(dataYearHeaderState, scaffoldState) },
            scrollStateSaver = ScrollStateSaver.STUB,
        )
    }
}
