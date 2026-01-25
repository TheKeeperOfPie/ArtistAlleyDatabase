package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_booth
import artistalleydatabase.modules.alley.generated.resources.alley_stamp_rally_column_fandom
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.TwoWayGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.AutoSizeText
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object StampRallySearchScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        lockedYear: DataYear?,
        lockedSeries: String?,
        scrollStateSaver: ScrollStateSaver,
        onClickBack: (() -> Unit)? = null,
        viewModel: StampRallySearchViewModel = viewModel {
            graph.stampRallySearchViewModelFactory.create(
                lockedYear = lockedYear,
                lockedSeries = lockedSeries,
                savedStateHandle = createSavedStateHandle(),
            )
        }
    ) {
        val gridState = scrollStateSaver.lazyStaggeredGridState()
        viewModel.sortFilterController.state.ImmediateScrollResetEffect(gridState)

        CompositionLocalProvider(LocalStableRandomSeed provides viewModel.randomSeed) {
            val dataYearHeaderState = rememberDataYearHeaderState(viewModel.dataYear, lockedYear)
            val entries = viewModel.results.collectAsLazyPagingItems()
            val navigationController = LocalNavigationController.current
            val lockedSeriesEntry by viewModel.lockedSeriesEntry.collectAsStateWithLifecycle()
            val languageOptionMedia = LocalLanguageOptionMedia.current
            val unfilteredCount by viewModel.unfilteredCount.collectAsStateWithLifecycle()
            val scaffoldState = rememberBottomSheetScaffoldState()
            SearchScreen(
                state = viewModel.searchState,
                eventSink = {
                    viewModel.onEvent(navigationController, Event.SearchEvent(it))
                },
                query = viewModel.query,
                entries = entries,
                unfilteredCount = { unfilteredCount },
                scaffoldState = scaffoldState,
                sortFilterState = viewModel.sortFilterController.state,
                gridState = gridState,
                header = { BottomSheetFilterDataYearHeader(dataYearHeaderState, scaffoldState) },
                title = { lockedSeriesEntry?.name(languageOptionMedia) },
                onClickBack = onClickBack,
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
