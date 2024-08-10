package com.thekeeperofpie.artistalleydatabase.anime2anime.game

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.anilist.Anime2AnimeConnectionsQuery
import com.anilist.Anime2AnimeCountQuery
import com.anilist.fragment.AniListMedia
import com.anilist.fragment.Anime2AnimeConnectionsStaffMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.LogUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChangesForList
import com.thekeeperofpie.artistalleydatabase.anime2anime.Anime2AnimeSubmitResult
import com.thekeeperofpie.artistalleydatabase.anime2anime.BuildConfig
import com.thekeeperofpie.artistalleydatabase.anime2anime.R
import com.thekeeperofpie.artistalleydatabase.compose.debounce
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.time.Duration.Companion.milliseconds

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

    open val options: List<SortFilterSection> = emptyList()
    val optionsState = SortFilterSection.ExpandedState()

    protected val refreshStart = MutableStateFlow(-1L)
    protected val refreshTarget = MutableStateFlow(-1L)
    private val moleculeScope = CoroutineScope(scope.coroutineContext + AndroidUiDispatcher.Main)
    private val optionsFlow by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(RecompositionMode.ContextClock) {
            debounce(currentValue = options(), duration = 500.milliseconds)
        }
    }

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
                .catch { emit(LoadingResult.error(R.string.anime2anime_error_loading_media, it)) }
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
                .catch { emit(LoadingResult.error(R.string.anime2anime_error_loading_media, it)) }
                .collectLatest { state.targetMedia.media = it }
        }
    }

    abstract fun options(): Options

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
                    R.string.anime2anime_error_loading_media,
                    response.exceptionOrNull(),
                )
            )
        }

        return combine(
            mediaListStatusController.allChanges(setOf(media.media.media.id.toString())),
            ignoreController.updates(),
            settings.showAdult,
            settings.showLessImportantTags,
            settings.showSpoilerTags,
        ) { mediaListUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
            val mediaUpdated = applyMediaFiltering(
                statuses = mediaListUpdates,
                ignoreController = ignoreController,
                showAdult = showAdult,
                showIgnored = true,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
                entry = media,
                transform = { media.media },
                media = media.media.media,
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

            if (mediaUpdated != null) {
                LoadingResult.success(mediaUpdated)
            } else {
                LoadingResult.error(R.string.anime2anime_error_loading_media)
            }
        }
    }

    protected suspend fun animeCountAndDate(): Pair<Int, Int> {
        val countResponse = animeCountResponse()
        // There was ~19000 anime when this was written, so use that if it can't be read
        val animeCount = countResponse?.count ?: 19000
        val seed =
            countResponse?.date ?: ZonedDateTime.now(ZoneId.of("UTC")).get(ChronoField.DAY_OF_YEAR)
        return animeCount to seed
    }

    fun refreshStart() {
        refreshStart.value = SystemClock.uptimeMillis()
    }

    fun refreshTarget() {
        refreshTarget.value = SystemClock.uptimeMillis()
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

        if (BuildConfig.DEBUG) {
            val voiceActors = nextMediaConnections?.characters?.edges
                ?.flatMap { it?.voiceActors.orEmpty() }
                ?.mapNotNull {
                    it?.id to (it.allCharacterMediaIds() + it.allStaffMediaIds())
                }
            val staff = nextMediaConnections?.staff?.edges?.mapNotNull { it?.node?.id }
            LogUtils.d(
                TAG,
                "Checking ${aniListMedia.id} for connection with $targetMediaId, " +
                        "considering voice actors $voiceActors and staff $staff"
            )
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
