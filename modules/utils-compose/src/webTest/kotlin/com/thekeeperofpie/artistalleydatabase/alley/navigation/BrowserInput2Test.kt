package com.thekeeperofpie.artistalleydatabase.alley.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.serialization.SavedStateConfiguration
import app.cash.burst.Burst
import com.eygraber.uri.encodeUri
import com.thekeeperofpie.artistalleydatabase.alley.navigation.BrowserInput2Test.Destination.Companion.SavedStateConfig
import com.thekeeperofpie.artistalleydatabase.test_utils.TestRootRoute
import com.thekeeperofpie.artistalleydatabase.test_utils.withHistoryChanges
import com.thekeeperofpie.artistalleydatabase.test_utils.yieldingWaitUntil
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput2
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TestNavigationOptions
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.navigation
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import kotlinx.browser.window
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

@Burst
@OptIn(ExperimentalTestApi::class)
class BrowserInput2Test {

    private val runTestDispatcher = StandardTestDispatcher()

    private val navigation get() = window.navigation!!

    @Test
    fun simulateUserJourney(gesture: NavGesture = NavGesture.COMPOSE) =
        runComposeUiTest(runTestContext = runTestDispatcher) {
            withHistoryChanges {
                var twoWayStack: TwoWayStack<Destination>? = null

                setContent {
                    val navStack = rememberTwoWayStack<Destination>(
                        initialDestinations = arrayOf(Destination.Home),
                        savedStateConfiguration = SavedStateConfig,
                        encode = { it.toEncodedRoute() },
                    )
                    twoWayStack = navStack

                    val browserInput = remember(navStack) {
                        BrowserInput2(
                            navHistory = navStack.routeHistory,
                            navigateTo = { TODO() },
                            navigateBy = navStack::navigateBy,
                            routePrefix = TestRootRoute,
                            coroutineContext = runTestDispatcher,
                        )
                    }
                    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current
                    DisposableEffect(dispatcherOwner, browserInput) {
                        val dispatcher = dispatcherOwner!!.navigationEventDispatcher
                        dispatcher.addInput(browserInput)
                        onDispose { dispatcher.removeInput(browserInput) }
                    }

                    NavigationTestHost(navStack = navStack)
                }

                val navStack = assertNotNull(twoWayStack)

                suspend fun assertDestination(destination: Destination) {
                    onNodeWithText(destination.toString()).assertIsDisplayed()
                    val expected = "/$TestRootRoute/${destination.toEncodedRoute()}".removeSuffix("/")
                    runTestDispatcher.yieldingWaitUntil {
                        expected == window.location.pathname.removeSuffix("/")
                    }
                }

                // 1. Home page
                println("\nStep 1\n")
                assertDestination(Destination.Home)

                // 2. Navigate to ArtistDetails
                println("\nStep 2\n")
                val artistOne = Destination.ArtistDetails()
                runOnUiThread { navStack.navigate(artistOne) }

                assertDestination(artistOne)

                // 3. Navigate to Merch and a different artist
                println("\nStep 3\n")
                val merch = Destination.Merch()

                runOnUiThread { navStack.navigate(merch) }
                assertDestination(merch)

                val artistTwo = Destination.ArtistDetails()
                runOnUiThread { navStack.navigate(artistTwo) }
                assertDestination(artistTwo)

                assertEquals(
                    expected = listOf(Destination.Home, artistOne, merch, artistTwo),
                    actual = navStack.navBackStack.toList(),
                )

                // 4. Move backwards 2 times
                println("\nStep 4\n")
                goBack(gesture, navStack)
                assertDestination(merch)

                goBack(gesture, navStack)
                assertDestination(artistOne)

                // 5. Move forwards 2 times
                println("\nStep 5\n")
                goForward(gesture, navStack)
                assertDestination(merch)

                goForward(gesture, navStack)
                assertDestination(artistTwo)

                // 6. Jump back twice at once
                println("\nStep 6\n")
                traverseBy(gesture, navStack, -2)
                assertDestination(artistOne)

                // 7. Jump forward twice at once
                println("\nStep 7\n")
                traverseBy(gesture, navStack, 2)
                assertDestination(artistTwo)

                // 8. Jump backwards to Home
                println("\nStep 8\n")
                traverseBy(gesture, navStack, -3)
                assertDestination(Destination.Home)

                // 9. Push a new destination from Home
                println("\nStep 9\n")
                val stampRallyDetails = Destination.StampRallyDetails()
                runOnUiThread { navStack.navigate(stampRallyDetails) }
                assertDestination(stampRallyDetails)

                assertEquals(2, navStack.navBackStack.size)
                assertEquals(0, navStack.navForwardStack.size)
            }
        }

    @Composable
    private fun NavigationTestHost(navStack: TwoWayStack<Destination>) {
        val backStack = navStack.navBackStack
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { it ->
                NavEntry(it) {
                    Text(
                        text = it.toString(),
                        modifier = Modifier.testTag("destination")
                    )
                }
            }
        )
    }

    private suspend fun goBack(gesture: NavGesture, navStack: TwoWayStack<Destination>) {
        when (gesture) {
            NavGesture.COMPOSE -> navStack.onBack()
            NavGesture.BROWSER -> navigation.back(TestNavigationOptions).committed.await()
        }
    }

    private suspend fun goForward(gesture: NavGesture, navStack: TwoWayStack<Destination>) {
        when (gesture) {
            NavGesture.COMPOSE -> navStack.onForward()
            NavGesture.BROWSER -> navigation.forward(TestNavigationOptions).committed.await()
        }
    }

    private suspend fun traverseBy(
        gesture: NavGesture,
        navStack: TwoWayStack<Destination>,
        target: Int,
    ) {
        when (gesture) {
            NavGesture.COMPOSE -> navStack.navigateBy(target)
            NavGesture.BROWSER -> {
                val key = navigation.entries()[navigation.currentEntry!!.index + target]!!.key!!
                window.navigation!!.traverseTo(key, TestNavigationOptions).committed.await()
            }
        }
    }

    enum class NavGesture {
        COMPOSE,
        BROWSER,
    }

    /**
     * Mirrors alley destinations, rather than moving the test there, since there's no real
     * dependency. The fields are randomly generated, solely for comparison in the test. The
     * absolute values don't matter.
     */
    @Serializable
    private sealed interface Destination : NavKey {
        @Serializable
        data object Home : Destination

        @Serializable
        data class ArtistDetails(
            val id: Uuid = Uuid.random(),
            val booth: String = "C39",
            val name: String = "First Last",
        ) : Destination

        @Serializable
        data class Merch(val id: Uuid = Uuid.random()) : Destination

        @Serializable
        data class StampRallyDetails(val id: Uuid = Uuid.random()) : Destination

        fun toEncodedRoute() = if (this == Home) {
            "" // Home route must always be an empty string
        } else {
            toString().encodeUri()
        }

        companion object {
            val SavedStateConfig = SavedStateConfiguration {
                serializersModule = SerializersModule {
                    polymorphic(baseClass = Destination::class) {
                        subclass(serializer = Home.serializer())
                        subclass(serializer = ArtistDetails.serializer())
                        subclass(serializer = Merch.serializer())
                        subclass(serializer = StampRallyDetails.serializer())
                    }
                }
            }
        }
    }
}
