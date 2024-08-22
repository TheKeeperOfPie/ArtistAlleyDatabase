package com.thekeeperofpie.artistalleydatabase.cds

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.AppJson
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CdsFromMediaViewModel @Inject constructor(
    application: Application,
    cdEntryDao: CdEntryDao,
    appJson: AppJson,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    var cdEntries by mutableStateOf(emptyList<CdEntryGridModel>())

    private val mediaId = savedStateHandle.get<String>("mediaId")!!

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            cdEntries = withContext(CustomDispatchers.IO) {
                cdEntryDao.searchSeriesByMediaId(appJson, mediaId)
                    .map { CdEntryGridModel.buildFromEntry(application, it) }
            }
        }
    }
}
