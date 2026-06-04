package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object AlleyRootScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onOpenArtist: (ArtistEntry, Int?) -> Unit,
        onOpenArtistImageFullscreen: (ArtistEntryGridModel, Int?) -> Unit,
        onOpenSeries: (DataYear, String) -> Unit,
        onOpenMerch: (DataYear, String) -> Unit,
        onOpenStampRally: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenStampRallyImageFullscreen: (StampRallyDatabaseEntry, initialImageIndex: Int) -> Unit,
        onOpenExport: () -> Unit,
        onOpenArtistChangelog: () -> Unit,
        onOpenStampRallyChangelog: () -> Unit,
        onOpenSeriesChangelog: (DataYear) -> Unit,
        onOpenMerchChangelog: (DataYear) -> Unit,
        onOpenFavoritesChangelog: (DataYear) -> Unit,
        onOpenSettings: () -> Unit,
    ) {
        val settings = remember(graph) { graph.settings }
        val scrollPositions = ScrollStateSaver.scrollPositions()
        val mapTransformState = MapScreen.rememberTransformState()
        var destination by rememberSaveable { mutableStateOf(settings.rootDestination.value) }
        NavigationScaffold(
            destination = { destination },
            onChangeDestination = {
                settings.rootDestination.value = it
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
                when (destination) {
                    AlleyRootDestination.ARTISTS ->
                        ArtistSearchScreen(
                            graph = graph,
                            lockedYear = null,
                            lockedSeries = null,
                            lockedMerch = null,
                            isRoot = true,
                            lockedSerializedBooths = null,
                            onClickBack = null,
                            onOpenArtist = onOpenArtist,
                            onOpenArtistImageFullscreen = onOpenArtistImageFullscreen,
                            onOpenMerch = onOpenMerch,
                            onOpenSeries = onOpenSeries,
                            onOpenExport = onOpenExport,
                            onOpenChangelog = onOpenArtistChangelog,
                            onOpenSettings = onOpenSettings,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.ARTISTS.name,
                                scrollPositions,
                            ),
                        )
                    AlleyRootDestination.BROWSE ->
                        BrowseScreen(
                            graph = graph,
                            onSeriesClick = onOpenSeries,
                            onMerchClick = onOpenMerch,
                            onOpenExport = onOpenExport,
                            onOpenSeriesChangelog = onOpenSeriesChangelog,
                            onOpenMerchChangelog = onOpenMerchChangelog,
                            onOpenSettings = onOpenSettings,
                        )
                    AlleyRootDestination.FAVORITES ->
                        FavoritesScreen(
                            graph = graph,
                            artistsScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.FAVORITES.name + "artists",
                                scrollPositions,
                            ),
                            ralliesScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.FAVORITES.name + "rallies",
                                scrollPositions,
                            ),
                            seriesScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.FAVORITES.name + "series",
                                scrollPositions,
                            ),
                            merchScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.FAVORITES.name + "merch",
                                scrollPositions,
                            ),
                            onNavigateToArtists = { destination = AlleyRootDestination.ARTISTS },
                            onNavigateToRallies = {
                                destination = AlleyRootDestination.STAMP_RALLIES
                            },
                            onNavigateToSeries = {
                                // TODO: This doesn't tab over to series
                                destination = AlleyRootDestination.BROWSE
                            },
                            onNavigateToMerch = {
                                // TODO: This doesn't tab over to merch
                                destination = AlleyRootDestination.BROWSE
                            },
                            onOpenArtist = onOpenArtist,
                            onOpenArtistImageFullscreen = onOpenArtistImageFullscreen,
                            onOpenMerch = onOpenMerch,
                            onOpenSeries = onOpenSeries,
                            onOpenStampRally = onOpenStampRally,
                            onOpenStampRallyImageFullscreen = onOpenStampRallyImageFullscreen,
                            onOpenExport = onOpenExport,
                            onOpenFavoritesChangelog = onOpenFavoritesChangelog,
                            onOpenSettings = onOpenSettings,
                        )
                    AlleyRootDestination.MAP ->
                        FavoritesMapScreen(
                            graph = graph,
                            mapTransformState = mapTransformState,
                            onArtistClick = { entry, imageIndex ->
                                onOpenArtist(entry.artist, imageIndex)
                            },
                        )
                    AlleyRootDestination.STAMP_RALLIES ->
                        StampRallySearchScreen(
                            graph = graph,
                            lockedYear = null,
                            lockedSeries = null,
                            onOpenStampRally = onOpenStampRally,
                            onOpenStampRallyImageFullscreen = onOpenStampRallyImageFullscreen,
                            onOpenExport = onOpenExport,
                            onOpenChangelog = onOpenStampRallyChangelog,
                            onOpenSettings = onOpenSettings,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.STAMP_RALLIES.name,
                                scrollPositions,
                            ),
                        )
                }
            }
        }
    }

    @Composable
    private fun NavigationScaffold(
        destination: () -> AlleyRootDestination,
        onChangeDestination: (AlleyRootDestination) -> Unit,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val movableContent = remember { movableContentOf(content) }
        val windowAdaptiveInfo = currentWindowAdaptiveInfo()
        val suiteType =
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
        if (suiteType == NavigationSuiteType.NavigationRail) {
            Row {
                NavigationRail(
                    modifier = Modifier
                        .animateEnterExit(
                            enter = slideInHorizontally { -it },
                            exit = slideOutHorizontally { -it },
                        )
                        .renderMaybeInSharedTransitionScopeOverlay(2f)
                ) {
                    AlleyRootDestination.entries.forEach {
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
            Column {
                Scaffold(modifier = Modifier.fillMaxSize().weight(1f)) {
                    movableContent(it)
                }
                NavigationBar(
                    modifier = Modifier
                        .animateEnterExit(
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it },
                        )
                        .renderMaybeInSharedTransitionScopeOverlay(2f)
                ) {
                    AlleyRootDestination.entries.forEach {
                        NavigationBarItem(
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
            }
        }
    }
}
