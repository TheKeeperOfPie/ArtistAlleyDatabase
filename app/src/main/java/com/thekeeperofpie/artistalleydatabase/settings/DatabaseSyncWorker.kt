package com.thekeeperofpie.artistalleydatabase.settings

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.android_utils.persistence.DatabaseSyncer
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.utils.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.utils.NotificationIds
import com.thekeeperofpie.artistalleydatabase.utils.NotificationProgressWorker
import com.thekeeperofpie.artistalleydatabase.utils.PendingIntentRequestCodes
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class DatabaseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncers: Set<@JvmSuppressWildcards DatabaseSyncer>,
) : NotificationProgressWorker(
    appContext = appContext,
    params = params,
    progressKey = KEY_PROGRESS,
    notificationChannel = NotificationChannels.SYNC,
    notificationIdOngoing = NotificationIds.SYNC_ONGOING,
    notificationIdFinished = NotificationIds.SYNC_FINISHED,
    smallIcon = R.drawable.baseline_sync_24,
    ongoingTitle = R.string.notification_sync_ongoing_title,
    successTitle = R.string.notification_sync_finished_title,
    failureTitle = R.string.notification_sync_failed_title,
    notificationClickDestination = NavDrawerItems.Browse,
    pendingIntentRequestCode = PendingIntentRequestCodes.SYNC_MAIN_ACTIVITY_OPEN,
) {

    companion object {
        const val UNIQUE_WORK_NAME = "database_fetch"

        private const val KEY_PROGRESS = "progress"
    }

    override suspend fun doWorkInternal(): Result {
        withContext(Dispatchers.IO) {
            val maxProgressValues = syncers.map { it.getMaxProgress() }
            var initialProgress = 0
            val maxProgress = maxProgressValues.sum()
            syncers.forEachIndexed { index, syncer ->
                val nextProgress = initialProgress + maxProgressValues[index]
                syncer.sync(
                    initialProgress = initialProgress,
                    maxProgress = nextProgress,
                    setProgress = ::setProgress,
                )
                initialProgress = nextProgress
                setProgress(initialProgress, maxProgress)
            }
        }

        return Result.success()
    }
}