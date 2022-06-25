package com.thekeeperofpie.artistalleydatabase.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.thekeeperofpie.artistalleydatabase.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    var exportUriString by mutableStateOf<String?>(null)
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    var userReadable by mutableStateOf(true)

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

        workManager.enqueueUniqueWork("export_all_entries", ExistingWorkPolicy.REPLACE, request)
    }
}