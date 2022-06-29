package com.thekeeperofpie.artistalleydatabase.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    companion object {
        private const val UNIQUE_WORK_NAME = "export_all_entries"
    }

    var exportUriString by mutableStateOf<String?>(null)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    var userReadable by mutableStateOf(true)
    var exportRequested by mutableStateOf(false)
    var exportProgress by mutableStateOf<Float?>(null)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            workManager.getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
                .asFlow()
                .mapNotNull { it.firstOrNull() }
                .map {
                    when (it.state) {
                        WorkInfo.State.ENQUEUED -> 0f
                        WorkInfo.State.RUNNING -> it.progress.getFloat("progress", 0f)
                        WorkInfo.State.SUCCEEDED -> 1f
                        WorkInfo.State.FAILED,
                        WorkInfo.State.BLOCKED,
                        WorkInfo.State.CANCELLED -> null
                    }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { exportProgress = it }
        }
    }

    fun onClickExport() {
        val exportUriString = exportUriString ?: run {
            errorResource = R.string.invalid_export_destination to null
            return
        }

        val request = if (userReadable) {
            OneTimeWorkRequestBuilder<ExportUserReadableWorker>()
        } else {
            OneTimeWorkRequestBuilder<ExportAppDataWorker>()
        }
            .setInputData(
                Data.Builder()
                    .putString(ExportUtils.KEY_OUTPUT_CONTENT_URI, exportUriString)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        exportRequested = true
        exportProgress = 0f
    }
}