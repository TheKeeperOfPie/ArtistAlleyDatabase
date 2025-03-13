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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.settings.SettingsScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapScreen
import com.thekeeperofpie.artistalleydatabase.utils.BuildVariant
import com.thekeeperofpie.artistalleydatabase.utils.isDebug
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
object ArtistAlleyAppScreen {

    @Composable
    operator fun invoke(
        component: ArtistAlleyComponent,
        navHostController: NavHostController,
    ) {
        Surface {
            val navigationController = LocalNavigationController.current
            val onArtistClick = { entry: ArtistEntryGridModel, imageIndex: Int ->
                navigationController.navigate(
                    Destinations.ArtistDetails(
                        year = entry.artist.year,
                        id = entry.id.valueId,
                        imageIndex = imageIndex.toString(),
                    )
                )
            }
            Column(modifier = Modifier.fillMaxSize()) {
                SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(navHostController, Destinations.Home) {
                            val navigationTypeMap = component.navigationTypeMap
                            sharedElementComposable<Destinations.Home>(
                                navigationTypeMap = navigationTypeMap,
                                enterTransition = null,
                                exitTransition = null,
                            ) {
                                AlleyRootScreen(
                                    component = component,
                                    onArtistClick = onArtistClick,
                                    onSeriesClick = {
                                        navigationController.navigate(Destinations.Series(null, it))
                                    },
                                    onMerchClick = {
                                        navigationController.navigate(Destinations.Merch(null, it))
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.ArtistDetails>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<Destinations.ArtistDetails>()
                                val viewModel = viewModel {
                                    component.artistDetailsViewModel(createSavedStateHandle())
                                }
                                ArtistDetailsScreen(
                                    entry = viewModel.entry,
                                    notes = { viewModel.notes },
                                    onNotesChange = { viewModel.notes = it },
                                    initialImageIndex = viewModel.initialImageIndex,
                                    images = viewModel::images,
                                    otherYears = viewModel::otherYears,
                                    eventSink = {
                                        when (it) {
                                            is ArtistDetailsScreen.Event.OpenMerch ->
                                                navigationController.navigate(
                                                    Destinations.Merch(route.year, it.merch)
                                                )
                                            is ArtistDetailsScreen.Event.OpenSeries ->
                                                navigationController.navigate(
                                                    Destinations.Series(route.year, it.series)
                                                )
                                            is ArtistDetailsScreen.Event.OpenStampRally ->
                                                navigationController.navigate(
                                                    Destinations.StampRallyDetails(
                                                        year = it.entry.year,
                                                        id = it.entry.id,
                                                    )
                                                )
                                            is ArtistDetailsScreen.Event.OpenOtherYear ->
                                                navigationController.navigate(
                                                    Destinations.ArtistDetails(
                                                        year = it.year,
                                                        id = route.id,
                                                    )
                                                )
                                            is ArtistDetailsScreen.Event.DetailsEvent ->
                                                when (val event = it.event) {
                                                    is DetailsScreen.Event.FavoriteToggle ->
                                                        viewModel.onFavoriteToggle(event.favorite)
                                                    DetailsScreen.Event.NavigateBack ->
                                                        navigationController.navigateUp()
                                                    DetailsScreen.Event.OpenMap ->
                                                        navigationController.navigate(
                                                            Destinations.ArtistMap(route.id)
                                                        )

                                                }
                                        }
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.ArtistMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.artistMapViewModel(createSavedStateHandle())
                                }
                                val mapViewModel =
                                    viewModel { component.mapViewModel(createSavedStateHandle()) }
                                ArtistMapScreen(
                                    viewModel = viewModel,
                                    mapViewModel = mapViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    onArtistClick = onArtistClick,
                                )
                            }

                            sharedElementComposable<Destinations.Settings>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel { component.settingsViewModel() }
                                SettingsScreen(sections = viewModel.sections)
                            }

                            sharedElementComposable<Destinations.StampRallyDetails>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<Destinations.StampRallyDetails>()
                                val viewModel = viewModel {
                                    component.stampRallyDetailsViewModel(
                                        createSavedStateHandle()
                                    )
                                }
                                StampRallyDetailsScreen(
                                    entry = viewModel.entry,
                                    notes = { viewModel.notes },
                                    onNotesChange = { viewModel.notes = it },
                                    initialImageIndex = viewModel.initialImageIndex,
                                    images = viewModel::images,
                                    eventSink = {
                                        when (it) {
                                            is StampRallyDetailsScreen.Event.DetailsEvent ->
                                                when (val event = it.event) {
                                                    is DetailsScreen.Event.FavoriteToggle ->
                                                        viewModel.onFavoriteToggle(event.favorite)
                                                    DetailsScreen.Event.NavigateBack ->
                                                        navigationController.navigateUp()
                                                    DetailsScreen.Event.OpenMap ->
                                                        navigationController.navigate(
                                                            Destinations.StampRallyMap(
                                                                year = route.year,
                                                                id = route.id,
                                                            )
                                                        )
                                                }
                                            is StampRallyDetailsScreen.Event.OpenArtist ->
                                                navigationController.navigate(
                                                    Destinations.ArtistDetails(
                                                        year = it.artist.year,
                                                        id = it.artist.id,
                                                    )
                                                )
                                        }
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.StampRallyMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.stampRallyMapViewModel(createSavedStateHandle())
                                }
                                val mapViewModel =
                                    viewModel { component.mapViewModel(createSavedStateHandle()) }
                                StampRallyMapScreen(
                                    viewModel = viewModel,
                                    mapViewModel = mapViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    onArtistClick = { entry, imageIndex ->
                                        navigationController.navigate(
                                            Destinations.ArtistDetails(
                                                year = entry.artist.year,
                                                id = entry.artist.id,
                                                imageIndex = imageIndex.toString(),
                                            )
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.Series>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<Destinations.Series>()
                                val sortViewModel =
                                    viewModel {
                                        component.artistSortFilterViewModel(
                                            createSavedStateHandle()
                                        )
                                    }
                                val viewModel = viewModel {
                                    component.artistSearchViewModel(
                                        createSavedStateHandle(),
                                        sortViewModel.state.filterParams,
                                    )
                                }
                                ArtistSearchScreen(
                                    viewModel = viewModel,
                                    sortViewModel = sortViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickMap = {
                                        navigationController.navigate(
                                            Destinations.SeriesMap(
                                                viewModel.lockedYear,
                                                route.series
                                            )
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.Merch>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<Destinations.Merch>()
                                val sortViewModel = viewModel {
                                    component.artistSortFilterViewModel(
                                        createSavedStateHandle(),
                                    )
                                }
                                val viewModel = viewModel {
                                    component.artistSearchViewModel(
                                        createSavedStateHandle(),
                                        sortViewModel.state.filterParams,
                                    )
                                }
                                ArtistSearchScreen(
                                    viewModel = viewModel,
                                    sortViewModel = sortViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickMap = {
                                        navigationController.navigate(
                                            Destinations.MerchMap(viewModel.lockedYear, route.merch)
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.SeriesMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.tagMapViewModel(createSavedStateHandle())
                                }
                                val mapViewModel = viewModel {
                                    component.mapViewModel(createSavedStateHandle())
                                }
                                TagMapScreen(
                                    viewModel = viewModel,
                                    mapViewModel = mapViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    onArtistClick = onArtistClick,
                                )
                            }

                            sharedElementComposable<Destinations.MerchMap>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.tagMapViewModel(createSavedStateHandle())
                                }
                                val mapViewModel =
                                    viewModel { component.mapViewModel(createSavedStateHandle()) }
                                TagMapScreen(
                                    viewModel = viewModel,
                                    mapViewModel = mapViewModel,
                                    onClickBack = navigationController::navigateUp,
                                    onArtistClick = onArtistClick,
                                )
                            }
                        }
                    }
                }

                if (BuildVariant.isDebug() && PlatformSpecificConfig.type != PlatformType.WASM) {
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
