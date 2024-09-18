package com.thekeeperofpie.artistalleydatabase.utils_compose.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.Serializable

@Suppress("DataClassPrivateConstructor")
@Serializable
data class SharedTransitionKey private constructor(val key: String) {
    companion object {
        @Composable
        fun makeKeyForId(id: String): SharedTransitionKey {
            val scopeKey = LocalSharedTransitionPrefixKeys.current
            return makeKey(scopeKey, id)
        }

        /**
         * Manually attach a scope without using [SharedTransitionKeyScope], useful for coercing an
         * earlier screen key to match a screen later in the navigation stack.
         */
        @Composable
        fun makeKeyWithScope(id: String, vararg prefixKeys: String?): SharedTransitionKey {
            val scopeKey = getScopeKey(prefixKeys)
            return makeKey(scopeKey, id)
        }

        private fun makeKey(scope: String?, id: String) = SharedTransitionKey(
            if (scope.isNullOrEmpty()) {
                id
            } else {
                "$scope-$id"
            }
        )

        fun deserialize(value: String) = SharedTransitionKey(value)
    }
}

val LocalSharedTransitionPrefixKeys = staticCompositionLocalOf { "" }

@Composable
fun <T> T.SharedTransitionKeyScope(vararg prefixKeys: String?, content: @Composable T.() -> Unit) {
    val scopeKey = getScopeKey(prefixKeys)
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

@Composable
private fun getScopeKey(prefixKeys: Array<out String?>): String {
    val currentPrefix = LocalSharedTransitionPrefixKeys.current
    val suffix = if (prefixKeys.isEmpty()) "" else prefixKeys.joinToString(separator = "-")
    return if (currentPrefix.isEmpty()) {
        suffix
    } else {
        "$currentPrefix-$suffix"
    }
}
