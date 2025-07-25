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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistMerchScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistSeriesScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.details.ArtistDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.artist.map.ArtistMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.export.QrCodeScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagesScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.import.ImportScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapScreen
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
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
        component: ArtistAlleyComponent,
        navHostController: NavHostController,
        rootSnackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    ) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navigationController = LocalNavigationController.current
            val onArtistClick = { entry: ArtistEntryGridModel, imageIndex: Int ->
                navigationController.navigate(
                    Destinations.ArtistDetails(entry.artist, imageIndex)
                )
            }
            Column(modifier = Modifier.fillMaxSize()) {
                SharedTransitionLayout(modifier = Modifier.weight(1f)) {
                    val languageOption by component.settings.languageOption
                        .collectAsStateWithLifecycle()
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this,
                        LocalLanguageOptionMedia provides languageOption,
                    ) {
                        NavHost(navHostController, Destinations.Home) {
                            val navigationTypeMap = component.navigationTypeMap
                            sharedElementComposable<Destinations.Home>(
                                navigationTypeMap = navigationTypeMap,
                                enterTransition = null,
                                exitTransition = null,
                            ) {
                                AlleyRootScreen(
                                    component = component,
                                    snackbarHostState = rootSnackbarHostState,
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
                                val images = viewModel.catalogImages
                                val pageCount = when {
                                    images.isEmpty() -> 0
                                    images.size == 1 -> 1
                                    else -> images.size + 1
                                }
                                val imageIndex = it.savedStateHandle
                                    .remove<Int>("imageIndex")
                                    ?.coerceAtMost(pageCount - 1)
                                    ?.takeIf { it >= 0 }
                                val imagePagerState = rememberImagePagerState(
                                    images,
                                    imageIndex ?: viewModel.initialImageIndex
                                )
                                LifecycleStartEffect(imagePagerState, imageIndex) {
                                    if (imageIndex != null) {
                                        imagePagerState.requestScrollToPage(imageIndex)
                                    }
                                    onStopOrDispose {}
                                }
                                val entry by viewModel.entry.collectAsStateWithLifecycle()
                                val otherArtists by viewModel.otherArtists.collectAsStateWithLifecycle()
                                val seriesInferred by viewModel.seriesInferred.collectAsStateWithLifecycle()
                                val seriesConfirmed by viewModel.seriesConfirmed.collectAsStateWithLifecycle()
                                val seriesImages by viewModel.seriesImages.collectAsStateWithLifecycle()
                                ArtistDetailsScreen(
                                    route = route,
                                    entry = { entry },
                                    otherArtists = { otherArtists },
                                    seriesInferred = { seriesInferred },
                                    seriesConfirmed = { seriesConfirmed },
                                    userNotesTextState = viewModel.userNotes,
                                    imagePagerState = imagePagerState,
                                    catalogImages = viewModel::catalogImages,
                                    seriesImages = { seriesImages },
                                    otherYears = viewModel::otherYears,
                                    eventSink = {
                                        when (it) {
                                            is ArtistDetailsScreen.Event.OpenArtist ->
                                                navigationController.navigate(
                                                    Destinations.ArtistDetails(
                                                        year = route.year,
                                                        id = it.artistId,
                                                        booth = null,
                                                        name = null,
                                                    )
                                                )
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
                                                    Destinations.StampRallyDetails(it.entry)
                                                )
                                            is ArtistDetailsScreen.Event.OpenOtherYear ->
                                                navigationController
                                                    .navigate(
                                                        route.copy(
                                                            year = it.year,
                                                            booth = null,
                                                            name = null,
                                                        )
                                                    )
                                            is ArtistDetailsScreen.Event.SeriesFavoriteToggle ->
                                                viewModel.onSeriesFavoriteToggle(
                                                    data = it.series,
                                                    favorite = it.favorite,
                                                )
                                            is ArtistDetailsScreen.Event.DetailsEvent ->
                                                when (val event = it.event) {
                                                    is DetailsScreen.Event.FavoriteToggle ->
                                                        viewModel.onFavoriteToggle(event.favorite)
                                                    DetailsScreen.Event.NavigateUp ->
                                                        navigationController.navigateUp()
                                                    is DetailsScreen.Event.OpenImage -> {
                                                        val artist = viewModel.entry.value?.artist
                                                        if (artist != null && artist.booth != null) {
                                                            navigationController.navigate(
                                                                Destinations.Images(
                                                                    year = route.year,
                                                                    id = route.id,
                                                                    type = Destinations.Images.Type.Artist(
                                                                        id = artist.id,
                                                                        booth = artist.booth,
                                                                        name = artist.name,
                                                                    ),
                                                                    initialImageIndex = event.imageIndex,
                                                                )
                                                            )
                                                        }
                                                    }
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
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = onArtistClick,
                                )
                            }

                            sharedElementComposable<Destinations.Images>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.imagesViewModel(createSavedStateHandle())
                                }
                                val route = it.toRoute<Destinations.Images>()
                                val imagePagerState = rememberImagePagerState(
                                    images = viewModel.images,
                                    initialImageIndex = route.initialImageIndex ?: 0,
                                )
                                val previousDestinationSavedStateHandle =
                                    navHostController.previousBackStackEntry?.savedStateHandle
                                val targetPage = imagePagerState.targetPage
                                LaunchedEffect(targetPage) {
                                    previousDestinationSavedStateHandle
                                        ?.set("imageIndex", targetPage)
                                }
                                ImagesScreen(
                                    route = route,
                                    images = viewModel::images,
                                    imagePagerState = imagePagerState,
                                )
                            }

                            sharedElementComposable<Destinations.Settings>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel { component.alleySettingsViewModel() }
                                AlleySettingsScreen(
                                    state = viewModel.state,
                                    eventSink = viewModel::onEvent,
                                )
                            }

                            sharedElementComposable<Destinations.StampRallies>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.stampRallySearchViewModel(createSavedStateHandle())
                                }
                                StampRallySearchScreen(
                                    viewModel = viewModel,
                                    sortFilterState = viewModel.sortFilterController.state,
                                    scrollStateSaver = ScrollStateSaver(),
                                    onClickBack = navigationController::popBackStack,
                                )
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
                                val images = viewModel.images
                                val pageCount = when {
                                    images.isEmpty() -> 0
                                    images.size == 1 -> 1
                                    else -> images.size + 1
                                }
                                val imageIndex = it.savedStateHandle
                                    .remove<Int>("imageIndex")
                                    ?.coerceAtMost(pageCount - 1)
                                    ?.takeIf { it >= 0 }
                                val imagePagerState = rememberImagePagerState(
                                    images,
                                    imageIndex ?: viewModel.initialImageIndex
                                )
                                LifecycleStartEffect(imagePagerState, imageIndex) {
                                    if (imageIndex != null) {
                                        imagePagerState.requestScrollToPage(imageIndex)
                                    }
                                    onStopOrDispose {}
                                }
                                val entry by viewModel.entry.collectAsStateWithLifecycle()
                                val series by viewModel.series.collectAsStateWithLifecycle()
                                val seriesImages by viewModel.seriesImages.collectAsStateWithLifecycle()
                                StampRallyDetailsScreen(
                                    route = route,
                                    entry = { entry },
                                    series = { series },
                                    userNotesTextState = viewModel.userNotes,
                                    images = viewModel::images,
                                    imagePagerState = imagePagerState,
                                    seriesImages = { seriesImages },
                                    eventSink = {
                                        when (it) {
                                            is StampRallyDetailsScreen.Event.DetailsEvent ->
                                                when (val event = it.event) {
                                                    is DetailsScreen.Event.FavoriteToggle ->
                                                        viewModel.onFavoriteToggle(event.favorite)
                                                    DetailsScreen.Event.NavigateUp ->
                                                        navigationController.navigateUp()
                                                    is DetailsScreen.Event.OpenImage -> {
                                                        viewModel.entry.value?.stampRally?.let {
                                                            navigationController.navigate(
                                                                Destinations.Images(
                                                                    year = route.year,
                                                                    id = route.id,
                                                                    type = Destinations.Images.Type.StampRally(
                                                                        id = it.id,
                                                                        hostTable = it.hostTable,
                                                                        fandom = it.fandom,
                                                                    ),
                                                                    initialImageIndex = event.imageIndex,
                                                                )
                                                            )
                                                        }
                                                    }
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
                                                    Destinations.ArtistDetails(it.artist)
                                                )
                                            is StampRallyDetailsScreen.Event.OpenSeries ->
                                                navigationController.navigate(
                                                    Destinations.Series(route.year, it.series)
                                                )
                                            is StampRallyDetailsScreen.Event.SeriesFavoriteToggle ->
                                                viewModel.onSeriesFavoriteToggle(
                                                    data = it.series,
                                                    favorite = it.favorite,
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
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = { entry, imageIndex ->
                                        navigationController.navigate(
                                            Destinations.ArtistDetails(entry.artist, imageIndex)
                                        )
                                    },
                                )
                            }

                            sharedElementComposable<Destinations.Series>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val route = it.toRoute<Destinations.Series>()
                                val viewModel = viewModel {
                                    component.artistSearchViewModel(createSavedStateHandle())
                                }
                                val seriesViewModel = viewModel {
                                    component.artistSeriesViewModel(createSavedStateHandle())
                                }
                                val hasRallies by viewModel.hasRallies.collectAsStateWithLifecycle()
                                ArtistSeriesScreen(
                                    artistSearchViewModel = viewModel,
                                    artistSeriesViewModel = seriesViewModel,
                                    sortFilterController = viewModel.sortFilterController,
                                    onClickBack = navigationController::navigateUp,
                                    scrollStateSaver = ScrollStateSaver(),
                                    showRalliesButton = { hasRallies },
                                    onClickRallies = {
                                        navigationController.navigate(
                                            Destinations.StampRallies(
                                                viewModel.year.value,
                                                route.series
                                            )
                                        )
                                    },
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
                                val viewModel = viewModel {
                                    component.artistSearchViewModel(createSavedStateHandle())
                                }
                                val merchViewModel = viewModel {
                                    component.artistMerchViewModel(createSavedStateHandle())
                                }
                                ArtistMerchScreen(
                                    artistSearchViewModel = viewModel,
                                    artistMerchViewModel = merchViewModel,
                                    sortFilterController = viewModel.sortFilterController,
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
                                    onClickBack = navigationController::popBackStack,
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
                                    onClickBack = navigationController::popBackStack,
                                    onArtistClick = onArtistClick,
                                )
                            }

                            sharedElementComposable<Destinations.Import>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel {
                                    component.importViewModel(createSavedStateHandle())
                                }
                                val navigationController = LocalNavigationController.current
                                ImportScreen(
                                    state = viewModel::state,
                                    importData = viewModel.route.data,
                                    onDismiss = { navigationController.popBackStack(Destinations.Home) },
                                    onConfirmImport = viewModel::confirm,
                                )
                            }

                            sharedElementDialog<Destinations.Export>(
                                navigationTypeMap = navigationTypeMap,
                            ) {
                                val viewModel = viewModel { component.qrCodeViewModel() }
                                val data by viewModel.data.collectAsStateWithLifecycle()
                                QrCodeScreen(data = { data }, onClickDownload = viewModel::download)
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
