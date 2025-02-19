package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_fandom
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_prize_limit
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_free
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_total_paid
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.prizeLimitText
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object StampRallySearchScreen {

    @Composable
    operator fun invoke(
        viewModel: StampRallySearchViewModel,
        sortViewModel: StampRallySortFilterViewModel,
        onEntryClick: (StampRallyEntryGridModel, Int) -> Unit,
        scrollStateSaver: ScrollStateSaver,
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        val sortFilterState = sortViewModel.state
        sortFilterState.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val showGridByDefault by sortViewModel.settings.showGridByDefault.collectAsStateWithLifecycle()
            val showRandomCatalogImage by sortViewModel.settings.showRandomCatalogImage.collectAsStateWithLifecycle()
            val forceOneDisplayColumn by sortViewModel.settings.forceOneDisplayColumn.collectAsStateWithLifecycle()
            val showOnlyFavorites by sortViewModel.settings.showOnlyFavorites.collectAsStateWithLifecycle()
            val displayType = SearchScreen.DisplayType.fromSerializedValue(
                viewModel.displayType.collectAsState().value
            )
            val dataYearHeaderState = rememberDataYearHeaderState(viewModel.dataYear, null)
            val entries = viewModel.results.collectAsLazyPagingItemsWithLifecycle()
            SearchScreen<StampRallySearchQuery, StampRallyEntryGridModel, StampRallyColumn>(
                viewModel = viewModel,
                entries = entries,
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
                dataYearHeaderState = dataYearHeaderState,
                displayType = { displayType },
                onDisplayTypeToggle = viewModel::onDisplayTypeToggle,
                gridState = gridState,
                onFavoriteToggle = viewModel::onFavoriteToggle,
                onIgnoredToggle = viewModel::onIgnoredToggle,
                onEntryClick = onEntryClick,
                shouldShowCount = { viewModel.query.isNotEmpty() || showOnlyFavorites },
                itemToSharedElementId = { it.stampRally.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    StampRallyListRow(entry, onFavoriteToggle, modifier)
                },
                columns = StampRallyColumn.entries,
                tableCell = { row, column ->
                    when (column) {
                        StampRallyColumn.BOOTH -> AutoSizeText(
                            text = row?.booth.orEmpty(),
                            modifier = Modifier.requiredSize(column.size)
                                .then(TwoWayGrid.modifierDefaultCellPadding)
                        )
                        StampRallyColumn.FANDOM -> Text(
                            text = row?.stampRally?.fandom.orEmpty(),
                            modifier = TwoWayGrid.modifierDefaultCellPadding
                        )
                    }
                },
            )
        }
    }

    @Composable
    private fun StampRallyListRow(
        entry: StampRallyEntryGridModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val stampRally = entry.stampRally
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .sharedBounds("container", stampRally.id, zIndexInOverlay = 1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = stampRally.hostTable,
                style = MaterialTheme.typography.titleLarge
                    .copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .sharedElement("hostTable", stampRally.id, zIndexInOverlay = 1f)
                    .padding(vertical = 8.dp)
            )

            Text(
                text = stampRally.fandom,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("fandom", stampRally.id, zIndexInOverlay = 1f)
                    .weight(1f)
                    .padding(vertical = 8.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                if (stampRally.prizeLimit != null) {
                    Text(
                        text = stringResource(
                            Res.string.alley_stamp_rally_prize_limit,
                            stampRally.prizeLimitText(),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                val totalCost = stampRally.totalCost
                if (totalCost != null) {
                    Text(
                        text = if (totalCost == 0L) {
                            stringResource(Res.string.alley_stamp_rally_total_free)
                        } else {
                            stringResource(
                                Res.string.alley_stamp_rally_total_paid,
                                totalCost,
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            val favorite = entry.favorite
            IconButton(
                onClick = { onFavoriteToggle(!favorite) },
                modifier = Modifier
                    .sharedElement("favorite", stampRally.id, zIndexInOverlay = 1f)
                    .align(Alignment.Top)
            ) {
                Icon(
                    imageVector = if (favorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Filled.FavoriteBorder
                    },
                    contentDescription = stringResource(
                        Res.string.alley_stamp_rally_favorite_icon_content_description
                    ),
                )
            }
        }
    }

    enum class StampRallyColumn(
        override val size: Dp,
        override val text: StringResource,
    ) : TwoWayGrid.Column {
        BOOTH(64.dp, Res.string.alley_stamp_rally_column_booth),
        FANDOM(160.dp, Res.string.alley_stamp_rally_column_fandom),
    }
}
