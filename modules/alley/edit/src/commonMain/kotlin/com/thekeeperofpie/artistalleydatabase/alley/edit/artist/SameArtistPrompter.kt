package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_add_error_loading_merge
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

class SameArtistPrompter(
    scope: CoroutineScope,
    private val artistInference: ArtistInference,
    private val artistFormState: ArtistFormState,
    dispatchers: CustomDispatchers,
    savedStateHandle: SavedStateHandle,
) {
    private val sameArtistId =
        savedStateHandle.getMutableStateFlow<Uuid?>("sameArtistId", null)
    private val sameArtist = sameArtistId
        .flatMapLatest {
            if (it == null) {
                flowOf(LoadingResult.empty())
            } else {
                flow {
                    emit(LoadingResult.loading())
                    val artist = artistInference.getPreviousYearData(it)
                    emit(
                        if (artist == null) {
                            LoadingResult.error(Res.string.alley_edit_artist_add_error_loading_merge)
                        } else {
                            LoadingResult.success(artist)
                        }
                    )
                }
            }
        }
        .stateIn(scope, SharingStarted.Lazily, LoadingResult.empty())

    private val hasPreviousYear =
        snapshotFlow { Uuid.parseOrNull(artistFormState.editorState.id.value.toString()) }
            .mapLatest {
                if (it == null) {
                    false
                } else {
                    artistInference.hasPreviousYear(it.toString())
                }
            }

    private val inferredArtists = hasPreviousYear.flatMapLatest {
        if (it) {
            flowOf(emptyList())
        } else {
            snapshotFlow {
                ArtistInference.Input.captureState(artistFormState)
                    .takeIf {
                        artistFormState.info.name.lockState.editable ||
                                artistFormState.links.stateSocialLinks.lockState.editable
                    }
            }
                .mapLatest {
                    if (it == null) {
                        emptyList()
                    } else {
                        artistInference.inferArtist(it)
                    }
                }
        }
    }
        .flowOn(dispatchers.io)
        .stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    val state = State(inferredArtists = inferredArtists, sameArtist = sameArtist)

    fun onClickSameArtist(artistId: Uuid) {
        sameArtistId.value = artistId
    }

    internal fun onDenySameArtist() {
        sameArtistId.value = null
    }

    internal fun onConfirmSameArtist() {
        val previousYearData = sameArtist.value.result ?: return
        artistFormState.editorState.id.value.setTextAndPlaceCursorAtEnd(previousYearData.artistId)
        previousYearData.name?.let {
            artistFormState.info.name.value.setTextAndPlaceCursorAtEnd(it)
            artistFormState.info.name.lockState = EntryLockState.LOCKED
        }
        sameArtistId.value = null
    }

    @Stable
    class State(
        val inferredArtists: StateFlow<List<ArtistInference.MatchResult>>,
        val sameArtist: StateFlow<LoadingResult<ArtistInference.PreviousYearData>>,
    )
}
