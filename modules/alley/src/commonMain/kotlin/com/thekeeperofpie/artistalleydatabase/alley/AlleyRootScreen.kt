package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
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
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_generic_filter_content_description
import com.hoc081098.flowext.interval
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.search.ArtistSearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.browse.BrowseScreen
import com.thekeeperofpie.artistalleydatabase.alley.favorite.FavoritesScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.MapScreen
import com.thekeeperofpie.artistalleydatabase.alley.map.favorites.FavoritesMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.FilterList
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderMaybeInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.rememberSortFilterBottomScaffoldState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
object AlleyRootScreen {

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
        val scrollPositions = ScrollStateSaver.scrollPositions()
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
            NavigationState(
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
                when (destination) {
                    AlleyRootDestination.ARTISTS ->
                        ArtistSearchScreen(
                            isRoot = true,
                            scaffoldState = artistsScaffoldState,
                            scrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.ARTISTS.name,
                                scrollPositions,
                            ),
                        )
                    AlleyRootDestination.BROWSE ->
                        BrowseScreen(
                            scaffoldState = browseScaffoldState,
                            seriesScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.BROWSE.name + "_series",
                                scrollPositions,
                            ),
                            merchScrollStateSaver = ScrollStateSaver.fromMap(
                                AlleyRootDestination.BROWSE.name + "_merch",
                                scrollPositions,
                            ),
                            selectedTabIndex = { selectedTabIndex },
                            onSelectedTabIndexChanged = { selectedTabIndex = it },
                            onSeriesClick = onOpenSeries,
                            onMerchClick = onOpenMerch,
                            onOpenSeriesChangelog = onOpenSeriesChangelog,
                            onOpenMerchChangelog = onOpenMerchChangelog,
                            onOpenSettings = onOpenSettings,
                        )
                    AlleyRootDestination.FAVORITES ->
                        FavoritesScreen(
                            scaffoldState = favoritesScaffoldState,
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
                        )
                    AlleyRootDestination.MAP ->
                        FavoritesMapScreen(
                            scaffoldState = mapScaffoldState,
                            settings = graph.settings,
                            mapTransformState = mapTransformState,
                            onArtistClick = { entry, imageIndex ->
                                onOpenArtist(entry.artist, imageIndex)
                            },
                        )
                    AlleyRootDestination.STAMP_RALLIES ->
                        StampRallySearchScreen(
                            isRoot = true,
                            scaffoldState = ralliesScaffoldState,
                            lockedYear = null,
                            lockedSeries = null,
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
        state: NavigationState,
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
            val scrollBehavior =
                FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.Bottom)
            val isHiding by remember(scrollBehavior) {
                derivedStateOf { scrollBehavior.state.offset != 0f }
            }
            // In case the user hides the bar and can't get it back, return it after a delay
            LaunchedEffect(scrollBehavior) {
                snapshotFlow {
                    // Read contentOffset so that the interval resets on every scroll
                    scrollBehavior.state.contentOffset to isHiding
                }
                    .flatMapLatest {
                        if (it.second) {
                            interval(5.seconds, 5.seconds)
                        } else {
                            emptyFlow()
                        }
                    }
                    .collectLatest {
                        val state = scrollBehavior.state
                        AnimationState(state.offset)
                            .animateTo(0f, scrollBehavior.snapAnimationSpec) {
                                state.offset = value
                            }
                    }
            }
            Scaffold(
                modifier = Modifier
                    .pointerInput(scrollBehavior) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Scroll) {
                                    val delta = event.changes.firstOrNull()?.scrollDelta
                                    if (delta != null) {
                                        scrollBehavior.onPostScroll(
                                            -delta,
                                            Offset.Zero,
                                            NestedScrollSource.UserInput,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .nestedScroll(scrollBehavior)
            ) {
                Box(Modifier.padding(it)) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        scrollBehavior = scrollBehavior,
                        floatingActionButton = {
                            val contentDescription =
                                stringResource(Res.string.alley_generic_filter_content_description)
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                tooltip = {
                                    PlainTooltip(
                                        modifier =
                                            Modifier.semantics {
                                                liveRegion = LiveRegionMode.Assertive
                                                paneTitle = contentDescription
                                            }
                                    ) {
                                        Text(contentDescription)
                                    }
                                },
                                state = rememberTooltipState(),
                            ) {
                                val scope = rememberCoroutineScope()
                                FloatingToolbarDefaults.VibrantFloatingActionButton(
                                    onClick = {
                                        scope.launch {
                                            when (destination()) {
                                                AlleyRootDestination.ARTISTS -> state.artistsScaffoldState
                                                AlleyRootDestination.BROWSE -> state.browseScaffoldState
                                                AlleyRootDestination.FAVORITES -> state.favoritesScaffoldState
                                                AlleyRootDestination.MAP -> state.mapScaffoldState
                                                AlleyRootDestination.STAMP_RALLIES -> state.ralliesScaffoldState
                                            }.bottomSheetState.toggle()
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = contentDescription,
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier.align(Alignment.BottomCenter)
                                .offset(y = -ScreenOffset)
                                .zIndex(1f)
                                .animateEnterExit(
                                    enter = slideInVertically { it * 2 },
                                    exit = slideOutVertically { it * 2 },
                                )
                                .renderMaybeInSharedTransitionScopeOverlay(2f),
                    ) {
                        val itemsWithText =
                            AlleyRootDestination.entries.map { it to stringResource(it.textRes) }
                        val currentDestination = destination()
                        AppBarRow {
                            itemsWithText.forEach { [item, text] ->
                                this@AppBarRow.toggleableItem(
                                    onCheckedChange = { if (it) onChangeDestination(item) },
                                    icon = { Icon(item.icon, text) },
                                    label = text,
                                    checked = item == currentDestination,
                                )
                            }
                        }
                    }

                    movableContent(it)
                }
            }
        }
    }

    private class NavigationState(
        val artistsScaffoldState: SortFilterBottomScaffoldState,
        val browseScaffoldState: SortFilterBottomScaffoldState,
        val favoritesScaffoldState: SortFilterBottomScaffoldState,
        val mapScaffoldState: SortFilterBottomScaffoldState,
        val ralliesScaffoldState: SortFilterBottomScaffoldState,
    )
}
