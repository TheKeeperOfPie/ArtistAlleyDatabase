package com.thekeeperofpie.artistalleydatabase.anime.songs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.media.details.AnimeMediaDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.media.MediaPlayer
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class AnimeSongsViewModel(
    private val animeSongsProvider: AnimeSongsProvider? = null,
    val mediaPlayer: MediaPlayer,
    @Assisted mediaDetailsViewModel: AnimeMediaDetailsViewModel,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeSongsViewModel"
    }

    val enabled = animeSongsProvider != null
    var animeSongs by mutableStateOf<AnimeSongs?>(null)
    private var animeSongStates by mutableStateOf(emptyMap<String, AnimeSongState>())

    init {
        if (animeSongsProvider != null) {
            viewModelScope.launch(CustomDispatchers.IO) {
                snapshotFlow { mediaDetailsViewModel.entry }
                    .flowOn(CustomDispatchers.Main)
                    .collectLatest {
                        val media = it.result?.media
                        if (media?.type == MediaType.ANIME) {
                            try {
                                val songEntries =
                                    animeSongsProvider.getSongs(media)
                                if (songEntries.isNotEmpty()) {
                                    val songStates = songEntries
                                        .map { AnimeSongState(id = it.id, entry = it) }
                                        .associateBy { it.id }

                                    withContext(CustomDispatchers.Main) {
                                        animeSongStates = songStates
                                        animeSongs = AnimeSongs(songEntries)
                                    }
                                }
                            } catch (e: Exception) {
                                Logger.e(TAG, e) { "Error loading from AnimeThemes" }
                            }
                        }
                    }
            }
        }
    }

    fun getAnimeSongState(animeSongId: String) = animeSongStates[animeSongId]!!

    fun onAnimeSongPlayAudioClick(animeSongId: String) {
        val state = animeSongStates[animeSongId]!!
        mediaPlayer.playPause(state.id, state.entry.audioUrl!!)
        animeSongStates.forEach { it.value.setExpanded(false) }
    }

    fun onAnimeSongProgressUpdate(animeSongId: String, progress: Float) {
        mediaPlayer.updateProgress(animeSongId, progress)
    }

    fun onAnimeSongExpandedToggle(animeSongId: String, expanded: Boolean) {
        animeSongStates.forEach {
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
        animeSongStates.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animeSongStates.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onCleared() {
        animeSongStates.forEach { mediaPlayer.stop(it.key) }
        super.onCleared()
    }

    fun animeSongsCollapseAll() {
        animeSongStates.forEach { it.value.setExpanded(false) }
        mediaPlayer.pause(null)
    }

    data class AnimeSongs(
        val entries: List<AnimeSongEntry>,
    )

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
