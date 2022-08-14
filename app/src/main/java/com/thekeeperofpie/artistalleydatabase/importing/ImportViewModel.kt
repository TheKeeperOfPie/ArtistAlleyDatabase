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
class ImportViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    var importUriString by mutableStateOf<String?>(null)
    var dryRun by mutableStateOf(true)
    var replaceAll by mutableStateOf(false)
    var importRequested by mutableStateOf(false)
    var importProgress by mutableStateOf<Float?>(null)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

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
                            it.progress.getFloat("progress", 0f).coerceAtMost(0.99f)
                        }
                        WorkInfo.State.SUCCEEDED -> if (importRequested) 1f else null
                        WorkInfo.State.FAILED -> {
                            importRequested = false
                            errorResource = R.string.import_last_failed to null
                            null
                        }
                        WorkInfo.State.CANCELLED -> {
                            importRequested = false
                            errorResource = R.string.import_last_canceled to null
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
            errorResource = R.string.invalid_import_source to null
            return
        }

        val request = OneTimeWorkRequestBuilder<ImportWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(
                Data.Builder()
                    .putString(ImportUtils.KEY_INPUT_CONTENT_URI, importUriString)
                    .putBoolean(ImportUtils.KEY_DRY_RUN, dryRun)
                    .putBoolean(ImportUtils.KEY_REPLACE_ALL, replaceAll)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            ImportUtils.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        importRequested = true
        importProgress = 0f
    }
}
