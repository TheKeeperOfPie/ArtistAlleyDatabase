package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import coil3.ImageLoader
import coil3.ImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope

@HiltAndroidApp
class CustomApplication : Application(), ScopedApplication, ImageLoaderFactory {

    override val scope = MainScope()
    override val app = this

    override fun newImageLoader() = ImageLoader.Builder(this)
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
        .build()
}
