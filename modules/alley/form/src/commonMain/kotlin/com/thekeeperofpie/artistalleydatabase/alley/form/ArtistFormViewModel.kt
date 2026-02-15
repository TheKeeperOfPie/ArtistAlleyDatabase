package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.form.ArtistFormAccessKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceField
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyFormDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.tags.FormTagAutocomplete
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.ExclusiveTask
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormViewModel(
    artistInference: ArtistInference,
    dispatchers: CustomDispatchers,
    editDatabase: AlleyEditDatabase,
    private val formDatabase: AlleyFormDatabase,
    seriesImagesStore: SeriesImagesStore,
    val tagAutocomplete: FormTagAutocomplete,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val saveTask = ExclusiveTask(viewModelScope, ::save)
    private var progress = savedStateHandle.getMutableStateFlow(
        json = Json,
        key = "progress",
    ) { ArtistFormScreen.State.Progress.LOADING }
    private val artist =
        savedStateHandle.getMutableStateFlow<ArtistDatabaseEntry.Impl?>("artist", null)
    private val rallies =
        savedStateHandle.getMutableStateFlow<List<StampRallyDatabaseEntry>>("rallies", emptyList())
    private val initialFormDiff =
        savedStateHandle.getMutableStateFlow<ArtistEntryDiff?>("initialFormDiff", null)
    private val previousYearData =
        artist.mapLatestNotNull { it?.id?.let(Uuid::parseOrNull) }
            .mapLatest {
                // These are used when merging, wait for them to be available before offering merge
                tagAutocomplete.seriesById.first()
                tagAutocomplete.merchById.first()
                artistInference.getPreviousYearData(it)
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val state = ArtistFormScreen.State(
        initialArtist = artist,
        initialRallies = rallies,
        previousYearData = previousYearData,
        progress = progress,
        stampRallyStates = savedStateHandle.saveable(
            key = "stampRallyStates",
            saver = StateUtils.SnapshotListSaver(StampRallyFormState.Saver),
        ) {
            SnapshotStateList<StampRallyFormState>()
        },
        formState = savedStateHandle.saveable(
            key = "formState",
            saver = ArtistFormScreen.State.FormState.Saver,
        ) {
            ArtistFormScreen.State.FormState()
        },
        initialFormDiff = initialFormDiff,
        saveTaskState = saveTask.state,
    )

    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    private val artistJob = ExclusiveProgressJob(viewModelScope, ::loadArtistInfo)

    fun initialize() {
        if (progress.value != ArtistFormScreen.State.Progress.LOADED) {
            artistJob.launch()
        }
    }

    private suspend fun loadArtistInfo() = try {
        withContext(PlatformDispatchers.IO) {
            val response = formDatabase.loadArtist(dataYear)
            if (response == null) {
                progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
                return@withContext
            }
            val baseArtist = response.artist
            this@ArtistFormViewModel.artist.value = baseArtist
            this@ArtistFormViewModel.rallies.value = response.stampRallies

            fun applyDiff(
                base: List<String>,
                diff: HistoryListDiff?,
            ): List<String> {
                val base = base.toMutableSet()
                base.removeAll(diff?.deleted.orEmpty().toSet())
                base.addAll(diff?.added.orEmpty().toSet())
                return base.toMutableList()
            }

            val artistFormDiff = response.artistFormDiff
            val artist = if (artistFormDiff == null) {
                baseArtist
            } else {
                baseArtist.copy(
                    booth = artistFormDiff.booth ?: baseArtist.booth,
                    name = artistFormDiff.name ?: baseArtist.name,
                    summary = artistFormDiff.summary ?: baseArtist.summary,
                    socialLinks = applyDiff(baseArtist.socialLinks, artistFormDiff.socialLinks),
                    storeLinks = applyDiff(baseArtist.storeLinks, artistFormDiff.storeLinks),
                    portfolioLinks = applyDiff(
                        baseArtist.portfolioLinks,
                        artistFormDiff.portfolioLinks
                    ),
                    catalogLinks = applyDiff(baseArtist.catalogLinks, artistFormDiff.catalogLinks),
                    notes = artistFormDiff.notes ?: baseArtist.notes,
                    commissions = applyDiff(baseArtist.commissions, artistFormDiff.commissions),
                    seriesInferred = applyDiff(
                        baseArtist.seriesInferred,
                        artistFormDiff.seriesInferred
                    ),
                    seriesConfirmed =
                        applyDiff(baseArtist.seriesConfirmed, artistFormDiff.seriesConfirmed),
                    merchInferred = applyDiff(
                        baseArtist.merchInferred,
                        artistFormDiff.merchInferred
                    ),
                    merchConfirmed = applyDiff(
                        baseArtist.merchConfirmed,
                        artistFormDiff.merchConfirmed
                    ),
                )
            }

            state.applyDatabaseEntry(
                artist = artist,
                seriesById = tagAutocomplete.seriesById.first(),
                merchById = tagAutocomplete.merchById.first(),
                mergeBehavior = FormMergeBehavior.REPLACE,
            )

            val existingStampRallyStates = state.stampRallyStates.toList()
            val emptyStampRallyDatabaseEntry by lazy {
                StampRallyFormState("").captureDatabaseEntry(dataYear).second
            }
            val newStampRallyStates =
                (response.stampRallies.map { it.id } + response.stampRallyFormDiffs.map { it.id })
                    .distinct()
                    .map { stampRallyId ->
                        response.stampRallies.find { it.id == stampRallyId }
                            ?: emptyStampRallyDatabaseEntry.copy(id = stampRallyId)
                    }
                    .map { baseStampRally ->
                        val existingState =
                            existingStampRallyStates.find { it.editorState.id.value.text.toString() == baseStampRally.id }
                        val baseState = existingState ?: StampRallyFormState(baseStampRally.id)
                        val stampRallyFormDiff =
                            response.stampRallyFormDiffs.find { it.id == baseStampRally.id }
                        val stampRally = if (stampRallyFormDiff == null) {
                            baseStampRally
                        } else {
                            // TODO: hostTable isn't handled, remove in favor of index 0?
                            val tables = applyDiff(baseStampRally.tables, stampRallyFormDiff.tables)
                            baseStampRally.copy(
                                fandom = stampRallyFormDiff.fandom ?: baseStampRally.fandom,
                                hostTable = tables.firstOrNull().orEmpty(),
                                tables = tables,
                                links = applyDiff(baseStampRally.links, stampRallyFormDiff.links),
                                tableMin = stampRallyFormDiff.tableMin ?: baseStampRally.tableMin,
                                prize = stampRallyFormDiff.prize ?: baseStampRally.prize,
                                prizeLimit = stampRallyFormDiff.prizeLimit
                                    ?: baseStampRally.prizeLimit,
                                series = applyDiff(
                                    baseStampRally.series,
                                    stampRallyFormDiff.series
                                ),
                                merch = applyDiff(baseStampRally.merch, stampRallyFormDiff.merch),
                            )
                        }
                        baseState.applyDatabaseEntry(
                            stampRally = stampRally,
                            seriesById = tagAutocomplete.seriesById.first(),
                            merchById = tagAutocomplete.merchById.first(),
                            mergeBehavior = FormMergeBehavior.REPLACE,
                        )
                        baseState.editorState.deleted = stampRallyFormDiff?.deleted == true
                        baseState
                    }
            state.stampRallyStates.replaceAll(newStampRallyStates)

            artistFormDiff?.formNotes?.let {
                state.formState.formNotes.value.setTextAndPlaceCursorAtEnd(it)
                state.formState.formNotes.lockState = EntryLockState.LOCKED
            }
            initialFormDiff.value = artistFormDiff

            // TODO: Support images?
            progress.value = ArtistFormScreen.State.Progress.LOADED
        }
    } catch (_: Throwable) {
        // Differentiate between 403 and generic error
        progress.value = ArtistFormScreen.State.Progress.BAD_AUTH
    }

    fun seriesPredictions(query: String) = tagAutocomplete.seriesPredictions(query)
    fun merchPredictions(query: String) = tagAutocomplete.merchPredictions(query)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickDone() {
        val artist = artist.value ?: return
        saveTask.triggerManual {
            val (images, artist) = state.captureDatabaseEntry(artist)
            val stampRallyEntries = state.stampRallyStates.toList()
                .map { it.captureDatabaseEntry(dataYear).second }
            CapturedState(
                images = images,
                artist = artist,
                stampRallyEntries = stampRallyEntries,
                deletedRallyIds = state.stampRallyStates.filter { it.editorState.deleted }
                    .map { it.editorState.id.value.text.toString() },
                formNotes = state.formState.formNotes.value.text.toString(),
            )
        }
    }

    fun onConfirmMerge(fieldState: Map<ArtistInferenceField, Boolean>) {
        if (fieldState.isEmpty()) return
        val artist = artist.value ?: return
        val previousYearData = previousYearData.value ?: return
        val seriesById =
            tagAutocomplete.seriesById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return
        val merchById =
            tagAutocomplete.merchById.replayCache.firstOrNull()?.takeIf { it.isNotEmpty() }
                ?: return

        val formEntry = state.captureDatabaseEntry(artist).second
        val mergeEntry = ArtistInferenceUtils.mergeEntry(
            formEntry = formEntry,
            previousYearData = previousYearData,
            fieldState = fieldState,
        )
        state.applyDatabaseEntry(
            artist = mergeEntry,
            seriesById = seriesById,
            merchById = merchById,
            mergeBehavior = FormMergeBehavior.APPEND,
        )
    }

    fun onSubmitPrivateKey(privateKey: String) {
        progress.value = ArtistFormScreen.State.Progress.LOADING
        ArtistFormAccessKey.setKey(privateKey)
        artistJob.launch()
    }

    fun onClickEditAgain() {
        progress.value = ArtistFormScreen.State.Progress.LOADING
        artistJob.launch()
    }

    private suspend fun save(data: CapturedState): BackendFormRequest.ArtistSave.Response =
        // TODO: Image support
        formDatabase.saveArtist(
            dataYear = dataYear,
            beforeArtist = artist.value!!,
            afterArtist = data.artist,
            beforeStampRallies = rallies.value,
            afterStampRallies = data.stampRallyEntries,
            deletedRallyIds = data.deletedRallyIds,
            formNotes = data.formNotes,
        ).also {
            progress.value = ArtistFormScreen.State.Progress.DONE
        }

    data class CapturedState(
        val images: List<EditImage>,
        val artist: ArtistDatabaseEntry.Impl,
        val stampRallyEntries: List<StampRallyDatabaseEntry>,
        val deletedRallyIds: List<String>,
        val formNotes: String,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormViewModel
    }
}
