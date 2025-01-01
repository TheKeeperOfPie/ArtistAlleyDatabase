package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.lifecycle.SavedStateHandle

interface AnimeNewsComponent {
    val newsSortFilterViewModel: (SavedStateHandle) -> NewsSortFilterViewModel
    val animeNewsViewModel: (NewsSortFilterViewModel) -> AnimeNewsViewModel
}
