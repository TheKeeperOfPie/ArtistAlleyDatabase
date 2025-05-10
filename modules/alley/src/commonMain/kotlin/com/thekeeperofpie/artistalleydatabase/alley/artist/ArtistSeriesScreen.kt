package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver

@OptIn(ExperimentalMaterial3Api::class)
object ArtistSeriesScreen {

    @Composable
    operator fun invoke(
        artistSearchViewModel: ArtistSearchViewModel,
        artistSeriesViewModel: ArtistSeriesViewModel,
        sortFilterController: ArtistSortFilterController,
        onClickBack: (() -> Unit)?,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
        scrollStateSaver: ScrollStateSaver,
        onClickMap: (() -> Unit)? = null,
    ) {
        val state = remember(artistSearchViewModel, sortFilterController) {
            ArtistSearchScreen.State(artistSearchViewModel, sortFilterController)
        }
        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        val navigationController = LocalNavigationController.current
        val seriesEntry by artistSeriesViewModel.seriesEntry.collectAsStateWithLifecycle()
        val seriesImage by artistSeriesViewModel.seriesImage.collectAsStateWithLifecycle()
        ArtistSearchScreen(
            state = state,
            sortFilterState = sortFilterController.state,
            eventSink = { artistSearchViewModel.onEvent(navigationController, it) },
            onClickBack = onClickBack,
            header = {
                Header(
                    state,
                    showSeries = artistSeriesViewModel.route.series != null,
                    seriesEntry = { seriesEntry },
                    seriesImage = { seriesImage },
                )
            },
            scaffoldState = scaffoldState,
            scrollStateSaver = scrollStateSaver,
            onClickMap = onClickMap,
        )
    }

    @Composable
    private fun Header(
        state: ArtistSearchScreen.State,
        showSeries: Boolean,
        seriesEntry: () -> SeriesEntry?,
        seriesImage: () -> String?,
    ) {
        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        Column {
            if (showSeries) {
                Card {
                    SeriesRow(
                        series = seriesEntry(),
                        image = seriesImage,
                        textStyle = LocalTextStyle.current,
                        showAllTitles = true,
                    )
                }
            }
            DataYearHeader(dataYearHeaderState)
        }
    }
}
