package com.thekeeperofpie.artistalleydatabase.alley.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
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
import com.eygraber.uri.decodeUri
import com.eygraber.uri.encodeUri
import com.thekeeperofpie.artistalleydatabase.alley.navigation.BrowserInputTest.Destination.Companion.SavedStateConfig
import com.thekeeperofpie.artistalleydatabase.test_utils.TestRootRoute
import com.thekeeperofpie.artistalleydatabase.test_utils.withHistoryChanges
import com.thekeeperofpie.artistalleydatabase.test_utils.yieldingWaitUntil
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.await
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.BrowserInput
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TestNavigationOptions
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TwoWayStack
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.navigation
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberTwoWayStack
import kotlinx.browser.window
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.uuid.Uuid

@Burst
@OptIn(ExperimentalTestApi::class)
class BrowserInputTest {

    private val runTestDispatcher = StandardTestDispatcher()

    private val navigation get() = window.navigation!!

    @Test
    fun simulateUserJourney(gesture: NavGesture = NavGesture.COMPOSE) =
        runComposeUiTest(runTestContext = runTestDispatcher) {
            withHistoryChanges {
                val navStack by setUpNavStack()

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
                    expected = listOf(
                        Destination.Home,
                        Destination.Home,
                        artistOne,
                        merch,
                        artistTwo
                    ),
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

                assertEquals(3, navStack.navBackStack.size)
                assertEquals(0, navStack.navForwardStack.size)
            }
        }

    @Test
    fun simulateReload(gesture: NavGesture = NavGesture.COMPOSE) =
        runComposeUiTest(runTestContext = runTestDispatcher) {
            withHistoryChanges {
                var navStackKey by mutableStateOf(false)
                val navStack by setUpNavStack(navStackKey = { navStackKey })

                // Save the reference for comparison later
                val navStackOne = navStack
                val artistDetails = Destination.ArtistDetails()
                val merch = Destination.Merch()
                runOnUiThread { navStackOne.navigate(artistDetails) }
                assertDestination(artistDetails)
                runOnUiThread { navStackOne.navigate(merch) }
                assertDestination(merch)

                goBack(gesture, navStackOne)
                assertDestination(artistDetails)

                // Simulate a reload by invalidating the nav stack, causing it to start from no
                // history. Actually reloading the browser in the test is not supported.
                runOnUiThread { navStackKey = true }
                awaitIdle()

                val navStackTwo = navStack
                assertNotSame(navStackOne, navStackTwo)

                // Drain the initial restoreStack handler
                runTestDispatcher.scheduler.runCurrent()
                runTestDispatcher.scheduler.advanceUntilIdle()

                assertDestination(artistDetails)
                assertEquals(
                    listOf(Destination.Home, Destination.Home, artistDetails),
                    navStackTwo.navBackStack.toList()
                )
                assertEquals(listOf(merch), navStackTwo.navForwardStack.toList())
            }
        }

    private fun ComposeUiTest.setUpNavStack(
        navStackKey: () -> Any = { false },
    ): State<TwoWayStack<Destination>> {
        var twoWayStack: State<TwoWayStack<Destination>?>? = null

        setContent {
            twoWayStack = remember { mutableStateOf(null) }
            key(navStackKey()) {
                val navStack = rememberTwoWayStack<Destination>(
                    initialDestinations = arrayOf(Destination.Home, Destination.Home),
                    savedStateConfiguration = SavedStateConfig,
                    encode = { it.toEncodedRoute() },
                )
                twoWayStack.value = navStack

                val browserInput = remember(navStack) {
                    BrowserInput(
                        navHistory = navStack.routeHistory,
                        restoreStack = {
                            navStack.restore(
                                back = it.back.map { Destination.parseRoute(it.route) } +
                                        Destination.parseRoute(it.current.route),
                                forward = it.forward.map { Destination.parseRoute(it.route) },
                            )
                        },
                        navigateTo = { navStack.navigate(Destination.parseRoute(it.route)) },
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
        }

        @Suppress("UNCHECKED_CAST")
        return assertNotNull(twoWayStack) as State<TwoWayStack<Destination>>
    }

    @Composable
    private fun NavigationTestHost(navStack: TwoWayStack<Destination>) {
        val backStack = navStack.navBackStack
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { it ->
                NavEntry(it) {
                    Text(text = it.toString())
                }
            }
        )
    }

    context(test: ComposeUiTest)
    private suspend fun assertDestination(destination: Destination) {
        val expected =
            "/$TestRootRoute/${destination.toEncodedRoute()}".removeSuffix("/")
        test.onNodeWithText(destination.toString()).assertIsDisplayed()
        runTestDispatcher.yieldingWaitUntil {
            expected == window.location.pathname.removeSuffix("/")
        }
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
            Json.encodeToString(this).encodeUri()
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

            fun parseRoute(route: String) = if (route.isEmpty()) {
                Home
            } else {
                Json.decodeFromString<Destination>(route.decodeUri())
            }
        }
    }
}
