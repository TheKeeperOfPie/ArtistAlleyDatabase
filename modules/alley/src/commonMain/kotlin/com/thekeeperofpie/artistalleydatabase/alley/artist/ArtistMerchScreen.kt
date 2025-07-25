package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_open_in_map
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterController
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.search.BottomSheetFilterDataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.tags.MerchRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ArtistMerchScreen {

    @Composable
    operator fun invoke(
        artistSearchViewModel: ArtistSearchViewModel,
        artistMerchViewModel: ArtistMerchViewModel,
        sortFilterController: ArtistSortFilterController,
        onClickBack: (() -> Unit)?,
        scrollStateSaver: ScrollStateSaver,
        onClickMap: () -> Unit,
        scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(),
    ) {
        val state = remember(artistSearchViewModel, sortFilterController) {
            ArtistSearchScreen.State(artistSearchViewModel, sortFilterController)
        }
        val navigationController = LocalNavigationController.current
        val merchEntry by artistMerchViewModel.merchEntry.collectAsStateWithLifecycle()
        ArtistSearchScreen(
            state = state,
            sortFilterState = sortFilterController.state,
            eventSink = { artistSearchViewModel.onEvent(navigationController, it) },
            onClickBack = onClickBack,
            header = {
                Header(
                    state = state,
                    sortFilterController = sortFilterController,
                    scaffoldState = scaffoldState,
                    showMerch = artistMerchViewModel.route.merch != null,
                    merchEntry = { merchEntry },
                    onFavoriteToggle = artistMerchViewModel::onFavoriteToggle,
                )
            },
            scaffoldState = scaffoldState,
            scrollStateSaver = scrollStateSaver,
            actions = {
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
        showMerch: Boolean,
        merchEntry: () -> MerchWithUserData?,
        onFavoriteToggle: (MerchWithUserData, Boolean) -> Unit,
    ) {
        val dataYearHeaderState = rememberDataYearHeaderState(state.year, state.lockedYear)
        Column {
            if (showMerch) {
                Card {
                    val data = merchEntry()
                    MerchRow(
                        data = data,
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
            }
            BottomSheetFilterDataYearHeader(dataYearHeaderState, scaffoldState)
        }
    }
}
