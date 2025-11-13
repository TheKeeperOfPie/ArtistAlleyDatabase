package com.thekeeperofpie.artistalleydatabase.alley.edit.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add
import artistalleydatabase.modules.entry.generated.resources.entry_search_clear
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.ui.DataYearHeader
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberDataYearHeaderState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.StaticSearchBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid
import artistalleydatabase.modules.entry.generated.resources.Res as EntryRes

internal object HomeScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onAddArtist: (dataYear: DataYear) -> Unit,
        onEditArtist: (dataYear: DataYear, id: Uuid) -> Unit,
        viewModel: HomeViewModel = viewModel {
            graph.homeViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        HomeScreen(
            query = viewModel.query,
            dataYear = viewModel.dataYear,
            entries = viewModel.entries,
            onAddArtist = { onAddArtist(viewModel.dataYear.value) },
            onEditArtist = { onEditArtist(viewModel.dataYear.value, it) },
        )
    }

    @Composable
    operator fun invoke(
        query: MutableStateFlow<String>,
        dataYear: MutableStateFlow<DataYear>,
        entries: StateFlow<List<ArtistSummary>>,
        onAddArtist: () -> Unit,
        onEditArtist: (id: Uuid) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            Scaffold(
                topBar = {
                    EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            var query by query.collectAsMutableStateWithLifecycle()
                            val isNotEmpty by remember { derivedStateOf { query.isNotEmpty() } }
                            NavigationBackHandler(
                                state = rememberNavigationEventState(NavigationEventInfo.None),
                                isBackEnabled = isNotEmpty,
                            ) {
                                query = ""
                            }

                            StaticSearchBar(
                                query = query,
                                onQueryChange = { query = it },
                                onSearch = {},
                                trailingIcon = {
                                    AnimatedVisibility(isNotEmpty) {
                                        IconButton(onClick = { query = "" }) {
                                            Icon(
                                                imageVector = Icons.Filled.Clear,
                                                contentDescription = stringResource(
                                                    EntryRes.string.entry_search_clear
                                                ),
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                },
                floatingActionButton = {
                    LargeFloatingActionButton(onClick = onAddArtist) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.alley_edit_artist_action_add),
                        )
                    }
                },
                modifier = Modifier.widthIn(max = 1200.dp)
            ) {
                val entries by entries.collectAsStateWithLifecycle()
                val dataYearHeaderState = rememberDataYearHeaderState(dataYear, null)
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 72.dp),
                    modifier = Modifier.padding(it)
                ) {
                    item(key = "dataYearHeader", contentType = "dataYearHeader") {
                        DataYearHeader(dataYearHeaderState, showFeedbackReminder = false)
                    }
                    items(items = entries, key = { it.id }, contentType = { "artistRow" }) {
                        Column{
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

            val text = artist.name ?: artist.id.toString()
            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .sharedElement("name", artist.id)
                    .weight(1f)
                    .padding(
                        start = if (artist.booth == null) 16.dp else 0.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    )
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        query = remember { MutableStateFlow("Some query") },
        dataYear = remember { MutableStateFlow(DataYear.LATEST) },
        entries = remember {
            ReadOnlyStateFlow(
                listOf(
                    ArtistSummary(
                        id = Uuid.parse("8ef67e71-ca6b-4527-80ea-8289d803d3c0"),
                        booth = "A01",
                        name = "Artist One"
                    ),
                    ArtistSummary(
                        id = Uuid.parse("0cc87f6f-6118-4b87-b442-44d5a78ad4a8"),
                        booth = "C39",
                        name = "Artist Two"
                    ),
                    ArtistSummary(
                        id = Uuid.parse("e979352b-7014-40bb-84f6-dba47225de4b"),
                        booth = "G08",
                        name = "Artist Three"
                    ),
                    ArtistSummary(
                        id = Uuid.parse("97e00ad6-77f1-4813-a499-76ed1de5f347"),
                        booth = "H11",
                        name = "Artist Four"
                    ),
                )
            )
        },
        onAddArtist = {},
        onEditArtist = {},
    )
}
