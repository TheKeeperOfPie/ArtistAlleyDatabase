package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import artistalleydatabase.modules.alley.generated.resources.alley_open_rallies
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ArtistSeriesScreen {

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
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    ) {
        val state = remember(artistSearchViewModel, sortFilterController) {
            ArtistSearchScreen.State(artistSearchViewModel, sortFilterController)
        }
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
                    state = state,
                    showSeries = artistSeriesViewModel.route.series != null,
                    seriesEntry = { seriesEntry },
                    seriesImage = { seriesImage },
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
