package com.thekeeperofpie.artistalleydatabase.alley.forum

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch

fun main() {
    application {
        val windowState = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "Artist Alley",
            state = windowState,
        ) {
            Column {
                val scope = rememberCoroutineScope()
                Button(onClick = { scope.launch { ForumSyncer.syncThreads() } }) {
                    Text("Sync forum")
                }
            }
        }
    }
}
