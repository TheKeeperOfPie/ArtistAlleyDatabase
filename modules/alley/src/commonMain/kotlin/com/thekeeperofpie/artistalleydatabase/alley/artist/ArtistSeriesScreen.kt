package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_rallies
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen.Event
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ArtistSeriesScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        route: AlleyDestination.Series,
        onClickBack: (() -> Unit)?,
        scrollStateSaver: ScrollStateSaver,
        onClickRallies: (DataYear) -> Unit,
        onClickMap: (DataYear?) -> Unit,
        onOpenArtist: (artist: ArtistEntry, imageIndex: Int?) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenExport: () -> Unit,
        onOpenChangelog: () -> Unit,
        onOpenSettings: () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        artistSearchViewModel: ArtistSearchViewModel = viewModel {
            graph.artistSearchViewModelFactory.create(
                lockedYear = route.year,
                lockedSeries = route.series,
                lockedMerch = null,
                isRoot = false,
                lockedSerializedBooths = null,
                savedStateHandle = createSavedStateHandle(),
            )
        },
        artistSeriesViewModel: ArtistSeriesViewModel = viewModel {
            graph.artistSeriesViewModelFactory.create(
                series = route.series,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        val hasRallies by artistSearchViewModel.hasRallies.collectAsStateWithLifecycle()
        ArtistSeriesScreen(
            artistSearchViewModel = artistSearchViewModel,
            artistSeriesViewModel = artistSeriesViewModel,
            sortFilterController = artistSearchViewModel.sortFilterController,
            onClickBack = onClickBack,
            scrollStateSaver = scrollStateSaver,
            showRalliesButton = { hasRallies },
            onClickRallies = { onClickRallies(artistSearchViewModel.year.value) },
            onClickMap = { onClickMap(artistSearchViewModel.lockedYear) },
            onOpenArtist = onOpenArtist,
            onOpenMerch = onOpenMerch,
            onOpenSeries = onOpenSeries,
            onOpenExport = onOpenExport,
            onOpenChangelog = onOpenChangelog,
            onOpenSettings = onOpenSettings,
            scaffoldState = scaffoldState,
        )

    }

    @Composable
    operator fun invoke(
        artistSearchViewModel: ArtistSearchViewModel,
        artistSeriesViewModel: ArtistSeriesViewModel,
        sortFilterController: ArtistSortFilterController,
        onClickBack: (() -> Unit)?,
        scrollStateSaver: ScrollStateSaver,
        showRalliesButton: () -> Boolean,
        onClickRallies: () -> Unit,
        onClickMap: () -> Unit,
        onOpenArtist: (artist: ArtistEntry, imageIndex: Int?) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenExport: () -> Unit,
        onOpenChangelog: () -> Unit,
        onOpenSettings: () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    ) {
        val state = remember(artistSearchViewModel, sortFilterController) {
            ArtistSearchScreen.State(artistSearchViewModel, sortFilterController)
        }
        val seriesEntry by artistSeriesViewModel.seriesEntry.collectAsStateWithLifecycle()
        val seriesImage by artistSeriesViewModel.seriesImage.collectAsStateWithLifecycle()
        val series by artistSearchViewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        ArtistSearchScreen(
            state = state,
            series = { series },
            sortFilterState = sortFilterController.state,
            eventSink = {
                when (it) {
                    is Event.SearchEvent -> when (val searchEvent = it.event) {
                        is SearchScreen.Event.FavoriteToggle<ArtistEntryGridModel> ->
                            artistSearchViewModel.toggleFavorite(
                                searchEvent.entry,
                                searchEvent.favorite
                            )
                        is SearchScreen.Event.IgnoreToggle<ArtistEntryGridModel> ->
                            artistSearchViewModel.toggleIgnored(
                                searchEvent.entry,
                                searchEvent.ignored
                            )
                        is SearchScreen.Event.OpenEntry<ArtistEntryGridModel> ->
                            onOpenArtist(searchEvent.entry.artist, searchEvent.imageIndex)
                        is SearchScreen.Event.ClearFilters<*> -> sortFilterController.clear()
                    }
                    is Event.OpenMerch -> onOpenMerch(artistSearchViewModel.year.value, it.merch)
                    is Event.OpenSeries -> onOpenSeries(artistSearchViewModel.year.value, it.series)
                }
            },
            onClickBack = onClickBack,
            header = {
                Header(
                    state = state,
                    sortFilterController = sortFilterController,
                    scaffoldState = scaffoldState,
                    seriesEntry = { seriesEntry },
                    seriesImage = { seriesImage },
                    onFavoriteToggle = artistSeriesViewModel::onFavoriteToggle,
                    onOpenExport = onOpenExport,
                    onOpenChangelog = onOpenChangelog,
                    onOpenSettings = onOpenSettings,
                )
            },
            scaffoldState = scaffoldState,
            scrollStateSaver = scrollStateSaver,
            actions = {
                if (showRalliesButton()) {
                    IconButton(onClick = onClickRallies) {
                        Icon(
                            imageVector = Icons.Default.Approval,
                            contentDescription = stringResource(Res.string.alley_open_rallies),
                        )
                    }
                }
                IconButton(onClick = onClickMap) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(Res.string.alley_open_in_map),
                    )
                }
            },
        )
    }

    @Composable
    private fun Header(
        state: ArtistSearchScreen.State,
        sortFilterController: ArtistSortFilterController,
        scaffoldState: BottomSheetScaffoldState,
        seriesEntry: () -> SeriesWithUserData?,
        seriesImage: () -> String?,
        onFavoriteToggle: (SeriesWithUserData, Boolean) -> Unit,
        onOpenExport: () -> Unit,
        onOpenChangelog: () -> Unit,
        onOpenSettings: () -> Unit,
    ) {
        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        Column {
            Card {
                val data = seriesEntry()
                SeriesRow(
                    data = data,
                    image = seriesImage,
                    textStyle = LocalTextStyle.current,
                    showAllTitles = true,
                    onFavoriteToggle = {
                        if (data != null) {
                            onFavoriteToggle(data, it)
                        }
                    },
                )
                HorizontalDivider()
                sortFilterController.showOnlyConfirmedTagsSection
                    .Content(sortFilterController.state.expanded, false)
            }
            BottomSheetFilterDataYearHeader(
                dataYearHeaderState = dataYearHeaderState,
                scaffoldState = scaffoldState,
                onOpenExport = onOpenExport,
                onOpenChangelog = onOpenChangelog,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}
