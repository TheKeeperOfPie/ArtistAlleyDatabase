package com.thekeeperofpie.artistalleydatabase.debug.network

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sebastianneubauer.jsontree.JsonTree
import com.thekeeperofpie.artistalleydatabase.debug.DebugComponent
import com.thekeeperofpie.artistalleydatabase.debug.R
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DebugNetworkPanel {

    @Composable
    operator fun invoke(debugComponent: DebugComponent) {
        val viewModel = viewModel { debugComponent.debugNetworkViewModel() }

        val expandedIds = remember { mutableStateListOf<String>() }
        Column {
            LazyColumn(Modifier.weight(1f)) {
                items(viewModel.graphQlData, key = { it.id }) {
                    val expanded by remember { derivedStateOf { expandedIds.contains(it.id) } }
                    val onClick = { expandedIds.toggle(it.id) }
                    Column(modifier = Modifier.clickable(onClick = onClick)) {
                        HeaderText(it, expanded, onDropdownIconClick = onClick)

                        var selectedIndex by remember { mutableIntStateOf(0) }
                        AnimatedVisibility(visible = expanded) {
                            Column {
                                TabRow(
                                    data = it,
                                    selectedIndex = selectedIndex,
                                    onSelectedIndexChange = { selectedIndex = it },
                                )
                                TabContent(data = it, selectedIndex = selectedIndex)
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TextButton(onClick = { viewModel.clear() }) {
                    Text(stringResource(R.string.debug_network_button_clear))
                }
            }
        }
    }

    @Composable
    private fun HeaderText(
        data: DebugNetworkController.GraphQlData,
        expanded: Boolean,
        onDropdownIconClick: () -> Unit,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = data.request.operationName,
                color = if (data.response?.errors.isNullOrEmpty()) {
                    Color.Unspecified
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = stringResource(
                    R.string.debug_network_graphql_header_dropdown_content_description
                ),
                onClick = onDropdownIconClick,
            )
        }
    }

    @Composable
    private fun TabRow(
        data: DebugNetworkController.GraphQlData,
        selectedIndex: Int,
        onSelectedIndexChange: (Int) -> Unit,
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab.entries.fastForEachIndexed { index, tab ->
                if (tab == Tab.Errors && data.response?.errors.isNullOrEmpty()) {
                    return@fastForEachIndexed
                }

                Tab(
                    selected = selectedIndex == index,
                    onClick = { onSelectedIndexChange(index) }) {
                    Text(
                        text = stringResource(tab.labelRes),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun TabContent(
        data: DebugNetworkController.GraphQlData,
        selectedIndex: Int,
    ) {
        AnimatedContent(
            targetState = Tab.entries[selectedIndex],
            label = "GraphQLData tab content",
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
        ) {
            when (it) {
                Tab.Query -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    TimestampText(data.request.timestamp)
                    RegularText(data.request.query)
                }
                Tab.Variables -> JsonText(data.request.variablesJson)
                Tab.Response -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    if (data.response?.final != true) {
                        CircularProgressIndicator()
                    }
                    if (data.response != null) {
                        TimestampText(data.response.timestamp)
                        JsonText(data.response.bodyJson)
                    }
                }
                Tab.Errors -> RegularText(text = data.response?.errors.toString())
            }
        }
    }

    @Composable
    private fun TimestampText(timestamp: Instant) {
        val datetime = remember {
            LocalDateTime.Formats.ISO.format(timestamp.toLocalDateTime(TimeZone.currentSystemDefault()))
        }
        Text(
            text = datetime,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        )
    }

    @Composable
    private fun RegularText(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .heightIn(max = 400.dp)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        )
    }

    @Composable
    private fun JsonText(json: String) {
        var error by remember { mutableStateOf<String?>(null) }
        if (error == null) {
            JsonTree(
                json = json,
                onLoading = { CircularProgressIndicator() },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                showIndices = true,
                showItemCount = true,
                onError = { error = it.stackTraceToString() },
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            )
        } else {
            Text(
                text = error.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    private enum class Tab(val labelRes: Int) {
        Query(R.string.debug_network_graphql_tab_query),
        Variables(R.string.debug_network_graphql_tab_variables),
        Response(R.string.debug_network_graphql_tab_response),
        Errors(R.string.debug_network_graphql_tab_errors),
    }
}
