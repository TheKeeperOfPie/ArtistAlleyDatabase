package com.thekeeperofpie.artistalleydatabase.alley.app

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.NavHostController
import com.thekeeperofpie.artistalleydatabase.alley.Destinations
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.KeyboardEventEffect
import dev.zacsweers.metro.createGraphFactory
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.StorageEvent

fun initWebSettings(onNewValue: (key: String, value: String?) -> Unit) {
    window.addEventListener("storage") {
        it as StorageEvent
        val key = it.key ?: return@addEventListener
        onNewValue(key, it.newValue)
    }
}

@OptIn(ExperimentalBrowserHistoryApi::class)
actual suspend fun bindToNavigationFixed(
    navHostController: NavHostController,
    deepLinker: DeepLinker,
) {
    val route = window.location.hash.substringAfter('#', "")
    if (route.startsWith("import")) {
        navHostController.navigate(Destinations.Import(route.removePrefix("import=")))
    }
    window.bindToNavigationFixed(navHostController, deepLinker)
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val scope = rememberCoroutineScope()
        val component = createGraphFactory<ArtistAlleyWebComponent.Factory>().create(scope)
        KeyboardEventEffect()
        App(component = component)
    }
}
