package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_expand_merch
import artistalleydatabase.modules.alley.generated.resources.alley_expand_series
import artistalleydatabase.modules.alley.generated.resources.alley_search_title_results_suffix
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen.Event
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen.State
import com.thekeeperofpie.artistalleydatabase.alley.artist.ui.ArtistSearchItem
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.text
import com.thekeeperofpie.artistalleydatabase.alley.links.tooltip
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchList
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchMoreResults
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.ui.AlleySearchBar
import com.thekeeperofpie.artistalleydatabase.alley.ui.ConventionCountdownHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.FeedbackHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.OpenInNew
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowDropDown
import com.thekeeperofpie.artistalleydatabase.icons.filled.ArrowDropUp
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.UnfoldMore
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopBarScaffold
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RootArtistSearchScreen(
    lockedYear: DataYear? = null,
    lockedSerializedBooths: String? = null,
    viewModel: ArtistSearchViewModel,
) {
    val state = viewModel.state
    CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
        val entries = state.results.collectAsLazyPagingItems()
        EnterAlwaysTopBarScaffold(
            topBar = {
                ArtistSearchScreenTopBar(state, entries, viewModel::onEvent)
            }
        ) { contentPadding ->
            ArtistSearchScreenContent(
                sortFilterState = state.sortFilterState,
                baseContentPadding = contentPadding,
                header = { Header(state, viewModel::onEvent) },
                entries = entries,
                series = state.series,
                unfilteredCount = state.unfilteredCount,
                displayType = state.displayType,
                eventSink = viewModel::onEvent,
            )
        }
    }
}

@Composable
fun ArtistSearchScreen(
    state: State,
    eventSink: (Event) -> Unit,
    header: @Composable () -> Unit,
    scaffoldState: SortFilterBottomScaffoldState = rememberSortFilterBottomScaffoldState(),
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    // TODO
//    sortFilterState.ImmediateScrollResetEffect(gridState)

    CompositionLocalProvider(LocalStableRandomSeed provides state.randomSeed) {
        val entries = state.results.collectAsLazyPagingItems()
        SortFilterBottomScaffold2(
            state = scaffoldState,
            enterAlwaysTopBar = {
                ArtistSearchScreenTopBar(state, entries, eventSink, actions)
            },
            sheetContent = {
                ArtistSortFilterSheetContent(
                    state = state.sortFilterState,
                    sheetState = scaffoldState.bottomSheetState,
                )
            },
        ) { _, contentPadding ->
            ArtistSearchScreenContent(
                sortFilterState = state.sortFilterState,
                baseContentPadding = contentPadding,
                header = header,
                entries = entries,
                series = state.series,
                unfilteredCount = state.unfilteredCount,
                displayType = state.displayType,
                eventSink = eventSink,
            )
        }
    }
}

@Composable
internal fun ArtistSearchScreenTopBar(
    state: State,
    entries: LazyPagingItems<ArtistEntryGridModel>,
    onEvent: (Event) -> Unit,
    additionalActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val count = entries.itemCount
    val lockedSeriesEntry = state.lockedSeriesEntry()
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
    AlleySearchBar(
        onClickBack = { onEvent(Event.Back) },
        query = state.query,
        itemCount = { entries.itemCount },
        title = { title },
        additionalActions = additionalActions,
        modifier = Modifier
            .animateEnterExit(
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
            )
            .renderMaybeInSharedTransitionScopeOverlay(1f)
    )
}

