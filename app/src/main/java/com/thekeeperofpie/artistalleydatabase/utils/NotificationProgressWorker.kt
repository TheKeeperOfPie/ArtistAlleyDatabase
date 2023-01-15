package com.thekeeperofpie.artistalleydatabase.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.thekeeperofpie.artistalleydatabase.MainActivity
import com.thekeeperofpie.artistalleydatabase.navigation.NavDrawerItems

abstract class NotificationProgressWorker(
    private val appContext: Context,
    params: WorkerParameters,
    private val progressKey: String,
    private val notificationChannel: NotificationChannel,
    private val notificationIdOngoing: NotificationId,
    private val notificationIdFinished: NotificationId,
    @DrawableRes private val smallIcon: Int,
    @StringRes ongoingTitle: Int,
    @StringRes private val successTitle: Int,
    @StringRes private val failureTitle: Int,
    private val notificationClickDestination: NavDrawerItems,
    private val pendingIntentRequestCode: PendingIntentRequestCode,
) : CoroutineWorker(appContext, params) {

    companion object {
        private val TAG = NotificationProgressWorker::class.java.name
    }

    private val cachedNotificationBuilder =
        NotificationCompat.Builder(appContext, notificationChannel.channel)
            .setContentTitle(appContext.getString(ongoingTitle))
            .setSmallIcon(smallIcon)
            .setOngoing(true)

    final override suspend fun doWork() = try {
        try {
            setForeground(getForegroundInfo())
        } catch (ignored: Exception) {
        }

        doWorkInternal()
    } catch (e: Exception) {
        Log.d(TAG, "Error running ${this.javaClass.name}", e)
        Result.failure()
    }.also {
        when (it) {
            is Result.Success -> notifyComplete(successTitle)
            is Result.Failure -> notifyComplete(failureTitle)
            else -> cancelNotification()
        }
    }

    abstract suspend fun doWorkInternal(): Result

    private fun cancelNotification() {
        NotificationManagerCompat.from(appContext).apply {
            cancel(notificationIdOngoing.id)
        }
    }

    private fun notifyComplete(@StringRes titleRes: Int) {
        NotificationManagerCompat.from(appContext).apply {
            cancel(notificationIdOngoing.id)
            notify(
                notificationIdFinished.id,
                NotificationCompat.Builder(appContext, notificationChannel.channel)
                    .setAutoCancel(true)
                    .setContentTitle(appContext.getString(titleRes))
                    .setSmallIcon(smallIcon)
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
        // TODO: Throttle this before it hits system server
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