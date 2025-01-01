@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import artistalleydatabase.modules.utils_compose.generated.resources.app_name
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.SharedInfra
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils_compose.CrashScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.WindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

fun main() {
    val exitEvents = MutableStateFlow(false)
    val existingExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Logger.e("AnichiveDesktop", throwable) { "Crash detected" }
        try {
            // TODO
//            settings.writeLastCrash(throwable)
        } catch (t: Throwable) {
            if (BuildVariant.isDebug()) {
                Logger.e("AnichiveDesktop", t) { "Error writing last crash" }
            }
        } finally {
            existingExceptionHandler?.uncaughtException(thread, throwable)
            exitEvents.value = true
        }
    }
    application {
        val exitEvent by exitEvents.collectAsState()
        LaunchedEffect(exitEvent) {
            if (exitEvent) {
                exitApplication()
            }
        }
        val scope = rememberCoroutineScope { Dispatchers.Main }
        val desktopComponent = DesktopComponent::class.create(scope)
        val settings = desktopComponent.settingsProvider

        SingletonImageLoader.setSafe { context ->
            ImageLoader.Builder(context)
                .components {
                    add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ ->
                        data.toString().toUri()
                    })
                    add(KtorNetworkFetcherFactory(desktopComponent.httpClient))
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
        val navigationTypeMap = desktopComponent.navigationTypeMap

        ThemeAndSingletons(desktopComponent) {
            val windowState = rememberWindowState()
            Window(
                onCloseRequest = ::exitApplication,
                title = runBlocking { getString(UtilsStrings.app_name) },
                state = windowState,
            ) {
                val windowSize = windowState.size
                val windowConfiguration = remember(windowSize) {
                    WindowConfiguration(
                        screenWidthDp = windowSize.width.takeOrElse { 600.dp },
                        screenHeightDp = windowSize.height.takeOrElse { 800.dp },
                    )
                }

                val cdEntryNavigator = desktopComponent.cdEntryNavigator
                val navHostController = rememberNavController()

                val navigationController = rememberNavigationController(navHostController)
                CompositionLocalProvider(
                    LocalWindowConfiguration provides windowConfiguration,
                    LocalNavigationController provides navigationController,
                ) {
                    Surface(modifier = Modifier.safeDrawingPadding()) {
                        SharedTransitionLayout {
                            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                                NavHost(
                                    navController = navHostController,
                                    startDestination = AnimeNavDestinations.HOME.id,
                                ) {
                                    AnimeNavigator.initialize(
                                        navigationController = navigationController,
                                        navGraphBuilder = this,
                                        upIconOption = null,
                                        navigationTypeMap = navigationTypeMap,
                                        onClickAuth = { TODO() },
                                        onClickSettings = { TODO() },
                                        onClickShowLastCrash = {
                                            navHostController.navigate(NavDestinations.CRASH.name)
                                        },
                                        component = desktopComponent,
                                        cdEntryComponent = desktopComponent,
                                    )

                                    cdEntryNavigator.initialize(
                                        onClickNav = { TODO() },
                                        navHostController = navHostController,
                                        navGraphBuilder = this,
                                        cdEntryComponent = desktopComponent,
                                    )

                                    sharedElementComposable(route = NavDestinations.CRASH.name) {
                                        SideEffect { settings.lastCrashShown.value = true }
                                        CrashScreen(
                                            crash = { settings.lastCrash.collectAsState().value },
                                            onClickBack = { navHostController.navigateUp() },
                                            onClickShare = { TODO() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeAndSingletons(
    desktopComponent: DesktopComponent,
    content: @Composable () -> Unit,
) {
    val settings = desktopComponent.settingsProvider
    DesktopTheme(settings) {
        SharedInfra(desktopComponent) {
            CompositionLocalProvider(
                LocalAnimeComponent provides desktopComponent,
                content = content,
            )
        }
    }
}

enum class NavDestinations {
    SETTINGS,
    CRASH,
}
