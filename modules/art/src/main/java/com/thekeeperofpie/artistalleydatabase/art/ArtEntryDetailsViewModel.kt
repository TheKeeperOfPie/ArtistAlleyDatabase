package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.withLatestFrom
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceDropdown
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.time.Instant
import java.util.Date
import javax.annotation.CheckReturnValue

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ArtEntryDetailsViewModel(
    protected val application: Application,
    protected val appJson: AppJson,
    protected val artEntryDao: ArtEntryDetailsDao,
    protected val dataConverter: DataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val aniListAutocompleter: AniListAutocompleter,
) : ViewModel() {

    companion object {
        private const val TAG = "ArtEntryDetailsViewModel"
    }

    protected val seriesSection = EntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val characterSection = EntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val sourceSection = SourceDropdown(locked = EntrySection.LockState.UNLOCKED)

    protected val artistSection = EntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val tagSection = EntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val printSizeSection = PrintSizeDropdown(lockState = EntrySection.LockState.UNLOCKED)

    protected val notesSection = EntrySection.LongText(
        headerRes = R.string.art_entry_notes_header,
        lockState = EntrySection.LockState.UNLOCKED
    )

    val sections = listOf(
        seriesSection,
        characterSection,
        sourceSection,
        artistSection,
        tagSection,
        printSizeSection,
        notesSection,
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    fun onImageSizeResult(width: Int, height: Int) {
        printSizeSection.onSizeChange(width, height)
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            artistSection.subscribePredictions(
                localCall = {
                    artEntryDao.queryArtists(it)
                        .map(Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            seriesSection.subscribePredictions(
                localCall = {
                    aniListAutocompleter.querySeriesLocal(it, artEntryDao::querySeries)
                },
                networkCall = aniListAutocompleter::querySeriesNetwork
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            aniListAutocompleter.characterPredictions(
                seriesSection.contentUpdates(),
                characterSection.valueUpdates(),
            ) { artEntryDao.queryCharacters(it) }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        characterSection.predictions = it.toMutableList()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            tagSection.subscribePredictions(
                localCall = {
                    artEntryDao.queryTags(it)
                        .map(Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            artistSection.contentUpdates()
                .withLatestFrom(
                    combine(
                        sourceSection.conventionSectionItem.updates(),
                        sourceSection.lockStateFlow,
                        ::Pair
                    )
                ) { artist, (convention, lock) -> Triple(artist, convention, lock) }
                .flatMapLatest {
                    // flatMapLatest to immediately drop request if lockState has changed
                    flowOf(it)
                        .filter { (_, _, lockState) ->
                            when (lockState) {
                                EntrySection.LockState.LOCKED -> false
                                EntrySection.LockState.UNLOCKED,
                                EntrySection.LockState.DIFFERENT,
                                null -> true
                            }
                        }
                        .filter { (_, convention, _) ->
                            convention.name.isNotEmpty()
                                    && convention.year != null && convention.year > 1000
                                    && (convention.hall.isEmpty() || convention.booth.isEmpty())
                        }
                        .mapNotNull { (artistEntries, convention) ->
                            artistEntries.firstNotNullOfOrNull {
                                artEntryDao
                                    .queryArtistForHallBooth(
                                        it.searchableValue,
                                        convention.name,
                                        convention.year!!
                                    )
                                    .takeUnless { it.isNullOrBlank() }
                                    ?.let<String, SourceType.Convention>(
                                        appJson.json::decodeFromString
                                    )
                                    ?.takeIf {
                                        it.name == convention.name && it.year == convention.year
                                    }
                            }
                        }
                }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        sourceSection.conventionSectionItem.updateHallBoothIfEmpty(
                            expectedName = it.name,
                            expectedYear = it.year!!,
                            newHall = it.hall,
                            newBooth = it.booth
                        )
                    }
                }
        }
    }

    protected fun buildModel(entry: ArtEntry): ArtEntryModel {
        val artists = entry.artists.map(Entry::Custom)
        val series = dataConverter.seriesEntries(entry.series(appJson))
        val characters = dataConverter.characterEntries(entry.characters(appJson))
        val tags = entry.tags.map(Entry::Custom)

        return ArtEntryModel(
            entry = entry,
            artists = artists,
            series = series,
            characters = characters,
            tags = tags,
            source = SourceType.fromEntry(appJson.json, entry)
        )
    }

    protected fun initializeForm(entry: ArtEntryModel) {
        artistSection.setContents(entry.artists, entry.artistsLocked)
        sourceSection.initialize(entry, entry.sourceLocked)
        seriesSection.setContents(entry.series, entry.seriesLocked)
        characterSection.setContents(entry.characters, entry.charactersLocked)
        printSizeSection.initialize(entry.printWidth, entry.printHeight, entry.printSizeLocked)
        tagSection.setContents(entry.tags, entry.tagsLocked)
        notesSection.setContents(entry.notes, entry.notesLocked)

        entry.characters.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::characterId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(characterSection::replaceContent)
                }
            }

        entry.series.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(seriesSection::replaceContent)
                }
            }
    }

    protected suspend fun makeEntry(imageUri: Uri?, id: String): ArtEntry? {
        val outputFile = ArtEntryUtils.getImageFile(application, id)
        val error = ImageUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            Log.e(TAG, "${application.getString(error.first)}: $imageUri", error.second)
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ImageUtils.getImageSize(outputFile)
        val sourceItem = sourceSection.selectedItem().toSource()

        return ArtEntry(
            id = id,
            artists = artistSection.finalContents().map { it.serializedValue },
            sourceType = sourceItem.serializedType,
            sourceValue = sourceItem.serializedValue(appJson.json),
            seriesSerialized = seriesSection.finalContents().map { it.serializedValue },
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            charactersSerialized = characterSection.finalContents().map { it.serializedValue },
            charactersSearchable = characterSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            tags = tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            printWidth = printSizeSection.finalWidth(),
            printHeight = printSizeSection.finalHeight(),
            notes = notesSection.value.trim(),
            locks = ArtEntry.Locks(
                artistsLocked = artistSection.lockState?.toSerializedValue(),
                seriesLocked = seriesSection.lockState?.toSerializedValue(),
                charactersLocked = characterSection.lockState?.toSerializedValue(),
                sourceLocked = sourceSection.lockState?.toSerializedValue(),
                tagsLocked = tagSection.lockState?.toSerializedValue(),
                notesLocked = notesSection.lockState?.toSerializedValue(),
                printSizeLocked = printSizeSection.lockState?.toSerializedValue(),
            )
        )
    }

    @CheckReturnValue
    suspend fun saveEntry(imageUri: Uri?, id: String): Boolean {
        val entry = makeEntry(imageUri, id) ?: return false
        entry.series(appJson)
            .filterIsInstance<Series.AniList>()
            .map { it.id }
            .let { mediaRepository.ensureSaved(it) }
            ?.let {
                errorResource = it
                return false
            }
        entry.characters(appJson)
            .filterIsInstance<Character.AniList>()
            .map { it.id }
            .let { characterRepository.ensureSaved(it) }
            ?.let {
                errorResource = it
                return false
            }
        artEntryDao.insertEntries(entry)
        return true
    }
}