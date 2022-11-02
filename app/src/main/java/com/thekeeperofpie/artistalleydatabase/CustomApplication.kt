package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.utils.NotificationChannels
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@HiltAndroidApp
class CustomApplication : Application(), Configuration.Provider, ScopedApplication {

    companion object {
        const val TAG = "ArtistAlleyDatabase"
    }

    override val scope = MainScope()

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        filesDir.toPath().resolve("art_entry_images").toFile().mkdirs()
        filesDir.toPath().resolve("cd_entry_images").toFile().mkdirs()

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