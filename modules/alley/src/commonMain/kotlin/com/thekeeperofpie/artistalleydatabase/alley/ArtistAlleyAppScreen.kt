package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination.Images.Type.StampRally
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistMerchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistSeriesScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogScreen
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesScreen
import com.thekeeperofpie.artistalleydatabase.alley.import.ImportScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapScreen
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementDialog
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
object ArtistAlleyAppScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        navHostController: NavHostController,
        rootSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navigationController = LocalNavigationController.current
            val onOpenArtist = { entry: ArtistEntry, imageIndex: Int? ->
                navigationController.navigate(
                    AlleyDestination.ArtistDetails(entry, imageIndex)
                )
            }
            val onOpenSeries = { year: DataYear?, series: String ->
                navigationController.navigate(
                    AlleyDestination.Series(year, series)
                )
            }
            val onOpenMerch = { year: DataYear?, merch: String ->
                navigationController.navigate(
                    AlleyDestination.Merch(year, merch)
                )
            }
            val onOpenStampRally = { entry: StampRallyEntry, initialImageIndex: String? ->
                navigationController.navigate(
                    AlleyDestination.StampRallyDetails(
                        entry = entry,
                        initialImageIndex = initialImageIndex
                    )
                )
            }
            val onOpenExport = { navigationController.navigate(AlleyDestination.Export) }
            val onOpenChangelog = { navigationController.navigate(AlleyDestination.Changelog) }
            val onOpenSettings = { navigationController.navigate(AlleyDestination.Settings) }
            Column(modifier = Modifier.fillMaxSize()) {
                SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                    val languageOption by graph.settings.languageOption
                        .collectAsStateWithLifecycle()
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this,
                        LocalLanguageOptionMedia provides languageOption,
                    ) {
                        NavHost(navHostController, AlleyDestination.Home) {
                            val navigationTypeMap = graph.navigationTypeMap
                            sharedElementComposable<AlleyDestination.Home>(
                                navigationTypeMap = navigationTypeMap,
                                enterTransition = null,
                                exitTransition = null,
                            ) {
                                AlleyRootScreen(
                                    graph = graph,
                                    snackbarHostState = rootSnackbarHostState,
                                    onOpenArtist = onOpenArtist,
                                    onOpenSeries = onOpenSeries,
                                    onOpenMerch = onOpenMerch,
                                    onOpenStampRally = onOpenStampRally,
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }

                            sharedElementComposable<AlleyDestination.ArtistDetails>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.ArtistDetails>()
                                ArtistDetailsScreen(
                                    graph = graph,
                                    route = route,
                                    entrySavedStateHandle = it.savedStateHandle,
                                    onOpenArtist = { year, artistId ->
                                        navigationController.navigate(
                                            AlleyDestination.ArtistDetails(
                                                year = year,
                                                id = artistId,
                                                booth = null,
                                                name = null,
                                                images = null,
                                                imageIndex = null,
                                            )
                                        )
                                    },
                                    onOpenMerch = onOpenMerch,
                                    onOpenSeries = onOpenSeries,
                                    onOpenStampRally = { onOpenStampRally(it, null) },
                                    onOpenOtherYear = {
                                        navigationController
                                            .navigate(
                                                route.copy(
                                                    year = it,
                                                    booth = null,
                                                    name = null,
                                                    images = null,
                                                )
                                            )
                                    },
                                    onOpenMap = {
                                        navigationController.navigate(
                                            AlleyDestination.ArtistMap(route.id)
                                        )
                                    },
                                    onOpenImages = { year, artistId, booth, name, images, imageIndex ->
                                        navigationController.navigate(
                                            AlleyDestination.Images(
                                                year = year,
                                                id = route.id,
                                                type = AlleyDestination.Images.Type.Artist(
                                                    // TODO: Does this have to be passed in separately?
                                                    id = artistId,
                                                    booth = booth,
                                                    name = name,
                                                ),
                                                images = images,
                                                initialImageIndex = imageIndex,
                                            )
                                        )
                                    },
                                    onNavigateUp = navigationController::navigateUp,
                                )
                            }

                            sharedElementComposable<AlleyDestination.ArtistsList>(navigationTypeMap) {
                                val route = it.toRoute<AlleyDestination.ArtistsList>()
                                ArtistSearchScreen(
                                    graph = graph,
                                    lockedYear = route.year,
                                    lockedSeries = null,
                                    lockedMerch = null,
                                    isRoot = false,
                                    lockedSerializedBooths = route.serializedBooths,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickBack = navigationController::navigateUp,
                                    onOpenArtist = { artist, imageIndex ->
                                        navigationController.navigate(
                                            AlleyDestination.ArtistDetails(artist, imageIndex)
                                        )
                                    },
                                    onOpenMerch = onOpenMerch,
                                    onOpenSeries = onOpenSeries,
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }

                            sharedElementComposable<AlleyDestination.ArtistMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                ArtistMapScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.ArtistMap>(),
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = { entry, imageIndex ->
                                        onOpenArtist(entry.artist, imageIndex)
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.Changelog>(navigationTypeMap) {
                                ChangelogScreen(
                                    graph = graph,
                                    onClickBack = navigationController::navigateUp,
                                    onClickSeries = {
                                        navigationController.navigate(
                                            AlleyDestination.Series(
                                                DataYear.ANIME_EXPO_2026,
                                                it,
                                            )
                                        )
                                    },
                                    onClickMerch = {
                                        navigationController.navigate(
                                            AlleyDestination.Merch(
                                                DataYear.ANIME_EXPO_2026,
                                                it,
                                            )
                                        )
                                    },
                                    onClickArtist = {
                                        navigationController.navigate(
                                            AlleyDestination.ArtistDetails(
                                                year = DataYear.ANIME_EXPO_2026,
                                                id = it.artistId.toString(),
                                                booth = it.booth,
                                                name = it.name,
                                            )
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.Images>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                ImagesScreen(
                                    route = it.toRoute<AlleyDestination.Images>(),
                                    previousDestinationSavedStateHandle =
                                        navHostController.previousBackStackEntry?.savedStateHandle,
                                    onNavigateBack = navigationController::popBackStack,
                                )
                            }

                            sharedElementComposable<AlleyDestination.Settings>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                AlleySettingsScreen(
                                    graph = graph,
                                    onNavigateBack = navigationController::popBackStack,
                                    onOpenExport = {
                                        navigationController.navigate(AlleyDestination.Export)
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.StampRallies>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.StampRallies>()
                                StampRallySearchScreen(
                                    graph = graph,
                                    lockedYear = route.year,
                                    lockedSeries = route.series,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickBack = navigationController::popBackStack,
                                    onOpenStampRally = onOpenStampRally,
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }

                            sharedElementComposable<AlleyDestination.StampRallyDetails>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.StampRallyDetails>()
                                StampRallyDetailsScreen(
                                    graph = graph,
                                    route = route,
                                    entrySavedStateHandle = it.savedStateHandle,
                                    onNavigateUp = navigationController::navigateUp,
                                    onOpenImages = { rallyId, hostTable, fandom, images, imageIndex ->
                                        navigationController.navigate(
                                            AlleyDestination.Images(
                                                year = route.year,
                                                id = route.id,
                                                type = StampRally(
                                                    id = rallyId,
                                                    hostTable = hostTable,
                                                    fandom = fandom,
                                                ),
                                                images = images,
                                                initialImageIndex = imageIndex,
                                            )
                                        )
                                    },
                                    onOpenMap = {
                                        navigationController.navigate(
                                            AlleyDestination.StampRallyMap(
                                                year = route.year,
                                                id = route.id,
                                            )
                                        )
                                    },
                                    onOpenArtist = onOpenArtist,
                                    onOpenSeries = onOpenSeries,
                                )
                            }

                            sharedElementComposable<AlleyDestination.StampRallyMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                StampRallyMapScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.StampRallyMap>(),
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = { entry, imageIndex ->
                                        navigationController.navigate(
                                            AlleyDestination.ArtistDetails(entry.artist, imageIndex)
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.Series>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.Series>()
                                ArtistSeriesScreen(
                                    graph = graph,
                                    route = route,
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickRallies = {
                                        navigationController.navigate(
                                            AlleyDestination.StampRallies(it, route.series)
                                        )
                                    },
                                    onClickMap = {
                                        navigationController.navigate(
                                            AlleyDestination.SeriesMap(it, route.series)
                                        )
                                    },
                                    onOpenArtist = onOpenArtist,
                                    onOpenMerch = onOpenMerch,
                                    onOpenSeries = onOpenSeries,
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }

                            sharedElementComposable<AlleyDestination.Merch>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.Merch>()
                                ArtistMerchScreen(
                                    graph = graph,
                                    route = route,
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickMap = {
                                        navigationController.navigate(
                                            AlleyDestination.MerchMap(it, route.merch)
                                        )
                                    },
                                    onOpenArtist = onOpenArtist,
                                    onOpenMerch = onOpenMerch,
                                    onOpenSeries = onOpenSeries,
                                    onOpenExport = onOpenExport,
                                    onOpenChangelog = onOpenChangelog,
                                    onOpenSettings = onOpenSettings,
                                )
                            }

                            sharedElementComposable<AlleyDestination.SeriesMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.SeriesMap>()
                                TagMapScreen(
                                    graph = graph,
                                    year = route.year,
                                    series = route.series,
                                    merch = null,
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = { entry, imageIndex ->
                                        onOpenArtist(entry.artist, imageIndex)
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.MerchMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<AlleyDestination.MerchMap>()
                                TagMapScreen(
                                    graph = graph,
                                    year = route.year,
                                    series = null,
                                    merch = route.merch,
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = { entry, imageIndex ->
                                        onOpenArtist(entry.artist, imageIndex)
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.Import>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                ImportScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.Import>(),
                                    onDismiss = { navigationController.popBackStack(AlleyDestination.Home) },
                                )
                            }

                            sharedElementDialog<AlleyDestination.Export>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                QrCodeScreen(
                                    graph = graph,
                                    onNavigateBack = navigationController::popBackStack,
                                )
                            }
                        }
                    }
                }

                if (BuildVariant.isDebug() && PlatformSpecificConfig.type != PlatformType.WEB) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "DEBUG",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}
