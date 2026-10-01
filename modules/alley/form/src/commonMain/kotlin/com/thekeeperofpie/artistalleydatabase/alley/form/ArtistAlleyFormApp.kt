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
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.lifecycle.createSavedStateHandle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventHandler
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.serialization.SavedStateConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditViewModel
import com.thekeeperofpie.artistalleydatabase.alley.navigation.rememberAlleyNavStack
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
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
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
fun rememberFormTwoWayStack() =
    rememberTwoWayStack<AlleyFormDestination>(
        AlleyFormDestination.Home,
        savedStateConfiguration = SavedStateConfig,
        encode = { AlleyFormDestination.toEncodedRoute(it) },
    )

@Composable
fun ArtistAlleyFormApp(
    graph: ArtistAlleyFormGraph,
    navStack: TwoWayStack<AlleyFormDestination> = rememberFormTwoWayStack(),
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
            // TODO: Merge or isolate graphs?
            val alleyNavStack = rememberAlleyNavStack()
            val navigatorGraph = retain(graph, alleyNavStack) {
                graph.formNavigatorGraphFactory.create(alleyNavStack)
            }
            CompositionLocalProvider(
                LocalSharedTransitionScope provides this,
                LocalNavigationResults provides rememberNavigationResults(),
                LocalMetroViewModelFactory provides navigatorGraph.metroViewModelFactory,
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
                val entryProvider = entryProvider {
                    addFormEntryProviders(
                        onNavigate = navStack::navigate,
                        onClickBack = onClickBack,
                    )
                }

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

fun EntryProviderScope<AlleyFormDestination>.addFormEntryProviders(
    onNavigate: (AlleyFormDestination) -> Unit,
    onClickBack: (force: Boolean) -> Unit,
) {
    sharedElementEntry<AlleyFormDestination.Home> {
        ArtistFormHomeScreen(
            onOpenForm = {
                onNavigate(
                    AlleyFormDestination.ArtistForm(DataYear.LATEST)
                )
            },
        )
    }
    sharedElementEntry<AlleyFormDestination.ArtistForm> { route ->
        ArtistFormScreen(
            dataYear = route.dataYear,
            onClickBack = onClickBack,
            onClickEditImages = { displayName, key, images ->
                onNavigate(
                    AlleyFormDestination.ImagesEdit(
                        requestKey = key,
                        images = images,
                        displayName = displayName,
                    )
                )
            },
        )
    }
    sharedElementEntry<AlleyFormDestination.ImagesEdit> { route ->
        ImagesEditScreen(
            requestKey = route.requestKey,
            displayName = route.displayName,
            initialImages = route.images,
            onClickBack = onClickBack,
            viewModel = assistedMetroViewModel<ImagesEditViewModel, ImagesEditViewModel.Factory> {
                create(
                    images = route.images,
                    savedStateHandle = it.createSavedStateHandle(),
                )
            },
        )
    }
}
