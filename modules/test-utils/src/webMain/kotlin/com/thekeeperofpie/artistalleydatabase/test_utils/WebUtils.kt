package com.thekeeperofpie.artistalleydatabase.test_utils

import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.TestNavigationOptions
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.navigation
import kotlinx.browser.window
import kotlinx.coroutines.await

// Looks like this is the runner's default page
const val TestRootRoute = "context.html"

/** The test environment doesn't reset its URL location automatically */
suspend fun withHistoryChanges(block: suspend () -> Unit) {
    resetLocation()
    block()
    resetLocation()
}

private suspend fun resetLocation() {
    val navigation = window.navigation!!
    val firstKey = navigation.entries().toList().first().key
    if (firstKey != null) {
        navigation.traverseTo(firstKey, TestNavigationOptions)
    } else {
        navigation.navigate(TestRootRoute)
    }.committed.await()
}
