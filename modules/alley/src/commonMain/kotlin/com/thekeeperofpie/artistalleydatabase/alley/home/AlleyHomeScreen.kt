package com.thekeeperofpie.artistalleydatabase.alley.home

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.zIndex
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.exitAlwaysScrollBehaviorAutoReset
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AlleyHomeScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onOpenArtist: (ArtistEntry, Int?) -> Unit,
        onOpenArtistImageFullscreen: (ArtistEntryGridModel, Int?, showOutdatedCatalogs: Boolean) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenStampRally: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenStampRallyImageFullscreen: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenArtistChangelog: (DataYear) -> Unit,
        onOpenStampRallyChangelog: (DataYear) -> Unit,
        onOpenSeriesChangelog: (DataYear) -> Unit,
        onOpenMerchChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
    ) {
        val settings = remember(graph) { graph.settings }
        val mapTransformState = MapScreen.rememberTransformState()

        // Mirror to support multiple roots, in theory
        var destination by rememberSaveable { mutableStateOf(settings.rootDestination) }
        val artistsScaffoldState = rememberSortFilterBottomScaffoldState()
        val browseScaffoldState = rememberSortFilterBottomScaffoldState()
        val favoritesScaffoldState = rememberSortFilterBottomScaffoldState()
        val mapScaffoldState = rememberSortFilterBottomScaffoldState()
        val ralliesScaffoldState = rememberSortFilterBottomScaffoldState()
        val state = remember(
            artistsScaffoldState,
            browseScaffoldState,
            favoritesScaffoldState,
            mapScaffoldState,
            ralliesScaffoldState,
        ) {
            AlleyHomeNavigationState(
                artistsScaffoldState = artistsScaffoldState,
                browseScaffoldState = browseScaffoldState,
                favoritesScaffoldState = favoritesScaffoldState,
                mapScaffoldState = mapScaffoldState,
                ralliesScaffoldState = ralliesScaffoldState,
            )
        }
        NavigationScaffold(
            state = state,
            destination = { destination },
            onChangeDestination = {
                settings.rootDestination = it
                destination = it
            },
        ) {
            Box(
                Modifier.fillMaxSize()
                    .padding(
                        // Ignore bottom so that animateEnterExit doesn't clip content
                        start = it.calculateStartPadding(LocalLayoutDirection.current),
                        end = it.calculateEndPadding(LocalLayoutDirection.current),
                    )
            ) {
                var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
                val saveableStateHolder = rememberSaveableStateHolder()
                saveableStateHolder.SaveableStateProvider(destination) {
                    when (destination) {
                        AlleyHomeDestination.ARTISTS ->
                            ArtistSearchScreen(
                                isRoot = true,
                                scaffoldState = artistsScaffoldState,
                            )
                        AlleyHomeDestination.BROWSE ->
                            BrowseScreen(
                                scaffoldState = browseScaffoldState,
                                selectedTabIndex = { selectedTabIndex },
                                onSelectedTabIndexChanged = { selectedTabIndex = it },
                                onSeriesClick = onOpenSeries,
                                onMerchClick = onOpenMerch,
                                onOpenSeriesChangelog = onOpenSeriesChangelog,
                                onOpenMerchChangelog = onOpenMerchChangelog,
                                onOpenSettings = onOpenSettings,
                            )
                        AlleyHomeDestination.FAVORITES ->
                            FavoritesScreen(
                                scaffoldState = favoritesScaffoldState,
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
                                scaffoldState = mapScaffoldState,
                                settings = graph.settings,
                                mapTransformState = mapTransformState,
                                onArtistClick = { entry, imageIndex ->
                                    onOpenArtist(entry.artist, imageIndex)
                                },
                            )
                        AlleyHomeDestination.STAMP_RALLIES ->
                            StampRallySearchScreen(
                                isRoot = true,
                                scaffoldState = ralliesScaffoldState,
                                lockedYear = null,
                                lockedSeries = null,
                            )
                    }
                }
            }
        }
    }

    @Composable
    private fun NavigationScaffold(
        state: AlleyHomeNavigationState,
        destination: () -> AlleyHomeDestination,
        onChangeDestination: (AlleyHomeDestination) -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val movableContent = remember { movableContentOf(content) }
        val windowAdaptiveInfo = currentWindowAdaptiveInfo()
        val suiteType =
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
        if (suiteType == NavigationSuiteType.NavigationRail) {
            Row {
                NavigationRail(
                    header = {
                        AlleyHomeFilterFloatingActionButton(
                            navigationState = state,
                            destination = destination,
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
                        navigationState = state,
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
