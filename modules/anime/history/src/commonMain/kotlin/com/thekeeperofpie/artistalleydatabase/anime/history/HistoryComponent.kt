package com.thekeeperofpie.artistalleydatabase.anime.history

import androidx.lifecycle.SavedStateHandle

interface HistoryComponent {
    val mediaHistoryViewModel: (SavedStateHandle) -> MediaHistoryViewModel.Factory
}
