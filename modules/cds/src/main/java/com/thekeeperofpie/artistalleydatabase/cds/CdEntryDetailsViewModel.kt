package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.emitNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.android_utils.suspend1
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
import com.thekeeperofpie.artistalleydatabase.form.EntryImageController
import com.thekeeperofpie.artistalleydatabase.form.EntrySection
import com.thekeeperofpie.artistalleydatabase.form.EntrySection.MultiText.Entry
import com.thekeeperofpie.artistalleydatabase.form.EntrySettings
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
import kotlinx.coroutines.delay
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
) : ViewModel() {

    companion object {
        private const val TAG = "CdEntryDetailsViewModel"
    }

    private enum class Type {
        ADD, SINGLE_EDIT, MULTI_EDIT
    }

    private lateinit var entryIds: List<String>
    private lateinit var type: Type

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

    var sectionsLoading by mutableStateOf(true)
        private set

    var saving by mutableStateOf(false)
        private set

    private var deleting by mutableStateOf(false)

    val entryImageController = EntryImageController(
        scopeProvider = { viewModelScope },
        application = application,
        settings = entrySettings,
        entryTypeId = CdEntryUtils.TYPE_ID,
        onError = { errorResource = it },
        imageContentDescriptionRes = R.string.cd_entry_image_content_description,
    )

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
                .collectLatest {
                    catalogIdSection.addOrReplaceContent(it)
                    catalogIdSection.lockState = EntrySection.LockState.LOCKED
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            catalogAlbumChosen()
                .collectLatest {
                    if (entryIds.size == 1)  {
                        it.coverFull?.toUri()?.let {
                            entryImageController.replaceMainImage(entryIds[0], it)
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
                                    artistRepository.getEntry(it.id)
                                        .map { it?.let(vgmdbDataConverter::artistEntry) }
                                        .catch {}
                                        .startWith(placeholder)
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
                Type.ADD -> null
                Type.SINGLE_EDIT -> {
                    // TODO: Move this delay into the UI layer
                    // Delay to allow the shared element transition to finish
                    delay(
                        AnimationUtils.multipliedByAnimatorScale(application, 350L)
                            .coerceAtLeast(350L)
                    )
                    buildModel(cdEntryDao.getEntry(entryIds.single()))
                }
                Type.MULTI_EDIT -> TODO("CD multi-edit support")
            }
            withContext(Dispatchers.Main) {
                model?.run(::initializeForm)
                sectionsLoading = false
            }
        }

        entryImageController.initialize(entryIds)
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

    protected fun buildModel(entry: CdEntry): CdEntryModel {
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

    private fun initializeForm(entry: CdEntryModel) {
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

    fun onConfirmDelete(navHostController: NavHostController) {
        if (deleting || saving) return
        if (type != Type.SINGLE_EDIT) {
            // Don't delete from details page unless editing a single entry to avoid mistakes
            return
        }

        deleting = true

        viewModelScope.launch(Dispatchers.IO) {
            cdEntryDao.delete(entryIds.single())
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
                TODO("Multi edit support")
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

    private suspend fun saveEntry(skipIgnoreableErrors: Boolean = false): Boolean {
        val saveImagesResult = entryImageController.saveImages() ?: return false

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
        cdEntryDao.insertEntries(entries)

        entryImageController.cleanUpImages(entryIds, saveImagesResult)

        return true
    }
}