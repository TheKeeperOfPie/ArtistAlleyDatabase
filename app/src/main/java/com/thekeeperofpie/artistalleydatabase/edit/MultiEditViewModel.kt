package com.thekeeperofpie.artistalleydatabase.edit

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.SourceType
import com.thekeeperofpie.artistalleydatabase.art.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDataConverter
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.json.ArtJson
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.Either
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MultiEditViewModel @Inject constructor(
    application: Application,
    private val artEntryEditDao: ArtEntryEditDao,
    aniListApi: com.thekeeperofpie.artistalleydatabase.anilist.AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    artJson: ArtJson,
    autocompleter: Autocompleter,
    dataConverter: ArtEntryDataConverter,
) : ArtEntryDetailsViewModel(
    application,
    artEntryEditDao,
    aniListApi,
    mediaRepository,
    characterRepository,
    artJson,
    autocompleter,
    dataConverter,
) {

    private lateinit var entryIds: List<String>

    val imageUris = mutableStateListOf<Either<File, Uri?>>()

    var loading = true
        private set

    var saving by mutableStateOf(false)
        private set

    fun initialize(entryIds: List<String>) {
        if (this::entryIds.isInitialized) return
        this.entryIds = entryIds

        imageUris.clear()
        imageUris.addAll(
            entryIds.map { ArtEntryUtils.getImageFile(application, it) }
                .map { Either.Left(it) }
        )

        viewModelScope.launch(Dispatchers.IO) {
            val firstEntry = artEntryEditDao.getEntry(entryIds.first())
            val differentValue = listOf(EntrySection.MultiText.Entry.Different)

            val series = firstEntry.series
                .takeIf { artEntryEditDao.distinctCountSeries(entryIds) == 1 }
                ?.map(dataConverter::databaseToSeriesEntry)
                ?: differentValue

            val characters = firstEntry.characters
                .takeIf { artEntryEditDao.distinctCountCharacters(entryIds) == 1 }
                ?.map(dataConverter::databaseToCharacterEntry)
                ?: differentValue

            val sourceTypeSame = artEntryEditDao.distinctCountSourceType(entryIds) == 1
            val sourceValueSame = artEntryEditDao.distinctCountSourceValue(entryIds) == 1

            val source = if (sourceTypeSame && sourceValueSame) {
                SourceType.fromEntry(artJson.json, firstEntry)
            } else {
                SourceType.Different
            }

            val artists = firstEntry.artists
                .takeIf { artEntryEditDao.distinctCountArtists(entryIds) == 1 }
                ?.map(EntrySection.MultiText.Entry::Custom)
                ?: differentValue

            val tags = firstEntry.tags
                .takeIf { artEntryEditDao.distinctCountTags(entryIds) == 1 }
                ?.map(EntrySection.MultiText.Entry::Custom)
                ?: differentValue

            val printWidth = firstEntry.printWidth
                ?.takeIf { artEntryEditDao.distinctCountPrintWidth(entryIds) == 1 }

            val printHeight = firstEntry.printHeight
                ?.takeIf { artEntryEditDao.distinctCountPrintHeight(entryIds) == 1 }

            val notes = firstEntry.notes
                ?.takeIf { artEntryEditDao.distinctCountNotes(entryIds) == 1 }
                ?: "Different"

            val artistsLocked = firstEntry.locks.artistsLocked
                .takeIf { artEntryEditDao.distinctCountArtistsLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val sourceLocked = firstEntry.locks.sourceLocked
                .takeIf { artEntryEditDao.distinctCountSourceLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val seriesLocked = firstEntry.locks.seriesLocked
                .takeIf { artEntryEditDao.distinctCountSeriesLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val charactersLocked = firstEntry.locks.charactersLocked
                .takeIf { artEntryEditDao.distinctCountCharactersLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val tagsLocked = firstEntry.locks.tagsLocked
                .takeIf { artEntryEditDao.distinctCountTagsLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val notesLocked = firstEntry.locks.notesLocked
                .takeIf { artEntryEditDao.distinctCountNotesLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val printSizeLocked = firstEntry.locks.printSizeLocked
                .takeIf { artEntryEditDao.distinctCountPrintSizeLocked(entryIds) == 1 }
                ?.let(EntrySection.LockState::from)
                ?: EntrySection.LockState.DIFFERENT

            val model = ArtEntryModel(
                artists = artists,
                series = series,
                characters = characters,
                tags = tags,
                source = source,
                printWidth = printWidth,
                printHeight = printHeight,
                notes = notes,
                artistsLocked = artistsLocked,
                sourceLocked = sourceLocked,
                seriesLocked = seriesLocked,
                charactersLocked = charactersLocked,
                tagsLocked = tagsLocked,
                notesLocked = notesLocked,
                printSizeLocked = printSizeLocked,
            )

            withContext(Dispatchers.Main) {
                initializeForm(model)
                loading = false
            }
        }
    }

    fun setImageUri(index: Int, uri: Uri?) {
        imageUris[index] = Either.Right(uri)
    }

    fun onClickSave(navHostController: NavController) {
        if (saving) return
        saving = true

        val series = seriesSection.finalContents()
        val characters = characterSection.finalContents()
        val tags = tagSection.finalContents()
        val artists = artistSection.finalContents()
        val sourceItem = sourceSection.selectedItem().toSource()

        val printWidth = printSizeSection.finalWidth()
        val printHeight = printSizeSection.finalHeight()
        val notes = notesSection.value

        val newImages = imageUris.mapIndexedNotNull { index, either ->
            if (either is Either.Right) {
                index to either.value
            } else null
        }.map { (index, uri) -> entryIds[index] to uri }

        viewModelScope.launch(Dispatchers.IO) {
            newImages.forEach { (entryId, uri) ->
                val outputFile = ArtEntryUtils.getImageFile(application, entryId)
                val error = ArtEntryUtils.writeEntryImage(application, outputFile, uri)
                if (error != null) {
                    withContext(Dispatchers.Main) {
                        errorResource = error
                    }
                    return@launch
                }
            }

            // TODO: Better communicate to user that "Different" value must be deleted,
            //  append is not currently supported.
            if (series.isNotEmpty()) {
                if (series.none { it is EntrySection.MultiText.Entry.Different }) {
                    artEntryEditDao.updateSeries(
                        entryIds,
                        series.map { it.serializedValue },
                        series.map { it.searchableValue },
                    )
                }
            }

            if (characters.isNotEmpty()) {
                if (characters.none { it is EntrySection.MultiText.Entry.Different }) {
                    artEntryEditDao.updateCharacters(
                        entryIds,
                        characters.map { it.serializedValue },
                        characters.map { it.searchableValue },
                    )
                }
            }

            if (sourceItem != SourceType.Different) {
                artEntryEditDao.updateSource(
                    entryIds,
                    sourceItem.serializedType,
                    sourceItem.serializedValue(artJson.json)
                )
            }

            if (artists.isNotEmpty()) {
                if (artists.none { it is EntrySection.MultiText.Entry.Different }) {
                    artEntryEditDao.updateArtists(entryIds, artists.map { it.serializedValue })
                }
            }

            if (tags.isNotEmpty()) {
                if (tags.none { it is EntrySection.MultiText.Entry.Different }) {
                    artEntryEditDao.updateTags(entryIds, tags.map { it.serializedValue })
                }
            }

            if (artistSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateArtistsLocked(
                    entryIds,
                    artistSection.lockState?.toSerializedValue() ?: false
                )
            }

            if (sourceSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateSourceLocked(
                    entryIds,
                    sourceSection.lockState?.toSerializedValue() ?: false
                )
            }

            if (seriesSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateSeriesLocked(
                    entryIds,
                    seriesSection.lockState?.toSerializedValue() ?: false
                )
            }

            if (characterSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateCharactersLocked(
                    entryIds,
                    characterSection.lockState?.toSerializedValue() ?: false
                )
            }

            if (tagSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateTagsLocked(
                    entryIds,
                    tagSection.lockState?.toSerializedValue() ?: false
                )
            }

            if (notesSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updateNotesLocked(
                    entryIds,
                    notesSection.lockState?.toSerializedValue() ?: false
                )

                // TODO: Real notes different value tracking
                if (notes.isNotEmpty() && notes.trim() != "Different") {
                    artEntryEditDao.updateNotes(entryIds, notes)
                }
            }

            if (printSizeSection.lockState != EntrySection.LockState.DIFFERENT) {
                artEntryEditDao.updatePrintSizeLocked(
                    entryIds,
                    printSizeSection.lockState?.toSerializedValue() ?: false
                )

                // TODO: Real print size different value tracking
                if (printWidth != null || printHeight != null) {
                    artEntryEditDao.updatePrintSize(entryIds, printWidth, printHeight)
                }
            }

            artEntryEditDao.updateLastEditTime(entryIds, Date.from(Instant.now()))

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}