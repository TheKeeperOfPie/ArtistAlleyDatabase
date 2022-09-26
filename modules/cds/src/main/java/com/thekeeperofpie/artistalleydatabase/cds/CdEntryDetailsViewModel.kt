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
import com.thekeeperofpie.artistalleydatabase.android_utils.emitNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.suspend1
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListDataConverter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListJson
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterRepository
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaRepository
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.vgmdb.SearchResults
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbUtils
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    protected val performerSection = EntrySection.MultiText(
        R.string.cd_entry_performers_header_zero,
        R.string.cd_entry_performers_header_one,
        R.string.cd_entry_performers_header_many,
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

    protected val discSection =
        DiscSection(json = vgmdbJson.json, lockState = EntrySection.LockState.UNLOCKED)

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
        performerSection,
        composerSection,
        seriesSection,
        characterSection,
        discSection,
        tagSection,
        notesSection
    )

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.valueUpdates()
                .filter { it.length > 4 }
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
                .collectLatest { catalogIdSection.addOrReplaceContent(it) }
        }

        viewModelScope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogAlbumChosen()
                .map(vgmdbDataConverter::titleEntry)
                .flowOn(Dispatchers.IO)
                .collectLatest { titleSection.addOrReplaceContent(it) }
        }

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
                                .mapNotNull { vgmdbJson.parseArtistColumn(it).rightOrNull() }
                                .map {
                                    val placeholder = vgmdbDataConverter.artistPlaceholder(it)
                                    val artistId = it.id
                                    if (artistId == null) {
                                        flowOf(placeholder)
                                    } else {
                                        artistRepository.getEntry(artistId)
                                            .map { it?.let(vgmdbDataConverter::artistEntry) }
                                            .catch {}
                                            .startWith(placeholder)
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
                .collectLatest { discSection.setDiscs(it) }
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
    protected fun catalogAlbumChosen() = catalogIdSection.predictionChosen
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

    protected fun buildModel(entry: CdEntry): CdEntryModel {
        val catalogId = vgmdbDataConverter.databaseToCatalogIdEntry(entry.catalogId)
        val titles = entry.titles.map(vgmdbDataConverter::databaseToTitleEntry)
        val performers = entry.performers.map(vgmdbDataConverter::databaseToArtistEntry)
        val composers = entry.composers.map(vgmdbDataConverter::databaseToArtistEntry)
        val series = entry.series.map(aniListDataConverter::databaseToSeriesEntry)
        val characters = entry.characters.map(aniListDataConverter::databaseToCharacterEntry)
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

    protected fun initializeForm(entry: CdEntryModel) {
        catalogIdSection.setContents(listOf(entry.catalogId), entry.titlesLocked)
        titleSection.setContents(entry.titles, entry.titlesLocked)
        performerSection.setContents(entry.performers, entry.performersLocked)
        composerSection.setContents(entry.composers, entry.composersLocked)
        seriesSection.setContents(entry.series, entry.seriesLocked)
        characterSection.setContents(entry.characters, entry.charactersLocked)
        discSection.setDiscs(entry.discs, entry.discsLocked)
        tagSection.setContents(entry.tags, entry.tagsLocked)
        notesSection.setContents(entry.notes, entry.notesLocked)

        val albumId = VgmdbUtils.albumId(entry.catalogId)
        if (albumId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                albumRepository.getEntry(albumId)
                    .filterNotNull()
                    .map(vgmdbDataConverter::catalogEntry)
                    .flowOn(Dispatchers.IO)
                    .collectLatest(catalogIdSection::replaceContent)
            }
        }

        entry.titles
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
            entry.performers to performerSection,
            entry.composers to composerSection,
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

        entry.characters
            .mapNotNull(AniListUtils::characterId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillCharacterField(it)
                        .flowOn(Dispatchers.IO)
                        .collectLatest(characterSection::replaceContent)
                }
            }

        entry.series
            .mapNotNull(AniListUtils::mediaId)
            .forEach {
                viewModelScope.launch(Dispatchers.Main) {
                    aniListAutocompleter.fillMediaField(it)
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
            performers = performerSection.finalContents().map { it.serializedValue },
            composers = composerSection.finalContents().map { it.serializedValue },
            series = seriesSection.finalContents().map { it.serializedValue },
            seriesSearchable = seriesSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            characters = characterSection.finalContents().map { it.serializedValue },
            charactersSearchable = characterSection.finalContents().map { it.searchableValue }
                .filterNot(String?::isNullOrBlank),
            discs = discSection.serializedValue(),
            tags = tagSection.finalContents().map { it.serializedValue },
            lastEditTime = Date.from(Instant.now()),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
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
        listOf(entry.performers, entry.composers).forEach {
            it.map { vgmdbJson.parseArtistColumn(it) }
                .mapNotNull { it.rightOrNull()?.id }
                .forEach(artistRepository::ensureSaved)
        }
        cdEntryDao.insertEntries(entry)
        return true
    }
}