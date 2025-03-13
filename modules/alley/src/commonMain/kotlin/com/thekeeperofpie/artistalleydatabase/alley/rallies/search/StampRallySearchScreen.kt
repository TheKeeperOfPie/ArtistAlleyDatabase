package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_fandom
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterOptionsPanel
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
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
        scrollStateSaver: ScrollStateSaver,
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        val sortFilterState = sortViewModel.state
        sortFilterState.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val dataYearHeaderState = rememberDataYearHeaderState(viewModel.dataYear, null)
            val entries = viewModel.results.collectAsLazyPagingItemsWithLifecycle()
            val query by viewModel.query.collectAsStateWithLifecycle()
            val navigationController = LocalNavigationController.current
            SearchScreen<StampRallyEntryGridModel, StampRallyColumn>(
                state = viewModel.searchState,
                eventSink = {
                    viewModel.onEvent(navigationController, Event.SearchEvent(it))
                },
                query = viewModel.query,
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
                dataYearHeaderState = dataYearHeaderState,
                gridState = gridState,
                shouldShowCount = { query.isNotEmpty() },
                itemToSharedElementId = { it.stampRally.id },
                itemRow = { entry, onFavoriteToggle, modifier ->
                    StampRallyListRow(entry, onFavoriteToggle, modifier)
                },
                columnHeader = { ColumnHeader(it) },
                tableCell = { row, column -> TableCell(row, column) },
            )
        }
    }

    @Composable
    fun ColumnHeader(column: StampRallyColumn) {
        // TODO: Support sort
        AutoSizeText(
            text = stringResource(column.text),
            modifier = Modifier.requiredWidth(column.size)
                .then(TwoWayGrid.modifierDefaultCellPadding)
        )
    }

    @Composable
    fun TableCell(row: StampRallyEntryGridModel?, column: StampRallyColumn) {
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
    }

    enum class StampRallyColumn(
        override val size: Dp,
        override val text: StringResource,
    ) : TwoWayGrid.Column {
        BOOTH(64.dp, Res.string.alley_stamp_rally_column_booth),
        FANDOM(160.dp, Res.string.alley_stamp_rally_column_fandom),
    }

    sealed interface Event {
        data class SearchEvent(val event: SearchScreen.Event<StampRallyEntryGridModel>) : Event
    }
}
