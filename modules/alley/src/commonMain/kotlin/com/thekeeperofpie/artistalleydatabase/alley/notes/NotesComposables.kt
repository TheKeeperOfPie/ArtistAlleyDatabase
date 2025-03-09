@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_details_notes
import artistalleydatabase.modules.alley.generated.resources.alley_redo
import artistalleydatabase.modules.alley.generated.resources.alley_undo
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesText(state: TextFieldState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.alley_details_notes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { state.undoState.undo() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Undo,
                    contentDescription = stringResource(Res.string.alley_undo),
                )
            }
            IconButton(onClick = { state.undoState.redo() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Redo,
                    contentDescription = stringResource(Res.string.alley_redo),
                )
            }
        }
        OutlinedTextField(
            state = state,
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 4, maxHeightInLines = 10),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
