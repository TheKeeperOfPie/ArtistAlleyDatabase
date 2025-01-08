package com.thekeeperofpie.artistalleydatabase.alley.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.utils.ComponentProvider
import kotlinx.coroutines.MainScope

class CustomApplication : Application(), ComponentProvider, SingletonImageLoader.Factory {

    private val scope = MainScope()

    private val applicationComponent by lazy {
        ArtistAlleyAndroidComponent::class.create(this, scope)
    }

    override fun onCreate() {
        super.onCreate()
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
        .build()

    @Suppress("UNCHECKED_CAST")
    override fun <T> singletonComponent() = applicationComponent as T
}
