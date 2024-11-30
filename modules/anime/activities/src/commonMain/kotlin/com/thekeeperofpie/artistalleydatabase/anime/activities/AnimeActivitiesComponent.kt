package com.thekeeperofpie.artistalleydatabase.anime.activities

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.activities.details.ActivityDetailsViewModel

interface AnimeActivitiesComponent {
    val animeActivityViewModelFactory: () -> AnimeActivityViewModel.Factory
    val activityDetailsViewModelFactory: (SavedStateHandle) -> ActivityDetailsViewModel.Factory
}
