package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.media.AudioManager
import android.media.AudioManagerIgnoreFocus
import android.os.StrictMode
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.thekeeperofpie.anichive.R
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.settings.SettingsProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltAndroidApp
class CustomApplication : Application(), Configuration.Provider, ScopedApplication,
    ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    companion object {
        const val TAG = "ArtistAlleyDatabase"
    }

    override val scope = MainScope()
    override val app = this

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settings: SettingsProvider

    private lateinit var audioManager: AudioManager

    override val workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        filesDir.resolve("art_entry_images").mkdirs()
        filesDir.resolve("cd_entry_images").mkdirs()

        // TODO: Figure out a real StrictMode policy
        // Ignore external file:// URIs policy
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        AniListOAuthStore.setShareTargetEnabled(this, false)

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
                    NotificationChannelCompat.Builder(
                        NotificationChannels.INFO.channel,
                        NotificationManagerCompat.IMPORTANCE_DEFAULT
                    )
                        .setName(getString(R.string.notification_channel_info_name))
                        .setDescription(getString(R.string.notification_channel_info_description))
                        .setShowBadge(true)
                        .build(),
                )
            )
    }

    override fun newImageLoader() = ImageLoader.Builder(this)
        .okHttpClient(
            if (settings.screenshotMode.value) {
                // Fails all calls, required in production to allow exact screenshots for release
                object : OkHttpClient() {
                    override fun newCall(request: Request): Call {
                        return okHttpClient.newCall(
                            request.newBuilder().get().url("127.0.0.1").build()
                        )
                    }
                }
            } else {
                okHttpClient
            }
        )
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizePercent(0.02)
                .build()
        }
        .crossfade(true)
        .build()

    override fun getSystemService(name: String): Any? {
        val service = super.getSystemService(name)
        if (service is AudioManager) {
            if (!::audioManager.isInitialized) {
                audioManager = try {
                    AudioManagerIgnoreFocus(service)
                } catch (ignored: Throwable) {
                    service
                }
            }
            return audioManager
        }
        return service
    }
}
