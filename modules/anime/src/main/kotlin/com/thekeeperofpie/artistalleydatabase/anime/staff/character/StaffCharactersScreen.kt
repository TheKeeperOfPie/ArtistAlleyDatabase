package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.anilist.StaffAndCharactersQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.SharedTransitionKey

@OptIn(ExperimentalMaterial3Api::class)
object StaffCharactersScreen {

    private val SCREEN_KEY = AnimeNavDestinations.STAFF_CHARACTERS.id

    @Composable
    operator fun invoke(
        viewModel: StaffCharactersViewModel,
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
    ) {
        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
        ) {
            val viewer by viewModel.viewer.collectAsState()
            HeaderAndListScreen(
                viewModel = viewModel,
                headerTextRes = R.string.anime_staff_characters_header,
                header = {
                    StaffHeader(
                        staffId = viewModel.staffId,
                        upIconOption = upIconOption,
                        progress = it,
                        headerValues = headerValues,
                        sharedTransitionKey = sharedTransitionKey,
                        onFavoriteChanged = {
                            viewModel.favoritesToggleHelper
                                .set(FavoriteType.STAFF, viewModel.staffId, it)
                        },
                    )
                },
                itemKey = { it.character.id },
                item = {
                    CharacterListRow(
                        screenKey = SCREEN_KEY,
                        viewer = viewer,
                        entry = it,
                        showRole = true,
                        onClickListEdit = editViewModel::initialize,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                },
                modifier = Modifier.padding(it)
            )
        }
    }

    data class Entry(
        val staff: StaffAndCharactersQuery.Data.Staff,
    )
}
