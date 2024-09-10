@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.desktop

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import artistalleydatabase.modules.utils_compose.generated.resources.app_name
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.map.Mapper
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.toUri
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionCharacters
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionStaff
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionVoiceActor
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaGenreDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.LocalMediaTagDialogController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGenrePreview
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaTagPreview
import com.thekeeperofpie.artistalleydatabase.anime.utils.FullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.anime.utils.LocalFullscreenImageHandler
import com.thekeeperofpie.artistalleydatabase.markdown.LocalMarkdown
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.CrashScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalAppConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.LocalImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberImageColorsState
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

fun main() = application {
    val scope = rememberCoroutineScope { Dispatchers.Main }
    val desktopComponent = DesktopComponent::class.create(scope)

    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components {
                add(Mapper<com.eygraber.uri.Uri, coil3.Uri> { data, _ -> data.toString().toUri() })
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

    val cdEntryNavigator = desktopComponent.cdEntryNavigator
    val ignoreController = desktopComponent.ignoreController
    val markdown = desktopComponent.markdown
    val mediaGenreDialogController = desktopComponent.mediaGenreDialogController
    val mediaTagDialogController = desktopComponent.mediaTagDialogController
    val navigationTypeMap = desktopComponent.navigationTypeMap
    val settings = desktopComponent.settingsProvider

    val fullScreenImageHandler = remember { FullscreenImageHandler() }

    Window(
        onCloseRequest = ::exitApplication,
        title = runBlocking { getString(UtilsStrings.app_name) },
    ) {
        val navHostController = rememberNavController()
        val languageOptionMedia by settings.languageOptionMedia.collectAsState()
        val languageOptionCharacters by settings.languageOptionCharacters.collectAsState()
        val languageOptionStaff by settings.languageOptionStaff.collectAsState()
        val languageOptionVoiceActor by settings.languageOptionVoiceActor.collectAsState()
        val showFallbackVoiceActor by settings.showFallbackVoiceActor.collectAsState()

        val size = window.size
        val density = LocalDensity.current
        val appConfiguration = remember(size) {
            AppConfiguration(
                screenHeightDp = density.run { size.height.toDp() }.value.toInt(),
                screenWidthDp = density.run { size.width.toDp() }.value.toInt(),
            )
        }

        val navigationCallback =
            remember(languageOptionMedia, languageOptionCharacters, languageOptionStaff) {
                AnimeNavigator.NavigationCallback(
                    navHostController = navHostController,
                    cdEntryNavigator = cdEntryNavigator,
                )
            }

        DesktopTheme(settings) {

            val imageColorsState = rememberImageColorsState()
            CompositionLocalProvider(
                LocalAppConfiguration provides appConfiguration,
                LocalNavHostController provides navHostController,
                LocalMediaTagDialogController provides mediaTagDialogController,
                LocalMediaGenreDialogController provides mediaGenreDialogController,
                LocalLanguageOptionMedia provides languageOptionMedia,
                LocalLanguageOptionCharacters provides languageOptionCharacters,
                LocalLanguageOptionStaff provides languageOptionStaff,
                LocalLanguageOptionVoiceActor provides
                        (languageOptionVoiceActor to showFallbackVoiceActor),
                LocalNavigationCallback provides navigationCallback,
                LocalFullscreenImageHandler provides fullScreenImageHandler,
                LocalMarkdown provides markdown,
                LocalImageColorsState provides imageColorsState,
                LocalIgnoreController provides ignoreController,
                LocalAnimeComponent provides desktopComponent,
            ) {
                Surface(modifier = Modifier.safeDrawingPadding()) {
                    SharedTransitionLayout {
                        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                            NavHost(
                                navController = navHostController,
                                startDestination = AnimeNavDestinations.HOME.id,
                            ) {
                                AnimeNavigator.initialize(
                                    navHostController = navHostController,
                                    navGraphBuilder = this,
                                    upIconOption = null,
                                    navigationTypeMap = navigationTypeMap,
                                    onClickAuth = { TODO() },
                                    onClickSettings = { TODO() },
                                    onClickShowLastCrash = {
                                        navHostController.navigate(NavDestinations.CRASH.name)
                                    },
                                    animeComponent = desktopComponent,
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

                val tagShown = mediaTagDialogController.tagShown
                if (tagShown != null) {
                    MediaTagPreview(tag = tagShown) {
                        mediaTagDialogController.tagShown = null
                    }
                }

                val genreShown = mediaGenreDialogController.genreShown
                if (genreShown != null) {
                    MediaGenrePreview(genre = genreShown) {
                        mediaGenreDialogController.genreShown = null
                    }
                }

                fullScreenImageHandler.ImageDialog()
            }
        }
    }
}

enum class NavDestinations {
    SETTINGS,
    CRASH,
}
