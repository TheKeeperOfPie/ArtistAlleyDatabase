package com.thekeeperofpie.artistalleydatabase.anime.schedule

interface ScheduleComponent {
    val airingScheduleSortFilterViewModelFactory: AiringScheduleSortFilterViewModel.Factory
    val airingScheduleViewModelFactoryFactory: AiringScheduleViewModel.TypedFactory.Factory
}
