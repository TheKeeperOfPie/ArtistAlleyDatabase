package com.thekeeperofpie.artistalleydatabase.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.WorkerParameters
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.notification_sync_failed_title
import artistalleydatabase.app.generated.resources.notification_sync_finished_title
import artistalleydatabase.app.generated.resources.notification_sync_ongoing_title
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.MainActivity
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems
import com.thekeeperofpie.artistalleydatabase.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.notification.NotificationIds
import com.thekeeperofpie.artistalleydatabase.notification.NotificationProgressWorker
import com.thekeeperofpie.artistalleydatabase.utils_room.DatabaseSyncer
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AssistedInject
class DatabaseSyncWorker(
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
    ongoingTitle = Res.string.notification_sync_ongoing_title,
    successTitle = Res.string.notification_sync_finished_title,
    failureTitle = Res.string.notification_sync_failed_title,
    notificationContentIntent = {
        PendingIntent.getActivity(
            appContext,
            PendingIntentRequestCodes.SYNC_MAIN_ACTIVITY_OPEN.code,
            Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setClass(appContext, MainActivity::class.java)
                putExtra(
                    MainActivity.STARTING_NAV_DESTINATION,
                    NavDrawerItems.BROWSE.id,
                )
            },
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    },
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

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): DatabaseSyncWorker
    }
}
