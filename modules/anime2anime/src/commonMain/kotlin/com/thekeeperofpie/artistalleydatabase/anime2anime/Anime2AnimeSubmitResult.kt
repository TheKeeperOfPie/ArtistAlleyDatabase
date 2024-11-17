package com.thekeeperofpie.artistalleydatabase.anime2anime

import com.anilist.data.fragment.AniListMedia

sealed interface Anime2AnimeSubmitResult {
    data object None : Anime2AnimeSubmitResult
    data object Loading : Anime2AnimeSubmitResult
    data object Success : Anime2AnimeSubmitResult
    data object Finished : Anime2AnimeSubmitResult
    data class NoConnection(val media: AniListMedia) : Anime2AnimeSubmitResult
    data class SameMedia(val media: AniListMedia) : Anime2AnimeSubmitResult
    data class MediaNotFound(val text: String) : Anime2AnimeSubmitResult
    data class FailedToLoad(val media: AniListMedia) : Anime2AnimeSubmitResult
}
