package com.thekeeperofpie.artistalleydatabase.art.details

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.anilist.AniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.art.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.art.SourceDropdown
import com.thekeeperofpie.artistalleydatabase.art.SourceType
import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.autocomplete.Autocompleter
import com.thekeeperofpie.artistalleydatabase.json.AppJson
import com.thekeeperofpie.artistalleydatabase.json.AppMoshi
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.split
import com.thekeeperofpie.artistalleydatabase.utils.start
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import java.time.Instant
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ArtEntryDetailsViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao,
    private val aniListApi: AniListApi,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val appMoshi: AppMoshi,
    protected val appJson: AppJson,
    private val autocompleter: Autocompleter,
    private val dataConverter: ArtEntryDataConverter,
) : ViewModel() {

    protected val seriesSection = ArtEntrySection.MultiText(
        R.string.art_entry_series_header_zero,
        R.string.art_entry_series_header_one,
        R.string.art_entry_series_header_many,
        lockState = ArtEntrySection.LockState.UNLOCKED,
    )

    protected val characterSection = ArtEntrySection.MultiText(
        R.string.art_entry_characters_header_zero,
        R.string.art_entry_characters_header_one,
        R.string.art_entry_characters_header_many,
        lockState = ArtEntrySection.LockState.UNLOCKED,
    )

    protected val sourceSection = SourceDropdown(locked = ArtEntrySection.LockState.UNLOCKED)

    protected val artistSection = ArtEntrySection.MultiText(
        R.string.art_entry_artists_header_zero,
        R.string.art_entry_artists_header_one,
        R.string.art_entry_artists_header_many,
        lockState = ArtEntrySection.LockState.UNLOCKED,
    )
    protected val tagSection = ArtEntrySection.MultiText(
        R.string.art_entry_tags_header_zero,
        R.string.art_entry_tags_header_one,
        R.string.art_entry_tags_header_many,
        lockState = ArtEntrySection.LockState.UNLOCKED,
    )

    protected val printSizeSection = PrintSizeDropdown()

    protected val notesSection = ArtEntrySection.LongText(
        headerRes = R.string.art_entry_notes_header,
        lockState = ArtEntrySection.LockState.UNLOCKED
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
            subscribeMultiTextSection(
                section = artistSection,
                localCall = {
                    artEntryDao.queryArtists(it)
                        .map(Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                })
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(
                seriesSection,
                localCall = autocompleter::querySeriesLocal,
                networkCall = autocompleter::querySeriesNetwork
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                seriesSection.contentUpdates()
                    .map { it.filterIsInstance<Entry.Prefilled>() }
                    .map { it.map { it.id } }
                    .distinctUntilChanged()
                    .flatMapLatest {
                        it.mapNotNull(String::toIntOrNull)
                            .map {
                                aniListApi.charactersByMedia(it)
                                    .map { it.map { dataConverter.characterEntry((it)) } }
                                    .start(emptyList())
                            }
                            .let {
                                combine(it) {
                                    it.fold(mutableListOf<Entry>()) { list, value ->
                                        list.apply { addAll(value) }
                                    }
                                }
                            }
                    }
                    .start(emptyList()),
                characterSection.valueUpdates()
                    .flatMapLatest { query ->
                        autocompleter.queryCharacters(query)
                            .map { query to it }
                    }
            ) { series, (query, charactersPair) ->
                val (charactersFirst, charactersSecond) = charactersPair
                val (seriesFirst, seriesSecond) = series.toMutableList().apply {
                    removeAll { seriesCharacter ->
                        charactersFirst.any { character ->
                            val seriesCharacterEntry = seriesCharacter as? Entry.Prefilled
                            if (seriesCharacterEntry != null) {
                                seriesCharacterEntry.id == (character as? Entry.Prefilled)?.id
                            } else {
                                false
                            }
                        } || charactersSecond.any { character ->
                            val seriesCharacterEntry = seriesCharacter as? Entry.Prefilled
                            if (seriesCharacterEntry != null) {
                                seriesCharacterEntry.id == (character as? Entry.Prefilled)?.id
                            } else {
                                false
                            }
                        }
                    }
                }
                    .split { it.text.contains(query) }
                charactersFirst + seriesFirst + charactersSecond + seriesSecond
            }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        characterSection.predictions = it.toMutableList()
                    }
                }
        }
        viewModelScope.launch(Dispatchers.IO) {
            subscribeMultiTextSection(
                section = tagSection,
                localCall = {
                    artEntryDao.queryTags(it)
                        .map(Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                })
        }

        viewModelScope.launch(Dispatchers.IO) {
            combine(
                artistSection.contentUpdates(),
                sourceSection.conventionSectionItem.updates(),
                ::Pair
            )
                .filter {
                    it.second.name.isNotEmpty()
                            && it.second.year != null && it.second.year!! > 1000
                            && it.second.hall.isEmpty() && it.second.booth.isEmpty()
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
                            .let { appJson.json.decodeFromString<SourceType.Convention>(it!!) }
                            .takeIf { it.name == convention.name && it.year == convention.year }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun subscribeMultiTextSection(
        section: ArtEntrySection.MultiText,
        localCall: suspend (String) -> List<Flow<Entry?>>,
        networkCall: suspend (query: String) -> Flow<List<Entry>> = {
            flowOf(emptyList())
        },
    ) {
        section.valueUpdates()
            .flatMapLatest { query ->
                val database = combine(localCall(query)) { it.toList() }
                val aniList = if (query.isBlank()) flowOf(emptyList()) else networkCall(query)
                combine(database, aniList) { local, network ->
                    local.filterNotNull().toMutableList().apply {
                        removeIf { source ->
                            network.any { target -> target.text.trim() == source.text.trim() }
                        }
                    } + network
                }
            }
            .collectLatest {
                withContext(Dispatchers.Main) {
                    section.predictions = it.toMutableList()
                }
            }
    }

    protected fun databaseToSeriesEntry(value: String) =
        when (val either = appMoshi.parseSeriesColumn(value)) {
            is Either.Right -> dataConverter.seriesEntry(either.value)
            is Either.Left -> Entry.Custom(either.value)
        }

    protected fun databaseToCharacterEntry(value: String) =
        when (val either = appMoshi.parseCharacterColumn(value)) {
            is Either.Right -> dataConverter.characterEntry(either.value)
            is Either.Left -> Entry.Custom(either.value)
        }

    protected fun buildModel(entry: ArtEntry): ArtEntryModel {
        val artists = entry.artists.map(Entry::Custom)
        val series = entry.series.map(::databaseToSeriesEntry)
        val characters = entry.characters.map(::databaseToCharacterEntry)
        val tags = entry.tags.map(Entry::Custom)

        return ArtEntryModel(
            entry = entry,
            artists = artists,
            series = series,
            characters = characters,
            tags = tags,
            sourceType = SourceType.fromEntry(appJson.json, entry)
        )
    }

    protected fun initializeForm(entry: ArtEntryModel) {
        artistSection.setContents(entry.artists)
        artistSection.lockState = entry.artistsLocked

        sourceSection.initialize(appJson.json, entry)
        sourceSection.lockState = entry.sourceLocked

        seriesSection.setContents(entry.series)
        seriesSection.lockState = entry.seriesLocked

        characterSection.setContents(entry.characters)
        characterSection.lockState = entry.charactersLocked

        printSizeSection.initialize(entry.printWidth, entry.printHeight)
        printSizeSection.lockState = entry.printSizeLocked

        tagSection.setContents(entry.tags)
        tagSection.lockState = entry.tagsLocked

        notesSection.value = entry.notes.orEmpty()
        notesSection.lockState = entry.notesLocked

        entry.characters.filterIsInstance<Entry.Prefilled>()
            .forEach {
                val characterId = it.id.toInt()
                viewModelScope.launch(Dispatchers.Main) {
                    characterRepository.getEntry(characterId)
                        .filterNotNull()
                        .flatMapLatest { character ->
                            // TODO: Batch query?
                            character.mediaIds
                                ?.map { mediaRepository.getEntry(it) }
                                ?.let { combine(it) { it.toList() } }
                                .let { it ?: flowOf(listOf(null)) }
                                .map { character to it.filterNotNull() }
                        }
                        .map { dataConverter.characterEntry(it.first, it.second) }
                        .filterNotNull()
                        .flowOn(Dispatchers.IO)
                        .collectLatest { newEntry ->
                            characterSection.replaceContents { entry ->
                                if (entry is Entry.Prefilled &&
                                    entry.id == characterId.toString()
                                ) newEntry else entry
                            }
                        }
                }
            }

        entry.series.filterIsInstance<Entry.Prefilled>()
            .forEach {
                val mediaId = it.id.toInt()
                viewModelScope.launch(Dispatchers.Main) {
                    mediaRepository.getEntry(mediaId)
                        .filterNotNull()
                        .map(dataConverter::seriesEntry)
                        .flowOn(Dispatchers.IO)
                        .collectLatest { newEntry ->
                            seriesSection.replaceContents { entry ->
                                if (entry is Entry.Prefilled &&
                                    entry.id == mediaId.toString()
                                ) newEntry else entry
                            }
                        }
                }
            }
    }

    protected suspend fun makeEntry(imageUri: Uri?, id: String): ArtEntry? {
        val outputFile = ArtEntryUtils.getImageFile(application, id)
        val error = ArtEntryUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ArtEntryUtils.getImageSize(outputFile)
        val sourceItem = sourceSection.selectedItem().toSource()

        return ArtEntry(
            id = id,
            artists = artistSection.finalContents().map { it.serializedValue },
            sourceType = sourceItem.serializedType,
            sourceValue = sourceItem.serializedValue(appJson.json),
            series = seriesSection.finalContents().map { it.serializedValue },
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            characters = characterSection.finalContents().map { it.serializedValue },
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
                artistsLocked = artistSection.lockState?.toSerializedValue() ?: false,
                seriesLocked = seriesSection.lockState?.toSerializedValue() ?: false,
                charactersLocked = characterSection.lockState?.toSerializedValue() ?: false,
                sourceLocked = sourceSection.lockState?.toSerializedValue() ?: false,
                tagsLocked = tagSection.lockState?.toSerializedValue() ?: false,
                notesLocked = notesSection.lockState?.toSerializedValue() ?: false,
                printSizeLocked = printSizeSection.lockState?.toSerializedValue() ?: false,
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String) {
        val entry = makeEntry(imageUri, id) ?: return
        entry.series
            .map { appMoshi.parseSeriesColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(mediaRepository::ensureSaved)
        entry.characters
            .map { appMoshi.parseCharacterColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(characterRepository::ensureSaved)
        artEntryDao.insertEntries(entry)
    }
}