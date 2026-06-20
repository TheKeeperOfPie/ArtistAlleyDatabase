package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.EditTagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LinkCategory
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.category
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

@AssistedInject
class RemoteArtistDataMergeViewModel(
    private val artistInference: ArtistInference,
    private val database: AlleyEditDatabase,
    private val dispatchers: CustomDispatchers,
    private val seriesImageLoader: SeriesImageLoader,
    val tagAutocomplete: EditTagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted private val id: ArtistRemoteEntry.Id,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val confirmedArtistId =
        savedStateHandle.getMutableStateFlow<Uuid?>("confirmedArtistId", null)

    internal val currentEntry = flowFromSuspend {
        database.loadRemoteArtistData(dataYear, id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val previousEntry = flowFromSuspend {
        database.loadRemoteArtistDataHistory(dataYear = dataYear, id = id, timestamp = null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val inferredArtists = currentEntry.mapLatest {
        if (it == null) return@mapLatest LoadingResult.loading()
        val linkModels = it.links.map(LinkModel::parse)
        LoadingResult.success(
            artistInference.inferArtist(
                input = ArtistInference.Input(
                    name = it.name,
                    socialLinks = linkModels.filter {
                        it.type.category == LinkCategory.SOCIALS || it.type.category == LinkCategory.SUPPORT
                    },
                    storeLinks = linkModels.filter { it.type.category == LinkCategory.STORES },
                    portfolioLinks = linkModels.filter { it.type.category == LinkCategory.PORTFOLIOS },
                    catalogLinks = emptyList()
                ),
                includePendingDataYears = true,
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, LoadingResult.loading())

    // Not saved since this is supposed to be read-only
    internal val entryInfo =
        combine(previousEntry, currentEntry, confirmedArtistId, ::Triple)
            .map { (previousEntry, currentEntry, confirmedArtistId) ->
                val artistId =
                    currentEntry?.confirmedId ?: previousEntry?.confirmedId ?: confirmedArtistId
                if (currentEntry == null) {
                    ArtistFormState(Uuid.random()) to null
                } else {
                    val artist = artistId?.let { database.loadArtist(dataYear, it) }
                    val entryInfo = RemoteArtistDataMergeScreen.EntryInfo(
                        artistId = artistId,
                        artist = artist,
                        previousEntry = previousEntry,
                        diff = RemoteArtistDataDiff.diff(
                            artist = artist,
                            previousEntry = previousEntry,
                            currentEntry = currentEntry,
                        )
                    )
                    if (artist == null) {
                        ArtistFormState(artistId ?: Uuid.random()) to entryInfo
                    } else {
                        ArtistFormState(Uuid.parse(artist.id))
                            .applyDatabaseEntry(
                                artist = artist,
                                seriesById = tagAutocomplete.seriesById.first(),
                                merchById = tagAutocomplete.merchById.first(),
                                mergeBehavior = FormMergeBehavior.REPLACE,
                            ) to entryInfo
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    val saveTaskState get() = saveTask.state

    fun seriesImage(info: SeriesInfo) = seriesImageLoader.getSeriesImage(info)

    fun onConfirmArtist(artistId: Uuid?) {
        confirmedArtistId.value = artistId
    }

    fun onClickSave(capturedState: ArtistFormState.CapturedState, openArtistEditAfter: Boolean) {
        val entry = currentEntry.value ?: return
        val initial = entryInfo.value?.second?.artist
        saveTask.triggerManual {
            SaveData(
                capturedState = capturedState,
                initial = initial,
                entry = entry,
                openArtistEditAfter = openArtistEditAfter,
            )
        }
    }

    private suspend fun save(data: SaveData) =
        withContext(dispatchers.io) {
            database.saveRemoteArtistData(
                dataYear = dataYear,
                initial = data.initial,
                updated = data.capturedState.artist,
                entry = data.entry,
                isHistory = false,
            ) to data.capturedState.artist.id.takeIf { data.openArtistEditAfter }?.let(Uuid::parse)
        }

    private data class SaveData(
        val capturedState: ArtistFormState.CapturedState,
        val initial: ArtistDatabaseEntry.Impl?,
        val entry: ArtistRemoteEntry,
        val openArtistEditAfter: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            id: ArtistRemoteEntry.Id,
            savedStateHandle: SavedStateHandle,
        ): RemoteArtistDataMergeViewModel
    }
}
