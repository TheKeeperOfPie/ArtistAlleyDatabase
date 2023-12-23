package com.thekeeperofpie.artistalleydatabase.anime2anime

import com.anilist.fragment.Anime2AnimeConnectionsMedia
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry

data class Anime2AnimeContinuation(
    val connections: List<Connection>,
    val media: MediaPreviewEntry,
    val characterAndStaffMetadata: Anime2AnimeConnectionsMedia,
) {
    sealed interface Connection {
        data class Character(
            val previousCharacter: CharacterNavigationData?,
            val character: CharacterNavigationData,
            val voiceActor: StaffNavigationData,
        ) : Connection

        data class Staff(
            val staff: StaffNavigationData,
            val previousRole: String?,
            val role: String?,
        ) : Connection
    }
}
