package com.thekeeperofpie.artistalleydatabase.entry.form

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.delete
import artistalleydatabase.modules.entry.generated.resources.move_down
import artistalleydatabase.modules.entry.generated.resources.move_up
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.swap
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> ReorderItemDropdown(
    show: () -> Boolean,
    onDismiss: () -> Unit,
    index: Int,
    items: SnapshotStateList<T>,
    modifier: Modifier = Modifier,
) {
    ReorderItemDropdown(
        show = show,
        onDismiss = onDismiss,
        index = index,
        totalSize = { items.size },
        onDelete = { items.removeAt(index) },
        onMoveUp = { items.swap(index, index - 1) },
        onMoveDown = { items.swap(index, index + 1) },
        modifier = modifier
    )
}

@Composable
fun ReorderItemDropdown(
    show: () -> Boolean,
    onDismiss: () -> Unit,
    index: Int,
    totalSize: () -> Int,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = show(),
            onDismissRequest = onDismiss,
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.delete)) },
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
            if (index > 0) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.move_up)) },
                    onClick = {
                        onMoveUp()
                        onDismiss()
                    }
                )
            }
            if (index < totalSize() - 1) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.move_down)) },
                    onClick = {
                        onMoveDown()
                        onDismiss()
                    }
                )
            }
        }
    }
}
