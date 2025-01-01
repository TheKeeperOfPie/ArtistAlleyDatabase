package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffDetailsViewModel
import kotlinx.coroutines.flow.Flow

interface StaffComponent {
    val animeMediaDetailsStaffViewModel: (Flow<MediaPreview?>) -> AnimeMediaDetailsStaffViewModel
    val staffCharactersSortFilterViewModel: (SavedStateHandle) -> StaffCharactersSortFilterViewModel
    val staffCharactersViewModelFactory: (SavedStateHandle, StaffCharactersSortFilterViewModel) -> StaffCharactersViewModel.Factory
    val staffDetailsViewModelFactory: (SavedStateHandle) -> StaffDetailsViewModel.Factory
}
