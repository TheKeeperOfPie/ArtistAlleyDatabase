package com.thekeeperofpie.artistalleydatabase.alley.edit.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
actual fun PreventUnloadEffect() {
    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = { it.preventDefault() }
        window.addEventListener("beforeunload", listener)
        onDispose {
            window.removeEventListener("beforeunload", listener)
        }
    }
}
