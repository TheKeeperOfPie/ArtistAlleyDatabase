package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToNavigation
import androidx.navigation.compose.rememberNavController
import artistalleydatabase.modules.alley.data.generated.resources.Res
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
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyAppScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationController
import kotlinx.browser.document
import kotlinx.browser.window
import okio.Buffer
import okio.fakefilesystem.FakeFileSystem
import org.jetbrains.compose.resources.ExperimentalResourceApi

private val fakeFileSystem = FakeFileSystem()

private lateinit var artistImageCache: ArtistImageCache

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val scope = rememberCoroutineScope()
        val component = ArtistAlleyWasmJsComponent::class.create(scope)
        artistImageCache = component.artistImageCache

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .crossfade(false)
                .components {
                    add(object : Fetcher.Factory<Uri> {
                        @OptIn(ExperimentalResourceApi::class)
                        override fun create(
                            data: Uri,
                            options: Options,
                            imageLoader: ImageLoader,
                        ): Fetcher? {
                            val filePath = data.path?.substringAfter(
                                "artistalleydatabase.modules.alley.data.generated.resources/",
                                "",
                            )
                            if (filePath.isNullOrBlank()) return null
                            return object : Fetcher {
                                override suspend fun fetch(): FetchResult? {
                                    val buffer = Buffer()
                                    buffer.write(Res.readBytes(filePath))
                                    return SourceFetchResult(
                                        source = ImageSource(buffer, fakeFileSystem),
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
                        .maxSizeBytes(1000 * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }

        val appTheme by component.settings.appTheme.collectAsStateWithLifecycle()
        AppTheme(appTheme = { appTheme }) {
            val windowSize = LocalWindowInfo.current.containerSize
            val density = LocalDensity.current
            val windowConfiguration = remember(windowSize, density) {
                WindowConfiguration(
                    screenWidthDp = density.run { windowSize.width.toDp() },
                    screenHeightDp = density.run { windowSize.height.toDp() },
                )
            }

            val navHostController = rememberNavController()
            val navigationController = rememberNavigationController(navHostController)
            CompositionLocalProvider(
                LocalWindowConfiguration provides windowConfiguration,
                LocalNavigationController provides navigationController,
            ) {
                ArtistAlleyAppScreen(component, navHostController)
                LaunchedEffect(navHostController) {
                    window.bindToNavigation(navHostController)
                }
            }
        }
    }
}
