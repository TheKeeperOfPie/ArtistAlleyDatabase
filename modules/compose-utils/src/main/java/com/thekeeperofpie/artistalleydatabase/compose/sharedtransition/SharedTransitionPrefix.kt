package com.thekeeperofpie.artistalleydatabase.compose.sharedtransition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.UUID

val LocalSharedTransitionPrefix = staticCompositionLocalOf { emptyList<String>() }

@Composable
fun SharedTransitionPrefixProvider(prefix: String? = null, content: @Composable () -> Unit) {
    val currentPrefixes = LocalSharedTransitionPrefix.current
    val nonNullPrefix = prefix ?: remember { UUID.randomUUID().toString() }
    CompositionLocalProvider(
        value = LocalSharedTransitionPrefix provides (currentPrefixes + nonNullPrefix),
        content = content,
    )
}
