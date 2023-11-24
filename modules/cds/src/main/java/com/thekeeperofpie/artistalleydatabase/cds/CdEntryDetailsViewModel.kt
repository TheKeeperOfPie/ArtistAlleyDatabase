package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryModel
import com.thekeeperofpie.artistalleydatabase.cds.section.CdEntrySections
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryImageController
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbUtils
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CdEntryDetailsViewModel @Inject constructor(
    application: Application,
    private val cdEntryDao: CdEntryDetailsDao,
    private val appJson: AppJson,
    private val aniListAutocompleter: AniListAutocompleter,
    private val vgmdbApi: VgmdbApi,
    private val vgmdbJson: VgmdbJson,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val vgmdbAutocompleter: VgmdbAutocompleter,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
    private val dataConverter: DataConverter,
    entrySettings: EntrySettings,
) : EntryDetailsViewModel<CdEntry, CdEntryModel>(
    CdEntry::class,
    application,
    CdEntryUtils.SCOPED_ID_TYPE,
    R.string.cd_entry_image_content_description,
    entrySettings,
    appJson,
) {
    companion object {
        private const val TAG = "CdEntryDetailsViewModel"
    }

    private val entrySections = CdEntrySections(
        cdEntryDao = cdEntryDao,
        aniListAutocompleter = aniListAutocompleter,
        vgmdbApi = vgmdbApi,
        vgmdbJson = vgmdbJson,
        vgmdbDataConverter = vgmdbDataConverter,
        vgmdbAutocompleter = vgmdbAutocompleter,
        albumRepository = albumRepository,
        artistRepository = artistRepository,
    )

    override val sections get() = entrySections.sections

    init {
        viewModelScope.launch(Dispatchers.IO) {
            entrySections.catalogAlbumChosen()
                .collectLatest {
                    if (entryIds.size <= 1) {
                        it.coverFull?.toUri()?.let {
                            entryImageController.replaceMainImage(entryIds.firstOrNull(), it)
                        }
                    }
                }
        }
    }

    override suspend fun buildAddModel() = null

    override suspend fun buildSingleEditModel(entryId: EntryId) =
        buildModel(cdEntryDao.getEntry(entryId.valueId))

    override suspend fun buildMultiEditModel(): CdEntryModel {
        TODO("Not yet implemented")
    }

    override fun entry() = when (type) {
        Type.ADD -> null
        Type.SINGLE_EDIT -> makeBaseEntry().copy(
            // Searchable values are ignored because they rely on network and aren't restored
            // TODO: Fix this and actually track searchable values alongside serialized values
            performersSearchable = emptyList(),
            composersSearchable = emptyList(),
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
        baseEntry.catalogId?.let { albumRepository.ensureSaved(listOf(it)) }
            ?.let {
                if (!skipIgnoreableErrors) {
                    errorResource = it
                    return false
                }
            }
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
        listOf(baseEntry.performers, baseEntry.composers).forEach {
            it.map { vgmdbJson.parseArtistColumn(it) }
                .mapNotNull { it.rightOrNull()?.id }
                .let { artistRepository.ensureSaved(it) }
                ?.let {
                    if (!skipIgnoreableErrors) {
                        errorResource = it
                        return false
                    }
                }
        }

        val allEntryIds = (entryIds + saveImagesResult.keys).toMutableSet()
        if (allEntryIds.isEmpty()) {
            // If there are no images and no existing edits, this will be empty, add a new ID
            allEntryIds += EntryId(scopedIdType, UUID.randomUUID().toString())
        }

        val entryImages = entryImageController.images.groupBy { it.entryId }
        val entries = allEntryIds.map {
            val entryImage = entryImages[it]?.firstOrNull()
            baseEntry.copy(
                id = it.valueId,
                imageWidth = entryImage?.finalWidth,
                imageHeight = entryImage?.finalHeight,
            )
        }
        cdEntryDao.insertEntries(entries)
        return true
    }

    override suspend fun saveMultiEditEntry(
        saveImagesResult: Map<EntryId, List<EntryImageController.SaveResult>>,
        skipIgnoreableErrors: Boolean,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEntry(entryId: EntryId) = cdEntryDao.delete(entryId.valueId)

    private fun buildModel(entry: CdEntry): CdEntryModel {
        val catalogId = vgmdbDataConverter.databaseToCatalogIdEntry(entry.catalogId)
        val titles = entry.titles.map(vgmdbDataConverter::databaseToTitleEntry)
        val performers = entry.performers.map(vgmdbDataConverter::databaseToArtistEntry)
        val composers = entry.composers.map(vgmdbDataConverter::databaseToArtistEntry)
        val series = dataConverter.seriesEntries(entry.series(appJson))
        val characters = dataConverter.characterEntries(entry.characters(appJson))
        val discs = entry.discs.mapNotNull(vgmdbDataConverter::databaseToDiscEntry)
        val tags = entry.tags.map(Entry::Custom)

        return CdEntryModel(
            entry = entry,
            catalogId = catalogId,
            titles = titles,
            performers = performers,
            composers = composers,
            series = series,
            characters = characters,
            discs = discs,
            tags = tags,
        )
    }

    override fun initializeForm(model: CdEntryModel) {
        entrySections.catalogIdSection.setContents(listOf(model.catalogId), model.catalogIdLocked)
        entrySections.titleSection.setContents(model.titles, model.titlesLocked)
        entrySections.performerSection.setContents(model.performers, model.performersLocked)
        entrySections.composerSection.setContents(model.composers, model.composersLocked)
        entrySections.seriesSection.setContents(model.series, model.seriesLocked)
        entrySections.characterSection.setContents(model.characters, model.charactersLocked)
        entrySections.discSection.setDiscs(model.discs, model.discsLocked)
        entrySections.tagSection.setContents(model.tags, model.tagsLocked)
        entrySections.notesSection.setContents(model.notes, model.notesLocked)

        val albumId = VgmdbUtils.albumId(model.catalogId)
        if (albumId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                albumRepository.getEntry(albumId)
                    .filterNotNull()
                    .map(vgmdbDataConverter::catalogEntry)
                    .flowOn(Dispatchers.IO)
                    .collectLatest(entrySections.catalogIdSection::replaceContent)
            }
        }

        model.titles
            .mapNotNull(VgmdbUtils::albumId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    albumRepository.getEntry(it)
                        .filterNotNull()
                        .map(vgmdbDataConverter::titleEntry)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(entrySections.titleSection::replaceContent)
                }
            }

        mapOf(
            model.performers to entrySections.performerSection,
            model.composers to entrySections.composerSection,
        ).forEach { (entrySection, formSection) ->
            entrySection
                .mapNotNull { vgmdbDataConverter.artistColumnData(it) }
                .forEach {
                    viewModelScope.launch(Dispatchers.Main) {
                        vgmdbAutocompleter.fillArtistField(it)
                            .flowOn(Dispatchers.IO)
                            .collectLatest(formSection::replaceContent)
                    }
                }
        }

        model.characters
            .mapNotNull(AniListUtils::characterId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(entrySections.characterSection::replaceContent)
                }
            }

        model.series
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(entrySections.seriesSection::replaceContent)
                }
            }
    }

    private fun makeBaseEntry(): CdEntry {
        return CdEntry(
            id = "",
            catalogId = entrySections.catalogIdSection.finalContents()
                .firstOrNull()?.serializedValue,
            titles = entrySections.titleSection.finalContents().map { it.serializedValue },
            performers = entrySections.performerSection.finalContents().map { it.serializedValue },
            composers = entrySections.composerSection.finalContents().map { it.serializedValue },
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
            discs = entrySections.discSection.serializedValue(),
            tags = entrySections.tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = null,
            imageHeight = null,
            notes = entrySections.notesSection.value.trim(),
            locks = CdEntry.Locks(
                catalogIdLocked = entrySections.catalogIdSection.lockState?.toSerializedValue(),
                titlesLocked = entrySections.titleSection.lockState?.toSerializedValue(),
                performersLocked = entrySections.performerSection.lockState?.toSerializedValue(),
                composersLocked = entrySections.composerSection.lockState?.toSerializedValue(),
                seriesLocked = entrySections.seriesSection.lockState?.toSerializedValue(),
                charactersLocked = entrySections.characterSection.lockState?.toSerializedValue(),
                discsLocked = entrySections.discSection.lockState?.toSerializedValue(),
                tagsLocked = entrySections.tagSection.lockState?.toSerializedValue(),
                notesLocked = entrySections.notesSection.lockState?.toSerializedValue(),
            )
        )
    }
}
