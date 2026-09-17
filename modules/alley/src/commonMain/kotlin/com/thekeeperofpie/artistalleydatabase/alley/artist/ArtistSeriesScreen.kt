package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_rallies
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ShowOnlyConfirmedTagsSection
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.ui.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Approval
import com.thekeeperofpie.artistalleydatabase.icons.filled.Map
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
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
        onOpenArtistImageFullscreen: (
            artist: ArtistEntryGridModel,
            imageIndex: Int?,
            showOutdatedCatalogs: Boolean,
        ) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        artistSearchViewModel: ArtistSearchViewModel = assistedMetroViewModel<ArtistSearchViewModel, ArtistSearchViewModel.Factory> {
            create(
                lockedYear = route.year,
                lockedSeries = route.series,
                savedStateHandle = it.createSavedStateHandle(),
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
            onClickBack = onClickBack,
            scrollStateSaver = scrollStateSaver,
            showRalliesButton = { hasRallies },
            onClickRallies = { onClickRallies(artistSearchViewModel.year.value) },
            onClickMap = { onClickMap(artistSearchViewModel.lockedYear) },
            onOpenArtist = onOpenArtist,
            onOpenArtistImageFullscreen = onOpenArtistImageFullscreen,
            onOpenMerch = onOpenMerch,
            onOpenSeries = onOpenSeries,
            onOpenChangelog = onOpenChangelog,
            onOpenSettings = onOpenSettings,
            scaffoldState = scaffoldState,
        )

    }

    @Composable
    operator fun invoke(
        artistSearchViewModel: ArtistSearchViewModel,
        artistSeriesViewModel: ArtistSeriesViewModel,
        onClickBack: (() -> Unit)?,
        scrollStateSaver: ScrollStateSaver,
        showRalliesButton: () -> Boolean,
        onClickRallies: () -> Unit,
        onClickMap: () -> Unit,
        onOpenArtist: (artist: ArtistEntry, imageIndex: Int?) -> Unit,
        onOpenArtistImageFullscreen: (
            artist: ArtistEntryGridModel,
            imageIndex: Int?,
            showOutdatedCatalogs: Boolean,
        ) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    ) {
        val sortFilterController = artistSearchViewModel.sortFilterController
        val state = remember(artistSearchViewModel, sortFilterController) {
            ArtistSearchScreen.State(artistSearchViewModel, sortFilterController)
        }
        val seriesEntry by artistSeriesViewModel.seriesEntry.collectAsStateWithLifecycle()
        val seriesImage by artistSeriesViewModel.seriesImage.collectAsStateWithLifecycle()
        val series by artistSearchViewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        val showOutdatedCatalogs by sortFilterController.state.persistentState.showOutdatedCatalogs.collectAsStateWithLifecycle()
        val seriesAutocompleteResults by artistSearchViewModel.seriesAutocompleteResults.collectAsStateWithLifecycle()
        ArtistSearchScreen(
            state = state,
            series = { series },
            showOutdatedCatalogs = { showOutdatedCatalogs },
            eventSink = artistSearchViewModel::onEvent,
            header = {
                Header(
                    state = state,
                    scaffoldState = scaffoldState,
                    seriesEntry = { seriesEntry },
                    seriesImage = { seriesImage },
                    onFavoriteToggle = artistSeriesViewModel::onFavoriteToggle,
                    onOpenChangelog = onOpenChangelog,
                    onOpenSettings = onOpenSettings,
                )
            },
            scaffoldState = scaffoldState,
            scrollStateSaver = scrollStateSaver,
            seriesImage = artistSearchViewModel::seriesImage,
            seriesAutocompleteResults = { seriesAutocompleteResults },
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
        scaffoldState: BottomSheetScaffoldState,
        seriesEntry: () -> SeriesWithUserData?,
        seriesImage: () -> String?,
        onFavoriteToggle: (SeriesWithUserData, Boolean) -> Unit,
        onOpenChangelog: (DataYear) -> Unit,
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
                    showNotes = true,
                    onFavoriteToggle = {
                        if (data != null) {
                            onFavoriteToggle(data, it)
                        }
                    },
                )
                HorizontalDivider()

                var showOnlyConfirmedTags by state.sortFilterState.persistentState.showOnlyConfirmedTags.collectAsMutableStateWithLifecycle()
                ShowOnlyConfirmedTagsSection(
                    enabled = { showOnlyConfirmedTags },
                    onEnabledChanged = { showOnlyConfirmedTags = it },
                )
            }
            BottomSheetFilterDataYearHeader(
                dataYearHeaderState = dataYearHeaderState,
                scaffoldState = scaffoldState,
                onOpenChangelog = onOpenChangelog,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}
