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
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.export_last_canceled
import artistalleydatabase.app.generated.resources.export_last_failed
import artistalleydatabase.app.generated.resources.invalid_export_destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@Inject
class ExportViewModel(private val workManager: WorkManager) : ViewModel() {

    var exportUriString by mutableStateOf<String?>(null)
    var errorResource by mutableStateOf<Pair<StringResource, Exception?>?>(null)
    var exportRequested by mutableStateOf(false)
    var exportProgress by mutableStateOf<Float?>(null)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            workManager.getWorkInfosForUniqueWorkLiveData(ExportUtils.UNIQUE_WORK_NAME)
                .asFlow()
                .mapNotNull { it.lastOrNull() }
                .map {
                    when (it.state) {
                        WorkInfo.State.ENQUEUED -> {
                            exportRequested = true
                            0f
                        }
                        WorkInfo.State.RUNNING -> {
                            exportRequested = true
                            // Ensure that 1f is only reported via a SUCCEEDED state
                            it.progress.getFloat(ExportUtils.KEY_PROGRESS, 0f).coerceAtMost(0.99f)
                        }
                        WorkInfo.State.SUCCEEDED -> if (exportRequested) 1f else null
                        WorkInfo.State.FAILED -> {
                            exportRequested = false
                            errorResource = Res.string.export_last_failed to null
                            null
                        }
                        WorkInfo.State.CANCELLED -> {
                            exportRequested = false
                            errorResource = Res.string.export_last_canceled to null
                            null
                        }
                        WorkInfo.State.BLOCKED -> null
                    }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { exportProgress = it }
        }
    }

    fun onClickExport() {
        if (exportRequested && exportProgress != 1f) return
        val exportUriString = exportUriString ?: run {
            errorResource = Res.string.invalid_export_destination to null
            return
        }

        val request = OneTimeWorkRequestBuilder<ExportWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(
                Data.Builder()
                    .putString(ExportUtils.KEY_OUTPUT_CONTENT_URI, exportUriString)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            ExportUtils.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        exportRequested = true
        exportProgress = 0f
    }
}
