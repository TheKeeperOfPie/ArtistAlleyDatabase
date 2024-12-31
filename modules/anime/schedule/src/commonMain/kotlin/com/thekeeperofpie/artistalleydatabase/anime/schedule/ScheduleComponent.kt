package com.thekeeperofpie.artistalleydatabase.anime.schedule

import com.thekeeperofpie.artistalleydatabase.utils_compose.ScopedSavedStateHandle

interface ScheduleComponent {
    val airingScheduleSortFilterViewModel: (ScopedSavedStateHandle) -> AiringScheduleSortFilterViewModel
    val airingScheduleViewModelFactory: (AiringScheduleSortFilterViewModel) -> AiringScheduleViewModel.Factory
}
