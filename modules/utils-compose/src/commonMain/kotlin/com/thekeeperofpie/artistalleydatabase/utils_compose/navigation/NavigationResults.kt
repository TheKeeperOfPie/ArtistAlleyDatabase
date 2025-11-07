package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun rememberNavigationResults(): NavigationResults {
    val scope = rememberCoroutineScope()
    val map = rememberSaveable(saver = SnapshotStateMapSaver) { mutableStateMapOf() }
    return rememberSaveable(scope, map) { NavigationResults(scope, map) }
}

@Composable
fun NavigationResultEffect(key: String, onResult: suspend (String) -> Unit) {
    val navigationResults = LocalNavigationResults.current
    LaunchedEffect(navigationResults) {
        val result = navigationResults.remove(key)
        if (result != null) {
            onResult(result)
        }
    }
}

private object SnapshotStateMapSaver : ComposeSaver<SnapshotStateMap<String, String>, String> {
    override fun SaverScope.save(value: SnapshotStateMap<String, String>) =
        Json.encodeToString<Map<String, String>>(value)

    override fun restore(value: String) = SnapshotStateMap<String, String>().apply {
        putAll(Json.decodeFromString<Map<String, String>>(value))
    }
}

@Stable
class NavigationResults(val scope: CoroutineScope, val map: SnapshotStateMap<String, String>,) {
    internal fun remove(key: String) = map.remove(key)

    operator fun set(key: String, result: String) {
        map[key] = result
    }

    fun launchSave(key: String, block: suspend () -> String) {
        scope.launch(PlatformDispatchers.IO) {
            map[key] = block()
        }
    }
}

val LocalNavigationResults: ProvidableCompositionLocal<NavigationResults> =
    compositionLocalOf { throw IllegalStateException("NavigationResults not provided") }
