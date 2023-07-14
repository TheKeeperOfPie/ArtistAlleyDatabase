package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.StaffAndCharactersQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

object StaffCharactersScreen {

    private val SCREEN_KEY = AnimeNavDestinations.STAFF_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: StaffCharactersViewModel,
        headerValues: StaffHeaderValues,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)

        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_staff_characters_header,
            header = {
                StaffHeader(
                    staffId = viewModel.headerId,
                    progress = it,
                    headerValues = headerValues,
                    colorCalculationState = colorCalculationState,
                )
            },
            itemKey = { it.character.id },
            item = {
                CharacterListRow(
                    screenKey = SCREEN_KEY,
                    entry = it,
                    showRole = true,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        )
    }

    data class Entry(
        val staff: StaffAndCharactersQuery.Data.Staff,
    )
}
