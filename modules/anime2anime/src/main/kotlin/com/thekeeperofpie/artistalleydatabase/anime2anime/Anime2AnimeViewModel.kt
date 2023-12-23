package com.thekeeperofpie.artistalleydatabase.anime2anime

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class Anime2AnimeViewModel @Inject constructor(
    private val api: AuthedAniListApi,
    aniListAutocompleter: AniListAutocompleter,
    mediaListStatusController: MediaListStatusController,
    ignoreController: IgnoreController,
    settings: AnimeSettings,
) : ViewModel() {

    companion object {
        private const val TAG = "Anime2AnimeViewModel"
    }

    val viewer = api.authedUser
    var text by mutableStateOf("")
    var predictions by mutableStateOf(emptyList<EntrySection.MultiText.Entry.Prefilled<AniListMedia>>())
    var error by mutableStateOf<Pair<Int, Exception?>?>(null)
        private set

    var startingMedia by mutableStateOf(LoadingResult.loading<Anime2AnimeContinuation>())

    // This is in reverse order to force align to the bottom of the list using reverseLayout = true
    var continuations by mutableStateOf(emptyList<Anime2AnimeContinuation>())
        private set

    private val refresh = MutableStateFlow(-1L)
    private var continuationsPrivate by mutableStateOf(emptyList<Anime2AnimeContinuation>())
    private var continuationsMutex = Mutex()
    private var continuationsJob: Job? = null

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh
                .onEach { startingMedia = LoadingResult.loading() }
                .mapLatest { api.anime2AnimeMedia("11757").media }
                .mapLatest {
                    if (it == null || it.isAdult == true
                        && !settings.showAdult.value
                    ) {
                        LoadingResult.error(
                            R.string.anime2anime_error_loading_media,
                            IOException("Cannot load media")
                        )
                    } else {
                        LoadingResult.success(
                            Anime2AnimeContinuation(
                                connections = emptyList(),
                                media = MediaPreviewEntry(it),
                                characterAndStaffMetadata = it,
                            )
                        )
                    }
                }
                .flatMapLatest {
                    val media = it.result?.media?.media
                    if (media == null) {
                        flowOf(it.transformResult<Anime2AnimeContinuation> { null })
                    } else {
                        combine(
                            mediaListStatusController.allChanges(setOf(media.id.toString())),
                            ignoreController.updates(),
                            settings.showAdult,
                            settings.showLessImportantTags,
                            settings.showSpoilerTags,
                        ) { mediaListUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
                            it.transformResult {
                                applyMediaFiltering(
                                    statuses = mediaListUpdates,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = true,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it.media },
                                    media = it.media.media,
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
                            }
                        }
                    }
                }
                .catch {
                    Log.e(TAG, "Failed to fetch media", it)
                    // TODO: Error handling
                }
                .collectLatest { startingMedia = it }
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
                .collectLatest { continuations = it.reversed() }
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

    private fun submitMedia(aniListMedia: AniListMedia) {
        // TODO: Filter/handle duplicates
        continuationsJob?.cancel()
        continuationsJob = viewModelScope.launch(CustomDispatchers.IO) {
            val targetMediaId = continuationsPrivate.lastOrNull()
                ?: startingMedia.result?.media?.media?.id
                ?: return@launch
            val previousMedia = continuationsPrivate.lastOrNull()
                ?.media
                ?: startingMedia.result?.media
            val previousMediaConnections = continuationsPrivate.lastOrNull()
                ?.characterAndStaffMetadata
                ?: startingMedia.result?.characterAndStaffMetadata
            try {
                val nextMediaConnections = api.anime2AnimeConnections(aniListMedia.id.toString())
                    .media
                val voiceActorConnections = nextMediaConnections?.characters?.edges
                    ?.mapNotNull { character ->
                        character?.voiceActors?.find {
                            (it?.characterMedia?.nodes?.any { it?.id == targetMediaId } ?: false)
                                    || (it?.staffMedia?.nodes?.any { it?.id == targetMediaId }
                                ?: false)
                        }?.let {
                            character to it
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

                if (voiceActorConnections.isEmpty() && staffConnections.isEmpty()) {
                    error = R.string.anime2anime_error_no_connection to null
                    return@launch
                }

                val previousCharacterIds = previousMediaConnections?.characters?.edges
                    ?.filter { it?.voiceActors?.any { previousVoiceActor -> voiceActorConnections.any { it.second.id == previousVoiceActor?.id } } == true }
                    ?.mapNotNull { it?.node?.id?.toString() }
                    .orEmpty()

                val nextMedia = api.anime2AnimeConnectionDetails(
                    mediaId = aniListMedia.id.toString(),
                    characterIds = previousCharacterIds + voiceActorConnections.map { it.first.id.toString() },
                    voiceActorIds = voiceActorConnections.map { it.second.id.toString() },
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
}
