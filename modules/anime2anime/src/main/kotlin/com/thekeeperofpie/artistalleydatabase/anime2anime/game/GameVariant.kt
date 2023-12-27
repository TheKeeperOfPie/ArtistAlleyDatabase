package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.anilist.Anime2AnimeConnectionsQuery
import com.anilist.Anime2AnimeCountQuery
import com.anilist.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChangesForList
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeSubmitResult
import com.thekeeperofpie.artistalleydatabase.anime2anime.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

@OptIn(ExperimentalCoroutinesApi::class)
abstract class GameVariant(
    val api: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    protected val userMediaListController: UserMediaListController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    private val scope: CoroutineScope,
    private val refresh: Flow<Long>,
    private val animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    private val onClearText: () -> Unit,
) {

    val state = GameState()

    private var continuations by mutableStateOf(emptyList<GameContinuation>())
    private var continuationsJob: Job? = null

    init {
        scope.launch(CustomDispatchers.Main) {
            snapshotFlow { continuations }
                .applyMediaStatusChangesForList(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    settings = settings,
                    media = { it.media.media },
                    mediaStatusAware = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
                            media = media.copy(
                                mediaListStatus = mediaListStatus,
                                progress = progress,
                                progressVolumes = progressVolumes,
                                scoreRaw = scoreRaw,
                                ignored = ignored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                            )
                        )
                    },
                )
                .flowOn(CustomDispatchers.IO)
                .collectLatest { state.continuations = it }
        }

        scope.launch(CustomDispatchers.Main) {
            refresh
                .onEach { state.startAndTargetMedia = LoadingResult.loading() }
                .flatMapLatest {
                    val result = loadStartAndTargetIds()
                    if (!result.success) {
                        return@flatMapLatest flowOf(
                            result.transformResult<GameStartAndTargetMedia> { null }
                        )
                    }
                    val (startId, targetId) = result.result!!
                    loadStartAndTargetMedia(startId.toString(), targetId.toString())
                }
                .catch { emit(LoadingResult.error(R.string.anime2anime_error_loading_media, it)) }
                .collectLatest { state.startAndTargetMedia = it }
        }
    }

    private suspend fun loadStartAndTargetMedia(
        startId: String,
        targetId: String,
    ): Flow<LoadingResult<GameStartAndTargetMedia>> {
        val startMedia = api.anime2AnimeMedia(startId).getOrNull()?.let {
            GameContinuation(
                connections = emptyList(),
                media = MediaPreviewEntry(it),
                characterAndStaffMetadata = it,
                scope = scope,
                aniListApi = api,
            )
        }
        val targetMedia = api.anime2AnimeMedia(targetId).getOrNull()?.let {
            GameContinuation(
                connections = emptyList(),
                media = MediaPreviewEntry(it),
                characterAndStaffMetadata = it,
                scope = scope,
                aniListApi = api,
            )
        }
        if (startMedia == null || targetMedia == null) {
            return flowOf(LoadingResult.error(R.string.anime2anime_error_loading_media))
        }

        return combine(
            mediaListStatusController.allChanges(
                setOfNotNull(
                    startMedia.media.media.id.toString(),
                    targetMedia.media.media.id.toString()
                )
            ),
            ignoreController.updates(),
            settings.showAdult,
            settings.showLessImportantTags,
            settings.showSpoilerTags,
        ) { mediaListUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
            val startMediaUpdated = applyMediaFiltering(
                statuses = mediaListUpdates,
                ignoreController = ignoreController,
                showAdult = showAdult,
                showIgnored = true,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
                entry = startMedia,
                transform = { startMedia.media },
                media = startMedia.media.media,
                forceShowIgnored = true,
                copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                    copy(
                        media = this.media.copy(
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    )
                }
            )
            val targetMediaUpdated = applyMediaFiltering(
                statuses = mediaListUpdates,
                ignoreController = ignoreController,
                showAdult = showAdult,
                showIgnored = true,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
                entry = targetMedia,
                transform = { targetMedia.media },
                media = targetMedia.media.media,
                forceShowIgnored = true,
                copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                    copy(
                        media = this.media.copy(
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    )
                }
            )

            if (startMediaUpdated != null && targetMediaUpdated != null) {
                LoadingResult.success(
                    GameStartAndTargetMedia(
                        startMedia = startMediaUpdated,
                        targetMedia = targetMediaUpdated,
                    )
                )
            } else {
                LoadingResult.error(R.string.anime2anime_error_loading_media)
            }
        }
    }

    abstract suspend fun loadStartAndTargetIds(): LoadingResult<Pair<Int, Int>>

    protected suspend fun animeCountAndDate(): Pair<Int, Int> {
        val countResponse = animeCountResponse()
        // There was ~19000 anime when this was written, so use that if it can't be read
        val animeCount = countResponse?.count ?: 19000
        val seed = countResponse?.date ?:
            ZonedDateTime.now(ZoneId.of("UTC")).get(ChronoField.DAY_OF_YEAR)
        return animeCount to seed
    }

    fun restart() {
        continuations = emptyList()
        state.lastSubmitResult = Anime2AnimeSubmitResult.None
    }

    fun submitMedia(media: AniListMedia) {
        // TODO: Filter/handle duplicates
        val startAndTargetMedia = state.startAndTargetMedia.result ?: return
        state.lastSubmitResult = Anime2AnimeSubmitResult.Loading
        continuationsJob?.cancel()
        continuationsJob = scope.launch(CustomDispatchers.IO) {
            try {
                val result = submit(startAndTargetMedia, media)
                withContext(CustomDispatchers.Main) {
                    if (result.newContinuation != null) {
                        continuations += result.newContinuation
                        onClearText()
                    }
                    state.lastSubmitResult = result.result
                }
            } catch (e: Exception) {
                // TODO: Error handling
            }
        }
    }

    private suspend fun submit(
        startAndTargetMedia: GameStartAndTargetMedia,
        media: AniListMedia,
    ): SubmitResult {
        val startingMedia = startAndTargetMedia.startMedia
        val lastTargetMedia = startAndTargetMedia.targetMedia
        val nextTargetMediaId = continuations.lastOrNull()?.media?.media?.id
            ?: startingMedia.media.media.id
        val previousMedia = continuations.lastOrNull()
            ?.media
            ?: startingMedia.media
        val previousMediaConnections = continuations.lastOrNull()
            ?.characterAndStaffMetadata
            ?: startingMedia.characterAndStaffMetadata

        val (voiceActorConnections, staffConnections) =
            checkConnectionExists(nextTargetMediaId, media)
        if (voiceActorConnections.isEmpty() && staffConnections.isEmpty()) {
            return SubmitResult(Anime2AnimeSubmitResult.NoConnection(media))
        }

        val previousCharacterIds = previousMediaConnections?.characters?.edges
            ?.filter { it?.voiceActors?.any { previousVoiceActor -> voiceActorConnections.any { it.voiceActor.id == previousVoiceActor?.id } } == true }
            ?.mapNotNull { it?.node?.id?.toString() }
            .orEmpty()

        // TODO: When traversing back to a media already loaded, re-add the same entry
        //  (to re-use Pager state/data)
        val nextMedia = api.anime2AnimeConnectionDetails(
            mediaId = media.id.toString(),
            characterIds = previousCharacterIds + voiceActorConnections.mapNotNull { it.character.node?.id?.toString() },
            voiceActorIds = voiceActorConnections.map { it.voiceActor.id.toString() },
            staffIds = staffConnections.map { it.node?.id.toString() },
        )
        val nextMediaMedia = nextMedia.media
            ?: return SubmitResult(Anime2AnimeSubmitResult.FailedToLoad(media))
        val nextMediaEntry = MediaPreviewEntry(nextMediaMedia)

        val connections =
            voiceActorConnections.mapNotNull { (characterTarget, voiceActorTarget) ->
                val previousCharacterId =
                    previousMediaConnections?.characters?.edges?.find { it?.voiceActors?.any { it?.id == voiceActorTarget.id } == true }?.node?.id
                val previousCharacter =
                    nextMedia.characters?.characters?.find { it?.id == previousCharacterId }
                val character =
                    nextMedia.characters?.characters?.find { it?.id == characterTarget.node?.id }
                character ?: return@mapNotNull null
                val voiceActor =
                    nextMedia.voiceActors?.staff?.find { it?.id == voiceActorTarget.id }
                        ?: return@mapNotNull null
                GameContinuation.Connection.Character(
                    previousCharacter = previousCharacter,
                    character = character,
                    voiceActor = voiceActor,
                )
            } + staffConnections.mapNotNull { staffTarget ->
                val staff = nextMedia.staff?.staff?.find { it?.id == staffTarget.node?.id }
                    ?: return@mapNotNull null
                val previousRole =
                    previousMediaConnections?.staff?.edges?.find { it?.node?.id == staffTarget.node?.id }?.role
                GameContinuation.Connection.Staff(
                    staff = staff,
                    previousRole = previousRole,
                    role = staffTarget.role,
                )
            }
        if (connections.isEmpty()) {
            return SubmitResult(Anime2AnimeSubmitResult.NoConnection(media))
        }
        val hitLastTarget = media.id == lastTargetMedia.media.media.id
        return SubmitResult(
            result = if (hitLastTarget) {
                Anime2AnimeSubmitResult.Finished
            } else {
                Anime2AnimeSubmitResult.Success
            },
            newContinuation = GameContinuation(
                connections = connections,
                media = nextMediaEntry,
                characterAndStaffMetadata = nextMediaMedia,
                scope = scope,
                aniListApi = api,
            ),
        )
    }

    private suspend fun checkConnectionExists(
        targetMediaId: Int,
        aniListMedia: AniListMedia,
    ): ConnectionResult {
        val nextMediaConnections = api.anime2AnimeConnections(aniListMedia.id.toString())
            .media
        // TODO: Ignore the narrator character
        val voiceActorConnections = nextMediaConnections?.characters?.edges
            ?.mapNotNull { character ->
                character?.voiceActors?.find {
                    (it?.characterMedia?.nodes?.any { it?.id == targetMediaId } ?: false)
                            || (it?.staffMedia?.nodes?.any { it?.id == targetMediaId }
                        ?: false)
                }?.let {
                    ConnectionResult.VoiceActorConnection(character, it)
                }
            }
            .orEmpty()
        val staffConnections = nextMediaConnections?.staff?.edges
            ?.filterNotNull()
            ?.filter {
                (it.node?.characterMedia?.nodes?.any { it?.id == targetMediaId } ?: false)
                        || (it.node?.staffMedia?.nodes?.any { it?.id == targetMediaId }
                    ?: false)
            }
            .orEmpty()

        return ConnectionResult(voiceActorConnections, staffConnections)
    }

    data class SubmitResult(
        val result: Anime2AnimeSubmitResult,
        val newContinuation: GameContinuation? = null,
    )

    data class ConnectionResult(
        val voiceActorConnections: List<VoiceActorConnection>,
        val staffConnections: List<Anime2AnimeConnectionsQuery.Data.Media.Staff.Edge>,
    ) {
        data class VoiceActorConnection(
            val character: Anime2AnimeConnectionsQuery.Data.Media.Characters.Edge,
            val voiceActor: Anime2AnimeConnectionsQuery.Data.Media.Characters.Edge.VoiceActor,
        )
    }
}
