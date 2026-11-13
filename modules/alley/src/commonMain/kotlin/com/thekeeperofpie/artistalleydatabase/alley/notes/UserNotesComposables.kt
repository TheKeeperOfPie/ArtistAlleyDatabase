package com.thekeeperofpie.artistalleydatabase.alley.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import artistalleydatabase.modules.alley.generated.resources.alley_redo
import artistalleydatabase.modules.alley.generated.resources.alley_undo
import artistalleydatabase.modules.alley.generated.resources.alley_user_notes
import artistalleydatabase.modules.alley.generated.resources.alley_user_notes_character_count
import artistalleydatabase.modules.alley.generated.resources.alley_user_notes_updated_at
import com.thekeeperofpie.artistalleydatabase.alley.database.UserNotesDao
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.Redo
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.Undo
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@Composable
fun UserNotesText(
    state: TextFieldState,
    updatedAt: () -> Instant?,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.System, // TODO: Composition local
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.alley_user_notes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier.padding(vertical = 8.dp)
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val updatedAt = updatedAt()
                    if (updatedAt != null) {
                        Text(
                            stringResource(
                                Res.string.alley_user_notes_updated_at,
                                HumanReadable.timeAgo(
                                    instant = updatedAt,
                                    baseInstant = clock.now(),
                                )
                            )
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    val length = state.text.length
                    Text(
                        text = stringResource(
                            Res.string.alley_user_notes_character_count,
                            length,
                            UserNotesDao.MAX_CHARACTER_COUNT
                        ),
                        color = if (length >= UserNotesDao.MAX_CHARACTER_COUNT) {
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

@AlleyPreview
@Composable
private fun UserNotesTextPreview() {
    val clock = object : Clock {
        override fun now() = Instant.DISTANT_PAST + 30.minutes
    }
    UserNotesText(
        state = rememberTextFieldState("Some user notes..."),
        updatedAt = { Instant.DISTANT_PAST },
        clock = clock,
    )
}
