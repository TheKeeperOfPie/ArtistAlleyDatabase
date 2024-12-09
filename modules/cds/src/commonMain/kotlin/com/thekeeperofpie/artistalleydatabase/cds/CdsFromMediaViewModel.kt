package com.thekeeperofpie.artistalleydatabase.cds

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class CdsFromMediaViewModel(
    appFileSystem: AppFileSystem,
    cdEntryDao: CdEntryDao,
    json: Json,
    @Assisted mediaId: String,
): ViewModel() {

    var cdEntries by mutableStateOf(emptyList<CdEntryGridModel>())

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            cdEntries = withContext(CustomDispatchers.IO) {
                cdEntryDao.searchSeriesByMediaId(json, mediaId)
                    .map { CdEntryGridModel.buildFromEntry(appFileSystem, it) }
            }
        }
    }
}
