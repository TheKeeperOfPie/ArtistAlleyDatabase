package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_action_refresh
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_tab_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_tab_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_title
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

object TagResolutionQueueScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyEditGraph,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        viewModel: TagResolutionViewModel = viewModel {
            graph.tagResolutionViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val unknownSeriesAndArtists by viewModel.unknownSeriesAndArtists.collectAsStateWithLifecycle()
        val unknownMerchAndArtists by viewModel.unknownMerchAndArtists.collectAsStateWithLifecycle()
        TagResolutionQueueScreen(
            unknownSeriesAndArtists = { unknownSeriesAndArtists },
            unknownMerchAndArtists = { unknownMerchAndArtists },
            onRefresh = viewModel::onRefresh,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
        )
    }

    @Composable
    operator fun invoke(
        unknownSeriesAndArtists: () -> List<Pair<String, List<ArtistSummary>>>,
        unknownMerchAndArtists: () -> List<Pair<String, List<ArtistSummary>>>,
        onRefresh: () -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            var tab by rememberSaveable { mutableStateOf(Tab.SERIES) }
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = { Text(stringResource(Res.string.alley_edit_tag_resolution_title)) },
                            actions = {
                                TooltipIconButton(
                                    icon = Icons.Default.Refresh,
                                    tooltipText = stringResource(Res.string.alley_edit_tag_resolution_action_refresh),
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
                },
                modifier = Modifier.widthIn(max = 1200.dp)
            ) {
                val modifier = Modifier.padding(it)
                when (tab) {
                    Tab.SERIES -> List(unknownSeriesAndArtists, onClickSeries, modifier)
                    Tab.MERCH -> List(unknownMerchAndArtists, onClickMerch, modifier)
                }
            }
        }
    }

    @Composable
    private fun List(
        items: () -> List<Pair<String, *>>,
        onClickItem: (String) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 72.dp),
            modifier = modifier,
        ) {
            items(items = items(), key = { it.first }) {
                Text(
                    text = it.first,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickItem(it.first) }
                        .padding(16.dp)
                )
            }
        }
    }

    private enum class Tab(val icon: ImageVector, val label: StringResource) {
        SERIES(Icons.Default.Tv, Res.string.alley_edit_tag_resolution_tab_series),
        MERCH(Icons.Default.ShoppingBag, Res.string.alley_edit_tag_resolution_tab_merch),
    }
}
