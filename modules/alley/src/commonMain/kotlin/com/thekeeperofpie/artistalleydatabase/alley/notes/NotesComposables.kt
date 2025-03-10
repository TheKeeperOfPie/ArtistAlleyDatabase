@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_details_notes
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesText(text: () -> String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.alley_details_notes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // TextFieldState doesn't show keyboard properly on mobile
//            Spacer(Modifier.weight(1f))
//            IconButton(onClick = { state.undoState.undo() }) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Default.Undo,
//                    contentDescription = stringResource(Res.string.alley_undo),
//                )
//            }
//            IconButton(onClick = { state.undoState.redo() }) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Default.Redo,
//                    contentDescription = stringResource(Res.string.alley_redo),
//                )
//            }
        }

        // Clipboard permission check not implemented
        CompositionLocalProvider(
            @Suppress("DEPRECATION")
            LocalClipboardManager provides FakeClipboardManager,
            LocalClipboard provides rememberFakeClipboard(),
        ) {
            // TextFieldState doesn't show keyboard properly on mobile
            OutlinedTextField(
                value = text(),
                onValueChange = onTextChange,
                minLines = 4,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Suppress("DEPRECATION")
private object FakeClipboardManager : ClipboardManager {
    override fun getText() = null
    override fun setText(annotatedString: AnnotatedString) = Unit
}

@Composable
private fun rememberFakeClipboard(): Clipboard {
    val clipboard = LocalClipboard.current
    return remember(clipboard) { FakeClipboard(clipboard) }
}

@OptIn(ExperimentalComposeUiApi::class)
private class FakeClipboard(private val realClipboard: Clipboard) : Clipboard {
    override val nativeClipboard: NativeClipboard
        get() = realClipboard.nativeClipboard

    override suspend fun getClipEntry() = null
    override suspend fun setClipEntry(clipEntry: ClipEntry?) = Unit
}
