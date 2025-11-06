package com.thekeeperofpie.artistalleydatabase.alley.edit

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.home.HomeScreen
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.interceptNavigateBack
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
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    val onClickBack = {
                        if (!interceptNavigateBack()) {
                            twoWayStack.onBack()
                        }
                    }
                    val entryProvider = entryProvider<NavKey> {
                        sharedElementEntry<AlleyEditDestination.Home> {
                            HomeScreen(
                                graph = graph,
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
                        sharedElementEntry<AlleyEditDestination.ArtistEdit> {
                            ArtistEditScreen(
                                route = it,
                                graph = graph,
                                onClickBack = onClickBack,
                            )
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
