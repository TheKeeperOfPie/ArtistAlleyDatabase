package com.thekeeperofpie.artistalleydatabase.anime.staff

import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.character.StaffCharactersViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.details.StaffDetailsViewModel

interface StaffComponent {
    val animeMediaDetailsStaffViewModelFactory: AnimeMediaDetailsStaffViewModel.Factory
    val staffCharactersSortFilterViewModelFactory: StaffCharactersSortFilterViewModel.Factory
    val staffCharactersViewModelFactoryFactory: StaffCharactersViewModel.TypedFactory.Factory
    val staffDetailsViewModelFactoryFactory: StaffDetailsViewModel.TypedFactory.Factory
    val staffSortFilterViewModelFactory: StaffSortFilterViewModel.Factory
}
