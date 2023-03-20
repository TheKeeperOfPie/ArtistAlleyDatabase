package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.os.StrictMode
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.notification.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@HiltAndroidApp
class CustomApplication : Application(), Configuration.Provider, ScopedApplication {

    companion object {
        const val TAG = "ArtistAlleyDatabase"
    }

    override val scope = MainScope()
    override val app = this

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        filesDir.resolve("art_entry_images").mkdirs()
        filesDir.resolve("cd_entry_images").mkdirs()

        // TODO: Figure out a real StrictMode policy
        // Ignore external file:// URIs policy
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        NotificationManagerCompat.from(this)
            .createNotificationChannelsCompat(
                listOf(
                    NotificationChannelCompat.Builder(
                        NotificationChannels.EXPORT.channel,
                        NotificationManagerCompat.IMPORTANCE_LOW
                    )
                        .setName(getString(R.string.notification_channel_export_name))
                        .setDescription(getString(R.string.notification_channel_export_description))
                        .setShowBadge(false)
                        .build(),
                    NotificationChannelCompat.Builder(
                        NotificationChannels.IMPORT.channel,
                        NotificationManagerCompat.IMPORTANCE_LOW
                    )
                        .setName(getString(R.string.notification_channel_import_name))
                        .setDescription(getString(R.string.notification_channel_import_description))
                        .setShowBadge(false)
                        .build(),
                    NotificationChannelCompat.Builder(
                        NotificationChannels.SYNC.channel,
                        NotificationManagerCompat.IMPORTANCE_LOW
                    )
                        .setName(getString(R.string.notification_channel_sync_name))
                        .setDescription(getString(R.string.notification_channel_sync_description))
                        .setShowBadge(false)
                        .build(),
                )
            )
    }
}