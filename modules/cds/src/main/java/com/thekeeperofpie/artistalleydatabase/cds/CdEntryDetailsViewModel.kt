package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Date

abstract class CdEntryDetailsViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDetailsDao,
    private val aniListJson: AniListJson,
    private val aniListDataConverter: AniListDataConverter,
    private val aniListAutocompleter: AniListAutocompleter,
    private val vgmdbApi: VgmdbApi,
    private val vgmdbJson: VgmdbJson,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val vgmdbAutocompleter: VgmdbAutocompleter,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val mediaRepository: MediaRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "CdEntryDetailsViewModel"
    }

    // TODO: Enforce single value
    protected val catalogIdSection = EntrySection.MultiText(
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val titleSection = EntrySection.MultiText(
        R.string.cd_entry_title_header_zero,
        R.string.cd_entry_title_header_one,
        R.string.cd_entry_title_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val vocalistSection = EntrySection.MultiText(
        R.string.cd_entry_vocalists_header_zero,
        R.string.cd_entry_vocalists_header_one,
        R.string.cd_entry_vocalists_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val composerSection = EntrySection.MultiText(
        R.string.cd_entry_composers_header_zero,
        R.string.cd_entry_composers_header_one,
        R.string.cd_entry_composers_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val seriesSection = EntrySection.MultiText(
        R.string.cd_entry_series_header_zero,
        R.string.cd_entry_series_header_one,
        R.string.cd_entry_series_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val characterSection = EntrySection.MultiText(
        R.string.cd_entry_characters_header_zero,
        R.string.cd_entry_characters_header_one,
        R.string.cd_entry_characters_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val tagSection = EntrySection.MultiText(
        R.string.cd_entry_tags_header_zero,
        R.string.cd_entry_tags_header_one,
        R.string.cd_entry_tags_header_many,
        lockState = EntrySection.LockState.UNLOCKED,
    )

    protected val notesSection = EntrySection.LongText(
        headerRes = R.string.cd_entry_notes_header,
        lockState = EntrySection.LockState.UNLOCKED
    )

    val sections = listOf(
        catalogIdSection,
        titleSection,
        vocalistSection,
        composerSection,
        seriesSection,
        characterSection,
        tagSection,
        notesSection
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.valueUpdates()
                .filter { it.length > 4 }
                .mapLatest { vgmdbApi.searchAlbums(it) }
                .catch {}
                .mapLatest {
                    it.map {
                        async {
                            try {
                                vgmdbApi.getAlbum(it.id)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error fetching album ${it.id}")
                                null
                            }
                        }
                    }
                        .awaitAll()
                        .filterNotNull()
                        .map(vgmdbDataConverter::catalogEntry)
                }
                .catch {}
                .flowOn(Dispatchers.IO)
                .collect { catalogIdSection.predictions = it }
        }

        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.predictionChosen
                .filterIsInstance<Entry.Prefilled<AlbumEntry>>()
                .map { vgmdbDataConverter.titleEntry(it.value) }
                .flowOn(Dispatchers.IO)
                .collectLatest { titleSection.addOrReplaceContent(it) }
        }

        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.predictionChosen
                .filterIsInstance<Entry.Prefilled<AlbumEntry>>()
                .flatMapLatest {
                    combine(
                        it.value.vocalists
                            .mapNotNull { vgmdbJson.parseVocalistColumn(it).rightOrNull() }
                            .map {
                                artistRepository.getEntry(it.id)
                                    .map { it?.let(vgmdbDataConverter::vocalistEntry) }
                                    .catch {}
                                    .startWith(vgmdbDataConverter.vocalistPlaceholder(it))
                            }
                    ) { it.toList().filterNotNull() }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { vocalistSection.addOrReplaceContents(it) }
        }

        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.predictionChosen
                .filterIsInstance<Entry.Prefilled<AlbumEntry>>()
                .flatMapLatest {
                    combine(
                        it.value.composers
                            .mapNotNull { vgmdbJson.parseComposerColumn(it).rightOrNull() }
                            .map {
                                artistRepository.getEntry(it.id)
                                    .map { it?.let(vgmdbDataConverter::composerEntry) }
                                    .catch {}
                                    .startWith(vgmdbDataConverter.composerPlaceholder(it))
                            }
                    ) { it.toList().filterNotNull() }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { composerSection.addOrReplaceContents(it) }
        }

        viewModelScope.launch(Dispatchers.IO) {
            vocalistSection.subscribePredictions(
                localCall = {
                    vgmdbAutocompleter.queryVocalistsLocal(it, cdEntryDao::queryVocalists)
                },
                networkCall = vgmdbAutocompleter::queryVocalistsNetwork
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            composerSection.subscribePredictions(
                localCall = {
                    vgmdbAutocompleter.queryComposersLocal(it, cdEntryDao::queryComposers)
                },
                networkCall = vgmdbAutocompleter::queryComposersNetwork
            )
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
                seriesSection.contentUpdates(),
                characterSection.valueUpdates(),
            ) { cdEntryDao.queryCharacters(it) }
                .collectLatest {
                    withContext(Dispatchers.Main) {
                        characterSection.predictions = it.toMutableList()
                    }
                }
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

    protected fun buildModel(entry: CdEntry): CdEntryModel {
        val catalogId = vgmdbDataConverter.databaseToCatalogIdEntry(entry.catalogId)
        val titles = entry.titles.map(vgmdbDataConverter::databaseToTitleEntry)
        val vocalists = entry.vocalists.map(vgmdbDataConverter::databaseToVocalistEntry)
        val composers = entry.composers.map(vgmdbDataConverter::databaseToComposerEntry)
        val series = entry.series.map(aniListDataConverter::databaseToSeriesEntry)
        val characters = entry.characters.map(aniListDataConverter::databaseToCharacterEntry)
        val tags = entry.tags.map(Entry::Custom)

        return CdEntryModel(
            entry = entry,
            catalogId = catalogId,
            titles = titles,
            vocalists = vocalists,
            composers = composers,
            series = series,
            characters = characters,
            tags = tags,
        )
    }

    protected fun initializeForm(entry: CdEntryModel) {
        catalogIdSection.setContents(listOf(entry.catalogId), entry.titlesLocked)
        titleSection.setContents(entry.titles, entry.titlesLocked)
        vocalistSection.setContents(entry.vocalists, entry.vocalistsLocked)
        composerSection.setContents(entry.composers, entry.composersLocked)
        seriesSection.setContents(entry.series, entry.seriesLocked)
        characterSection.setContents(entry.characters, entry.charactersLocked)
        tagSection.setContents(entry.tags, entry.tagsLocked)
        notesSection.setContents(entry.notes, entry.notesLocked)

        val albumId = (entry.catalogId as? Entry.Prefilled<*>)?.id
        if (albumId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                albumRepository.getEntry(albumId)
                    .filterNotNull()
                    .map(vgmdbDataConverter::catalogEntry)
                    .flowOn(Dispatchers.IO)
                    .collectLatest { newEntry ->
                        catalogIdSection.replaceContents { entry ->
                            if (entry is Entry.Prefilled<*>
                                && entry.id == albumId
                            ) newEntry else entry
                        }
                    }
            }
        }

        entry.titles.filterIsInstance<Entry.Prefilled<*>>()
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    albumRepository.getEntry(it.id)
                        .filterNotNull()
                        .map(vgmdbDataConverter::titleEntry)
                        .flowOn(Dispatchers.IO)
                        .collectLatest { newEntry ->
                            titleSection.replaceContents { entry ->
                                if (entry is Entry.Prefilled<*>
                                    && entry.id == it.id
                                ) newEntry else entry
                            }
                        }
                }
            }

        entry.vocalists.filterIsInstance<Entry.Prefilled<*>>()
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    vgmdbAutocompleter.fillVocalistField(it.id)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(vocalistSection::replaceContent)
                }
            }

        entry.composers.filterIsInstance<Entry.Prefilled<*>>()
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    vgmdbAutocompleter.fillComposerField(it.id)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(composerSection::replaceContent)
                }
            }

        entry.characters.filterIsInstance<Entry.Prefilled<*>>()
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it.id)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(characterSection::replaceContent)
                }
            }

        entry.series.filterIsInstance<Entry.Prefilled<*>>()
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it.id)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(seriesSection::replaceContent)
                }
            }
    }

    protected suspend fun makeEntry(imageUri: Uri?, id: String): CdEntry? {
        val outputFile = CdEntryUtils.getImageFile(application, id)
        val error = ImageUtils.writeEntryImage(application, outputFile, imageUri)
        if (error != null) {
            Log.e(TAG, "${application.getString(error.first)}: $imageUri", error.second)
            withContext(Dispatchers.Main) {
                errorResource = error
            }
            return null
        }
        val (imageWidth, imageHeight) = ImageUtils.getImageSize(outputFile)

        return CdEntry(
            id = id,
            catalogId = catalogIdSection.finalContents().firstOrNull()?.serializedValue,
            titles = titleSection.finalContents().map { it.serializedValue },
            vocalists = vocalistSection.finalContents().map { it.serializedValue },
            composers = composerSection.finalContents().map { it.serializedValue },
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
            notes = notesSection.value.trim(),
            locks = CdEntry.Locks(
                catalogIdLocked = catalogIdSection.lockState?.toSerializedValue(),
                titlesLocked = titleSection.lockState?.toSerializedValue(),
                vocalistsLocked = vocalistSection.lockState?.toSerializedValue(),
                composersLocked = composerSection.lockState?.toSerializedValue(),
                seriesLocked = seriesSection.lockState?.toSerializedValue(),
                charactersLocked = characterSection.lockState?.toSerializedValue(),
                tagsLocked = tagSection.lockState?.toSerializedValue(),
                notesLocked = notesSection.lockState?.toSerializedValue(),
            )
        )
    }

    suspend fun saveEntry(imageUri: Uri?, id: String): Boolean {
        val entry = makeEntry(imageUri, id) ?: return false
        entry.catalogId?.let(albumRepository::ensureSaved)
        entry.series
            .map { aniListJson.parseSeriesColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(mediaRepository::ensureSaved)
        entry.characters
            .map { aniListJson.parseCharacterColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(characterRepository::ensureSaved)
        entry.vocalists
            .map { vgmdbJson.parseVocalistColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(artistRepository::ensureSaved)
        entry.composers
            .map { vgmdbJson.parseComposerColumn(it) }
            .mapNotNull { it.rightOrNull()?.id }
            .forEach(artistRepository::ensureSaved)
        cdEntryDao.insertEntries(entry)
        return true
    }
}