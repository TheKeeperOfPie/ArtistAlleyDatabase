package com.thekeeperofpie.artistalleydatabase.alley.edit.form

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_tab_history
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_tab_queue
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal object ArtistFormQueueScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onSelectEntry: (artistId: Uuid) -> Unit,
        onSelectHistoryEntry: (artistId: Uuid, formTimestamp: Instant) -> Unit,
        viewModel: ArtistFormQueueViewModel = viewModel {
            graph.artistFormQueueViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val queue by viewModel.queue.collectAsStateWithLifecycle()
        val history by viewModel.history.collectAsStateWithLifecycle()
        ArtistFormQueueScreen(
            queue = { queue },
            history = { history },
            onRefresh = viewModel::refresh,
            onSelectEntry = onSelectEntry,
            onSelectHistoryEntry = onSelectHistoryEntry,
        )
    }

    @Composable
    operator fun invoke(
        queue: () -> List<ArtistFormQueueEntry>,
        history: () -> List<ArtistFormHistoryEntry>,
        onRefresh: () -> Unit,
        onSelectEntry: (artistId: Uuid) -> Unit,
        onSelectHistoryEntry: (artistId: Uuid, formTimestamp: Instant) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            var tab by rememberSaveable { mutableStateOf(Tab.QUEUE) }
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column {
                            TopAppBar(
                                title = { Text(stringResource(Res.string.alley_edit_artist_form_queue_title)) },
                                actions = {
                                    TooltipIconButton(
                                        icon = Icons.Default.Refresh,
                                        tooltipText = stringResource(Res.string.alley_edit_artist_form_queue_action_refresh),
                                        onClick = onRefresh,
                                    )
                                }
                            )

                            PrimaryScrollableTabRow(
                                selectedTabIndex = Tab.entries.indexOf(tab),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab.entries.forEach {
                                    LeadingIconTab(
                                        selected = tab == it,
                                        onClick = { tab = it },
                                        icon = { Icon(it.icon, null) },
                                        text = { Text(stringResource(it.label)) },
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.widthIn(max = 1200.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val entries = when (tab) {
                    Tab.QUEUE ->
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 72.dp),
                            modifier = Modifier.padding(it)
                        ) {
                            items(
                                items = queue(),
                                key = { it.artistId },
                                contentType = { "artistRow" }) {
                                Column {
                                    ArtistRow(
                                        booth = it.afterBooth?.ifBlank { null } ?: it.beforeBooth,
                                        name = it.afterName?.ifBlank { null }
                                            ?: it.beforeName?.ifBlank { null }
                                            ?: it.artistId.toString(),
                                        modifier = Modifier.clickable { onSelectEntry(it.artistId) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    Tab.HISTORY ->
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 72.dp),
                            modifier = Modifier.padding(it)
                        ) {
                            items(
                                items = history(),
                                key = { listOf(it.artistId.toString(), it.timestamp.toString()) },
                                contentType = { "artistRow" }) {
                                Column {
                                    // TODO: Open details
                                    ArtistRow(
                                        booth = it.booth,
                                        name = it.name ?: it.artistId.toString(),
                                        modifier = Modifier.clickable { onSelectHistoryEntry(it.artistId, it.timestamp) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                }
            }
        }
    }

    @Composable
    private fun ArtistRow(
        booth: String?,
        name: String,
        modifier: Modifier = Modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            if (booth != null) {
                Text(
                    text = booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                )
            }

            Text(
                text = name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (booth.isNullOrBlank()) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )
        }
    }

    private enum class Tab(val icon: ImageVector, val label: StringResource) {
        QUEUE(Icons.AutoMirrored.Default.List, Res.string.alley_edit_artist_form_queue_tab_queue),
        HISTORY(Icons.Default.History, Res.string.alley_edit_artist_form_queue_tab_history),
    }
}
