package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.request.Options
import coil3.request.crossfade
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditApp
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.DebugTestData
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.mouseNavigationEvents
import dev.zacsweers.metro.createGraphFactory
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.io.asInputStream
import okio.FileSystem
import okio.buffer
import okio.source

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    application {
        val scope = rememberCoroutineScope { Dispatchers.Main }
        val graph = createGraphFactory<ArtistAlleyFormDesktopGraph.Factory>()
            .create(scope)

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(Mapper<PlatformImageKey, PlatformFile> { data, _ ->
                        PlatformImageCache[data]
                    })
                    add(object : Fetcher.Factory<Uri> {
                        override fun create(
                            data: Uri,
                            options: Options,
                            imageLoader: ImageLoader,
                        ): Fetcher? {
                            if (data.scheme != "jar") return null
                            return object : Fetcher {
                                override suspend fun fetch(): FetchResult? {
                                    val source =
                                        graph.appFileSystem.openUriSource(Uri.parse(data.toString()))
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
                    addPlatformFileSupport()
                }
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizeBytes(1000 * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }

        LaunchedEffect(Unit) {
            runBlocking {
                DebugTestData.initialize(graph.editDatabase, graph.formDatabase)
            }
        }

        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Artist Alley Form",
            state = windowState,
        ) {
            AlleyTheme(appTheme = { AppThemeSetting.AUTO }) {
                val windowSize = windowState.size
                val windowConfiguration = remember(windowSize) {
                    WindowConfiguration(
                        screenWidthDp = windowSize.width.takeOrElse { 600.dp },
                        screenHeightDp = windowSize.height.takeOrElse { 800.dp },
                    )
                }

                CompositionLocalProvider(
                    LocalWindowConfiguration provides windowConfiguration,
                ) {
                    Box(modifier = Modifier.mouseNavigationEvents()) {
                        var showForm by rememberSaveable { mutableStateOf(false) }
                        val navStack = rememberArtistAlleyEditTopLevelStacks()
                        ArtistAlleyEditApp(
                            graph = graph,
                            navStack = navStack,
                            onDebugOpenForm = { showForm = true },
                        )

                        // Simulates opening the form app in a different browser tab
                        if (showForm) {
                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                onBackCompleted = { showForm = false },
                            )
                            Surface {
                                Column {
                                    IconButton(onClick = { showForm = false }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                        )
                                    }
                                    ArtistAlleyFormApp(graph = graph)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
