package com.thekeeperofpie.artistalleydatabase.alley.edit.catalog

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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_catalogs_queue_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_catalogs_queue_action_confirm_delete_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_catalogs_queue_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_catalogs_queue_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_catalogs_queue_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.Delete
import com.thekeeperofpie.artistalleydatabase.icons.filled.Refresh
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object ArtistCatalogsQueueScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        dataYear: DataYear,
        onSelectEntry: (Catalog) -> Unit,
        viewModel: ArtistCatalogsQueueViewModel = viewModel {
            graph.artistCatalogsQueueViewModelFactory.create(dataYear)
        },
    ) {
        val queue by viewModel.queue.collectAsStateWithLifecycle()
        ArtistCatalogsQueueScreen(
            queue = { queue },
            onRefresh = viewModel::refresh,
            onSelectEntry = onSelectEntry,
            onConfirmDeleteEntry = viewModel::deleteEntry,
        )
    }

    @Composable
    operator fun invoke(
        queue: () -> List<Catalog>,
        onRefresh: () -> Unit,
        onSelectEntry: (Catalog) -> Unit,
        onConfirmDeleteEntry: (Catalog) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        TopAppBar(
                            title = { Text(stringResource(Res.string.alley_edit_artist_catalogs_queue_title)) },
                            actions = {
                                TooltipIconButton(
                                    icon = Icons.Default.Refresh,
                                    tooltipText = stringResource(Res.string.alley_edit_artist_catalogs_queue_action_refresh),
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
                        key = { it.booth + it.artistId },
                    ) {
                        Column {
                            ArtistCatalogRow(
                                catalog = it,
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
    private fun ArtistCatalogRow(
        catalog: Catalog,
        onConfirmDeleteEntry: (Catalog) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            Text(
                text = catalog.booth,
                style = MaterialTheme.typography.titleLarge
                    .copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
            )

            Text(
                text = catalog.link,
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
                                Res.string.alley_edit_artist_catalogs_queue_action_confirm_delete_title,
                                catalog.booth,
                                catalog.link
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onConfirmDeleteEntry(catalog)
                            showConfirmDeleteDialog = false
                        }) {
                            Text(stringResource(Res.string.alley_edit_artist_catalogs_queue_action_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDeleteDialog = false }) {
                            Text(stringResource(Res.string.alley_edit_artist_catalogs_queue_action_cancel))
                        }
                    }
                )
            }
        }
    }

    data class Catalog(
        val booth: String,
        val artistId: Uuid,
        val link: String,
    )
}
