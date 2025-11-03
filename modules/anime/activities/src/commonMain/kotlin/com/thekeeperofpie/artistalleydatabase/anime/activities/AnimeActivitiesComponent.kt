package com.thekeeperofpie.artistalleydatabase.anime.activities

import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.details.ActivityDetailsViewModel

interface AnimeActivitiesComponent {
    val animeActivityViewModelFactoryFactory: AnimeActivityViewModel.TypedFactory.Factory
    val activitySortFilterViewModelFactory: ActivitySortFilterViewModel.Factory
    val activityDetailsViewModelFactoryFactory: ActivityDetailsViewModel.TypedFactory.Factory
}
