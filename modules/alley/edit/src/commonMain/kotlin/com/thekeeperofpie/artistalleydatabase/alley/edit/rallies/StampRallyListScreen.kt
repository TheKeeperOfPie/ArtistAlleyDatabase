package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_action_refresh_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_list_search_placeholder
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_list_sort_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

internal object StampRallyListScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onAddStampRally: (dataYear: DataYear) -> Unit,
        onEditStampRally: (dataYear: DataYear, id: String) -> Unit,
        viewModel: StampRallyListViewModel = viewModel {
            graph.stampRallyListViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        StampRallyListScreen(
            state = viewModel.state,
            seriesById = { seriesById },
            onRefresh = viewModel::refresh,
            onAddStampRally = { onAddStampRally(viewModel.state.dataYear.value) },
            onEditStampRally = { onEditStampRally(viewModel.state.dataYear.value, it) },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        seriesById: () -> Map<String, SeriesInfo>,
        onRefresh: () -> Unit,
        onAddStampRally: () -> Unit,
        onEditStampRally: (id: String) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            val entries by state.entries.collectAsStateWithLifecycle()
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val query = state.query
                            val isNotEmpty by remember { derivedStateOf { query.text.isNotEmpty() } }
                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                isBackEnabled = isNotEmpty,
                                onBackCompleted = query::clearText,
                            )

                            StaticSearchBar(
                                query = query,
                                onSearch = {},
                                placeholder = {
                                    Text(
                                        stringResource(
                                            Res.string.alley_edit_stamp_rally_list_search_placeholder,
                                            entries.size,
                                        )
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        AnimatedVisibility(isNotEmpty) {
                                            IconButton(onClick = query::clearText) {
                                                Icon(
                                                    imageVector = Icons.Filled.Clear,
                                                    contentDescription = stringResource(
                                                        EntryRes.string.entry_search_clear
                                                    ),
                                                )
                                            }
                                        }
                                        IconButton(onClick = onRefresh) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = stringResource(Res.string.alley_edit_stamp_rally_action_refresh_content_description)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            val dataYearHeaderState =
                                rememberDataYearHeaderState(state.dataYear, null)
                            DataYearHeader(
                                state = dataYearHeaderState,
                                showFeedbackReminder = false,
                                onOpenExport = {},
                                onOpenChangelog = {},
                                onOpenSettings = {},
                                additionalActions = {
                                    var expanded by remember { mutableStateOf(false) }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = Dp.Hairline,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(12.dp),
                                            )
                                            .clickable { expanded = !expanded }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.Sort,
                                            contentDescription = stringResource(Res.string.alley_edit_stamp_rally_list_sort_content_description)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val sortBy by state.sortBy.collectAsStateWithLifecycle()
                                        Text(stringResource(sortBy.label))
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                        ) {
                                            StampRallyListSortBy.entries.forEach {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(it.label)) },
                                                    onClick = {
                                                        state.sortBy.value = it
                                                        expanded = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    }
                },
                floatingActionButton = {
                    val dataYear by state.dataYear.collectAsStateWithLifecycle()
                    val currentYear = remember {
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
                    }
                    if (dataYear.dates.year >= currentYear) {
                        LargeFloatingActionButton(onClick = onAddStampRally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.alley_edit_stamp_rally_action_add),
                            )
                        }
                    }
                },
                modifier = Modifier.widthIn(max = 1200.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val dataYear by state.dataYear.collectAsStateWithLifecycle()
                val sortBy by state.sortBy.collectAsStateWithLifecycle()
                val state = key(dataYear, sortBy) { rememberLazyListState() }
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    items(items = entries, key = { it.id }, contentType = { "artistRow" }) {
                        Column {
                            StampRallySummaryRow(
                                stampRally = it,
                                seriesById = seriesById,
                                modifier = Modifier.clickable { onEditStampRally(it.id) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Stable
    class State(
        val query: TextFieldState,
        val dataYear: MutableStateFlow<DataYear>,
        val sortBy: MutableStateFlow<StampRallyListSortBy>,
        val entries: StateFlow<List<StampRallySummary>>,
    )
}
