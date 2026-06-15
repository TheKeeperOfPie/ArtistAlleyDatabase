package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.links

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rallies_queue_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rallies_queue_action_confirm_delete_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rallies_queue_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rallies_queue_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rallies_queue_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Delete
import com.thekeeperofpie.artistalleydatabase.icons.filled.Refresh
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.stringResource

object StampRallyLinksQueueScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        dataYear: DataYear,
        onSelectEntry: (Entry) -> Unit,
        viewModel: StampRallyLinksQueueViewModel = viewModel {
            graph.stampRallyLinksQueueViewModelFactory.create(dataYear)
        },
    ) {
        val queue by viewModel.queue.collectAsStateWithLifecycle()
        StampRallyLinksQueueScreen(
            queue = { queue },
            onRefresh = viewModel::refresh,
            onSelectEntry = onSelectEntry,
            onConfirmDeleteEntry = viewModel::deleteEntry,
        )
    }

    @Composable
    operator fun invoke(
        queue: () -> List<Entry>,
        onRefresh: () -> Unit,
        onSelectEntry: (Entry) -> Unit,
        onConfirmDeleteEntry: (Entry) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(stringResource(Res.string.alley_edit_stamp_rallies_queue_title)) },
                            actions = {
                                TooltipIconButton(
                                    icon = Icons.Default.Refresh,
                                    tooltipText = stringResource(Res.string.alley_edit_stamp_rallies_queue_action_refresh),
                                    onClick = onRefresh,
                                )
                            }
                        )
                    }
                },
                modifier = Modifier.widthIn(max = 1200.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    items(
                        items = queue(),
                        key = { it.link },
                    ) {
                        Column {
                            StampRallyQueueEntryRow(
                                entry = it,
                                onConfirmDeleteEntry = onConfirmDeleteEntry,
                                modifier = Modifier.clickable { onSelectEntry(it) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun StampRallyQueueEntryRow(
        entry: Entry,
        onConfirmDeleteEntry: (Entry) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = entry.booths.firstOrNull() ?: "   ",
                style = MaterialTheme.typography.titleLarge
                    .copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
            )

            Text(
                text = entry.link,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )

            var showConfirmDeleteDialog by remember { mutableStateOf(false) }
            IconButton(onClick = { showConfirmDeleteDialog = true }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }

            if (showConfirmDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDeleteDialog = false },
                    title = {
                        Text(
                            stringResource(
                                Res.string.alley_edit_stamp_rallies_queue_action_confirm_delete_title,
                                entry.booths.joinToString(),
                                entry.link,
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onConfirmDeleteEntry(entry)
                            showConfirmDeleteDialog = false
                        }) {
                            Text(stringResource(Res.string.alley_edit_stamp_rallies_queue_action_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeleteDialog = false }) {
                            Text(stringResource(Res.string.alley_edit_stamp_rallies_queue_action_cancel))
                        }
                    }
                )
            }
        }
    }

    data class Entry(
        val link: String,
        val booths: List<String>,
    )
}
