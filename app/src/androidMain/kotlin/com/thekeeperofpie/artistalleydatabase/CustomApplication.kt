package com.thekeeperofpie.artistalleydatabase

import android.app.Application
import android.media.AudioManager
import android.media.AudioManagerIgnoreFocus
import android.os.StrictMode
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Configuration
import artistalleydatabase.app.generated.resources.Res
import artistalleydatabase.app.generated.resources.notification_channel_export_description
import artistalleydatabase.app.generated.resources.notification_channel_export_name
import artistalleydatabase.app.generated.resources.notification_channel_import_description
import artistalleydatabase.app.generated.resources.notification_channel_import_name
import artistalleydatabase.app.generated.resources.notification_channel_info_description
import artistalleydatabase.app.generated.resources.notification_channel_info_name
import artistalleydatabase.app.generated.resources.notification_channel_sync_description
import artistalleydatabase.app.generated.resources.notification_channel_sync_name
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.toUri
import coil3.util.DebugLogger
import com.thekeeperofpie.anichive.BuildConfig
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.PlatformOAuthStore
import com.thekeeperofpie.artistalleydatabase.notification.NotificationChannels
import com.thekeeperofpie.artistalleydatabase.utils.ComponentProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class CustomApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory,
    ComponentProvider {

    companion object {
        const val TAG = "ArtistAlleyDatabase"
        private const val DEBUG_COIL = false
    }

    val scope = MainScope()

    private val applicationComponent by lazy {
        ApplicationComponent::class.create(this)
    }

    private lateinit var audioManager: AudioManager

    override val workManagerConfiguration by lazy {
        Configuration.Builder()
            .setWorkerFactory(applicationComponent.injectedWorkerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val existingExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                applicationComponent.settingsProvider.writeLastCrash(throwable)
            } catch (t: Throwable) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Error writing last crash", t)
                }
            }
            existingExceptionHandler?.uncaughtException(thread, throwable)
        }

        filesDir.resolve("art_entry_images").mkdirs()
        filesDir.resolve("cd_entry_images").mkdirs()

        // Clear any old cropped images
        try {
            filesDir.resolve("art_entry_images/crop").deleteRecursively()
        } catch (ignored: Throwable) {
        }
        try {
            filesDir.resolve("cd_entry_images/crop").deleteRecursively()
        } catch (ignored: Throwable) {
        }

        // TODO: Figure out a real StrictMode policy
        // Ignore external file:// URIs policy
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())

        PlatformOAuthStore.setShareTargetEnabled(this, false)

        runBlocking {
            NotificationManagerCompat.from(this@CustomApplication)
                .createNotificationChannelsCompat(
                    listOf(
                        NotificationChannelCompat.Builder(
                            NotificationChannels.EXPORT.channel,
                            NotificationManagerCompat.IMPORTANCE_LOW
                        )
                            .setName(getString(Res.string.notification_channel_export_name))
                            .setDescription(getString(Res.string.notification_channel_export_description))
                            .setShowBadge(false)
                            .build(),
                        NotificationChannelCompat.Builder(
                            NotificationChannels.IMPORT.channel,
                            NotificationManagerCompat.IMPORTANCE_LOW
                        )
                            .setName(getString(Res.string.notification_channel_import_name))
                            .setDescription(getString(Res.string.notification_channel_import_description))
                            .setShowBadge(false)
                            .build(),
                        NotificationChannelCompat.Builder(
                            NotificationChannels.SYNC.channel,
                            NotificationManagerCompat.IMPORTANCE_LOW
                        )
                            .setName(getString(Res.string.notification_channel_sync_name))
                            .setDescription(getString(Res.string.notification_channel_sync_description))
                            .setShowBadge(false)
                            .build(),
                        NotificationChannelCompat.Builder(
                            NotificationChannels.INFO.channel,
                            NotificationManagerCompat.IMPORTANCE_DEFAULT
                        )
                            .setName(getString(Res.string.notification_channel_info_name))
                            .setDescription(getString(Res.string.notification_channel_info_description))
                            .setShowBadge(true)
                            .build(),
                    )
                )
        }
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(context)
        .components {
            add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ -> data.toString().toUri() })
        }
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizePercent(0.02)
                .build()
        }
        .crossfade(true)
        .run {
            if (!DEBUG_COIL) return@run this
            @Suppress("KotlinConstantConditions")
            when (BuildConfig.BUILD_TYPE) {
                "debug", "internal" -> logger(DebugLogger())
                else -> this
            }
        }
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

    @Suppress("UNCHECKED_CAST")
    override fun <T> singletonComponent() = applicationComponent as T
}
