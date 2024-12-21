package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.lifecycle.SavedStateHandle

interface SeasonalComponent {
    val seasonalViewModelFactory: (SavedStateHandle) -> SeasonalViewModel.Factory
}
