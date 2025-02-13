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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_merch
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_series
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_summary
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_expand_merch
import artistalleydatabase.modules.alley.generated.resources.alley_expand_series
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistListRow
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.Tooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object ArtistSearchScreen {

    @Composable
    operator fun invoke(
        viewModel: ArtistSearchViewModel,
        sortViewModel: ArtistSortFilterViewModel,
        onClickBack: (() -> Unit)?,
        onEntryClick: (ArtistEntryGridModel, imageIndex: Int) -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
        onSeriesClick: (String) -> Unit,
        onMerchClick: (String) -> Unit,
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        val sortFilterState = sortViewModel.state
        sortFilterState.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val showGridByDefault by sortViewModel.settings.showGridByDefault.collectAsStateWithLifecycle()
            val showRandomCatalogImage by sortViewModel.settings.showRandomCatalogImage.collectAsStateWithLifecycle()
            val forceOneDisplayColumn by sortViewModel.settings.forceOneDisplayColumn.collectAsStateWithLifecycle()
            val showOnlyFavorites by sortViewModel.settings.showOnlyFavorites.collectAsStateWithLifecycle()
            val showOnlyCatalogImages by sortViewModel.onlyCatalogImages.collectAsStateWithLifecycle()
            val displayType = SearchScreen.DisplayType.fromSerializedValue(
                viewModel.displayType.collectAsState().value
            )
            val entries = viewModel.results.collectAsLazyPagingItemsWithLifecycle()
            var sortOption by sortViewModel.sortOption.collectAsMutableStateWithLifecycle()
            var sortAscending by sortViewModel.sortAscending.collectAsMutableStateWithLifecycle()
            SearchScreen<ArtistSearchQuery, ArtistEntryGridModel, ArtistColumn>(
                viewModel = viewModel,
                title = { viewModel.lockedSeries ?: viewModel.lockedMerch },
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
                showGridByDefault = { showGridByDefault },
                showRandomCatalogImage = { showRandomCatalogImage },
                forceOneDisplayColumn = { forceOneDisplayColumn },
                displayType = { displayType },
                onDisplayTypeToggle = viewModel::onDisplayTypeToggle,
                gridState = gridState,
                onFavoriteToggle = viewModel::onFavoriteToggle,
                onIgnoredToggle = viewModel::onIgnoredToggle,
                onEntryClick = onEntryClick,
                shouldShowCount = {
                    viewModel.query.isNotEmpty()
                            || showOnlyFavorites
                            || showOnlyCatalogImages
                            || viewModel.lockedSeries != null
                            || viewModel.lockedMerch != null
                },
                itemToSharedElementId = { it.artist.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    ArtistListRow(
                        entry = entry,
                        onFavoriteToggle = onFavoriteToggle,
                        onSeriesClick = onSeriesClick,
                        modifier = modifier
                    )
                },
                columns = ArtistColumn.entries,
                columnHeader = { column ->
                    val columnSortOption = when (column) {
                        ArtistColumn.BOOTH -> ArtistSearchSortOption.BOOTH
                        ArtistColumn.NAME -> ArtistSearchSortOption.ARTIST
                        else -> null
                    }
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
                },
                tableCell = { row, column ->
                    TableCell(
                        row = row,
                        column = column,
                        onEntryClick = onEntryClick,
                        onSeriesClick = onSeriesClick,
                        onMerchClick = onMerchClick,
                    )
                },
            )
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
            ArtistColumn.SERIES -> TagsFlowRow(
                column = column,
                tags = row?.series,
                contentDescription = Res.string.alley_expand_series,
                onEntryClick = { if (row != null) onEntryClick(row, 1) },
                onTagClick = onSeriesClick,
            )
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
                        IconWithTooltip(
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
                        IconWithTooltip(
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
                            is CommissionModel.Link -> IconWithTooltip(
                                imageVector = it.icon,
                                tooltipText = it.link,
                                onClick = { uriHandler.openUri(it.link) },
                            )
                            CommissionModel.OnSite -> Tooltip(
                                text = stringResource(Res.string.alley_artist_commission_on_site_tooltip)
                            ) {
                                CommissionChip(
                                    model = it,
                                    label = {
                                        Text(stringResource(Res.string.alley_artist_commission_on_site))
                                    },
                                )
                            }
                            CommissionModel.Online -> Tooltip(
                                text = stringResource(Res.string.alley_artist_commission_online_tooltip)
                            ) {
                                CommissionChip(
                                    model = it,
                                    label = {
                                        Text(stringResource(Res.string.alley_artist_commission_online))
                                    },
                                )
                            }
                            is CommissionModel.Unknown ->
                                CommissionChip(model = it, label = { Text(it.host) })
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
}
