package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.home.HomeScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry

@Composable
fun ArtistAlleyEditApp(
    graph: ArtistAlleyEditGraph,
    twoWayStack: ArtistAlleyEditTwoWayStack = rememberArtistAlleyEditTwoWayStack(),
) {
    Scaffold { paddingValues ->
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
                    DisposableEffect(navigationEventDispatcherOwner, twoWayStack) {
                        navigationEventDispatcherOwner?.navigationEventDispatcher
                            ?.addHandler(twoWayStack)
                        onDispose { twoWayStack.remove() }
                    }

                    val onClickBack = { force: Boolean ->
                        if (force) {
                            twoWayStack.onBack()
                        } else {
                            onClickBackInput.backCompleted()
                        }
                    }
                    val entryProvider = entryProvider<NavKey> {
                        sharedElementEntry<AlleyEditDestination.Home> {
                            HomeScreen(
                                graph = graph,
                                onAddArtist = {
                                    twoWayStack.navigate(AlleyEditDestination.ArtistAdd(it))
                                },
                                onEditArtist = { dataYear, artistId ->
                                    twoWayStack.navigate(
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
                                    twoWayStack.navigate(
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
                                    twoWayStack.navigate(
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
                    }

                    val decoratedNavEntries =
                        (twoWayStack.navBackStack + twoWayStack.navForwardStack)
                            .flatMap {
                                key(it.toString()) {
                                    rememberDecoratedNavEntries(
                                        backStack = listOf(it),
                                        entryDecorators = listOf(
                                            rememberSaveableStateHolderNavEntryDecorator(),
                                            rememberViewModelStoreNavEntryDecorator()
                                        ),
                                        entryProvider = entryProvider,
                                    )
                                }
                            }

                    NavDisplay(
                        entries = decoratedNavEntries.take(twoWayStack.navBackStack.size),
                        onBack = twoWayStack::onBack,
                        transitionSpec = {
                            slideInHorizontally(initialOffsetX = { it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { -it })
                        },
                        popTransitionSpec = {
                            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it })
                        },
                        predictivePopTransitionSpec = {
                            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                                    slideOutHorizontally(targetOffsetX = { it })
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
