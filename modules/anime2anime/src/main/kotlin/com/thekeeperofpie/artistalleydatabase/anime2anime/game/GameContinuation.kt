package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.anilist.fragment.Anime2AnimeConnectionsMedia
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import kotlinx.coroutines.CoroutineScope

data class GameContinuation(
    val connections: List<Connection>,
    val media: MediaPreviewEntry,
    val characterAndStaffMetadata: Anime2AnimeConnectionsMedia,
    val scope: CoroutineScope,
    val aniListApi: AuthedAniListApi,
) {
    val hasCharacters = characterAndStaffMetadata.characters?.edges
        ?.any { it?.voiceActors?.isNotEmpty() ?: false } ?: false
    var charactersExpanded by mutableStateOf(false)
    val characters = Pager(config = PagingConfig(5)) {
        AniListPagingSource(perPage = 5) {
            aniListApi.anime2AnimeMediaCharacters(
                mediaId = media.media.id.toString(),
                page = it,
                perPage = 5,
            )
                .getOrThrow()
                ?.characters
                .let {
                    it?.pageInfo to it?.edges?.filterNotNull().orEmpty()
                        .map(CharacterUtils::toDetailsCharacter)
                        .filter { it.languageToVoiceActor.isNotEmpty() }
                }
        }
    }.flow
        .enforceUniqueIds { it.id }
        .cachedIn(scope)

    val hasStaff = characterAndStaffMetadata.staff?.edges?.isNotEmpty() ?: false
    var staffExpanded by mutableStateOf(false)
    val staff = Pager(config = PagingConfig(5)) {
        AniListPagingSource(perPage = 5) {
            aniListApi.anime2AnimeMediaStaff(
                mediaId = media.media.id.toString(),
                page = it,
                perPage = 5,
            )
                .getOrThrow()
                ?.staff
                .let {
                    it?.pageInfo to it?.edges?.filterNotNull().orEmpty().map {
                        DetailsStaff(
                            id = it.node.id.toString(),
                            name = it.node.name,
                            image = it.node.image?.large,
                            role = it.role,
                            staff = it.node,
                        )
                    }
                }
        }
    }.flow
        .enforceUniqueIds { it.idWithRole }
        .cachedIn(scope)

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
