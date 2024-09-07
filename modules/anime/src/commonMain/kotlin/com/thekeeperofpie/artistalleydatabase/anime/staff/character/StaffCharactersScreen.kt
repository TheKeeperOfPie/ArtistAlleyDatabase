package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_characters_header
import com.anilist.StaffAndCharactersQuery
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeader
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffHeaderValues
import com.thekeeperofpie.artistalleydatabase.anime.utils.HeaderAndListScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey

@OptIn(ExperimentalMaterial3Api::class)
object StaffCharactersScreen {

    @Composable
    operator fun invoke(
        viewModel: StaffCharactersViewModel,
        upIconOption: UpIconOption?,
        headerValues: StaffHeaderValues,
        sharedTransitionKey: SharedTransitionKey?,
    ) {
        val animeComponent = LocalAnimeComponent.current
        val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            viewModel = editViewModel,
        ) {
            val viewer by viewModel.viewer.collectAsState()
            HeaderAndListScreen(
                viewModel = viewModel,
                headerTextRes = Res.string.anime_staff_characters_header,
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
                        viewer = viewer,
                        entry = it,
                        onClickListEdit = editViewModel::initialize,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        showRole = true
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
