package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.lifecycle.SavedStateHandle
import com.thekeeperofpie.artistalleydatabase.anime.activity.details.ActivityDetailsViewModel

interface AnimeActivitiesComponent {
    val activityDetailsViewModel: (SavedStateHandle) -> ActivityDetailsViewModel
}
