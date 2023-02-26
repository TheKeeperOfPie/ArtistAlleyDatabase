package com.thekeeperofpie.artistalleydatabase.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.SettingsProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtist
import com.thekeeperofpie.artistalleydatabase.musical_artists.MusicalArtistDao
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
) : ViewModel() {

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
        val request = OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            DatabaseSyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
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

                        cdEntry.performers.forEach {
                            musicalArtistDao.insert(musicalArtists)
                        }
                    }
                }
            }
        }
    }

    fun onClickCropClear() {
        settingsProvider.settingsData.cropDocumentUri = null
    }
}