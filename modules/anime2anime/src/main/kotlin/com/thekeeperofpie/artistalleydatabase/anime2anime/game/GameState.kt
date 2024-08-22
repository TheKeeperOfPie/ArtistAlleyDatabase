package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeSubmitResult
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.LoadingResult
import kotlinx.coroutines.flow.MutableStateFlow

class GameState {

    val startMedia = MediaState()
    val targetMedia = MediaState()

    var continuations by mutableStateOf(emptyList<GameContinuation>())

    var lastSubmitResult by mutableStateOf<Anime2AnimeSubmitResult>(Anime2AnimeSubmitResult.None)

    class MediaState {
        var media by mutableStateOf(LoadingResult.loading<GameContinuation>())
        var customText by mutableStateOf("")
        var customPredictions by mutableStateOf(emptyList<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>())
        var customMediaId = MutableStateFlow<String?>(null)
    }
}
