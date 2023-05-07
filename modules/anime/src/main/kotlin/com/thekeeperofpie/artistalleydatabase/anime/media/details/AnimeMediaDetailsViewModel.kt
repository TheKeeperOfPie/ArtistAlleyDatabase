package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.app.Application
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaDetailsQuery
import com.anilist.fragment.MediaDetailsListEntry
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditData
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesApi
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesUtils
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val application: Application,
    private val aniListApi: AuthedAniListApi,
    private val cdEntryDao: CdEntryDao,
    private val appJson: AppJson,
    private val animeThemesApi: AnimeThemesApi,
    val mediaPlayer: AppMediaPlayer,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeMediaDetailsViewModel"
    }

    lateinit var mediaId: String

    val loading = MutableStateFlow(false)
    val media = MutableStateFlow<MediaDetailsQuery.Data.Media?>(null)
    var errorResource = MutableStateFlow<Pair<Int, Exception?>?>(null)
    var animeSongs = MutableStateFlow<AnimeSongs?>(null)
    var cdEntries = MutableStateFlow<List<CdEntryGridModel>>(emptyList())

    val scoreFormat = MutableStateFlow(ScoreFormat.POINT_100)

    var trailerPlaybackPosition = 0f

    var listEntry = MutableStateFlow<MediaDetailsListEntry?>(null)

    private val animeSongStates = MutableStateFlow(emptyMap<String, AnimeSongState>())

    val editData = MediaEditData()

    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId

        viewModelScope.launch(CustomDispatchers.IO) {
            loading.value = true
            try {
                media.value = aniListApi.mediaDetails(mediaId).data?.media
            } catch (e: Exception) {
                errorResource.value = R.string.anime_media_error_loading_details to e
            } finally {
                loading.value = false
            }

            initializeListEntry(media.value?.mediaListEntry)

            cdEntries.value = cdEntryDao.searchSeriesByMediaId(appJson, mediaId)
                .map { CdEntryGridModel.buildFromEntry(application, it) }

            val mediaValue = media.value
            if (mediaValue?.type == MediaType.ANIME) {
                try {
                    val anime = animeThemesApi.getAnime(mediaId)
                    val songEntries = anime
                        ?.animethemes
                        ?.flatMap { animeTheme ->
                            animeTheme.animeThemeEntries.map {
                                val video = it.videos.firstOrNull()
                                AnimeSongEntry(
                                    id = it.id,
                                    type = animeTheme.type,
                                    title = animeTheme.song?.title.orEmpty(),
                                    spoiler = it.spoiler,
                                    artists = animeTheme.song?.artists
                                        ?.map { buildArtist(mediaValue, it) }
                                        .orEmpty(),
                                    episodes = it.episodes,
                                    videoUrl = video?.link,
                                    audioUrl = video?.audio?.link,
                                    link = AnimeThemesUtils.buildWebsiteLink(anime, animeTheme, it),
                                )
                            }
                        }
                        .orEmpty()

                    if (songEntries.isNotEmpty()) {
                        val songStates = songEntries
                            .map { AnimeSongState(id = it.id, entry = it) }
                            .associateBy { it.id }

                        withContext(CustomDispatchers.Main) {
                            animeSongStates.value = songStates
                            animeSongs.value = AnimeSongs(songEntries)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading from AnimeThemes", e)
                }
            }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(listEntry, scoreFormat, ::Pair)
                .collectLatest { (listEntry, format) ->
                    initializeListEntry(listEntry)

                    editData.score = listEntry?.score?.toInt()?.let {
                        when (format) {
                            ScoreFormat.POINT_10_DECIMAL -> String.format("%.1f", it / 10f)
                            ScoreFormat.POINT_10 -> (it / 10).toString()
                            ScoreFormat.POINT_100,
                            ScoreFormat.POINT_5,
                            ScoreFormat.POINT_3,
                            ScoreFormat.UNKNOWN__ -> it.toString()
                        }
                    }.orEmpty()
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            aniListApi.authedUser
                .mapNotNull { it?.mediaListOptions?.scoreFormat }
                .collect(scoreFormat::emit)
        }
    }

    private fun initializeListEntry(listEntry: MediaDetailsListEntry?) {
        this.listEntry.value = listEntry
        editData.status = listEntry?.status
        editData.progress = listEntry?.progress?.toString().orEmpty()
        editData.repeat = listEntry?.repeat?.toString().orEmpty()
        editData.startDate = MediaUtils.parseLocalDate(listEntry?.startedAt)
        editData.endDate = MediaUtils.parseLocalDate(listEntry?.completedAt)
        editData.priority = listEntry?.priority?.toString().orEmpty()
        editData.private = listEntry?.private ?: false
        editData.updatedAt = listEntry?.updatedAt?.toLong()
        editData.createdAt = listEntry?.createdAt?.toLong()
    }

    // TODO: Something better than exact string matching
    private fun buildArtist(
        media: MediaDetailsQuery.Data.Media,
        artist: AnimeTheme.Song.Artist,
    ): AnimeSongEntry.Artist {
        val edges = media.characters?.edges?.filterNotNull()
        val edge = edges?.find {
            val characterName = it.node?.name ?: return@find false
            characterName.full == artist.character ||
                    (characterName.alternative?.any { it == artist.character } == true)
        }

        val voiceActor = edge?.voiceActors?.filterNotNull()
            ?.firstOrNull {
                it.name?.full == artist.name ||
                        (it.name?.alternative?.any { it == artist.name } == true)
            }

        val character = edge?.node?.let {
            AnimeSongEntry.Artist.Character(
                aniListId = it.id.toString(),
                image = it.image?.large,
                name = it.name?.userPreferred ?: artist.character ?: "",
            )
        }

        val artistImage = voiceActor?.image?.large
            ?: (artist.images.firstOrNull {
                it.facet == AnimeTheme.Song.Artist.Images.Facet.SmallCover
            } ?: artist.images.firstOrNull())?.link

        return AnimeSongEntry.Artist(
            id = artist.id,
            aniListId = voiceActor?.id?.toString(),
            animeThemesSlug = artist.slug,
            name = artist.name,
            image = artistImage,
            asCharacter = !artist.character.isNullOrBlank(),
            character = character,
        )
    }

    fun getAnimeSongState(animeSongId: String) = animeSongStates.value[animeSongId]!!

    fun onAnimeSongPlayAudioClick(animeSongId: String) {
        val state = animeSongStates.value[animeSongId]!!
        mediaPlayer.playPause(state.id, state.entry.audioUrl!!)
        animeSongStates.value.forEach { it.value.setExpanded(false) }
    }

    fun onAnimeSongProgressUpdate(animeSongId: String, progress: Float) {
        mediaPlayer.updateProgress(animeSongId, progress)
    }

    fun onAnimeSongExpandedToggle(animeSongId: String, expanded: Boolean) {
        animeSongStates.value.forEach {
            if (it.key == animeSongId) {
                if (expanded) {
                    if (!it.value.expanded()) {
                        it.value.setExpanded(true)
                        mediaPlayer.prepare(animeSongId, it.value.entry.videoUrl!!)
                    }
                } else {
                    if (it.value.expanded()) {
                        it.value.setExpanded(false)
                        mediaPlayer.pause(animeSongId)
                    }
                }
            } else {
                it.value.setExpanded(false)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        animeSongStates.value.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animeSongStates.value.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onCleared() {
        animeSongStates.value.forEach { mediaPlayer.stop(it.key) }
        super.onCleared()
    }

    fun animeSongsCollapseAll() {
        animeSongStates.value.forEach { it.value.setExpanded(false) }
        mediaPlayer.pause(null)
    }

    fun onDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        if (start) {
            editData.startDate = selectedDate
        } else {
            editData.endDate = selectedDate
        }
    }

    fun onStatusChange(status: MediaListStatus?) {
        editData.status = status
        when (status) {
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.PAUSED,
            MediaListStatus.REPEATING,
            MediaListStatus.UNKNOWN__, null -> Unit
            MediaListStatus.COMPLETED -> {
                media.value?.run { episodes ?: volumes }
                    ?.let { editData.progress = it.toString() }
                editData.endDate = LocalDate.now()
            }
            MediaListStatus.DROPPED -> {
                editData.endDate = LocalDate.now()
            }
        }
    }

    fun onEditSheetValueChange(sheetValue: SheetValue): Boolean {
        if (sheetValue != SheetValue.Hidden) return true
        if (editData.isEqualTo(listEntry.value, scoreFormat.value)) return true
        editData.showConfirmClose = true
        return false
    }

    fun onClickDelete() {
        if (editData.saving || editData.deleting) return
        editData.deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.deleteMediaListEntry(listEntry.value?.id?.toString()!!)
                withContext(CustomDispatchers.Main) {
                    initializeListEntry(null)
                    editData.deleting = false
                    editData.showing = false
                    editData.showConfirmClose = false
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    editData.deleting = false
                    editData.errorRes = R.string.anime_media_edit_error_deleting to e
                }
            }
        }
    }

    fun onClickSave() {
        if (editData.status == null) {
            editData.saving = false
            editData.errorRes = R.string.anime_media_edit_error_invalid_status to null
            return
        }

        if (editData.saving || editData.deleting) return
        editData.saving = true

        // Read values on main thread before entering coroutine
        val scoreRaw = editData.scoreRaw(scoreFormat.value)
        val progress = editData.progress
        val repeat = editData.repeat
        val priority = editData.priority
        val status = editData.status
        val private = editData.private
        val startDate = editData.startDate
        val endDate = editData.endDate

        viewModelScope.launch(CustomDispatchers.IO) {
            fun validateFieldAsInt(field: String): Int? {
                if (field.isBlank()) return 0
                return field.toIntOrNull()
            }

            if (scoreRaw == null) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.errorRes = R.string.anime_media_edit_error_invalid_score to null
                }
                return@launch
            }

            val progressAsInt = validateFieldAsInt(progress)
            if (progressAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.errorRes = R.string.anime_media_edit_error_invalid_progress to null
                }
                return@launch
            }

            val repeatAsInt = validateFieldAsInt(repeat)
            if (repeatAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.errorRes = R.string.anime_media_edit_error_invalid_repeat to null
                }
                return@launch
            }

            val priorityAsInt = validateFieldAsInt(priority)
            if (priorityAsInt == null) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.errorRes = R.string.anime_media_edit_error_invalid_priority to null
                }
                return@launch
            }

            try {
                val result = aniListApi.saveMediaListEntry(
                    id = listEntry.value?.id?.toString(),
                    mediaId = mediaId,
                    type = media.value?.type,
                    status = status,
                    scoreRaw = scoreRaw,
                    progress = progressAsInt,
                    repeat = repeatAsInt,
                    priority = priorityAsInt,
                    private = private,
                    startedAt = startDate,
                    completedAt = endDate,
                    hiddenFromStatusLists = null,
                )
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.showing = false
                    editData.showConfirmClose = false
                    initializeListEntry(result)
                }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    editData.saving = false
                    editData.errorRes = R.string.anime_media_edit_error_saving to e
                }
            }
        }
    }

    data class AnimeSongs(
        val entries: List<AnimeSongEntry>,
    )

    data class AnimeSongEntry(
        val id: String,
        val type: AnimeTheme.Type?,
        val title: String,
        val spoiler: Boolean,
        val artists: List<Artist>,
        val episodes: String?,
        val videoUrl: String?,
        val audioUrl: String?,
        val link: String?,
    ) {
        data class Artist(
            val id: String,
            val aniListId: String?,
            val animeThemesSlug: String,
            val name: String,
            val image: String?,
            val asCharacter: Boolean,
            val character: Character?,
        ) {
            val link by lazy {
                if (aniListId != null) {
                    AniListUtils.staffUrl(aniListId)
                } else {
                    AnimeThemesUtils.artistUrl(animeThemesSlug)
                }
            }

            data class Character(
                val aniListId: String,
                val image: String?,
                val name: String,
            )
        }
    }

    // State separated from immutable data so that recomposition is as granular as possible
    class AnimeSongState(
        val id: String,
        val entry: AnimeSongEntry,
    ) {

        private var _expanded by mutableStateOf(false)

        fun expanded() = _expanded

        fun setExpanded(expanded: Boolean) {
            this._expanded = expanded
        }
    }
}
