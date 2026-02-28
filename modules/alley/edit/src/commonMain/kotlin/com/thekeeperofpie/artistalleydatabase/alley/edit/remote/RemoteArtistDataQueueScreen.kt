package com.thekeeperofpie.artistalleydatabase.alley.edit.remote

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_queue_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_queue_tab_history
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_queue_tab_queue
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_remote_artist_data_queue_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistRemoteEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal object RemoteArtistDataQueueScreen {


    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onSelectEntry: (ArtistRemoteEntry) -> Unit,
        viewModel: RemoteArtistDataQueueViewModel = viewModel {
            graph.remoteArtistDataQueueViewModel
        },
    ) {
        val data by viewModel.data.collectAsStateWithLifecycle()
        RemoteArtistDataQueueScreen(
            data = data,
            onRefresh = viewModel::refresh,
            onSelectEntry = onSelectEntry,
        )
    }

    @Composable
    operator fun invoke(
        data: List<ArtistRemoteEntry>,
        onRefresh: () -> Unit,
        onSelectEntry: (ArtistRemoteEntry) -> Unit,
    ) {
        val (queue, history) = remember(data) { data.partition { !it.consumed } }
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            var tab by rememberSaveable { mutableStateOf(Tab.QUEUE) }
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column {
                            TopAppBar(
                                title = { Text(stringResource(Res.string.alley_edit_remote_artist_data_queue_title)) },
                                actions = {
                                    TooltipIconButton(
                                        icon = Icons.Default.Refresh,
                                        tooltipText = stringResource(Res.string.alley_edit_remote_artist_data_queue_action_refresh),
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
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    items(
                        items = when (tab) {
                            Tab.QUEUE -> queue
                            Tab.HISTORY -> history
                        },
                        key = { it.booth + it.name },
                    ) {
                        Column {
                            ArtistDataRow(
                                booth = it.booth,
                                name = it.name.orEmpty(),
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
    private fun ArtistDataRow(
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
        QUEUE(
            Icons.AutoMirrored.Default.List,
            Res.string.alley_edit_remote_artist_data_queue_tab_queue
        ),
        HISTORY(Icons.Default.History, Res.string.alley_edit_remote_artist_data_queue_tab_history),
    }
}
