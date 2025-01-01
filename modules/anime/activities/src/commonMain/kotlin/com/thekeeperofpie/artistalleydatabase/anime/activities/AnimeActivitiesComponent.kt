package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.activities.details.ActivityDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute

interface AnimeActivitiesComponent {
    val animeActivityViewModelFactory: (ActivitySortFilterViewModel) -> AnimeActivityViewModel.Factory
    val activitySortFilterViewModel: (SavedStateHandle, MediaDetailsRoute, ActivitySortFilterViewModel.InitialParams) -> ActivitySortFilterViewModel
    val activityDetailsViewModelFactory: (SavedStateHandle) -> ActivityDetailsViewModel.Factory
}
