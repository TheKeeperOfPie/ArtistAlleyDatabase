package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.lifecycle.SavedStateHandle

interface StudiosComponent {
    val studioMediaSortFilterViewModel: (SavedStateHandle) -> StudioMediaSortFilterViewModel
    val studioMediasViewModelFactory: (SavedStateHandle, StudioMediaSortFilterViewModel) -> StudioMediasViewModel.Factory
}
