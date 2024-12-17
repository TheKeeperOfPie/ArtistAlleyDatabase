package com.thekeeperofpie.artistalleydatabase.anime.schedule

interface ScheduleComponent {
    val airingScheduleViewModelFactory: () -> AiringScheduleViewModel.Factory
}
