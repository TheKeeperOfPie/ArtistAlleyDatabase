package com.thekeeperofpie.artistalleydatabase.alley.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        val scope = rememberCoroutineScope()
                        Button(onClick = { scope.launch { ForumSyncer.verifyChannel() } }) {
                            Text("Verify channel")
                        }
                        Button(onClick = { scope.launch { ForumSyncer.syncPinned() } }) {
                            Text("Sync pinned")
                        }
                        Button(onClick = { scope.launch { ForumSyncer.syncThreads() } }) {
                            Text("Sync forum")
                        }
                        val error = ForumSyncer.error
                        if (error != null) {
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
