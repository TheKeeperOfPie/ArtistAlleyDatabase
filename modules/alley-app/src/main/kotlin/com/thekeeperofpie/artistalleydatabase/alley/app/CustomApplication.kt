package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.alley.DataInitializer
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import javax.inject.Inject

@HiltAndroidApp
class CustomApplication : Application(), ScopedApplication, SingletonImageLoader.Factory {

    override val scope = MainScope()
    override val app = this

    @Inject
    lateinit var dataInitializer: DataInitializer

    override fun onCreate() {
        super.onCreate()
        dataInitializer.init()
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(context)
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
        .build()
}
