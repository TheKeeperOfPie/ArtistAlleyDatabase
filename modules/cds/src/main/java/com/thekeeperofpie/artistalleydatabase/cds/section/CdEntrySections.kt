package com.thekeeperofpie.artistalleydatabase.cds.section

import android.util.Log
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.cds.R
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.emitNotNull
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.suspend1
import com.thekeeperofpie.artistalleydatabase.vgmdb.SearchResults
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import kotlinx.coroutines.CoroutineScope
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
import kotlin.time.Duration.Companion.seconds

class CdEntrySections(
    private val cdEntryDao: CdEntryDetailsDao,
    private val aniListAutocompleter: AniListAutocompleter,
    private val vgmdbApi: VgmdbApi,
    private val vgmdbJson: VgmdbJson,
    private val vgmdbDataConverter: VgmdbDataConverter,
    private val vgmdbAutocompleter: VgmdbAutocompleter,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    defaultLockState: EntrySection.LockState = EntrySection.LockState.UNLOCKED,
) {
    companion object {
        const val TAG = "CdEntrySections"
    }

    // TODO: Enforce single value
    val catalogIdSection = EntrySection.MultiText(
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        R.string.cd_entry_catalog_header,
        lockState = defaultLockState,
    )

    val titleSection = EntrySection.MultiText(
        R.string.cd_entry_title_header_zero,
        R.string.cd_entry_title_header_one,
        R.string.cd_entry_title_header_many,
        lockState = defaultLockState,
    )

    val performerSection = EntrySection.MultiText(
        R.string.cd_entry_performers_header_zero,
        R.string.cd_entry_performers_header_one,
        R.string.cd_entry_performers_header_many,
        lockState = defaultLockState,
    )

    val composerSection = EntrySection.MultiText(
        R.string.cd_entry_composers_header_zero,
        R.string.cd_entry_composers_header_one,
        R.string.cd_entry_composers_header_many,
        lockState = defaultLockState,
    )

    val seriesSection = EntrySection.MultiText(
        R.string.cd_entry_series_header_zero,
        R.string.cd_entry_series_header_one,
        R.string.cd_entry_series_header_many,
        lockState = defaultLockState,
    )

    val characterSection = EntrySection.MultiText(
        R.string.cd_entry_characters_header_zero,
        R.string.cd_entry_characters_header_one,
        R.string.cd_entry_characters_header_many,
        lockState = defaultLockState,
    )

    val discSection =
        DiscSection(json = vgmdbJson.json, lockState = defaultLockState)

    val tagSection = EntrySection.MultiText(
        R.string.cd_entry_tags_header_zero,
        R.string.cd_entry_tags_header_one,
        R.string.cd_entry_tags_header_many,
        lockState = defaultLockState,
    )

    val notesSection = EntrySection.LongText(
        headerRes = R.string.cd_entry_notes_header,
        lockState = defaultLockState,
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

    fun subscribeSectionPredictions(scope: CoroutineScope) {
        scope.launch(Dispatchers.Main) {
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
                                    .catch {
                                        Log.d(TAG, "Error fetching album $albumId", it)
                                    }
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
        scope.launch(Dispatchers.Main) {
            @Suppress("OPT_IN_USAGE")
            catalogIdSection.predictionChosen
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
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

        scope.launch(Dispatchers.Main) {
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
            scope.launch(Dispatchers.Main) {
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
                                        else -> flowOf(EntrySection.MultiText.Entry.Custom(it))
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
            scope.launch(Dispatchers.IO) {
                section.subscribePredictions(
                    localCall = { vgmdbAutocompleter.queryArtistsLocal(it, function) },
                    networkCall = vgmdbAutocompleter::queryArtistsNetwork
                )
            }
        }

        scope.launch(Dispatchers.IO) {
            seriesSection.subscribePredictions(
                localCall = {
                    aniListAutocompleter.querySeriesLocal(it, cdEntryDao::querySeries)
                },
                networkCall = aniListAutocompleter::querySeriesNetwork
            )
        }

        scope.launch(Dispatchers.IO) {
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

        scope.launch(Dispatchers.Main) {
            catalogAlbumChosen()
                .map(vgmdbDataConverter::discEntries)
                .flowOn(Dispatchers.IO)
                .collectLatest { discSection.setDiscs(it, EntrySection.LockState.LOCKED) }
        }

        scope.launch(Dispatchers.IO) {
            tagSection.subscribePredictions(
                localCall = {
                    cdEntryDao.queryTags(it)
                        .map(EntrySection.MultiText.Entry::Custom)
                        .map { flowOf(it) }
                        .ifEmpty { listOf(flowOf(null)) }
                }
            )
        }
    }

    // TODO: Share emissions downstream
    @Suppress("OPT_IN_USAGE")
    fun catalogAlbumChosen() =
        catalogIdSection.predictionChosen
            .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
            .flatMapLatest {
                when (val value = it.value) {
                    is AlbumEntry -> flowOf(value)
                    is SearchResults.AlbumResult -> albumRepository.getEntry(value.id)
                        .filterNotNull()
                        .take(1)
                    else -> emptyFlow()
                }
            }
}
