package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.withLatestFrom
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
import com.thekeeperofpie.artistalleydatabase.form.EntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.form.EntryId
import com.thekeeperofpie.artistalleydatabase.form.EntryImageController
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.form.EntrySettings
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
open class ArtEntryDetailsViewModel @Inject constructor(
    application: Application,
    protected val appJson: AppJson,
    protected val artEntryDao: ArtEntryDetailsDao,
    private val dataConverter: DataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val aniListAutocompleter: AniListAutocompleter,
    private val artSettings: ArtSettings,
    entrySettings: EntrySettings,
) : EntryDetailsViewModel<ArtEntry, ArtEntryModel>(
    application,
    ArtEntryUtils.SCOPED_ID_TYPE,
    R.string.art_entry_image_content_description,
    entrySettings,
) {

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

    override suspend fun buildAddModel() = artSettings.loadArtEntryTemplate()?.let(::buildModel)

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        buildModel(artEntryDao.getEntry(entryId.valueId))

    override suspend fun buildMultiEditModel(): ArtEntryModel {
        val firstEntry = artEntryDao.getEntry(entryIds.first().valueId)
        val differentValue = listOf(Entry.Different)

        val entryValueIds = entryIds.map { it.valueId }
        val series = artEntryDao.distinctCountSeries(entryValueIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.series(appJson) }
            ?.let(dataConverter::seriesEntries)
            ?: differentValue

        val characters = artEntryDao.distinctCountCharacters(entryValueIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.characters(appJson) }
            ?.let(dataConverter::characterEntries)
            ?: differentValue

        val sourceTypeSame = artEntryDao.distinctCountSourceType(entryValueIds) == 1
        val sourceValueSame = artEntryDao.distinctCountSourceValue(entryValueIds) == 1

        val source = if (sourceTypeSame && sourceValueSame) {
            SourceType.fromEntry(appJson.json, firstEntry)
        } else {
            SourceType.Different
        }

        val artists = firstEntry.artists
            .takeIf { artEntryDao.distinctCountArtists(entryValueIds) == 1 }
            ?.map(Entry::Custom)
            ?: differentValue

        val tags = firstEntry.tags
            .takeIf { artEntryDao.distinctCountTags(entryValueIds) == 1 }
            ?.map(Entry::Custom)
            ?: differentValue

        val printWidth = firstEntry.printWidth
            ?.takeIf { artEntryDao.distinctCountPrintWidth(entryValueIds) == 1 }

        val printHeight = firstEntry.printHeight
            ?.takeIf { artEntryDao.distinctCountPrintHeight(entryValueIds) == 1 }

        val notes = firstEntry.notes
            ?.takeIf { artEntryDao.distinctCountNotes(entryValueIds) == 1 }
            ?: "Different"

        val artistsLocked = firstEntry.locks.artistsLocked
            .takeIf { artEntryDao.distinctCountArtistsLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val sourceLocked = firstEntry.locks.sourceLocked
            .takeIf { artEntryDao.distinctCountSourceLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val seriesLocked = firstEntry.locks.seriesLocked
            .takeIf { artEntryDao.distinctCountSeriesLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val charactersLocked = firstEntry.locks.charactersLocked
            .takeIf { artEntryDao.distinctCountCharactersLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val tagsLocked = firstEntry.locks.tagsLocked
            .takeIf { artEntryDao.distinctCountTagsLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val notesLocked = firstEntry.locks.notesLocked
            .takeIf { artEntryDao.distinctCountNotesLocked(entryValueIds) == 1 }
            ?.let(EntrySection.LockState::from)
            ?: EntrySection.LockState.DIFFERENT

        val printSizeLocked = firstEntry.locks.printSizeLocked
            .takeIf { artEntryDao.distinctCountPrintSizeLocked(entryValueIds) == 1 }
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

    override suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean
    ): Boolean {
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
                id = it.valueId,
                imageWidth = entryImage?.croppedWidth ?: entryImage?.width,
                imageHeight = entryImage?.croppedHeight ?: entryImage?.height
            )
        }
        artEntryDao.insertEntries(entries)
        return true
    }

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, EntryImageController.SaveResult>,
        skipIgnoreableErrors: Boolean
    ): Boolean {
        val series = seriesSection.finalContents()
        val characters = characterSection.finalContents()
        val tags = tagSection.finalContents()
        val artists = artistSection.finalContents()
        val sourceItem = sourceSection.selectedItem().toSource()

        val printWidth = printSizeSection.finalWidth()
        val printHeight = printSizeSection.finalHeight()
        val notes = notesSection.value

        val entryValueIds = entryIds.map { it.valueId }

        // TODO: Better communicate to user that "Different" value must be deleted,
        //  append is not currently supported.
        if (series.isNotEmpty()) {
            if (series.none { it is Entry.Different }) {
                artEntryDao.updateSeries(
                    entryValueIds,
                    series.map { it.serializedValue },
                    series.map { it.searchableValue },
                )
            }
        }

        if (characters.isNotEmpty()) {
            if (characters.none { it is Entry.Different }) {
                artEntryDao.updateCharacters(
                    entryValueIds,
                    characters.map { it.serializedValue },
                    characters.map { it.searchableValue },
                )
            }
        }

        if (sourceItem != SourceType.Different) {
            artEntryDao.updateSource(
                entryValueIds,
                sourceItem.serializedType,
                sourceItem.serializedValue(appJson.json)
            )
        }

        if (artists.isNotEmpty()) {
            if (artists.none { it is Entry.Different }) {
                artEntryDao.updateArtists(entryValueIds, artists.map { it.serializedValue })
            }
        }

        if (tags.isNotEmpty()) {
            if (tags.none { it is Entry.Different }) {
                artEntryDao.updateTags(entryValueIds, tags.map { it.serializedValue })
            }
        }

        if (artistSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateArtistsLocked(
                entryValueIds,
                artistSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (sourceSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSourceLocked(
                entryValueIds,
                sourceSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (seriesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSeriesLocked(
                entryValueIds,
                seriesSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (characterSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateCharactersLocked(
                entryValueIds,
                characterSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (tagSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateTagsLocked(
                entryValueIds,
                tagSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (notesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateNotesLocked(
                entryValueIds,
                notesSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real notes different value tracking
            if (notes.isNotEmpty() && notes.trim() != "Different") {
                artEntryDao.updateNotes(entryValueIds, notes)
            }
        }

        if (printSizeSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updatePrintSizeLocked(
                entryValueIds,
                printSizeSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real print size different value tracking
            if (printWidth != null || printHeight != null) {
                artEntryDao.updatePrintSize(entryValueIds, printWidth, printHeight)
            }
        }

        artEntryDao.updateLastEditTime(entryValueIds, Date.from(Instant.now()))
        return true
    }

    override suspend fun deleteEntry(entryId: EntryId) = artEntryDao.delete(entryId.valueId)

    override fun onImageSizeResult(widthToHeightRatio: Float) {
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

    override fun initializeForm(model: ArtEntryModel) {
        artistSection.setContents(model.artists, model.artistsLocked)
        sourceSection.initialize(model, model.sourceLocked)
        seriesSection.setContents(model.series, model.seriesLocked)
        characterSection.setContents(model.characters, model.charactersLocked)
        printSizeSection.initialize(model.printWidth, model.printHeight, model.printSizeLocked)
        tagSection.setContents(model.tags, model.tagsLocked)
        notesSection.setContents(model.notes, model.notesLocked)

        model.characters.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::characterId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(characterSection::replaceContent)
                }
            }

        model.series.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(seriesSection::replaceContent)
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
}