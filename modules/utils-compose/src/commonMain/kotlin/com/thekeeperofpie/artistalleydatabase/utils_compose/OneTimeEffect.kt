package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun OneTimeEffect(vararg keys: Any, block: suspend () -> Unit) {
    var hasRun by rememberSaveable(*keys) { mutableStateOf(false) }
    val updatedBlock by rememberUpdatedState(block)
    LaunchedEffect(hasRun) {
        if (!hasRun) {
            updatedBlock()
            hasRun = true
        }
    }
}
