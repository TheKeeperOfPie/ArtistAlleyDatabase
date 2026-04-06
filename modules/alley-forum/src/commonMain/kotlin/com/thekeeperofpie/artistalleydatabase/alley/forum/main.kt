package com.thekeeperofpie.artistalleydatabase.alley.forum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.allCaps
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
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
                            Text("Sync threads")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val start = rememberTextFieldState()
                            val end = rememberTextFieldState()
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val locale = LocalLocale.current
                                val inputTransformation = remember(locale) {
                                    InputTransformation.maxLength(3)
                                        .allCaps(locale)
                                        .then {
                                            if (!asCharSequence().all { it.isLetterOrDigit() }) {
                                                revertAllChanges()
                                            }
                                        }
                                }
                                TextField(
                                    state = start,
                                    label = { Text("Start booth") },
                                    inputTransformation = inputTransformation,
                                    modifier = Modifier.width(IntrinsicSize.Min)
                                        .widthIn(min = 100.dp)
                                )
                                TextField(
                                    state = end,
                                    label = { Text("End booth") },
                                    inputTransformation = inputTransformation,
                                    modifier = Modifier.width(IntrinsicSize.Min)
                                        .widthIn(min = 100.dp)
                                )
                            }
                            val boothStart = Booth.fromStringOrNull(start.text.toString())
                            val boothEnd = Booth.fromStringOrNull(end.text.toString())
                            Button(onClick = {
                                if (boothStart != null && boothEnd != null) {
                                    scope.launch {
                                        ForumSyncer.syncThreads(boothStart..boothEnd)
                                    }
                                }
                            }) {
                                if (boothStart != null && boothEnd != null) {
                                    Text("Sync $boothStart to $boothEnd")
                                } else {
                                    CircularProgressIndicator()
                                }
                            }
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
