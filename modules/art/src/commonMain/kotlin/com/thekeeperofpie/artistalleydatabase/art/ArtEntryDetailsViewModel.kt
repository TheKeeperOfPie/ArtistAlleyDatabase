package com.thekeeperofpie.artistalleydatabase.art

import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.art.generated.resources.Res
import artistalleydatabase.modules.art.generated.resources.art_template_saved
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryModel
import com.thekeeperofpie.artistalleydatabase.art.persistence.ArtSettings
import com.thekeeperofpie.artistalleydatabase.art.sections.ArtEntrySections
import com.thekeeperofpie.artistalleydatabase.art.sections.SourceType
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryImageController
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.image.crop.CropController
import com.thekeeperofpie.artistalleydatabase.image.crop.CropSettings
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Inject
class ArtEntryDetailsViewModel(
    appFileSystem: AppFileSystem,
    private val json: Json,
    private val artEntryDao: ArtEntryDetailsDao,
    private val dataConverter: DataConverter,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val aniListAutocompleter: AniListAutocompleter,
    private val artSettings: ArtSettings,
    settings: CropSettings,
    cropController: (CoroutineScope) -> CropController,
    customDispatchers: CustomDispatchers,
) : EntryDetailsViewModel<ArtEntry, ArtEntryModel>(
    entryClass = ArtEntry::class,
    appFileSystem = appFileSystem,
    scopedIdType = ArtEntryUtils.SCOPED_ID_TYPE,
    json = json,
    settings = settings,
    cropControllerFunction = cropController,
    customDispatchers = customDispatchers,
) {
    private val entrySections = ArtEntrySections()

    override val sections: List<EntrySection>
        get() = entrySections.sections

    init {
        entrySections.subscribeSectionPredictions(
            viewModelScope,
            artEntryDao,
            aniListAutocompleter,
            json,
        )
    }

    override suspend fun buildAddModel() = artSettings.artEntryTemplate.value?.let(::buildModel)

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        buildModel(artEntryDao.getEntry(entryId.valueId))

    override suspend fun buildMultiEditModel(): ArtEntryModel {
        val firstEntry = artEntryDao.getEntry(entryIds.first().valueId)
        val differentValue = listOf(Entry.Different)

        val entryValueIds = entryIds.map { it.valueId }
        val series = artEntryDao.distinctCountSeries(entryValueIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.series(json) }
            ?.let(dataConverter::seriesEntries)
            ?: differentValue

        val characters = artEntryDao.distinctCountCharacters(entryValueIds)
            .takeIf { it == 1 }
            ?.let { firstEntry.characters(json) }
            ?.let(dataConverter::characterEntries)
            ?: differentValue

        val sourceTypeSame = artEntryDao.distinctCountSourceType(entryValueIds) == 1
        val sourceValueSame = artEntryDao.distinctCountSourceValue(entryValueIds) == 1

        val source = if (sourceTypeSame && sourceValueSame) {
            SourceType.fromEntry(json, firstEntry)
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

    override fun entry() = when (type) {
        Type.ADD -> null
        Type.SINGLE_EDIT -> makeBaseEntry().copy(
            // Searchable values are ignored because they rely on network and aren't restored
            // TODO: Fix this and actually track searchable values alongside serialized values
            seriesSearchable = emptyList(),
            charactersSearchable = emptyList(),
            lastEditTime = null
        )
        Type.MULTI_EDIT -> null // TODO: Doesn't handle multi-edit unsaved change detection
    }

    override suspend fun saveSingleEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean,
    ): Boolean {
        val baseEntry = makeBaseEntry()
        baseEntry.series(json)
            .filterIsInstance<Series.AniList>()
            .map { it.id }
            .let { mediaRepository.ensureSaved(it) }
            ?.let {
                if (!skipIgnoreableErrors) {
                    errorResource = it.first to it.second
                    return false
                }
            }
        baseEntry.characters(json)
            .filterIsInstance<Character.AniList>()
            .map { it.id }
            .let { characterRepository.ensureSaved(it) }
            ?.let {
                if (!skipIgnoreableErrors) {
                    errorResource = it.first to it.second
                    return false
                }
            }

        val allEntryIds = if (entryIds.isEmpty() && saveImagesResult.isEmpty()) {
            // TODO: There must be a better way to propagate no image saves
            // If no images provided, mock an empty save result
            setOf(EntryId(scopedIdType, Uuid.random().toString()))
        } else {
            (entryIds + saveImagesResult.keys).toSet()
        }
        val entryImages = entryImageController.images.groupBy { it.entryId }
        val entries = allEntryIds.map {
            val result = saveImagesResult[it]?.firstOrNull()
            baseEntry.copy(
                id = it.valueId,
                imageWidth = result?.width,
                imageHeight = result?.height,
            )
        }
        artEntryDao.insertEntries(entries)
        return true
    }

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean,
    ): Boolean {
        val series = entrySections.seriesSection.finalContents()
        val characters = entrySections.characterSection.finalContents()
        val tags = entrySections.tagSection.finalContents()
        val artists = entrySections.artistSection.finalContents()
        val sourceItem = entrySections.sourceSection.selectedItem().toSource()

        val printWidth = entrySections.printSizeSection.finalWidth()
        val printHeight = entrySections.printSizeSection.finalHeight()
        val notes = entrySections.notesSection.value

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
                sourceItem.serializedValue(json)
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

        if (entrySections.artistSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateArtistsLocked(
                entryValueIds,
                entrySections.artistSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (entrySections.sourceSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSourceLocked(
                entryValueIds,
                entrySections.sourceSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (entrySections.seriesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateSeriesLocked(
                entryValueIds,
                entrySections.seriesSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (entrySections.characterSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateCharactersLocked(
                entryValueIds,
                entrySections.characterSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (entrySections.tagSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateTagsLocked(
                entryValueIds,
                entrySections.tagSection.lockState?.toSerializedValue() ?: false
            )
        }

        if (entrySections.notesSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updateNotesLocked(
                entryValueIds,
                entrySections.notesSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real notes different value tracking
            if (notes.isNotEmpty() && notes.trim() != "Different") {
                artEntryDao.updateNotes(entryValueIds, notes)
            }
        }

        if (entrySections.printSizeSection.lockState != EntrySection.LockState.DIFFERENT) {
            artEntryDao.updatePrintSizeLocked(
                entryValueIds,
                entrySections.printSizeSection.lockState?.toSerializedValue() ?: false
            )

            // TODO: Real print size different value tracking
            if (printWidth != null || printHeight != null) {
                artEntryDao.updatePrintSize(entryValueIds, printWidth, printHeight)
            }
        }

        artEntryDao.updateLastEditTime(entryValueIds, Clock.System.now())
        return true
    }

    override suspend fun deleteEntry(entryId: EntryId) = artEntryDao.delete(entryId.valueId)

    override fun onImageSizeResult(widthToHeightRatio: Float) {
        entrySections.printSizeSection.onSizeChange(widthToHeightRatio)
    }

    protected fun buildModel(entry: ArtEntry): ArtEntryModel {
        val artists = entry.artists.map(Entry::Custom)
        val series = dataConverter.seriesEntries(entry.series(json))
        val characters = dataConverter.characterEntries(entry.characters(json))
        val tags = entry.tags.map(Entry::Custom)

        return ArtEntryModel(
            entry = entry,
            artists = artists,
            series = series,
            characters = characters,
            tags = tags,
            source = SourceType.fromEntry(json, entry)
        )
    }

    override fun initializeForm(model: ArtEntryModel) {
        entrySections.artistSection.setContents(model.artists, model.artistsLocked)
        entrySections.sourceSection.initialize(model, model.sourceLocked)
        entrySections.seriesSection.setContents(model.series, model.seriesLocked)
        entrySections.characterSection.setContents(model.characters, model.charactersLocked)
        entrySections.printSizeSection.initialize(
            model.printWidth,
            model.printHeight,
            model.printSizeLocked
        )
        entrySections.tagSection.setContents(model.tags, model.tagsLocked)
        entrySections.notesSection.setContents(model.notes, model.notesLocked)

        model.characters.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::characterId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(entrySections.characterSection::replaceContent)
                }
            }

        model.series.filterIsInstance<Entry.Prefilled<*>>()
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(entrySections.seriesSection::replaceContent)
                }
            }
    }

    protected fun makeBaseEntry(): ArtEntry {
        val sourceItem = entrySections.sourceSection.selectedItem().toSource()

        return ArtEntry(
            id = "",
            artists = entrySections.artistSection.finalContents().map { it.serializedValue },
            sourceType = sourceItem.serializedType,
            sourceValue = sourceItem.serializedValue(json),
            seriesSerialized = entrySections.seriesSection.finalContents()
                .map { it.serializedValue },
            seriesSearchable = entrySections.seriesSection.finalContents()
                .map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            charactersSerialized = entrySections.characterSection.finalContents()
                .map { it.serializedValue },
            charactersSearchable = entrySections.characterSection.finalContents()
                .map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            tags = entrySections.tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Clock.System.now(),
            imageWidth = null,
            imageHeight = null,
            printWidth = entrySections.printSizeSection.finalWidth(),
            printHeight = entrySections.printSizeSection.finalHeight(),
            notes = entrySections.notesSection.value.trim(),
            locks = ArtEntry.Locks(
                artistsLocked = entrySections.artistSection.lockState?.toSerializedValue(),
                seriesLocked = entrySections.seriesSection.lockState?.toSerializedValue(),
                charactersLocked = entrySections.characterSection.lockState?.toSerializedValue(),
                sourceLocked = entrySections.sourceSection.lockState?.toSerializedValue(),
                tagsLocked = entrySections.tagSection.lockState?.toSerializedValue(),
                notesLocked = entrySections.notesSection.lockState?.toSerializedValue(),
                printSizeLocked = entrySections.printSizeSection.lockState?.toSerializedValue(),
            )
        )
    }

    fun onClickSaveTemplate() {
        artSettings.artEntryTemplate.value = makeBaseEntry()
        errorResource = Res.string.art_template_saved to null
    }
}
