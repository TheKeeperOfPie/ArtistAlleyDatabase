package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.emitNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.suspend1
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryModel
import com.thekeeperofpie.artistalleydatabase.cds.section.DiscSection
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.data.Character
import com.thekeeperofpie.artistalleydatabase.data.DataConverter
import com.thekeeperofpie.artistalleydatabase.data.Series
import com.thekeeperofpie.artistalleydatabase.entry.EntryDetailsViewModel
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.entry.EntryImageController
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.entry.EntrySettings
import com.thekeeperofpie.artistalleydatabase.vgmdb.SearchResults
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbUtils
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

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

    // TODO: Enforce single value
    private val catalogIdSection = EntrySection.MultiText(
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val titleSection = EntrySection.MultiText(
        R.string.cd_entry_title_header_zero,
        R.string.cd_entry_title_header_one,
        R.string.cd_entry_title_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val performerSection = EntrySection.MultiText(
        R.string.cd_entry_performers_header_zero,
        R.string.cd_entry_performers_header_one,
        R.string.cd_entry_performers_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val composerSection = EntrySection.MultiText(
        R.string.cd_entry_composers_header_zero,
        R.string.cd_entry_composers_header_one,
        R.string.cd_entry_composers_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val seriesSection = EntrySection.MultiText(
        R.string.cd_entry_series_header_zero,
        R.string.cd_entry_series_header_one,
        R.string.cd_entry_series_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val characterSection = EntrySection.MultiText(
        R.string.cd_entry_characters_header_zero,
        R.string.cd_entry_characters_header_one,
        R.string.cd_entry_characters_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val discSection =
        DiscSection(json = vgmdbJson.json, lockState = EntrySection.LockState.UNLOCKED)

    private val tagSection = EntrySection.MultiText(
        R.string.cd_entry_tags_header_zero,
        R.string.cd_entry_tags_header_one,
        R.string.cd_entry_tags_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    private val notesSection = EntrySection.LongText(
        headerRes = R.string.cd_entry_notes_header,
        lockState = EntrySection.LockState.UNLOCKED
    )

    override val sections = listOf(
        catalogIdSection,
        titleSection,
        performerSection,
        composerSection,
        seriesSection,
        characterSection,
        discSection,
        tagSection,
        notesSection
    )

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.valueUpdates()
                .debounce(2.seconds)
                .filter { it.length > 5 }
                .flatMapLatest {
                    flow { emit(vgmdbApi.searchAlbums(it)) }
                        .catch {}
                        .flatMapLatest {
                            it.map {
                                val albumId = it.id
                                flow { emitNotNull(vgmdbApi.getAlbum(albumId)) }
                                    .catch { Log.d(TAG, "Error fetching album $albumId", it) }
                                    .map(vgmdbDataConverter::catalogEntry)
                                    .startWith(vgmdbDataConverter.catalogIdPlaceholder(it))
                            }.let { combine(it) { it.toList().distinctBy { it.id } } }
                        }
                        .startWith(item = emptyList())
                }
                .catch {}
                .flowOn(Dispatchers.IO)
                .collect { catalogIdSection.predictions = it }
        }

        // If a search result was chosen, fill it with the final album response when it returns
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.predictionChosen
                .filterIsInstance<Entry.Prefilled<*>>()
                .mapLatestNotNull { (it.value as? SearchResults.AlbumResult)?.id }
                .flatMapLatest {
                    albumRepository.getEntry(it)
                        .filterNotNull()
                        .take(1)
                }
                .map(vgmdbDataConverter::catalogEntry)
                .flowOn(Dispatchers.IO)
                .collectLatest {
                    catalogIdSection.addOrReplaceContent(it)
                    catalogIdSection.lockIfUnlocked()
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            catalogAlbumChosen()
                .collectLatest {
                    if (entryIds.size <= 1) {
                        it.coverFull?.toUri()?.let {
                            entryImageController.replaceMainImage(entryIds.firstOrNull(), it)
                        }
                    }
                }
        }

        viewModelScope.launch(Dispatchers.Main) {
            catalogAlbumChosen()
                .map(vgmdbDataConverter::titleEntry)
                .flowOn(Dispatchers.IO)
                .collectLatest { titleSection.addOrReplaceContent(it) }
        }

        // TODO: Compare VGMdb performer names to AniList media -> character -> VA
        //  to automatically fill character section
        mapOf(
            { album: AlbumEntry -> album.performers } to performerSection,
            { album: AlbumEntry -> album.composers } to composerSection,
        ).forEach { (entryFunction, formSection) ->
            viewModelScope.launch(Dispatchers.Main) {
                @Suppress("OPT_IN_USAGE")
                catalogAlbumChosen()
                    .flatMapLatest {
                        combine(
                            entryFunction(it)
                                .map {
                                    when (val result = vgmdbJson.parseArtistColumn(it)) {
                                        is Either.Right -> {
                                            val value = result.value
                                            val placeholder =
                                                vgmdbDataConverter.artistPlaceholder(value)
                                            artistRepository.getEntry(value.id)
                                                .map { it?.let(vgmdbDataConverter::artistEntry) }
                                                .catch {}
                                                .startWith(placeholder)
                                        }
                                        else -> flowOf(Entry.Custom(it))
                                    }
                                }
                        ) { it.toList().filterNotNull() }
                    }
                    .flowOn(Dispatchers.IO)
                    .collectLatest { formSection.addOrReplaceContents(it) }
            }
        }

        mapOf(
            performerSection to suspend1(cdEntryDao::queryPerformers),
            composerSection to suspend1(cdEntryDao::queryComposers),
        ).forEach { (section, function) ->
            viewModelScope.launch(Dispatchers.IO) {
                section.subscribePredictions(
                    localCall = { vgmdbAutocompleter.queryArtistsLocal(it, function) },
                    networkCall = vgmdbAutocompleter::queryArtistsNetwork
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            seriesSection.subscribePredictions(
                localCall = {
                    aniListAutocompleter.querySeriesLocal(it, cdEntryDao::querySeries)
                },
                networkCall = aniListAutocompleter::querySeriesNetwork
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            aniListAutocompleter.characterPredictions(
                characterSection.lockStateFlow,
                seriesSection.contentUpdates(),
                characterSection.valueUpdates(),
            ) { cdEntryDao.queryCharacters(it) }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        characterSection.predictions = it.toMutableList()
                    }
                }
        }

        viewModelScope.launch(Dispatchers.Main) {
            catalogAlbumChosen()
                .map(vgmdbDataConverter::discEntries)
                .flowOn(Dispatchers.IO)
                .collectLatest { discSection.setDiscs(it, EntrySection.LockState.LOCKED) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            tagSection.subscribePredictions(
                localCall = {
                    cdEntryDao.queryTags(it)
                        .map(Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }
    }

    @Suppress("OPT_IN_USAGE")
    private fun catalogAlbumChosen() = catalogIdSection.predictionChosen
        .filterIsInstance<Entry.Prefilled<*>>()
        .flatMapLatest {
            when (val value = it.value) {
                is AlbumEntry -> flowOf(value)
                is SearchResults.AlbumResult -> albumRepository.getEntry(value.id)
                    .filterNotNull()
                    .take(1)
                else -> emptyFlow()
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
        skipIgnoreableErrors: Boolean
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
        skipIgnoreableErrors: Boolean
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
        catalogIdSection.setContents(listOf(model.catalogId), model.titlesLocked)
        titleSection.setContents(model.titles, model.titlesLocked)
        performerSection.setContents(model.performers, model.performersLocked)
        composerSection.setContents(model.composers, model.composersLocked)
        seriesSection.setContents(model.series, model.seriesLocked)
        characterSection.setContents(model.characters, model.charactersLocked)
        discSection.setDiscs(model.discs, model.discsLocked)
        tagSection.setContents(model.tags, model.tagsLocked)
        notesSection.setContents(model.notes, model.notesLocked)

        val albumId = VgmdbUtils.albumId(model.catalogId)
        if (albumId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                albumRepository.getEntry(albumId)
                    .filterNotNull()
                    .map(vgmdbDataConverter::catalogEntry)
                    .flowOn(Dispatchers.IO)
                    .collectLatest(catalogIdSection::replaceContent)
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
                        .collectLatest(titleSection::replaceContent)
                }
            }

        mapOf(
            model.performers to performerSection,
            model.composers to composerSection,
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
                        .collectLatest(characterSection::replaceContent)
                }
            }

        model.series
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(seriesSection::replaceContent)
                }
            }
    }

    private fun makeBaseEntry(): CdEntry {
        return CdEntry(
            id = "",
            catalogId = catalogIdSection.finalContents().firstOrNull()?.serializedValue,
            titles = titleSection.finalContents().map { it.serializedValue },
            performers = performerSection.finalContents().map { it.serializedValue },
            composers = composerSection.finalContents().map { it.serializedValue },
            seriesSerialized = seriesSection.finalContents().map { it.serializedValue },
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            charactersSerialized = characterSection.finalContents().map { it.serializedValue },
            charactersSearchable = characterSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            discs = discSection.serializedValue(),
            tags = tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = null,
            imageHeight = null,
            notes = notesSection.value.trim(),
            locks = CdEntry.Locks(
                catalogIdLocked = catalogIdSection.lockState?.toSerializedValue(),
                titlesLocked = titleSection.lockState?.toSerializedValue(),
                performersLocked = performerSection.lockState?.toSerializedValue(),
                composersLocked = composerSection.lockState?.toSerializedValue(),
                seriesLocked = seriesSection.lockState?.toSerializedValue(),
                charactersLocked = characterSection.lockState?.toSerializedValue(),
                discsLocked = discSection.lockState?.toSerializedValue(),
                tagsLocked = tagSection.lockState?.toSerializedValue(),
                notesLocked = notesSection.lockState?.toSerializedValue(),
            )
        )
    }
}
