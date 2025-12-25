package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormScreen
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberDecoratedNavEntries
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.sharedElementEntry
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

private val SavedStateConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = AlleyFormDestination.Home.serializer())
            subclass(serializer = AlleyFormDestination.ArtistForm.serializer())
        }
    }
}

@Composable
fun rememberFormTwoWayStack() = rememberTwoWayStack(AlleyFormDestination.Home, SavedStateConfig)

@Composable
fun ArtistAlleyFormApp(
    graph: ArtistAlleyFormGraph,
    navStack: TwoWayStack = rememberFormTwoWayStack(),
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
                // TODO: Unify all of this somewhere
                val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
                val onClickBackInput = remember { DirectNavigationEventInput() }
                DisposableEffect(onClickBackInput) {
                    val dispatcher = navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?: return@DisposableEffect onDispose {}
                    dispatcher.addInput(onClickBackInput)
                    onDispose { dispatcher.removeInput(onClickBackInput) }
                }
                DisposableEffect(navigationEventDispatcherOwner, navStack) {
                    val handler = object : NavigationEventHandler<NavigationEventInfo>(
                        initialInfo = NavigationEventInfo.None,
                        isBackEnabled = true,
                        isForwardEnabled = false,
                    ) {
                        override fun onBackCompleted() {
                            navStack.onBack()
                        }
                    }
                    navigationEventDispatcherOwner?.navigationEventDispatcher
                        ?.addHandler(handler)
                    onDispose { handler.remove() }
                }

                val onClickBack: (Boolean) -> Unit = { force: Boolean ->
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

                val decoratedNavEntries = rememberDecoratedNavEntries(
                    twoWayStack = navStack,
                    entryProvider = entryProvider,
                )

                Scaffold {
                    NavDisplay(
                        entries = decoratedNavEntries.take(navStack.navBackStack.size),
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
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }
}

@Composable
private fun entryProvider(
    graph: ArtistAlleyFormGraph,
    navStack: TwoWayStack,
    onClickBack: (force: Boolean) -> Unit,
) = entryProvider<NavKey> {
    sharedElementEntry<AlleyFormDestination.Home> {
        ArtistFormHomeScreen(
            onClickNext = {
                navStack.navigate(
                    AlleyFormDestination.ArtistForm(DataYear.ANIME_EXPO_2026)
                )
            },
        )
    }
    sharedElementEntry<AlleyFormDestination.ArtistForm> { route ->
        ArtistFormScreen(
            dataYear = route.dataYear,
            onClickBack = onClickBack,
            viewModel = viewModel {
                graph.artistFormViewModelFactory.create(
                    dataYear = route.dataYear,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        )
    }
}
