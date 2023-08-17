package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.StaffAndCharactersQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption

object StaffCharactersScreen {

    private val SCREEN_KEY = AnimeNavDestinations.STAFF_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: StaffCharactersViewModel,
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
    ) {
        HeaderAndListScreen(
            viewModel = viewModel,
            headerTextRes = R.string.anime_staff_characters_header,
            header = {
                StaffHeader(
                    staffId = viewModel.headerId,
                    upIconOption = upIconOption,
                    progress = it,
                    headerValues = headerValues,
                    onFavoriteChanged = {
                        viewModel.favoritesToggleHelper
                            .set(FavoriteType.STAFF, viewModel.headerId, it)
                    },
                )
            },
            itemKey = { it.character.id },
            item = {
                CharacterListRow(
                    screenKey = SCREEN_KEY,
                    entry = it,
                    showRole = true,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        )
    }

    data class Entry(
        val staff: StaffAndCharactersQuery.Data.Staff,
    )
}
