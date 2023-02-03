package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.hoc081098.flowext.withLatestFrom
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.art.sections.PrintSizeDropdown
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceDropdown
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.form.EntryImageController
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.form.EntrySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class ArtEntryDetailsViewModel @Inject constructor(
    protected val application: Application,
    protected val appJson: AppJson,
    protected val artEntryDao: ArtEntryDetailsDao,
    private val dataConverter: DataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val aniListAutocompleter: AniListAutocompleter,
    private val artSettings: ArtSettings,
    entrySettings: EntrySettings,
) : ViewModel() {

    private enum class Type {
        ADD, SINGLE_EDIT, MULTI_EDIT
    }

    private lateinit var entryIds: List<String>
    private lateinit var type: Type

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

    var sectionsLoading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    private var deleting by mutableStateOf(false)

    val entryImageController = EntryImageController(
        scopeProvider = { viewModelScope },
        application = application,
        settings = entrySettings,
        entryTypeId = ArtEntryUtils.TYPE_ID,
        onError = { errorResource = it },
        imageContentDescriptionRes = R.string.art_entry_image_content_description,
        onImageSizeResult = { width, height -> onImageSizeResult(height / width.toFloat()) }
    )

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

    fun initialize(entryIds: List<String>) {
        if (this::entryIds.isInitialized) return
        this.entryIds = entryIds
        this.type = when (entryIds.size) {
            0 -> Type.ADD
            1 -> Type.SINGLE_EDIT
            else -> Type.MULTI_EDIT
        }

        viewModelScope.launch(Dispatchers.IO) {
            val model = when (type) {
                Type.ADD -> artSettings.loadArtEntryTemplate()?.let(::buildModel)
                Type.SINGLE_EDIT -> {
                    // TODO: Move this delay into the UI layer
                    // Delay to allow the shared element transition to finish
                    delay(
                        AnimationUtils.multipliedByAnimatorScale(application, 350L)
                            .coerceAtLeast(350L)
                    )
                    buildModel(artEntryDao.getEntry(entryIds.single()))
                }
                Type.MULTI_EDIT -> readMultiEditEntry()
            }
            withContext(Dispatchers.Main) {
                model?.run(::initializeForm)
                sectionsLoading = false
            }
        }

        entryImageController.initialize(entryIds)
    }

    private suspend fun readMultiEditEntry(): ArtEntryModel {
        val firstEntry = artEntryDao.getEntry(entryIds.first())
        val differentValue = listOf(Entry.Different)

        val series = artEntryDao.distinctCountSeries(entryIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.series(appJson) }
            ?.let(dataConverter::seriesEntries)
            ?: differentValue

        val characters = artEntryDao.distinctCountCharacters(entryIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.characters(appJson) }
            ?.let(dataConverter::characterEntries)
            ?: differentValue

        val sourceTypeSame = artEntryDao.distinctCountSourceType(entryIds) == 1
        val sourceValueSame = artEntryDao.distinctCountSourceValue(entryIds) == 1

        val source = if (sourceTypeSame && sourceValueSame) {
            SourceType.fromEntry(appJson.json, firstEntry)
        } else {
            SourceType.Different
        }

        val artists = firstEntry.artists
            .takeIf { artEntryDao.distinctCountArtists(entryIds) == 1 }
            ?.map(Entry::Custom)
            ?: differentValue

        val tags = firstEntry.tags
            .takeIf { artEntryDao.distinctCountTags(entryIds) == 1 }
            ?.map(Entry::Custom)
            ?: differentValue

        val printWidth = firstEntry.printWidth
            ?.takeIf { artEntryDao.distinctCountPrintWidth(entryIds) == 1 }

        val printHeight = firstEntry.printHeight
            ?.takeIf { artEntryDao.distinctCountPrintHeight(entryIds) == 1 }

        val notes = firstEntry.notes
            ?.takeIf { artEntryDao.distinctCountNotes(entryIds) == 1 }
            ?: "Different"

        val artistsLocked = firstEntry.locks.artistsLocked
            .takeIf { artEntryDao.distinctCountArtistsLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val sourceLocked = firstEntry.locks.sourceLocked
            .takeIf { artEntryDao.distinctCountSourceLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val seriesLocked = firstEntry.locks.seriesLocked
            .takeIf { artEntryDao.distinctCountSeriesLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val charactersLocked = firstEntry.locks.charactersLocked
            .takeIf { artEntryDao.distinctCountCharactersLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val tagsLocked = firstEntry.locks.tagsLocked
            .takeIf { artEntryDao.distinctCountTagsLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val notesLocked = firstEntry.locks.notesLocked
            .takeIf { artEntryDao.distinctCountNotesLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val printSizeLocked = firstEntry.locks.printSizeLocked
            .takeIf { artEntryDao.distinctCountPrintSizeLocked(entryIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        return ArtEntryModel(
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
    }

    // TODO: Read image ratio from EntryImage directly for section
    private fun onImageSizeResult(widthToHeightRatio: Float) {
        printSizeSection.onSizeChange(widthToHeightRatio)
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

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting || saving) return
        if (type != Type.SINGLE_EDIT) {
            // Don't delete from details page unless editing a single entry to avoid mistakes
            return
        }

        deleting = true

        viewModelScope.launch(Dispatchers.IO) {
            artEntryDao.delete(entryIds.single())
            withContext(Dispatchers.Main) {
                navHostController.popBackStack()
            }
        }
    }

    fun onClickSave(navHostController: NavHostController) {
        save(navHostController, skipIgnoreableErrors = false)
    }

    fun onLongClickSave(navHostController: NavHostController) {
        save(navHostController, skipIgnoreableErrors = true)
    }

    private fun save(navHostController: NavHostController, skipIgnoreableErrors: Boolean) {
        if (saving || deleting) return
        saving = true

        viewModelScope.launch(Dispatchers.IO) {
            val success = if (type == Type.MULTI_EDIT) {
                saveMultiEditEntry()
            } else {
                saveEntry(skipIgnoreableErrors = skipIgnoreableErrors)
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    navHostController.popBackStack()
                } else {
                    saving = false
                }
            }
        }
    }

    protected fun makeBaseEntry(): ArtEntry {
        val sourceItem = sourceSection.selectedItem().toSource()

        return ArtEntry(
            id = "",
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
            imageWidth = null,
            imageHeight = null,
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
    private suspend fun saveEntry(skipIgnoreableErrors: Boolean = false): Boolean {
        val saveImagesResult = entryImageController.saveImages() ?: return false

        val baseEntry = makeBaseEntry()
        baseEntry.series(appJson)
            .filterIsInstance<Series.AniList>()
            .map { it.id }
            .let { mediaRepository.ensureSaved(it) }
            ?.let {
                if (!skipIgnoreableErrors) {
                    errorResource = it
                    return false
                }
            }
        baseEntry.characters(appJson)
            .filterIsInstance<Character.AniList>()
            .map { it.id }
            .let { characterRepository.ensureSaved(it) }
            ?.let {
                if (!skipIgnoreableErrors) {
                    errorResource = it
                    return false
                }
            }

        val allEntryIds = (entryIds + saveImagesResult.keys).toSet()
        val entryImages = entryImageController.images.groupBy { it.entryId }
        val entries = allEntryIds.map {
            val entryImage = entryImages[it]?.firstOrNull()
            baseEntry.copy(
                id = it,
                imageWidth = entryImage?.croppedWidth ?: entryImage?.width,
                imageHeight = entryImage?.croppedHeight ?: entryImage?.height
            )
        }
        artEntryDao.insertEntries(entries)

        entryImageController.cleanUpImages(entryIds, saveImagesResult)

        return true
    }

    private suspend fun saveMultiEditEntry(): Boolean {
        val series = seriesSection.finalContents()
        val characters = characterSection.finalContents()
        val tags = tagSection.finalContents()
        val artists = artistSection.finalContents()
        val sourceItem = sourceSection.selectedItem().toSource()

        val printWidth = printSizeSection.finalWidth()
        val printHeight = printSizeSection.finalHeight()
        val notes = notesSection.value

        val saveImagesResult = entryImageController.saveImages() ?: return false

        // TODO: Better communicate to user that "Different" value must be deleted,
        //  append is not currently supported.
        if (series.isNotEmpty()) {
            if (series.none { it is Entry.Different }) {
                artEntryDao.updateSeries(
                    entryIds,
                    series.map { it.serializedValue },
                    series.map { it.searchableValue },
                )
            }
        }

        if (characters.isNotEmpty()) {
            if (characters.none { it is Entry.Different }) {
                artEntryDao.updateCharacters(
                    entryIds,
                    characters.map { it.serializedValue },
                    characters.map { it.searchableValue },
                )
            }
        }

        if (sourceItem != SourceType.Different) {
            artEntryDao.updateSource(
                entryIds,
                sourceItem.serializedType,
                sourceItem.serializedValue(appJson.json)
            )
        }

        if (artists.isNotEmpty()) {
            if (artists.none { it is Entry.Different }) {
                artEntryDao.updateArtists(entryIds, artists.map { it.serializedValue })
            }
        }

        if (tags.isNotEmpty()) {
            if (tags.none { it is Entry.Different }) {
                artEntryDao.updateTags(entryIds, tags.map { it.serializedValue })
            }
        }

        if (artistSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateArtistsLocked(
                entryIds,
                artistSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (sourceSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSourceLocked(
                entryIds,
                sourceSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (seriesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSeriesLocked(
                entryIds,
                seriesSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (characterSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateCharactersLocked(
                entryIds,
                characterSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (tagSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateTagsLocked(
                entryIds,
                tagSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (notesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateNotesLocked(
                entryIds,
                notesSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real notes different value tracking
            if (notes.isNotEmpty() && notes.trim() != "Different") {
                artEntryDao.updateNotes(entryIds, notes)
            }
        }

        if (printSizeSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updatePrintSizeLocked(
                entryIds,
                printSizeSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real print size different value tracking
            if (printWidth != null || printHeight != null) {
                artEntryDao.updatePrintSize(entryIds, printWidth, printHeight)
            }
        }

        artEntryDao.updateLastEditTime(entryIds, Date.from(Instant.now()))

        entryImageController.cleanUpImages(entryIds, saveImagesResult)
        return true
    }
}