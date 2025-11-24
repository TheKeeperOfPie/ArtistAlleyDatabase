package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.HomeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.ArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.TopLevelStackKey
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberArtistAlleyEditTopLevelStacks
import com.thekeeperofpie.artistalleydatabase.alley.edit.navigation.rememberDecoratedNavEntries
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.series.SeriesListScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtistAlleyEditApp(
    graph: ArtistAlleyEditGraph,
    navStack: ArtistAlleyEditTopLevelStacks = rememberArtistAlleyEditTopLevelStacks(),
) {
    CompositionLocalProvider(LocalNavigationController provides remember {
        object : NavigationController {
            override fun navigateUp(): Boolean = false
            override fun navigate(navDestination: NavDestination) {}
            override fun popBackStack() = false
            override fun popBackStack(navDestination: NavDestination) = false
        }
    }) {
        SharedTransitionLayout {
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
                LocalNavigationResults provides rememberNavigationResults(),
            ) {
                val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
                val onClickBackInput = remember { DirectNavigationEventInput() }
                DisposableEffect(onClickBackInput) {
                    val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?: return@DisposableEffect onDispose {}
                    dispatcher.addInput(onClickBackInput)
                    onDispose { dispatcher.removeInput(onClickBackInput) }
                }
                DisposableEffect(navigationEventDispatcherOwner, navStack) {
                    navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?.addHandler(navStack)
                    onDispose { navStack.remove() }
                }

                val onClickBack = { force: Boolean ->
                    if (force) {
                        navStack.onBack()
                    } else {
                        onClickBackInput.backCompleted()
                    }
                }
                val entryProvider = entryProvider(
                    graph = graph,
                    navStack = navStack,
                    onClickBack = onClickBack,
                )

                val decoratedNavEntries = rememberDecoratedNavEntries(navStack, entryProvider)

                NavigationSuiteScaffold(
                    navigationSuiteItems = {
                        TopLevelStackKey.entries.forEachIndexed { index, key ->
                            item(
                                icon = {
                                    Icon(
                                        imageVector = key.icon,
                                        contentDescription = stringResource(key.title)
                                    )
                                },
                                label = { Text(stringResource(key.title)) },
                                selected = navStack.topLevelStackIndex == index,
                                onClick = { navStack.moveToTopLevelStack(index) },
                                modifier = Modifier.zIndex(3f)
                            )
                        }
                    }
                ) {
                    NavDisplay(
                        entries = navStack.calculateBackStack(decoratedNavEntries),
                        onBack = navStack::onBack,
                        transitionSpec = {
                            slideInHorizontally(initialOffsetX = { it }) togetherWith fadeOut()
                        },
                        popTransitionSpec = {
                            fadeIn() togetherWith slideOutHorizontally(targetOffsetX = { it })
                        },
                        predictivePopTransitionSpec = {
                            slideInHorizontally(initialOffsetX = { it }) togetherWith fadeOut()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun entryProvider(
    graph: ArtistAlleyEditGraph,
    navStack: ArtistAlleyEditTopLevelStacks,
    onClickBack: (force: Boolean) -> Unit,
) = entryProvider<NavKey> {
    sharedElementEntry<AlleyEditDestination.Home> {
        HomeScreen(
            graph = graph,
            onAddArtist = {
                navStack.navigate(AlleyEditDestination.ArtistAdd(it))
            },
            onEditArtist = { dataYear, artistId ->
                navStack.navigate(
                    AlleyEditDestination.ArtistEdit(
                        dataYear,
                        artistId
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistAdd> { route ->
        ArtistEditScreen(
            dataYear = route.dataYear,
            artistId = null,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(
                        route.dataYear,
                        displayName,
                        images
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ArtistEdit> { route ->
        ArtistEditScreen(
            dataYear = route.dataYear,
            artistId = route.artistId,
            graph = graph,
            onClickBack = onClickBack,
            onClickEditImages = { displayName, images ->
                navStack.navigate(
                    AlleyEditDestination.ImagesEdit(
                        route.dataYear,
                        displayName,
                        images
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.ImagesEdit> {
        ImagesEditScreen(route = it, graph = graph, onClickBack = onClickBack)
    }
    sharedElementEntry<AlleyEditDestination.Series> {
        SeriesListScreen(
            graph = graph,
            onClickEditSeries = {
                navStack.navigate(AlleyEditDestination.SeriesEdit(it))
            },
            onClickAddSeries = {
                navStack.navigate(AlleyEditDestination.SeriesAdd())
            },
        )
    }
    sharedElementEntry<AlleyEditDestination.SeriesAdd> {
        SeriesEditScreen(
            seriesId = it.seriesId,
            initialInfo = null,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.SeriesEdit> {
        SeriesEditScreen(
            seriesId = it.series.uuid,
            initialInfo = it.series,
            graph = graph,
            onClickBack = onClickBack,
        )
    }
    sharedElementEntry<AlleyEditDestination.Merch> {
        Text("Merch screen", Modifier.background(Color.Blue).padding(16.dp))
    }
}
