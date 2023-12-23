package com.thekeeperofpie.artistalleydatabase.anime2anime

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.Anime2AnimeConnectionsQuery
import com.anilist.Anime2AnimeRandomAnimeQuery
import com.anilist.fragment.AniListMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChangesForList
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoField
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class Anime2AnimeViewModel @Inject constructor(
    private val api: AuthedAniListApi,
    private val aniListAutocompleter: AniListAutocompleter,
    private val mediaListStatusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
) : ViewModel() {

    companion object {
        private const val TAG = "Anime2AnimeViewModel"
        private const val MIN_MEDIA_POPULARITY = 10000
    }

    val viewer = api.authedUser
    var text by mutableStateOf("")
    var predictions by mutableStateOf(emptyList<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>())
    var error by mutableStateOf<Pair<Int, Exception?>?>(null)
        private set

    var startAndTargetMedia by mutableStateOf(LoadingResult.loading<Anime2AnimeStartAndTargetMedia>())

    var continuations by mutableStateOf(emptyList<Anime2AnimeContinuation>())
        private set

    var lastSubmitResult by mutableStateOf(LastSubmitResult.None)

    private val refresh = MutableStateFlow(-1L)
    private var continuationsPrivate by mutableStateOf(emptyList<Anime2AnimeContinuation>())
    private var continuationsJob: Job? = null

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh
                .onEach { startAndTargetMedia = LoadingResult.loading() }
                .flatMapLatest {
                    val (startId, targetId) = loadDaily() ?: return@flatMapLatest flowOf(
                        LoadingResult.error(R.string.anime2anime_error_loading_media)
                    )
                    loadStartAndTargetMedia(startId.toString(), targetId.toString())
                }
                .catch {
                    Log.e(TAG, "Failed to fetch media", it)
                    // TODO: Error handling
                }
                .collectLatest { startAndTargetMedia = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { text }
                .debounce(500.milliseconds)
                .filter(String::isNotBlank)
                .flatMapLatest(aniListAutocompleter::querySeriesNetwork)
                .flowOn(CustomDispatchers.IO)
                .collectLatest { predictions = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { continuationsPrivate }
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
                .collectLatest { continuations = it }
        }
    }

    fun onRefresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

    fun onSubmit() {
        val entry = predictions.firstOrNull()
        if (entry == null) {
            error = R.string.anime2anime_error_media_not_found to null
            return
        }

        submitMedia(entry.value)
    }

    fun onChooseMedia(aniListMedia: AniListMedia) {
        text = ""
        submitMedia(aniListMedia)
    }

    private suspend fun loadDaily(): Pair<Int, Int>? {
        val countResponse = api.anime2AnimeCount()
        // There was ~19000 anime when this was written, so use that if it can't be read
        val animeCount = countResponse.getOrNull()?.count ?: 19000
        val seed = countResponse.getOrNull()?.date ?: Instant.now().get(ChronoField.DAY_OF_YEAR)
        val random = Random(seed)

        // TODO: Error handling if no anime returned
        val startAnime = randomAnime(random, animeCount) ?: return null
        val targetAnime = randomAnime(random, animeCount) ?: return null

        // TODO: Handle really rare occurrence of getting the same show
        if (startAnime.id == targetAnime.id) return null
        return startAnime.id to targetAnime.id
    }

    private suspend fun loadStartAndTargetMedia(
        startId: String,
        targetId: String,
    ): Flow<LoadingResult<Anime2AnimeStartAndTargetMedia>> {
        val startMedia = api.anime2AnimeMedia(startId).getOrNull()?.let {
            Anime2AnimeContinuation(
                connections = emptyList(),
                media = MediaPreviewEntry(it),
                characterAndStaffMetadata = it,
            )
        }
        val targetMedia = api.anime2AnimeMedia(targetId).getOrNull()?.let {
            Anime2AnimeContinuation(
                connections = emptyList(),
                media = MediaPreviewEntry(it),
                characterAndStaffMetadata = it,
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
                    Anime2AnimeStartAndTargetMedia(
                        startMedia = startMediaUpdated,
                        targetMedia = targetMediaUpdated,
                    )
                )
            } else {
                LoadingResult.error(R.string.anime2anime_error_loading_media)
            }
        }
    }

    private suspend fun randomAnime(
        random: Random,
        totalAnimeCount: Int,
    ): Anime2AnimeRandomAnimeQuery.Data.Page.Medium? {
        var attempts = 0
        var randomAnime: Anime2AnimeRandomAnimeQuery.Data.Page.Medium? = null
        while (randomAnime == null && attempts++ < 5) {
            val randomPage = random.nextInt(1, totalAnimeCount / 25)
            randomAnime = api.anime2AnimeRandomAnime(randomPage).getOrNull()
                ?.filterNotNull()
                ?.find { (it.popularity ?: 0) >= MIN_MEDIA_POPULARITY }
        }
        return randomAnime
    }

    private fun submitMedia(aniListMedia: AniListMedia) {
        // TODO: Filter/handle duplicates
        continuationsJob?.cancel()
        continuationsJob = viewModelScope.launch(CustomDispatchers.IO) {
            val startingMedia = startAndTargetMedia?.result?.startMedia
            val targetMediaId = continuationsPrivate.lastOrNull()?.media?.media?.id
                ?: startingMedia?.media?.media?.id
                ?: return@launch
            val previousMedia = continuationsPrivate.lastOrNull()
                ?.media
                ?: startingMedia?.media
            val previousMediaConnections = continuationsPrivate.lastOrNull()
                ?.characterAndStaffMetadata
                ?: startingMedia?.characterAndStaffMetadata
            try {
                val (voiceActorConnections, staffConnections) =
                    checkConnectionExists(targetMediaId, aniListMedia)
                if (voiceActorConnections.isEmpty() && staffConnections.isEmpty()) {
                    error = R.string.anime2anime_error_no_connection to null
                    return@launch
                }

                val previousCharacterIds = previousMediaConnections?.characters?.edges
                    ?.filter { it?.voiceActors?.any { previousVoiceActor -> voiceActorConnections.any { it.voiceActor.id == previousVoiceActor?.id } } == true }
                    ?.mapNotNull { it?.node?.id?.toString() }
                    .orEmpty()

                val nextMedia = api.anime2AnimeConnectionDetails(
                    mediaId = aniListMedia.id.toString(),
                    characterIds = previousCharacterIds + voiceActorConnections.map { it.character.id.toString() },
                    voiceActorIds = voiceActorConnections.map { it.voiceActor.id.toString() },
                    staffIds = staffConnections.map { it.node?.id.toString() },
                )
                val nextMediaMedia = nextMedia.media
                val nextMediaEntry = nextMediaMedia?.let(::MediaPreviewEntry)

                val connections =
                    voiceActorConnections.mapNotNull { (characterTarget, voiceActorTarget) ->
                        val previousCharacterId =
                            previousMediaConnections?.characters?.edges?.find { it?.voiceActors?.any { it?.id == voiceActorTarget.id } == true }?.node?.id
                        val previousCharacter =
                            nextMedia.characters?.characters?.find { it?.id == previousCharacterId }
                        val character =
                            nextMedia.characters?.characters?.find { it?.id == characterTarget.id }
                                ?: return@mapNotNull null
                        val voiceActor =
                            nextMedia.voiceActors?.staff?.find { it?.id == voiceActorTarget.id }
                                ?: return@mapNotNull null
                        Anime2AnimeContinuation.Connection.Character(
                            previousCharacter = previousCharacter,
                            character = character,
                            voiceActor = voiceActor,
                        )
                    } + staffConnections.mapNotNull { staffTarget ->
                        val staff = nextMedia.staff?.staff?.find { it?.id == staffTarget.node?.id }
                            ?: return@mapNotNull null
                        val previousRole =
                            previousMediaConnections?.staff?.edges?.find { it?.node?.id == staffTarget.node?.id }?.role
                        Anime2AnimeContinuation.Connection.Staff(
                            staff = staff,
                            previousRole = previousRole,
                            role = staffTarget.role,
                        )
                    }
                if (nextMediaMedia != null && nextMediaEntry != null) {
                    withContext(CustomDispatchers.Main) {
                        continuationsPrivate += Anime2AnimeContinuation(
                            connections = connections,
                            media = nextMediaEntry,
                            characterAndStaffMetadata = nextMediaMedia,
                        )
                    }
                }
            } catch (e: Exception) {
                // TODO: Error handling
            }
        }
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

    data class ConnectionResult(
        val voiceActorConnections: List<VoiceActorConnection>,
        val staffConnections: List<Anime2AnimeConnectionsQuery.Data.Media.Staff.Edge>,
    ) {
        data class VoiceActorConnection(
            val character: Anime2AnimeConnectionsQuery.Data.Media.Characters.Edge,
            val voiceActor: Anime2AnimeConnectionsQuery.Data.Media.Characters.Edge.VoiceActor,
        )
    }

    sealed interface LastSubmitResult {
        data object None : LastSubmitResult
    }
}
