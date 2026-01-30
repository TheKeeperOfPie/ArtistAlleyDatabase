package com.thekeeperofpie.artistalleydatabase.alley

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
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
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.rallies.details.StampRallyDetailsScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.map.StampRallyMapScreen
import com.thekeeperofpie.artistalleydatabase.alley.rallies.search.StampRallySearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.settings.AlleySettingsScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapScreen
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberDecoratedNavEntries
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementDialog
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.ScrollStateSaver

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
object ArtistAlleyAppScreen {

    private val transitionSpec = fadeIn() togetherWith fadeOut()

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        modifier: Modifier = Modifier,
        navStack: AlleyNavStack = rememberAlleyNavStack(),
    ) {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
            val languageOption by graph.settings.languageOption
                .collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
                LocalNavigationResults provides rememberNavigationResults(),
                LocalLanguageOptionMedia provides languageOption,
            ) {
                val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
                DisposableEffect(navigationEventDispatcherOwner, navStack) {
                    navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?.addHandler(navStack)
                    onDispose { navStack.remove() }
                }

                val entryProvider = entryProvider(graph = graph, navStack = navStack)

                val decoratedNavEntries =
                    rememberDecoratedNavEntries(navStack.twoWayStack, entryProvider)
                NavDisplay(
                    entries = navStack.calculateBackStack(decoratedNavEntries),
                    onBack = navStack::onBack,
                    sceneStrategy = DialogSceneStrategy<NavKey>() then SinglePaneSceneStrategy(),
                    transitionSpec = { transitionSpec },
                    popTransitionSpec = { transitionSpec },
                    predictivePopTransitionSpec = { transitionSpec },
                )
            }
        }
    }

    @Composable
    private fun entryProvider(
        graph: ArtistAlleyGraph,
        navStack: AlleyNavStack,
    ): (NavKey) -> NavEntry<NavKey> {
        val onOpenArtist = { entry: ArtistEntry, imageIndex: Int? ->
            navStack.navigate(
                AlleyDestination.ArtistDetails(entry, imageIndex)
            )
        }
        val onOpenSeries = { year: DataYear?, series: String ->
            navStack.navigate(
                AlleyDestination.Series(year, series)
            )
        }
        val onOpenMerch = { year: DataYear?, merch: String ->
            navStack.navigate(
                AlleyDestination.Merch(year, merch)
            )
        }
        val onOpenStampRally = { entry: StampRallyDatabaseEntry, initialImageIndex: String? ->
            navStack.navigate(
                AlleyDestination.StampRallyDetails(
                    entry = entry,
                    initialImageIndex = initialImageIndex
                )
            )
        }
        val onOpenExport = { navStack.navigate(AlleyDestination.Export) }
        val onOpenChangelog = { navStack.navigate(AlleyDestination.Changelog) }
        val onOpenSettings = { navStack.navigate(AlleyDestination.Settings) }
        // Real navigate up doesn't work
        val navigateUp = navStack::onBack
        return entryProvider {
            sharedElementEntry<AlleyDestination.Home> {
                AlleyRootScreen(
                    graph = graph,
                    onOpenArtist = onOpenArtist,
                    onOpenSeries = onOpenSeries,
                    onOpenMerch = onOpenMerch,
                    onOpenStampRally = onOpenStampRally,
                    onOpenExport = onOpenExport,
                    onOpenChangelog = onOpenChangelog,
                    onOpenSettings = onOpenSettings,
                )
            }

            sharedElementEntry<AlleyDestination.ArtistDetails> { route ->
                ArtistDetailsScreen(
                    graph = graph,
                    route = route,
                    onOpenArtist = { year, artistId ->
                        navStack.navigate(
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
                        navStack.navigate(
                            route.copy(
                                year = it,
                                booth = null,
                                name = null,
                                images = null,
                            )
                        )
                    },
                    onOpenMap = {
                        navStack.navigate(
                            AlleyDestination.ArtistMap(route.id)
                        )
                    },
                    onOpenImages = { year, artistId, booth, name, images, imageIndex ->
                        navStack.navigate(
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
                    onNavigateUp = navigateUp,
                )
            }

            sharedElementEntry<AlleyDestination.ArtistsList> {
                ArtistSearchScreen(
                    graph = graph,
                    lockedYear = it.year,
                    lockedSeries = null,
                    lockedMerch = null,
                    isRoot = false,
                    lockedSerializedBooths = it.serializedBooths,
                    scrollStateSaver = ScrollStateSaver(),
                    onClickBack = navStack::onBack,
                    onOpenArtist = { artist, imageIndex ->
                        navStack.navigate(
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

            sharedElementEntry<AlleyDestination.ArtistMap> {
                ArtistMapScreen(
                    graph = graph,
                    route = it,
                    onClickBack = navStack::onBack,
                    onArtistClick = { entry, imageIndex ->
                        onOpenArtist(entry.artist, imageIndex)
                    },
                )
            }

            sharedElementEntry<AlleyDestination.Changelog> {
                ChangelogScreen(
                    graph = graph,
                    onClickBack = navStack::onBack,
                    onClickSeries = {
                        navStack.navigate(
                            AlleyDestination.Series(
                                DataYear.ANIME_EXPO_2026,
                                it,
                            )
                        )
                    },
                    onClickMerch = {
                        navStack.navigate(
                            AlleyDestination.Merch(
                                DataYear.ANIME_EXPO_2026,
                                it,
                            )
                        )
                    },
                    onClickArtist = {
                        navStack.navigate(
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

            sharedElementEntry<AlleyDestination.Images> {
                ImagesScreen(
                    route = it,
                    onNavigateBack = navStack::onBack,
                )
            }

            sharedElementEntry<AlleyDestination.Settings> {
                AlleySettingsScreen(
                    graph = graph,
                    onNavigateBack = navStack::onBack,
                    onOpenExport = {
                        navStack.navigate(AlleyDestination.Export)
                    },
                )
            }

            sharedElementEntry<AlleyDestination.StampRallies> {
                StampRallySearchScreen(
                    graph = graph,
                    lockedYear = it.year,
                    lockedSeries = it.series,
                    scrollStateSaver = ScrollStateSaver(),
                    onClickBack = navStack::onBack,
                    onOpenStampRally = onOpenStampRally,
                    onOpenExport = onOpenExport,
                    onOpenChangelog = onOpenChangelog,
                    onOpenSettings = onOpenSettings,
                )
            }

            sharedElementEntry<AlleyDestination.StampRallyDetails> { route ->
                StampRallyDetailsScreen(
                    graph = graph,
                    route = route,
                    onNavigateUp = navStack::onBack,
                    onOpenImages = { rallyId, hostTable, fandom, images, imageIndex ->
                        navStack.navigate(
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
                        navStack.navigate(
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

            sharedElementEntry<AlleyDestination.StampRallyMap> {
                StampRallyMapScreen(
                    graph = graph,
                    route = it,
                    onClickBack = navStack::onBack,
                    onArtistClick = { entry, imageIndex ->
                        navStack.navigate(
                            AlleyDestination.ArtistDetails(entry.artist, imageIndex)
                        )
                    },
                )
            }

            sharedElementEntry<AlleyDestination.Series> { route ->
                ArtistSeriesScreen(
                    graph = graph,
                    route = route,
                    onClickBack = navStack::onBack,
                    scrollStateSaver = ScrollStateSaver(),
                    onClickRallies = {
                        navStack.navigate(
                            AlleyDestination.StampRallies(it, route.series)
                        )
                    },
                    onClickMap = {
                        navStack.navigate(
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

            sharedElementEntry<AlleyDestination.Merch> { route ->
                ArtistMerchScreen(
                    graph = graph,
                    route = route,
                    onClickBack = navStack::onBack,
                    scrollStateSaver = ScrollStateSaver(),
                    onClickMap = {
                        navStack.navigate(
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

            sharedElementEntry<AlleyDestination.SeriesMap> {
                TagMapScreen(
                    graph = graph,
                    year = it.year,
                    series = it.series,
                    merch = null,
                    onClickBack = navStack::onBack,
                    onArtistClick = { entry, imageIndex ->
                        onOpenArtist(entry.artist, imageIndex)
                    },
                )
            }

            sharedElementEntry<AlleyDestination.MerchMap> {
                TagMapScreen(
                    graph = graph,
                    year = it.year,
                    series = null,
                    merch = it.merch,
                    onClickBack = navStack::onBack,
                    onArtistClick = { entry, imageIndex ->
                        onOpenArtist(entry.artist, imageIndex)
                    },
                )
            }

            sharedElementEntry<AlleyDestination.Import> {
                ImportScreen(
                    graph = graph,
                    route = it,
                    onDismiss = navStack::onBack,
                )
            }

            sharedElementDialog<AlleyDestination.Export> {
                QrCodeScreen(
                    graph = graph,
                    onNavigateBack = navStack::onBack,
                )
            }
        }
    }
}
