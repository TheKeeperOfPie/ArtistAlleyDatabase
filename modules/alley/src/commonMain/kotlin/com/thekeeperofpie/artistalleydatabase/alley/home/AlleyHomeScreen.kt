package com.thekeeperofpie.artistalleydatabase.alley.home

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.createSavedStateHandle
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.RootArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesScreen
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesViewModel
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.RootStampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchViewModel
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySortFilterSheetContent
import com.thekeeperofpie.artistalleydatabase.alley.tags.BrowseViewModel
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.exitAlwaysScrollBehaviorAutoReset
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffold2
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AlleyHomeScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onOpenArtist: (ArtistEntry, Int?) -> Unit,
    ) {
        val settings = remember(graph) { graph.settings }
        val mapTransformState = MapScreen.rememberTransformState()

        // Mirror to support multiple roots, in theory
        var destination by rememberSaveable { mutableStateOf(settings.rootDestination) }

        val artistsViewModel =
            assistedMetroViewModel<ArtistSearchViewModel, ArtistSearchViewModel.Factory> {
                create(
                    isRoot = true,
                    savedStateHandle = it.createSavedStateHandle(),
                )
            }
        val browseViewModel = assistedMetroViewModel<BrowseViewModel, BrowseViewModel.Factory> {
            create(it.createSavedStateHandle())
        }
        val favoritesViewModel =
            assistedMetroViewModel<FavoritesViewModel, FavoritesViewModel.Factory> {
                create(it.createSavedStateHandle())
            }
        val mapViewModel =
            assistedMetroViewModel<FavoritesMapViewModel, FavoritesMapViewModel.Factory> {
                create(it.createSavedStateHandle())
            }
        val ralliesViewModel = assistedMetroViewModel<StampRallySearchViewModel, StampRallySearchViewModel.Factory> {
            create(
                isRoot = true,
                savedStateHandle = it.createSavedStateHandle(),
            )
        }

        val scaffoldState = rememberSortFilterBottomScaffoldState()
        NavigationScaffold(
            scaffoldState = scaffoldState,
            destination = { destination },
            onChangeDestination = {
                settings.rootDestination = it
                destination = it
            },
            sheetContent = {
                when (destination) {
                    AlleyHomeDestination.ARTISTS ->
                        ArtistSortFilterSheetContent(
                            state = artistsViewModel.state.sortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                        )
                    AlleyHomeDestination.BROWSE -> BrowseScreen.SheetContent(
                        state = browseViewModel.state,
                        bottomSheetState = scaffoldState.bottomSheetState,
                    )
                    AlleyHomeDestination.FAVORITES -> FavoritesScreen.SheetContent(
                        state = favoritesViewModel.state,
                        bottomSheetState = scaffoldState.bottomSheetState,
                    )
                    AlleyHomeDestination.MAP ->
                        FavoritesMapFilterSheetContent(
                            state = mapViewModel.filterState,
                            mapTransformState = mapTransformState,
                            sheetState = scaffoldState.bottomSheetState,
                        )
                    AlleyHomeDestination.STAMP_RALLIES ->
                        StampRallySortFilterSheetContent(
                            state = ralliesViewModel.state.sortFilterState,
                            sheetState = scaffoldState.bottomSheetState,
                        )
                }
            },
        ) { scaffoldPadding ->
            Box(
                Modifier.fillMaxSize()
                    .padding(
                        // Ignore bottom so that animateEnterExit doesn't clip content
                        start = scaffoldPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = scaffoldPadding.calculateEndPadding(LocalLayoutDirection.current),
                    )
            ) {
                val saveableStateHolder = rememberSaveableStateHolder()
                saveableStateHolder.SaveableStateProvider(destination) {
                    when (destination) {
                        AlleyHomeDestination.ARTISTS ->
                            RootArtistSearchScreen(viewModel = artistsViewModel)
                        AlleyHomeDestination.BROWSE ->
                            BrowseScreen(browseViewModel = browseViewModel)
                        AlleyHomeDestination.FAVORITES ->
                            FavoritesScreen(
                                viewModel = favoritesViewModel,
                                onNavigateToArtists = {
                                    destination = AlleyHomeDestination.ARTISTS
                                },
                                onNavigateToRallies = {
                                    destination = AlleyHomeDestination.STAMP_RALLIES
                                },
                                onNavigateToSeries = {
                                    // TODO: This doesn't tab over to series
                                    destination = AlleyHomeDestination.BROWSE
                                },
                                onNavigateToMerch = {
                                    // TODO: This doesn't tab over to merch
                                    destination = AlleyHomeDestination.BROWSE
                                },
                            )
                        AlleyHomeDestination.MAP ->
                            FavoritesMapScreen(
                                viewModel = mapViewModel,
                                settings = graph.settings,
                                mapTransformState = mapTransformState,
                                scaffoldPadding = scaffoldPadding,
                                bottomSheetState = scaffoldState.bottomSheetState,
                                onArtistClick = { entry, imageIndex ->
                                    onOpenArtist(entry.artist, imageIndex)
                                },
                            )
                        AlleyHomeDestination.STAMP_RALLIES ->
                            RootStampRallySearchScreen(viewModel = ralliesViewModel)
                    }
                }
            }
        }
    }

    @Composable
    private fun NavigationScaffold(
        scaffoldState: SortFilterBottomScaffoldState,
        destination: () -> AlleyHomeDestination,
        onChangeDestination: (AlleyHomeDestination) -> Unit,
        sheetContent: @Composable ColumnScope.() -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val sheetPeekHeight by remember {
            derivedStateOf {
                if (destination() == AlleyHomeDestination.MAP) {
                    102.dp
                } else {
                    Dp.Unspecified
                }
            }
        }
        SortFilterBottomScaffold2(
            state = scaffoldState,
            sheetPeekHeight = { sheetPeekHeight },
            showFilterFab = false,
            sheetContent = sheetContent,
        ) { _, _ ->
            val movableContent = remember { movableContentOf(content) }
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val suiteType =
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
            if (suiteType == NavigationSuiteType.NavigationRail) {
                Row {
                    NavigationRail(
                        header = {
                            AlleyHomeFilterFloatingActionButton(
                                scaffoldState = scaffoldState,
                                vibrant = false,
                            )
                        },
                        modifier = Modifier
                            .animateEnterExit(
                                enter = slideInHorizontally { -it },
                                exit = slideOutHorizontally { -it },
                            )
                            .renderMaybeInSharedTransitionScopeOverlay(2f)
                    ) {
                        AlleyHomeDestination.entries.forEach {
                            NavigationRailItem(
                                icon = {
                                    Icon(
                                        it.icon,
                                        contentDescription = stringResource(it.textRes)
                                    )
                                },
                                label = { Text(stringResource(it.textRes)) },
                                selected = it == destination(),
                                onClick = { onChangeDestination(it) },
                            )
                        }
                    }
                    Scaffold(modifier = Modifier.fillMaxSize()) {
                        movableContent(it)
                    }
                }
            } else {
                val scrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehaviorAutoReset()
                Scaffold(
                    modifier = Modifier.exitAlwaysScrollBehaviorAutoReset(scrollBehavior)
                ) {
                    Box(Modifier.padding(it)) {
                        AlleyHomeNavigationToolbar(
                            scaffoldState = scaffoldState,
                            scrollBehavior = scrollBehavior,
                            destination = destination,
                            onChangeDestination = onChangeDestination,
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .offset(y = -ScreenOffset)
                                .zIndex(1f)
                                .animateEnterExit(
                                    enter = slideInVertically { it * 2 },
                                    exit = slideOutVertically { it * 2 },
                                )
                                .renderMaybeInSharedTransitionScopeOverlay(2f),
                        )

                        movableContent(it)
                    }
                }
            }
        }
    }
}
