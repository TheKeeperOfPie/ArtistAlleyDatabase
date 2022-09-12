package com.thekeeperofpie.artistalleydatabase.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.MainActivity
import com.thekeeperofpie.artistalleydatabase.R
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems

abstract class ImportExportWorker(
    private val appContext: Context,
    params: WorkerParameters,
    private val progressKey: String,
    private val notificationChannel: NotificationChannel,
    private val notificationIdOngoing: NotificationId,
    private val notificationIdFinished: NotificationId,
    @StringRes ongoingTitle: Int,
    @StringRes private val finishedTitle: Int,
    private val notificationClickDestination: NavDrawerItems,
    private val pendingIntentRequestCode: PendingIntentRequestCode,
) :
    CoroutineWorker(appContext, params) {

    private val cachedNotificationBuilder =
        NotificationCompat.Builder(appContext, notificationChannel.channel)
            .setContentTitle(appContext.getString(ongoingTitle))
            .setSmallIcon(R.drawable.baseline_import_export_24)
            .setOngoing(true)

    protected fun notifyComplete() {
        NotificationManagerCompat.from(appContext).apply {
            cancel(notificationIdOngoing.id)
            notify(
                notificationIdFinished.id,
                NotificationCompat.Builder(appContext, notificationChannel.channel)
                    .setAutoCancel(true)
                    .setContentTitle(
                        appContext.getString(finishedTitle)
                    )
                    .setSmallIcon(R.drawable.baseline_import_export_24)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            appContext,
                            pendingIntentRequestCode.code,
                            Intent().apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                setClass(appContext, MainActivity::class.java)
                                putExtra(
                                    MainActivity.STARTING_NAV_DESTINATION,
                                    notificationClickDestination.id
                                )
                            },
                            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                    .build()
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            notificationIdOngoing.id,
            cachedNotificationBuilder
                .setProgress(1, 0, true)
                .build()
        )
    }

    protected fun setProgress(progress: Int, max: Int) {
        // TODO: Multi-module progress
        setProgressAsync(
            Data.Builder()
                .putFloat(progressKey, progress / max.coerceAtLeast(1).toFloat())
                .build()
        )
        NotificationManagerCompat.from(appContext).notify(
            notificationIdOngoing.id,
            cachedNotificationBuilder.setProgress(max, progress, false).build()
        )
    }
}