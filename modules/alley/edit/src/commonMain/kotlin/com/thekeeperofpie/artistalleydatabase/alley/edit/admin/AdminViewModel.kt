package com.thekeeperofpie.artistalleydatabase.alley.edit.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.edit.remote.RemoteDataDiffer
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ExclusiveProgressJob
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.launch
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Inject
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
class AdminViewModel(
    private val database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    remoteDataDiffer: RemoteDataDiffer,
) : ViewModel() {

    private val createJob = ExclusiveProgressJob(viewModelScope, ::createDatabases)
    private val deleteFakeArtistDataJob = ExclusiveProgressJob(viewModelScope, ::deleteFakeArtistData)
    val fakeArtistFormLink = flowFromSuspend { database.fakeArtistFormLink() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val remoteSyncFile = MutableStateFlow<PlatformFile?>(null)
    internal val remoteSyncDiff = remoteSyncFile
        .mapLatest {
            if (it == null) {
                emptyList()
            } else {
                remoteDataDiffer.calculateDiff(DataYear.ANIME_EXPO_2026, it)
            }
        }
        .flowOn(dispatchers.io)
        .stateInForCompose(emptyList())

    internal fun onClickCreate() = createJob.launch()
    internal fun onClickClearFakeArtistData() = deleteFakeArtistDataJob.launch()

    internal fun onRemoteSyncFileChosen(file: PlatformFile?) {
        this.remoteSyncFile.value = file
    }

    internal fun submitRemoteDiff(diff: List<ArtistRemoteEntry>) {
        viewModelScope.launch {
            database.submitRemoteArtistData(DataYear.ANIME_EXPO_2026, diff)
            remoteSyncFile.value = null
        }
    }

    private suspend fun createDatabases() = database.databaseCreate()
    private suspend fun deleteFakeArtistData() = database.deleteFakeArtistData()
}
