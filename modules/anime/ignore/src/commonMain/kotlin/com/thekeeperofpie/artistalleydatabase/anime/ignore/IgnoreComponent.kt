package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.lifecycle.SavedStateHandle

interface IgnoreComponent {
    val animeMediaIgnoreViewModelFactory: (SavedStateHandle) -> AnimeMediaIgnoreViewModel.Factory
}
