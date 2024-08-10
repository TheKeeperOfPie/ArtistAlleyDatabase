package com.thekeeperofpie.artistalleydatabase.android_utils.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters

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
    private val notificationContentIntent: () -> PendingIntent,
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
        @SuppressLint("RestrictedApi")
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

            // TODO: Prompt user for POST_NOTIFICATIONS permission
            if (ContextCompat.checkSelfPermission(
                    appContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@apply
            }

            notify(
                notificationIdFinished.id,
                NotificationCompat.Builder(appContext, notificationChannel.channel)
                    .setAutoCancel(true)
                    .setContentTitle(appContext.getString(titleRes))
                    .setSmallIcon(smallIcon)
                    .setContentIntent(notificationContentIntent())
                    .build()
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = cachedNotificationBuilder
            .setProgress(1, 0, true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationIdOngoing.id,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(notificationIdOngoing.id, notification)
        }
    }

    protected fun setProgress(progress: Int, max: Int) {
        // TODO: Throttle this before it hits system server
        setProgressAsync(
            Data.Builder()
                .putFloat(progressKey, progress / max.coerceAtLeast(1).toFloat())
                .build()
        )
        if (ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(appContext).notify(
                notificationIdOngoing.id,
                cachedNotificationBuilder.setProgress(max, progress, false).build()
            )
        }
    }
}
