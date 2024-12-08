package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.lifecycle.SavedStateHandle

interface StudiosComponent {
    val studioMediasViewModelFactory: (SavedStateHandle) -> StudioMediasViewModel.Factory
}
