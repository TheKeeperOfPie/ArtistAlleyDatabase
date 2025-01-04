package com.thekeeperofpie.artistalleydatabase.cds.search

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.thekeeperofpie.artistalleydatabase.anilist.AniListAutocompleter
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.cds.section.CdEntrySections
import com.thekeeperofpie.artistalleydatabase.entry.EntrySection
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.search.EntrySearchViewModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbAutocompleter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbDataConverter
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbUtils
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumRepository
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class CdSearchViewModel(
    private val appFileSystem: AppFileSystem,
    private val cdEntryDao: CdEntryDetailsDao,
    aniListAutocompleter: AniListAutocompleter,
    vgmdbApi: VgmdbApi,
    vgmdbJson: VgmdbJson,
    vgmdbDataConverter: VgmdbDataConverter,
    vgmdbAutocompleter: VgmdbAutocompleter,
    albumRepository: AlbumRepository,
    artistRepository: ArtistRepository,
) : EntrySearchViewModel<CdSearchQuery, CdEntryGridModel>() {

    private val entrySections = CdEntrySections(
        cdEntryDao = cdEntryDao,
        aniListAutocompleter = aniListAutocompleter,
        vgmdbApi = vgmdbApi,
        vgmdbJson = vgmdbJson,
        vgmdbDataConverter = vgmdbDataConverter,
        vgmdbAutocompleter = vgmdbAutocompleter,
        albumRepository = albumRepository,
        artistRepository = artistRepository,
        defaultLockState = EntrySection.LockState.DIFFERENT,
    )

    // TODO: Track search is hard to implement
    override val sections = entrySections.sections
        .filterNot { it == entrySections.discSection }

    init {
        entrySections.subscribeSectionPredictions(viewModelScope)
    }

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            it.forEach {
                appFileSystem.deleteRecursively(
                    EntryUtils.getEntryImageFolder(appFileSystem, it.id)
                )
                cdEntryDao.delete(it.id.valueId)
            }
        }

    override fun searchOptions() = snapshotFlow {
        val performersContents = entrySections.performerSection.finalContents()
        val composersContents = entrySections.composerSection.finalContents()
        val seriesContents = entrySections.seriesSection.finalContents()
        val characterContents = entrySections.characterSection.finalContents()
        CdSearchQuery(
            catalogId = entrySections.catalogIdSection.finalContents()
                .firstOrNull()?.serializedValue,
            titles = entrySections.titleSection.finalContents().map { it.serializedValue },
            performers = performersContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            performersById = performersContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(VgmdbUtils::artistId),
            composers = composersContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            composersById = composersContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(VgmdbUtils::artistId),
            series = seriesContents.filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            seriesById = seriesContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::mediaId),
            characters = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Custom>()
                .map { it.serializedValue }
                .filterNot(String::isBlank),
            charactersById = characterContents
                .filterIsInstance<EntrySection.MultiText.Entry.Prefilled<*>>()
                .mapNotNull(AniListUtils::characterId),
            discs = entrySections.discSection.serializedValue(),
            tags = entrySections.tagSection.finalContents().map { it.serializedValue },
            notes = entrySections.notesSection.value.trim(),
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
    }

    override fun mapQuery(
        query: String,
        options: CdSearchQuery,
    ): Flow<PagingData<CdEntryGridModel>> =
        Pager(PagingConfig(pageSize = 20)) { cdEntryDao.search(query, options) }
            .flow.cachedIn(viewModelScope)
            .onEach {
                viewModelScope.launch(Dispatchers.Main) {
                    // TODO: There must be a better way to sync selected items
                    clearSelected()
                }
            }
            .map {
                it.map {
                    CdEntryGridModel.buildFromEntry(appFileSystem, it)
                }
            }
}
