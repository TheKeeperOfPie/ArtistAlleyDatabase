package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import artistalleydatabase.modules.anime2anime.generated.resources.Res
import artistalleydatabase.modules.anime2anime.generated.resources.anime2anime_error_loading_media
import co.touchlab.kermit.Logger
import com.anilist.data.Anime2AnimeConnectionsQuery
import com.anilist.data.Anime2AnimeCountQuery
import com.anilist.data.fragment.AniListMedia
import com.anilist.data.fragment.Anime2AnimeConnectionsStaffMedia
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeSubmitResult
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeUiDispatcher
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
abstract class GameVariant<Options>(
    val api: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    protected val userMediaListController: UserMediaListController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    protected val scope: CoroutineScope,
    private val animeCountResponse: suspend () -> Anime2AnimeCountQuery.Data.SiteStatistics.Anime.Node?,
    private val onClearText: () -> Unit,
) {
    companion object {
        private const val TAG = "GameVariant"
    }

    val state = GameState()

    open val options: List<SortFilterSectionState> = emptyList()
    val optionsState = SortFilterExpandedState()

    protected val refreshStart = MutableStateFlow(-1L)
    protected val refreshTarget = MutableStateFlow(-1L)
    private val moleculeScope = CoroutineScope(scope.coroutineContext + ComposeUiDispatcher.Main)

    private var continuations by mutableStateOf(emptyList<GameContinuation>())
    private var continuationsJob: Job? = null

    abstract val optionsFlow: StateFlow<Options>

    init {
        scope.launch(CustomDispatchers.Main) {
            snapshotFlow { continuations }
                .flatMapLatest { data ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        data.mapNotNull {
                            applyMediaFiltering(
                                statuses = statuses,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it,
                                filterableData = it.media.mediaFilterable,
                                copy = { copy(media = media.copy(mediaFilterable = it)) },
                            )
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { state.continuations = it }
        }

        scope.launch(CustomDispatchers.Main) {
            combine(refreshStart, optionsFlow) { _, options -> options }
                .onEach { state.startMedia.media = LoadingResult.loading() }
                .flatMapLatest {
                    val result = loadStartId(it)
                    if (!result.success) {
                        return@flatMapLatest flowOf(result.transformResult<GameContinuation> { null })
                    }
                    loadMedia(result.result!!.toString())
                }
                .flowOn(CustomDispatchers.IO)
                .catch { emit(LoadingResult.error(Res.string.anime2anime_error_loading_media, it)) }
                .collectLatest { state.startMedia.media = it }
        }

        scope.launch(CustomDispatchers.Main) {
            combine(refreshTarget, optionsFlow) { _, options -> options }
                .onEach { state.targetMedia.media = LoadingResult.loading() }
                .flatMapLatest {
                    val result = loadTargetId(it)
                    if (!result.success) {
                        return@flatMapLatest flowOf(result.transformResult<GameContinuation> { null })
                    }
                    loadMedia(result.result!!.toString())
                }
                .flowOn(CustomDispatchers.IO)
                .catch { emit(LoadingResult.error(Res.string.anime2anime_error_loading_media, it)) }
                .collectLatest { state.targetMedia.media = it }
        }
    }

    abstract suspend fun loadStartId(options: Options): LoadingResult<Int>
    abstract suspend fun loadTargetId(options: Options): LoadingResult<Int>

    protected abstract fun resetStartMedia()
    protected abstract fun resetTargetMedia()

    fun resetStart() {
        restart()
        resetStartMedia()
    }

    fun resetTarget() {
        restart()
        resetStartMedia()
    }

    private suspend fun loadMedia(mediaId: String): Flow<LoadingResult<GameContinuation>> {
        val response = api.anime2AnimeMedia(mediaId)
        val media = response.getOrNull()?.let {
            GameContinuation(
                connections = emptyList(),
                media = MediaPreviewEntry(it),
                characterAndStaffMetadata = it,
                scope = scope,
                aniListApi = api,
            )
        }

        if (media == null) {
            return flowOf(
                LoadingResult.error(
                    error = Res.string.anime2anime_error_loading_media,
                    throwable = response.exceptionOrNull(),
                )
            )
        }

        return combine(
            mediaListStatusController.allChanges(setOf(media.media.media.id.toString())),
            ignoreController.updates(),
            settings.mediaFilteringData(forceShowIgnored = true),
        ) { mediaListUpdates, _, filteringData ->
            val mediaUpdated = applyMediaFiltering(
                statuses = mediaListUpdates,
                ignoreController = ignoreController,
                filteringData = filteringData,
                entry = media,
                filterableData = media.media.mediaFilterable,
                copy = { copy(media = this.media.copy(mediaFilterable = it)) },
            )

            if (mediaUpdated != null) {
                LoadingResult.success(mediaUpdated)
            } else {
                LoadingResult.error(Res.string.anime2anime_error_loading_media)
            }
        }
    }

    protected suspend fun animeCountAndDate(): Pair<Int, Int> {
        val countResponse = animeCountResponse()
        // There was ~19000 anime when this was written, so use that if it can't be read
        val animeCount = countResponse?.count ?: 19000
        val seed = countResponse?.date ?: Clock.System.now().toLocalDateTime(TimeZone.UTC).dayOfYear
        return animeCount to seed
    }

    fun refreshStart() {
        refreshStart.value = Clock.System.now().toEpochMilliseconds()
    }

    fun refreshTarget() {
        refreshTarget.value = Clock.System.now().toEpochMilliseconds()
    }

    fun restart() {
        continuations = emptyList()
        state.lastSubmitResult = Anime2AnimeSubmitResult.None
    }

    fun onChooseStartMedia(media: AniListMedia) {
        state.startMedia.customMediaId.value = media.id.toString()
    }

    fun onChooseTargetMedia(media: AniListMedia) {
        state.targetMedia.customMediaId.value = media.id.toString()
    }

    fun submitMedia(media: AniListMedia) {
        // TODO: Filter/handle duplicates
        val startMedia = state.startMedia.media.result ?: return
        val targetMedia = state.targetMedia.media.result ?: return
        state.lastSubmitResult = Anime2AnimeSubmitResult.Loading
        continuationsJob?.cancel()
        continuationsJob = scope.launch(CustomDispatchers.IO) {
            try {
                val result = submit(startMedia, targetMedia, media)
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
        startMedia: GameContinuation,
        targetMedia: GameContinuation,
        media: AniListMedia,
    ): SubmitResult {
        val nextTargetMediaId = continuations.lastOrNull()?.media?.media?.id
            ?: startMedia.media.media.id
        val previousMedia = continuations.lastOrNull()
            ?.media
            ?: startMedia.media
        val previousMediaConnections = continuations.lastOrNull()
            ?.characterAndStaffMetadata
            ?: startMedia.characterAndStaffMetadata
        if (media.id == previousMedia.media.id) {
            return SubmitResult(Anime2AnimeSubmitResult.SameMedia(media))
        }

        val (voiceActorConnections, staffConnections) =
            checkConnectionExists(nextTargetMediaId, media)
        if (voiceActorConnections.isEmpty() && staffConnections.isEmpty()) {
            return SubmitResult(Anime2AnimeSubmitResult.NoConnection(media))
        }

        val previousCharacterIds = previousMediaConnections.characters?.edges
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
        val hitLastTarget = media.id == targetMedia.media.media.id
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

    private fun Anime2AnimeConnectionsStaffMedia?.allCharacterMediaIds() =
        this?.characters0?.nodes?.filterNotNull()?.map { it.id }.orEmpty() +
                this?.characters1?.nodes?.filterNotNull()?.map { it.id }.orEmpty() +
                this?.characters2?.nodes?.filterNotNull()?.map { it.id }.orEmpty()

    private fun Anime2AnimeConnectionsStaffMedia?.allStaffMediaIds() =
        this?.staff0?.nodes?.filterNotNull()?.map { it.id }.orEmpty() +
                this?.staff1?.nodes?.filterNotNull()?.map { it.id }.orEmpty() +
                this?.staff2?.nodes?.filterNotNull()?.map { it.id }.orEmpty()

    private suspend fun checkConnectionExists(
        targetMediaId: Int,
        aniListMedia: AniListMedia,
    ): ConnectionResult {
        val nextMediaConnections = api.anime2AnimeConnections(aniListMedia.id.toString())
            .media

        if (BuildVariant.isDebug()) {
            val voiceActors = nextMediaConnections?.characters?.edges
                ?.flatMap { it?.voiceActors.orEmpty() }
                ?.mapNotNull {
                    it?.id to (it.allCharacterMediaIds() + it.allStaffMediaIds())
                }
            val staff = nextMediaConnections?.staff?.edges?.mapNotNull { it?.node?.id }
            Logger.d(TAG) {
                "Checking ${aniListMedia.id} for connection with $targetMediaId, " +
                        "considering voice actors $voiceActors and staff $staff"
            }
        }
        // TODO: Ignore the narrator character
        val voiceActorConnections = nextMediaConnections?.characters?.edges
            ?.mapNotNull { character ->
                character?.voiceActors?.find {
                    it.allCharacterMediaIds().contains(targetMediaId)
                            || it.allStaffMediaIds().contains(targetMediaId)
                }?.let {
                    ConnectionResult.VoiceActorConnection(character, it)
                }
            }
            .orEmpty()
        val staffConnections = nextMediaConnections?.staff?.edges
            ?.filterNotNull()
            ?.filter {
                it.node.allCharacterMediaIds().contains(targetMediaId)
                        || it.node.allStaffMediaIds().contains(targetMediaId)
            }
            .orEmpty()

        return ConnectionResult(voiceActorConnections, staffConnections)
    }

    fun switchStartTarget() {
        val startMedia = state.startMedia.media
        state.startMedia.media = state.targetMedia.media
        state.targetMedia.media = startMedia

        continuations = emptyList()
        state.lastSubmitResult = Anime2AnimeSubmitResult.None
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
