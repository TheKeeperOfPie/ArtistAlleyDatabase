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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

@Composable
fun rememberNavigationResults(): NavigationResults {
    val scope = rememberCoroutineScope()
    val map = rememberSaveable(saver = SnapshotStateMapSaver) { mutableStateMapOf() }
    return rememberSaveable(scope, map) { NavigationResults(scope, map) }
}

@Composable
inline fun <reified T : Any> NavigationResultEffect(
    key: NavigationResults.Key<T>,
    crossinline onResult: suspend (T) -> Unit,
) {
    val navigationResults = LocalNavigationResults.current
    LaunchedEffect(navigationResults) {
        val result = withContext(PlatformDispatchers.IO) {
            navigationResults.remove<T>(key)
        }
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
class NavigationResults(val scope: CoroutineScope, val map: SnapshotStateMap<String, String>) {
    inline fun <reified T : Any> remove(key: Key<T>) = map.remove(key.key)?.let {
        Json.decodeFromString<T>(it)
    }

    inline operator fun <reified T : Any> set(key: Key<T>, value: T) {
        map[key.key] = Json.encodeToString<T>(value)
    }

    inline fun <reified T : Any> launchSave(key: Key<T>, crossinline block: suspend () -> T) {
        scope.launch(PlatformDispatchers.IO) {
            map[key.key] = Json.encodeToString<T>(block())
        }
    }

    data class Key<T : Any>(val key: String, val clazz: KClass<T>) {
        companion object {
            inline operator fun <reified T : Any> invoke(key: String) = Key(key, T::class)
        }
    }
}

val LocalNavigationResults: ProvidableCompositionLocal<NavigationResults> =
    compositionLocalOf { throw IllegalStateException("NavigationResults not provided") }
