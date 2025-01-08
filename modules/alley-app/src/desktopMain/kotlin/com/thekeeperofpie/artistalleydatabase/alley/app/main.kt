package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.memory.MemoryCache
import coil3.request.Options
import coil3.request.crossfade
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationController
import kotlinx.coroutines.Dispatchers
import kotlinx.io.asInputStream
import okio.FileSystem
import okio.buffer
import okio.source

fun main() {
    application {
        val scope = rememberCoroutineScope { Dispatchers.Main }
        val component = ArtistAlleyDesktopComponent::class.create(scope)

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(object : Fetcher.Factory<Uri> {
                        override fun create(
                            data: Uri,
                            options: Options,
                            imageLoader: ImageLoader,
                        ): Fetcher? {
                            if (data.scheme != "jar") return null
                            return object : Fetcher {
                                override suspend fun fetch(): FetchResult? {
                                    val source = component.appFileSystem.openUriSource(Uri.parse(data.toString()))
                                        ?.asInputStream()?.source()?.buffer() ?: return null
                                    return SourceFetchResult(
                                        source = ImageSource(
                                            source = source,
                                            fileSystem = FileSystem.SYSTEM
                                        ),
                                        mimeType = null,
                                        dataSource = DataSource.DISK,
                                    )
                                }
                            }
                        }
                    })
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizeBytes(1024 * 1024)
                        .build()
                }
                .diskCache(null) // TODO
                .crossfade(true)
                .build()
        }

        val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
        MaterialTheme(colorScheme = colorScheme) {
            val windowState = rememberWindowState()
            Window(
                onCloseRequest = ::exitApplication,
                title = "Artist Alley",
                state = windowState,
            ) {
                val windowSize = windowState.size
                val windowConfiguration = remember(windowSize) {
                    WindowConfiguration(
                        screenWidthDp = windowSize.width.takeOrElse { 600.dp },
                        screenHeightDp = windowSize.height.takeOrElse { 800.dp },
                    )
                }

                val navHostController = rememberNavController()
                val navigationController = rememberNavigationController(navHostController)
                CompositionLocalProvider(
                    LocalWindowConfiguration provides windowConfiguration,
                    LocalNavigationController provides navigationController,
                ) {
                    ArtistAlleyAppScreen(component)
                }
            }
        }
    }
}
