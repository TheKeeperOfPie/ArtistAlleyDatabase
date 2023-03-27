package com.thekeeperofpie.artistalleydatabase.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbApi
import com.thekeeperofpie.artistalleydatabase.vgmdb.VgmdbJson
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.VgmdbArtistDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mediaEntryDao: MediaEntryDao,
    private val characterEntryDao: CharacterEntryDao,
    private val albumEntryDao: AlbumEntryDao,
    private val cdEntryDao: CdEntryDao,
    private val musicalArtistDao: MusicalArtistDao,
    private val vgmdbArtistDao: VgmdbArtistDao,
    private val vgmdbJson: VgmdbJson,
    private val workManager: WorkManager,
    private val settingsProvider: SettingsProvider,
    private val vgmdbApi: VgmdbApi,
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private var onClickDatabaseFetch: (WorkManager) -> Unit = {}

    fun initialize(onClickDatabaseFetch: (WorkManager) -> Unit) {
        this.onClickDatabaseFetch = onClickDatabaseFetch
    }

    fun clearAniListCache() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaEntryDao.deleteAll()
            characterEntryDao.deleteAll()
        }
    }

    fun clearVgmdbCache() {
        viewModelScope.launch(Dispatchers.IO) {
            albumEntryDao.deleteAll()
            vgmdbArtistDao.deleteAll()
        }
    }

    fun onClickDatabaseFetch() {
        onClickDatabaseFetch(workManager)
    }

    fun onClickClearDatabaseById(databaseType: SettingsScreen.DatabaseType, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (id.isBlank()) {
                when (databaseType) {
                    SettingsScreen.DatabaseType.ANILIST_CHARACTERS -> characterEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.ANILIST_MEDIA -> mediaEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.VGMDB_ALBUMS -> albumEntryDao.deleteAll()
                    SettingsScreen.DatabaseType.VGMDB_ARTISTS -> vgmdbArtistDao.deleteAll()
                    SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> musicalArtistDao.deleteAll()
                }
            } else {
                when (databaseType) {
                    SettingsScreen.DatabaseType.ANILIST_CHARACTERS -> characterEntryDao.delete(id)
                    SettingsScreen.DatabaseType.ANILIST_MEDIA -> mediaEntryDao.delete(id)
                    SettingsScreen.DatabaseType.VGMDB_ALBUMS -> albumEntryDao.delete(id)
                    SettingsScreen.DatabaseType.VGMDB_ARTISTS -> vgmdbArtistDao.delete(id)
                    SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> musicalArtistDao.delete(id)
                }
            }
        }
    }

    fun onClickRebuildDatabase(databaseType: SettingsScreen.DatabaseType) {
        viewModelScope.launch(Dispatchers.IO) {
            when (databaseType) {
                SettingsScreen.DatabaseType.ANILIST_CHARACTERS,
                SettingsScreen.DatabaseType.ANILIST_MEDIA,
                SettingsScreen.DatabaseType.VGMDB_ALBUMS,
                SettingsScreen.DatabaseType.VGMDB_ARTISTS -> {
                    // TODO: Rebuild?
                }
                SettingsScreen.DatabaseType.MUSICAL_ARTISTS -> {
                    musicalArtistDao.deleteAll()
                    cdEntryDao.iterateEntriesNoTransaction { _: Int, cdEntry: CdEntry ->
                        val musicalArtists = cdEntry.performers.map(vgmdbJson::parseArtistColumn)
                            .map {
                                when (it) {
                                    is Either.Left -> {
                                        MusicalArtist(
                                            id = "custom_${it.value}",
                                            name = it.value,
                                            type = MusicalArtist.Type.CUSTOM,
                                        )
                                    }
                                    is Either.Right -> {
                                        val artistId = it.value.id
                                        val artistEntry = vgmdbArtistDao.getEntry(artistId)
                                        if (artistEntry == null) {
                                            MusicalArtist(
                                                id = "vgmdb_$artistId",
                                                name = it.value.name ?: "",
                                                type = MusicalArtist.Type.VGMDB,
                                            )
                                        } else {
                                            MusicalArtist(
                                                id = "vgmdb_${artistEntry.id}",
                                                name = artistEntry.name,
                                                type = MusicalArtist.Type.VGMDB,
                                                image = artistEntry.pictureThumb,
                                            )
                                        }
                                    }
                                }
                            }

                        musicalArtistDao.insert(musicalArtists)
                    }
                }
            }
        }
    }

    fun onClickCropClear() {
        settingsProvider.settingsData.cropDocumentUri = null
    }

    fun checkMismatchedCdEntryData() {
        viewModelScope.launch(Dispatchers.IO) {
            var offset = 0
            var entries = cdEntryDao.getEntries(limit = 50, offset = offset)
            while (entries.isNotEmpty()) {
                entries.filter {
                    it.catalogId?.isNotBlank() == true
                }.forEach {
                    val (catalogId, albumId) = when (val result =
                        vgmdbJson.parseCatalogIdColumn(it.catalogId)) {
                        // Ignore non-VGMdb entries
                        is Either.Left -> return@forEach
                        is Either.Right -> result.value.catalogId to result.value.id
                    }
                    if (catalogId == null) {
                        Log.d(TAG, "Empty catalogId, entryId = ${it.id}")
                        return@forEach
                    }

                    val album = vgmdbApi.getAlbum(albumId) ?: run {
                        Log.d(TAG, "Failed to load album for $catalogId")
                        return@forEach
                    }

                    if (it.performers.size != album.performers.size) {
                        Log.d(
                            TAG,
                            "Mismatched performer for $catalogId," +
                                    " expected = ${album.performers.size}," +
                                    " actual = ${it.performers.size}"
                        )
                    }

                    if (it.composers.size != album.composers.size) {
                        Log.d(
                            TAG,
                            "Mismatched composers for $catalogId," +
                                    " expected = ${album.composers.size}," +
                                    " actual = ${it.composers.size}"
                        )
                    }
                }

                offset += entries.size
                entries = cdEntryDao.getEntries(limit = 50, offset = offset)
            }
        }
    }
}