package com.thekeeperofpie.artistalleydatabase.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumEntryDao
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mediaEntryDao: MediaEntryDao,
    private val characterEntryDao: CharacterEntryDao,
    private val albumEntryDao: AlbumEntryDao,
    private val artistEntryDao: ArtistEntryDao,
    private val workManager: WorkManager,
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
            artistEntryDao.deleteAll()
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
            when (databaseType) {
                SettingsScreen.DatabaseType.ANILIST -> {
                    mediaEntryDao.delete(id)
                    characterEntryDao.delete(id)
                }
                SettingsScreen.DatabaseType.VGMDB -> {
                    albumEntryDao.delete(id)
                    artistEntryDao.delete(id)
                }
            }
        }
    }
}