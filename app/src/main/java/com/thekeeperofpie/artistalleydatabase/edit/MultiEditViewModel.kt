package com.thekeeperofpie.artistalleydatabase.edit

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
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
    aniListApi: AniListApi,
    mediaRepository: MediaRepository,
    characterRepository: CharacterRepository,
    appMoshi: AppMoshi,
) : ArtEntryDetailsViewModel(
    application,
    artEntryEditDao,
    aniListApi,
    mediaRepository,
    characterRepository,
    appMoshi
) {

    private lateinit var entryIds: List<String>

    val imageUris = mutableStateListOf<Either<File, Uri?>>()

    var loading = true
        private set

    private var saving = false

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
            val differentValue = listOf(
                // TODO: String resource
                ArtEntrySection.MultiText.Entry.Custom("Different", id = "Different")
            )

            val series = firstEntry.series
                .takeIf { artEntryEditDao.distinctCountSeries(entryIds).also {
                    Log.d("Debug", "distinct count series = $it")
                } == 1 }
                ?.map(::databaseToSeriesEntry)
                ?: differentValue

            val characters = firstEntry.characters
                .takeIf { artEntryEditDao.distinctCountCharacters(entryIds) == 1 }
                ?.map(::databaseToCharacterEntry)
                ?: differentValue

            val sourceValue = firstEntry.sourceValue
                ?.takeIf { artEntryEditDao.distinctCountSourceValue(entryIds) == 1 }
                ?: "Different"

            // TODO: Fix source multi-edit
            var sourceType = firstEntry.sourceType
                ?.takeIf { artEntryEditDao.distinctCountSourceType(entryIds) == 1 }
                ?: "Different"
            if (sourceValue == "Different") {
                sourceType = "Different"
            }

            val artists = firstEntry.artists
                .takeIf { artEntryEditDao.distinctCountArtists(entryIds) == 1 }
                ?.map(ArtEntrySection.MultiText.Entry::Custom)
                ?: differentValue

            val tags = firstEntry.tags
                .takeIf { artEntryEditDao.distinctCountTags(entryIds) == 1 }
                ?.map(ArtEntrySection.MultiText.Entry::Custom)
                ?: differentValue

            val printWidth = firstEntry.printWidth
                ?.takeIf { artEntryEditDao.distinctCountPrintWidth(entryIds) == 1 }

            val printHeight = firstEntry.printHeight
                ?.takeIf { artEntryEditDao.distinctCountPrintHeight(entryIds) == 1 }

            val notes = firstEntry.notes
                ?.takeIf { artEntryEditDao.distinctCountNotes(entryIds) == 1 }
                ?: "Different"

            val model = ArtEntryModel(
                artists = artists,
                series = series,
                characters = characters,
                tags = tags,
                sourceType = sourceType,
                sourceValue = sourceValue,
                printWidth = printWidth,
                printHeight = printHeight,
                notes = notes,
                locks = ArtEntry.Locks.EMPTY,
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

        val series = seriesSection.finalContents().filterNot {
            it is ArtEntrySection.MultiText.Entry.Custom && it.id == "Different"
        }
        val characters = characterSection.finalContents().filterNot {
            it is ArtEntrySection.MultiText.Entry.Custom && it.id == "Different"
        }
        val tags = tagSection.finalContents().filterNot {
            it is ArtEntrySection.MultiText.Entry.Custom && it.id == "Different"
        }
        val artists = artistSection.finalContents().filterNot {
            it is ArtEntrySection.MultiText.Entry.Custom && it.id == "Different"
        }
//        val (sourceType, sourceValue) = sourceSection.finalTypeToValue()
        val printWidth = printSizeSection.finalWidth()
        val printHeight = printSizeSection.finalHeight()
        val notes = notesSection.value

        val newImages = imageUris.mapIndexedNotNull { index, either ->
            if (either is Either.Right) {
                index to either.right
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

            if (series.isNotEmpty()) {
                artEntryEditDao.updateSeries(
                    entryIds,
                    series.map { it.serializedValue },
                    series.map { it.searchableValue },
                )
            }

            if (characters.isNotEmpty()) {
                artEntryEditDao.updateCharacters(
                    entryIds,
                    characters.map { it.serializedValue },
                    characters.map { it.searchableValue },
                )
            }

            // TODO: Source handling
//            if (sourceType.isNotEmpty() && sourceValue.isNotEmpty()) {
//
//            }

            if (artists.isNotEmpty()) {
                artEntryEditDao.updateArtists(entryIds, artists.map { it.serializedValue })
            }

            if (tags.isNotEmpty()) {
                artEntryEditDao.updateTags(entryIds, tags.map { it.serializedValue })
            }

            if (printWidth != null || printHeight != null) {
                artEntryEditDao.updatePrintSize(entryIds, printWidth, printHeight)
            }

            if (notes.isNotEmpty() && notes.trim() != "Different") {
                artEntryEditDao.updateNotes(entryIds, notes)
            }

            artEntryEditDao.updateLastEditTime(entryIds, Date.from(Instant.now()))

            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }
}