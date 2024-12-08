package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffDetailsViewModel
import kotlinx.coroutines.flow.Flow

interface StaffComponent {
    val animeMediaDetailsStaffViewModel: (SavedStateHandle, Flow<MediaPreview?>) -> AnimeMediaDetailsStaffViewModel
    val staffCharactersViewModelFactory: (SavedStateHandle) -> StaffCharactersViewModel.Factory
    val staffDetailsViewModelFactory: (SavedStateHandle) -> StaffDetailsViewModel.Factory
}