package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_commissions
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_links
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_name
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_store
import artistalleydatabase.modules.alley.generated.resources.alley_artist_column_summary
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online_tooltip
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
        onEntryClick: (ArtistEntryGridModel, Int) -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
        onSeriesClick: (String) -> Unit,
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
                tableCell = { row, column ->
                    when (column) {
                        // TODO: Dynamic minimum column size by measuring header text
                        ArtistColumn.BOOTH -> AutoSizeText(
                            text = row?.booth.orEmpty(),
                            modifier = Modifier.requiredSize(column.size)
                                .then(TwoWayGrid.modifierDefaultCellPadding)
                        )
                        ArtistColumn.NAME -> Text(
                            text = row?.artist?.name.orEmpty(),
                            modifier = TwoWayGrid.modifierDefaultCellPadding
                        )
                        ArtistColumn.SUMMARY -> Text(
                            text = row?.artist?.summary.orEmpty(),
                            modifier = TwoWayGrid.modifierDefaultCellPadding
                        )
                        ArtistColumn.LINKS -> FlowRow {
                            val uriHandler = LocalUriHandler.current
                            row?.artist?.linkModels?.forEach {
                                IconWithTooltip(
                                    imageVector = it.logo?.icon ?: Icons.Default.Link,
                                    tooltipText = it.link,
                                    onClick = { uriHandler.openUri(it.link) },
                                )
                            }
                        }
                        ArtistColumn.STORE -> FlowRow {
                            val uriHandler = LocalUriHandler.current
                            row?.artist?.storeLinkModels?.forEach {
                                IconWithTooltip(
                                    imageVector = it.logo?.icon ?: Icons.Default.Link,
                                    tooltipText = it.link,
                                    onClick = { uriHandler.openUri(it.link) },
                                )
                            }
                        }
                        ArtistColumn.COMMISSIONS -> FlowRow {
                            val uriHandler = LocalUriHandler.current
                            row?.artist?.commissionModels?.forEach {
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
                                    CommissionModel.Online ->Tooltip(
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
                },
            )
        }
    }

    @Composable
    private fun CommissionChip(model: CommissionModel, label: @Composable () -> Unit) {
        Box(Modifier.width(ArtistColumn.COMMISSIONS.size).padding(horizontal = 4.dp)) {
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
        BOOTH(64.dp, Res.string.alley_artist_column_booth),
        NAME(160.dp, Res.string.alley_artist_column_name),
        SUMMARY(400.dp, Res.string.alley_artist_column_summary),
        LINKS(200.dp, Res.string.alley_artist_column_links),
        STORE(200.dp, Res.string.alley_artist_column_store),
        COMMISSIONS(200.dp, Res.string.alley_artist_column_commissions),
    }
}
