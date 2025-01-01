package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.lifecycle.SavedStateHandle

interface ScheduleComponent {
    val airingScheduleSortFilterViewModel: (SavedStateHandle) -> AiringScheduleSortFilterViewModel
    val airingScheduleViewModelFactory: (AiringScheduleSortFilterViewModel) -> AiringScheduleViewModel.Factory
}
