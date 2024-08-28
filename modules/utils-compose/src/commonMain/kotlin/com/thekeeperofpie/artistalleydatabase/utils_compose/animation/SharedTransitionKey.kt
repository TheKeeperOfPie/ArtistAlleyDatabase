package com.thekeeperofpie.artistalleydatabase.utils_compose.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable

@Serializable
class SharedTransitionKey private constructor(val key: String) {

    companion object {
        @Composable
        fun makeKeyForId(id: String) = SharedTransitionKey("${LocalSharedTransitionPrefixKeys.current}-$id")

        fun deserialize(value: String) = SharedTransitionKey(value)
    }
}

private val LocalSharedTransitionKeys = compositionLocalOf<Pair<String, String>> {
    throw IllegalStateException("SharedTransition keys not provided")
}

val LocalSharedTransitionPrefixKeys = staticCompositionLocalOf<String> { "" }

@Composable
fun <T> T.SharedTransitionKeyScope(vararg prefixKeys: String?, content: @Composable T.() -> Unit) {
    val currentPrefix = LocalSharedTransitionPrefixKeys.current
    val suffix = if (prefixKeys.isEmpty()) "" else "-${prefixKeys.joinToString(separator = "-")}"
    val scopeKey = "$currentPrefix$suffix"
    if (scopeKey.isEmpty()) {
        content()
    } else {
        CompositionLocalProvider(LocalSharedTransitionPrefixKeys provides scopeKey) {
            content()
        }
    }
}

@Composable
fun SharedTransitionKeyScope(vararg prefixKeys: String?, content: @Composable () -> Unit) =
    Unit.SharedTransitionKeyScope(*prefixKeys) { content() }
