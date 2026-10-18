package com.thekeeperofpie.artistalleydatabase.utils_compose.transform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope

suspend fun listener(
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    onUpdate: @Composable () -> Unit,
) {
    coroutineScope {
        transform(scope = this, context = dispatcher) { onUpdate() }
    }
}
