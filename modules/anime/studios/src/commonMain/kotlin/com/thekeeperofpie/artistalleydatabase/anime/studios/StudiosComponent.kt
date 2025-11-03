package com.thekeeperofpie.artistalleydatabase.anime.studios

interface StudiosComponent {
    val studioMediaSortFilterViewModelFactory: StudioMediaSortFilterViewModel.Factory
    val studioMediasViewModelFactoryFactory: StudioMediasViewModel.TypedFactory.Factory
    val studiosSortFilterViewModelFactory: StudiosSortFilterViewModel.Factory
}
