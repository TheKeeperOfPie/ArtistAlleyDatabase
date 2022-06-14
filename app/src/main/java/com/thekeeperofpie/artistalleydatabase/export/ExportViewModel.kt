package com.thekeeperofpie.artistalleydatabase.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    fun scheduleExport(outputContentUri: Uri) {
        val request = OneTimeWorkRequestBuilder<ExportArtEntriesWorker>()
            .setInputData(
                Data.Builder()
                    .putString(
                        ExportArtEntriesWorker.KEY_OUTPUT_CONTENT_URI,
                        outputContentUri.toString()
                    )
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork("export_all_entries", ExistingWorkPolicy.REPLACE, request)
    }
}