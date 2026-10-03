package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigateEvent
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationOptions
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationOptionsHistory
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TestNavigationOptions
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.navigation
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.events.Event

// Karma runner's default page
const val TestRootRoute = "context.html"

/** The test environment doesn't reset its URL location automatically */
suspend fun withHistoryChanges(block: suspend () -> Unit) {
    resetLocation()
    block()
    resetLocation()
}

private suspend fun resetLocation() {
    val navigation = window.navigation!!
    val entries = navigation.entries().toList()
    val navigateListener: (Event) -> Unit = { it.unsafeCast<NavigateEvent>().intercept() }
    window.navigation?.addEventListener("navigate", navigateListener)

    // Navigation API doesn't allow wiping the forward stack, so instead start every test with
    // the smallest possible common state. Which involves navigating to the root, replacing it with
    // [TestRootRoute], calling `navigate` with a fake URL to branch to a new forward stack, and
    // replacing it again with [TestRootRoute]. This leaves 2 URLs in the history, both set to
    // [TestRootRoute], and all tests will just assume that the first route entry is duplicated.
    val firstKey = entries.first().key
    if (firstKey != null) navigation.traverseTo(firstKey, TestNavigationOptions).finished.await()
    val url = "${window.location.origin}/$TestRootRoute"
    val fakeUrl = "${window.location.origin}/test.html"
    navigation.navigate(url, NavigationOptions(history = NavigationOptionsHistory.REPLACE)).finished.await()
    navigation.navigate(fakeUrl).finished.await()
    navigation.navigate(url, NavigationOptions(history = NavigationOptionsHistory.REPLACE))
        .finished.await()
    window.navigation?.removeEventListener("navigate", navigateListener)
}
