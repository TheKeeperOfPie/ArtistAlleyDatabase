package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.filter.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudiosSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.users.data.filter.UsersSortFilterParams
import kotlinx.coroutines.flow.StateFlow

interface SearchComponent {
    val animeSearchViewModelFactory: (
        SavedStateHandle,
        unlocked: StateFlow<Boolean>,
        animeSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
        mangaSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
        StateFlow<CharacterSortFilterParams>,
        StateFlow<StaffSortFilterParams>,
        StateFlow<StudiosSortFilterParams>,
        StateFlow<UsersSortFilterParams>,
    ) -> AnimeSearchViewModel.Factory
}
