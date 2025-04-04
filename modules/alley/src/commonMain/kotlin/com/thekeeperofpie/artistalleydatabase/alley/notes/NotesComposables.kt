@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_notes
import artistalleydatabase.modules.alley.generated.resources.alley_notes_character_count
import artistalleydatabase.modules.alley.generated.resources.alley_redo
import artistalleydatabase.modules.alley.generated.resources.alley_undo
import com.thekeeperofpie.artistalleydatabase.alley.database.NotesDao
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesText(state: TextFieldState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.alley_notes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.weight(1f))
            IconButton(enabled = state.undoState.canUndo, onClick = { state.undoState.undo() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Undo,
                    contentDescription = stringResource(Res.string.alley_undo),
                )
            }
            IconButton(enabled = state.undoState.canRedo, onClick = { state.undoState.redo() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Redo,
                    contentDescription = stringResource(Res.string.alley_redo),
                )
            }
        }

        OutlinedTextField(
            state = state,
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 4,
                maxHeightInLines = 10,
            ),
            supportingText = {
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val length = state.text.length
                    Text(
                        text = stringResource(
                            Res.string.alley_notes_character_count,
                            length,
                            NotesDao.MAX_CHARACTER_COUNT
                        ),
                        color = if (length >= NotesDao.MAX_CHARACTER_COUNT) {
                            MaterialTheme.colorScheme.error
                        } else {
                            Color.Unspecified
                        },
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
