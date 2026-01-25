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
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistMerchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistSeriesScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogScreen
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesScreen
import com.thekeeperofpie.artistalleydatabase.alley.import.ImportScreen
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
            val onArtistClick = { entry: ArtistEntryGridModel, imageIndex: Int ->
                navigationController.navigate(
                    AlleyDestination.ArtistDetails(entry.artist, imageIndex)
                )
            }
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
                                    onArtistClick = onArtistClick,
                                    onSeriesClick = {
                                        navigationController.navigate(
                                            AlleyDestination.Series(
                                                null,
                                                it
                                            )
                                        )
                                    },
                                    onMerchClick = {
                                        navigationController.navigate(
                                            AlleyDestination.Merch(
                                                null,
                                                it
                                            )
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<AlleyDestination.ArtistDetails>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                ArtistDetailsScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.ArtistDetails>(),
                                    entrySavedStateHandle = it.savedStateHandle,
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
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                )
                            }

                            sharedElementComposable<AlleyDestination.ArtistMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                ArtistMapScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.ArtistMap>(),
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = onArtistClick,
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
                                )
                            }

                            sharedElementComposable<AlleyDestination.Settings>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                AlleySettingsScreen(graph = graph)
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
                                    onArtistClick = onArtistClick,
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
                                    onArtistClick = onArtistClick,
                                )
                            }

                            sharedElementComposable<AlleyDestination.Import>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val navigationController = LocalNavigationController.current
                                ImportScreen(
                                    graph = graph,
                                    route = it.toRoute<AlleyDestination.Import>(),
                                    onDismiss = { navigationController.popBackStack(AlleyDestination.Home) },
                                )
                            }

                            sharedElementDialog<AlleyDestination.Export>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                QrCodeScreen(graph = graph)
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
