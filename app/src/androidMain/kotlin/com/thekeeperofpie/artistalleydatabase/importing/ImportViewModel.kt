package com.thekeeperofpie.artistalleydatabase.importing

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
import artistalleydatabase.app.generated.resources.import_last_canceled
import artistalleydatabase.app.generated.resources.import_last_failed
import artistalleydatabase.app.generated.resources.invalid_import_source
import com.thekeeperofpie.artistalleydatabase.utils.DatabaseSyncWorker
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@Inject
class ImportViewModel(private val workManager: WorkManager) : ViewModel() {

    var importUriString by mutableStateOf<String?>(null)
    var dryRun by mutableStateOf(true)
    var replaceAll by mutableStateOf(false)
    var syncAfter by mutableStateOf(true)
    var importRequested by mutableStateOf(false)
    var importProgress by mutableStateOf<Float?>(null)
    var errorResource by mutableStateOf<Pair<StringResource, Exception?>?>(null)

    init {
        viewModelScope.launch(Dispatchers.Main) {
            workManager.getWorkInfosForUniqueWorkLiveData(ImportUtils.UNIQUE_WORK_NAME)
                .asFlow()
                .mapNotNull { it.lastOrNull() }
                .map {
                    when (it.state) {
                        WorkInfo.State.ENQUEUED -> {
                            importRequested = true
                            0f
                        }
                        WorkInfo.State.RUNNING -> {
                            importRequested = true
                            // Ensure that 1f is only reported via a SUCCEEDED state
                            it.progress.getFloat(ImportUtils.KEY_PROGRESS, 0f).coerceAtMost(0.99f)
                        }
                        WorkInfo.State.SUCCEEDED -> if (importRequested) 1f else null
                        WorkInfo.State.FAILED -> {
                            importRequested = false
                            errorResource = Res.string.import_last_failed to null
                            null
                        }
                        WorkInfo.State.CANCELLED -> {
                            importRequested = false
                            errorResource = Res.string.import_last_canceled to null
                            null
                        }
                        WorkInfo.State.BLOCKED -> null
                    }
                }
                .flowOn(Dispatchers.IO)
                .collectLatest { importProgress = it }
        }
    }

    fun onClickImport() {
        if (importRequested && importProgress != 1f) return
        val importUriString = importUriString ?: run {
            errorResource = Res.string.invalid_import_source to null
            return
        }

        val importRequest = OneTimeWorkRequestBuilder<ImportWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(
                Data.Builder()
                    .putString(ImportUtils.KEY_INPUT_CONTENT_URI, importUriString)
                    .putBoolean(ImportUtils.KEY_DRY_RUN, dryRun)
                    .putBoolean(ImportUtils.KEY_REPLACE_ALL, replaceAll)
                    .build()
            )
            .build()

        val additionalRequests = if (syncAfter) {
            listOf(
                OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build()
            )
        } else {
            emptyList()
        }

        workManager.beginUniqueWork(
            ImportUtils.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            importRequest
        )
            .then(additionalRequests)
            .enqueue()
        importRequested = true
        importProgress = 0f
    }
}
