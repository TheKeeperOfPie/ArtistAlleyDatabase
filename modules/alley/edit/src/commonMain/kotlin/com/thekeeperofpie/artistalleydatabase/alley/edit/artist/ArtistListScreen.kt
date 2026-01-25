package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_refresh_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_search_placeholder
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_list_sort_content_description
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.uuid.Uuid
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

internal object ArtistListScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onAddArtist: (dataYear: DataYear) -> Unit,
        onEditArtist: (dataYear: DataYear, id: Uuid) -> Unit,
        viewModel: ArtistListViewModel = viewModel {
            graph.artistListViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        ArtistListScreen(
            state = viewModel.state,
            onRefresh = viewModel::refresh,
            onAddArtist = { onAddArtist(viewModel.state.dataYear.value) },
            onEditArtist = { onEditArtist(viewModel.state.dataYear.value, it) },
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        onRefresh: () -> Unit,
        onAddArtist: () -> Unit,
        onEditArtist: (id: Uuid) -> Unit,
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
                                            Res.string.alley_edit_artist_list_search_placeholder,
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
                                                contentDescription = stringResource(Res.string.alley_edit_artist_action_refresh_content_description)
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
                                            contentDescription = stringResource(Res.string.alley_edit_artist_list_sort_content_description)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        val sortBy by state.sortBy.collectAsStateWithLifecycle()
                                        Text(stringResource(sortBy.label))
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                        ) {
                                            ArtistListSortBy.entries.forEach {
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

                            val tab by state.tab.collectAsStateWithLifecycle()
                            PrimaryScrollableTabRow(
                                selectedTabIndex = ArtistListTab.entries.indexOf(tab),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ArtistListTab.entries.forEach {
                                    LeadingIconTab(
                                        selected = tab == it,
                                        onClick = { state.tab.value = it },
                                        icon = { Icon(it.icon, null) },
                                        text = { Text(stringResource(it.label)) },
                                    )
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    val dataYear by state.dataYear.collectAsStateWithLifecycle()
                    val currentYear = remember {
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
                    }
                    if (dataYear.dates.year >= currentYear) {
                        LargeFloatingActionButton(onClick = onAddArtist) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(Res.string.alley_edit_artist_action_add),
                            )
                        }
                    }
                },
                modifier = Modifier.widthIn(max = 1200.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val dataYear by state.dataYear.collectAsStateWithLifecycle()
                val sortBy by state.sortBy.collectAsStateWithLifecycle()
                val tab by state.tab.collectAsStateWithLifecycle()
                val state = key(dataYear, sortBy, tab) { rememberLazyListState() }
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    items(items = entries, key = { it.id }, contentType = { "artistRow" }) {
                        Column {
                            ArtistRow(it, modifier = Modifier.clickable { onEditArtist(it.id) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ArtistRow(artist: ArtistSummary, modifier: Modifier = Modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(horizontal = 16.dp),
        ) {
            artist.booth?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleLarge
                        .copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                        .sharedElement("booth", artist.id)
                )
            }

            val text = artist.name?.takeUnless { it.isBlank() } ?: artist.id.toString()
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("name", artist.id)
                    .weight(1f)
                    .padding(
                        start = if (artist.booth.isNullOrBlank()) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )
        }
    }

    @Stable
    class State(
        val query: TextFieldState,
        val dataYear: MutableStateFlow<DataYear>,
        val sortBy: MutableStateFlow<ArtistListSortBy>,
        val tab: MutableStateFlow<ArtistListTab>,
        val entries: StateFlow<List<ArtistSummary>>,
    )
}

@Preview
@Composable
private fun HomeScreenPreview() {
    ArtistListScreen(
        state = remember {
            val baseSummary = ArtistSummary(
                id = Uuid.random(),
                booth = "",
                name = "",
                socialLinks = emptyList(),
                storeLinks = emptyList(),
                portfolioLinks = emptyList(),
                catalogLinks = emptyList(),
                seriesInferred = emptyList(),
                seriesConfirmed = emptyList(),
                merchInferred = emptyList(),
                merchConfirmed = emptyList(),
                images = emptyList(),
            )
            ArtistListScreen.State(
                query = TextFieldState("Two"),
                dataYear = MutableStateFlow(DataYear.LATEST),
                tab = MutableStateFlow(ArtistListTab.ALL),
                sortBy = MutableStateFlow(ArtistListSortBy.NAME),
                entries = ReadOnlyStateFlow(
                    listOf(
                        baseSummary.copy(
                            id = Uuid.parse("8ef67e71-ca6b-4527-80ea-8289d803d3c0"),
                            booth = "A01",
                            name = "Artist One"
                        ),
                        baseSummary.copy(
                            id = Uuid.parse("0cc87f6f-6118-4b87-b442-44d5a78ad4a8"),
                            booth = "C39",
                            name = "Artist Two"
                        ),
                        baseSummary.copy(
                            id = Uuid.parse("e979352b-7014-40bb-84f6-dba47225de4b"),
                            booth = "G08",
                            name = "Artist Three"
                        ),
                        baseSummary.copy(
                            id = Uuid.parse("97e00ad6-77f1-4813-a499-76ed1de5f347"),
                            booth = "H11",
                            name = "Artist Four"
                        ),
                    )
                )
            )
        },
        onRefresh = {},
        onAddArtist = {},
        onEditArtist = {},
    )
}