@Composable
internal fun ArtistSearchScreenContent(
    sortFilterState: ArtistSortFilterState,
    baseContentPadding: () -> PaddingValues,
    header: @Composable () -> Unit,
    entries: LazyPagingItems<ArtistEntryGridModel>,
    series: () -> Map<String, GetSeriesTitles>,
    unfilteredCount: () -> Int,
    displayType: () -> SearchDisplayType,
    eventSink: (Event) -> Unit,
    modifier: Modifier = Modifier,
    noResultsItem: (@Composable () -> Unit)? = null,
) {
    val gridState = rememberLazyStaggeredGridState()
    val displayType = displayType()
    if (displayType == SearchDisplayType.TABLE) {
        TwoWayGrid(
            columns = ArtistSearchColumn.entries,
            baseContentPadding = baseContentPadding,
            header = header,
            rows = entries,
            unfilteredCount = unfilteredCount,
            columnHeader = {
                ArtistSearchScreen.ColumnHeader(
                    column = it,
                    sortOption = sortFilterState.persistentState.sortFilterSettings::artistsSortOption,
                    sortAscending = sortFilterState.persistentState.sortFilterSettings::artistsSortAscending,
                )
            },
            tableCell = { row, column ->
                ArtistSearchScreen.TableCell(
                    row = row,
                    column = column,
                    series = series,
                    onEntryClick = { entry, imageIndex ->
                        eventSink(Event.OpenEntry(entry, imageIndex))
                    },
                    onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                    onMerchClick = { eventSink(Event.OpenMerch(it)) },
                )
            },
            noResultsHeader = noResultsItem,
            moreResultsFooter = {
                SearchMoreResults(
                    unfilteredCount = unfilteredCount,
                    itemCount = { entries.itemCount },
                    onClick = { eventSink(Event.ClearFilters) },
                )
            },
            modifier = modifier,
        )
    } else {
        SearchList(
            entries = entries,
            itemToId = { it.id.scopedId },
            displayType = { displayType },
            baseContentPadding = baseContentPadding,
            forceOneDisplayColumn = sortFilterState.persistentState.displaySettings::forceOneDisplayColumn,
            unfilteredCount = unfilteredCount,
            gridState = gridState,
            header = header,
            itemRow = { entry ->
                ArtistSearchItem(
                    displayType = displayType,
                    artistWithUserData = entry.data,
                    showGridByDefault = sortFilterState.persistentState.displaySettings.showGridByDefault,
                    showRandomCatalogImage = sortFilterState.persistentState.displaySettings.showRandomCatalogImage,
                    blockCrossAxisScrolling = { gridState.isScrollInProgress },
                    showOutdatedCatalogs = sortFilterState.persistentState.sortFilterSettings::showOutdatedCatalogs,
                    onFavoriteToggle = { eventSink(Event.FavoriteToggle(entry, it)) },
                    onIgnoredToggle = { eventSink(Event.IgnoreToggle(entry, it)) },
                    onClick = { imageIndex ->
                        eventSink(
                            Event.OpenEntry(
                                entry,
                                imageIndex
                            )
                        )
                    },
                    onClickFullscreen = { imageIndex ->
                        eventSink(Event.OpenImageFullscreen(entry, imageIndex))
                    },
                    tagRow = {
                        SeriesRow(
                            series = entry.series.mapNotNull { series()[it] },
                            onSeriesClick = { eventSink(Event.OpenSeries(it)) },
                            onMoreClick = { eventSink(Event.OpenEntry(entry, 1)) },
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                )
            },
            noResultsItem = noResultsItem,
            moreResultsItem = {
                SearchMoreResults(
                    unfilteredCount = unfilteredCount,
                    itemCount = { entries.itemCount },
                    onClick = { eventSink(Event.ClearFilters) },
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun Header(state: State, eventSink: (Event) -> Unit) {
    val dataYearHeaderState = rememberDataYearHeaderState(
        dataYear = state.year,
        onDataYearChange = state.onYearChange,
        lockedYear = state.lockedYear,
    )
    Column {
        ConventionCountdownHeader(
            dataYearHeaderState.year,
            onOpenExport = { eventSink(Event.OpenExport) },
        )
        FeedbackHeader(dataYearHeaderState.year)
        DataYearHeader(
            state = dataYearHeaderState,
            onOpenChangelog = { eventSink(Event.OpenChangelog) },
            onOpenSettings = { eventSink(Event.OpenSettings) },
        )
    }
}

@Composable
private fun TagsCell(
    column: ArtistSearchColumn,
    tags: List<String>?,
    moreContentDescription: StringResource,
    onTagClick: (String) -> Unit,
    onMoreClick: () -> Unit,
) {
    if (tags.isNullOrEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        tags.take(TagUtils.TAGS_TO_SHOW)
            .forEach {
                SuggestionChip(
                    onClick = { onTagClick(it) },
                    label = { Text(text = it, modifier = Modifier.padding(vertical = 4.dp)) },
                    modifier = Modifier.widthIn(max = column.size - 16.dp)
                )
            }
        if (tags.size > TagUtils.TAGS_TO_SHOW) {
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

object ArtistSearchScreen {

    @Composable
    fun ColumnHeader(
        column: ArtistSearchColumn,
        sortOption: () -> ArtistSearchSortOption,
        sortAscending: () -> Boolean,
    ) {
        val columnSortOption = when (column) {
            ArtistSearchColumn.BOOTH -> ArtistSearchSortOption.BOOTH
            ArtistSearchColumn.NAME -> ArtistSearchSortOption.ARTIST
            else -> null
        }
        var sortOption = sortOption()
        var sortAscending = sortAscending()
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
                .then(TwoWayGrid.DefaultCellPaddingModifier)
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
        row: ArtistEntryGridModel?,
        column: ArtistSearchColumn,
        series: () -> Map<String, GetSeriesTitles>,
        onEntryClick: (ArtistEntryGridModel, imageIndex: Int) -> Unit,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
    ) {
        val clickableCellModifier = Modifier.fillMaxSize()
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .conditionallyNonNull(row) { clickable { onEntryClick(it, 1) } }
            .then(TwoWayGrid.DefaultCellPaddingModifier)
        when (column) {
            // TODO: Dynamic minimum column size by measuring header text
            ArtistSearchColumn.BOOTH -> Box(clickableCellModifier) {
                AutoSizeText(text = row?.booth.orEmpty())
            }
            ArtistSearchColumn.NAME -> Box(clickableCellModifier) {
                Text(text = row?.artist?.name.orEmpty())
            }
            ArtistSearchColumn.SUMMARY -> Box(clickableCellModifier) {
                Text(text = row?.artist?.summary.orEmpty())
            }
            ArtistSearchColumn.SERIES -> {
                val shuffledSeries = row?.series
                val languageOption = LocalLanguageOptionMedia.current
                val series = series()
                val seriesNames = remember(shuffledSeries, series, languageOption) {
                    shuffledSeries?.map { series[it]?.name(languageOption) ?: it }
                }
                TagsCell(
                    column = column,
                    tags = seriesNames,
                    moreContentDescription = Res.string.alley_expand_series,
                    onTagClick = onSeriesClick,
                    onMoreClick = { if (row != null) onEntryClick(row, 1) },
                )
            }
            ArtistSearchColumn.MERCH -> TagsCell(
                column = column,
                tags = row?.merch,
                moreContentDescription = Res.string.alley_expand_merch,
                onTagClick = onMerchClick,
                onMoreClick = { if (row != null) onEntryClick(row, 1) },
            )
            ArtistSearchColumn.SOCIAL_LINKS -> row?.artist?.socialLinkModels?.let { socialLinkModels ->
                val uriHandler = LocalUriHandler.current
                Grid(socialLinkModels.size) {
                    val linkModel = socialLinkModels[it]
                    TooltipIconButton(
                        icon = linkModel.logo?.icon ?: Icons.Default.Link,
                        tooltipText = linkModel.link,
                        positioning = TooltipAnchorPosition.Above,
                        onClick = { uriHandler.openUri(linkModel.link) },
                    )
                }
            }
            ArtistSearchColumn.STORE_LINKS -> row?.artist?.storeLinkModels?.let { storeLinkModels ->
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
            ArtistSearchColumn.COMMISSIONS -> row?.artist?.commissionModels?.let {
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
                            is CommissionModel.Unknown,
                                -> TooltipBox(
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

    @Stable
    class State(
        val lockedSeriesEntry: () -> SeriesInfo?,
        val lockedMerch: String?,
        val lockedYear: DataYear?,
        val randomSeed: Int,
        val year: () -> DataYear,
        val onYearChange: (DataYear) -> Unit,
        val query: TextFieldState,
        val results: Flow<PagingData<ArtistEntryGridModel>>,
        val unfilteredCount: () -> Int,
        val sortFilterState: ArtistSortFilterState,
        val displayType: () -> SearchDisplayType,
        val series: () -> Map<String, GetSeriesTitles>,
    )

    sealed interface Event {
        data class FavoriteToggle(val entry: ArtistEntryGridModel, val favorite: Boolean) : Event
        data class IgnoreToggle(val entry: ArtistEntryGridModel, val ignored: Boolean) : Event
        data class OpenEntry(val entry: ArtistEntryGridModel, val imageIndex: Int) : Event
        data class OpenImageFullscreen(val entry: ArtistEntryGridModel, val imageIndex: Int) : Event
        data class OpenMerch(val merch: String) : Event
        data class OpenSeries(val series: String) : Event
        data object Back : Event
        data object ClearFilters : Event
        data object OpenChangelog : Event
        data object OpenExport : Event
        data object OpenSettings : Event
    }
}

@AlleyPreview
@Composable
private fun Preview() {
    val results = ArtistWithUserDataProvider.values.take(5)
        .toList()
        .map {
            ArtistEntryGridModel.buildFromEntry(
                randomSeed = 1,
                showOnlyConfirmedTags = false,
                entry = it,
            )
        }
    val sortFilterState = ArtistSortFilterState.rememberForPreview()
    val state = remember(results) {
        State(
            lockedSeriesEntry = { null },
            lockedMerch = null,
            lockedYear = null,
            randomSeed = 1,
            year = { DataYear.ANIME_EXPO_2025 },
            onYearChange = {},
            query = TextFieldState(""),
            results = MutableStateFlow(PagingData.from(results)),
            unfilteredCount = { 1000 },
            sortFilterState = sortFilterState,
            displayType = { SearchDisplayType.CARD },
            series = { emptyMap() },
        )
    }

    val dataYearHeaderState = rememberDataYearHeaderState(
        dataYear = state.year,
        onDataYearChange = state.onYearChange,
        lockedYear = state.lockedYear,
    )
    ArtistSearchScreen(
        state = state,
        eventSink = {},
        header = {
            DataYearHeader(
                state = dataYearHeaderState,
                onOpenChangelog = {},
                onOpenSettings = {},
            )
        },
    )
}
