package com.thekeeperofpie.artistalleydatabase.anime.characters

import androidx.lifecycle.SavedStateHandle
import com.anilist.data.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.characters.details.AnimeCharacterDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.anime.characters.media.CharacterMediasViewModel
import kotlinx.coroutines.flow.Flow

interface CharactersComponent {
    val animeCharacterDetailsViewModelFactory: (SavedStateHandle) -> AnimeCharacterDetailsViewModel.Factory
    val animeMediaDetailsCharactersViewModel: (mediaId: String, Flow<MediaDetailsQuery.Data.Media.Characters?>) -> AnimeMediaDetailsCharactersViewModel
    val characterMediasViewModelFactory: (SavedStateHandle) -> CharacterMediasViewModel.Factory
}
