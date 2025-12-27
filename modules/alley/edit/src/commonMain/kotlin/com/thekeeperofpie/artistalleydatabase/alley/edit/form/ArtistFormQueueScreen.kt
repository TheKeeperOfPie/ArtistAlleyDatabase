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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_queue_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistFormQueueEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

internal object ArtistFormQueueScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onSelectEntry: (artistId: Uuid) -> Unit,
        viewModel: ArtistFormQueueViewModel = viewModel {
            graph.artistFormQueueViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val entries by viewModel.entries.collectAsStateWithLifecycle()
        ArtistFormQueueScreen(
            entries = { entries },
            onRefresh = viewModel::refresh,
            onSelectEntry = onSelectEntry,
        )
    }

    @Composable
    operator fun invoke(
        entries: () -> List<ArtistFormQueueEntry>,
        onRefresh: () -> Unit,
        onSelectEntry: (artistId: Uuid) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val entries = entries()
            Scaffold(
                topBar = {
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
                },
                modifier = Modifier.widthIn(max = 1200.dp)
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    items(items = entries, key = { it.artistId }, contentType = { "artistRow" }) {
                        Column {
                            ArtistRow(
                                artist = it,
                                modifier = Modifier.clickable { onSelectEntry(it.artistId) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ArtistRow(artist: ArtistFormQueueEntry, modifier: Modifier = Modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            val booth = artist.afterBooth?.ifBlank { null } ?: artist.beforeBooth
            if (booth != null) {
                Text(
                    text = booth,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                )
            }

            val text = artist.afterName?.ifBlank { null }
                ?: artist.beforeName?.ifBlank { null }
                ?: artist.artistId.toString()
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (booth == null) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )
        }
    }
}
