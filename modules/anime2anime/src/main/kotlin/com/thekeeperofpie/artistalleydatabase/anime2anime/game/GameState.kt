package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeSubmitResult

class GameState {

    var startAndTargetMedia by mutableStateOf(LoadingResult.loading<GameStartAndTargetMedia>())

    var continuations by mutableStateOf(emptyList<GameContinuation>())

    var lastSubmitResult by mutableStateOf<Anime2AnimeSubmitResult>(Anime2AnimeSubmitResult.None)
}
