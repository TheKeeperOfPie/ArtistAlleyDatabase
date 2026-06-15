@file:OptIn(ExperimentalFlexBoxApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexAlignItems
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexBoxConfig
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_inference_by_both
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_inference_by_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_inference_by_tables
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StampRallySummaryRow(
    stampRally: StampRallySummary,
    seriesById: () -> Map<String, SeriesInfo>,
    modifier: Modifier = Modifier,
) = StampRallySummaryRow(
    stampRallyId = stampRally.id,
    fandom = stampRally.fandom,
    hostTable = stampRally.hostTable,
    series = stampRally.series,
    seriesById = seriesById,
    modifier = modifier,
)

@Composable
fun StampRallySummaryRow(
    stampRallyId: String,
    fandom: String?,
    hostTable: String?,
    series: List<String>,
    seriesById: () -> Map<String, SeriesInfo>,
    modifier: Modifier = Modifier,
) {
    @OptIn(ExperimentalFlexBoxApi::class)
    FlexBox(
        config = FlexBoxConfig {
            wrap(FlexWrap.Wrap)
            columnGap(12.dp)
            alignItems(FlexAlignItems.Center)
        },
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Text(
            text = hostTable?.ifEmpty { null } ?: "   ",
            style = MaterialTheme.typography.titleLarge
                .copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        val text = fandom?.takeUnless { it.isBlank() } ?: stampRallyId
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        if (series.isNotEmpty()) {
            val seriesById = seriesById()
            val series = remember(series) {
                series.map { seriesById[it] ?: SeriesInfo.fake(it) }
            }
            series.forEach {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(it.name(LocalLanguageOptionMedia.current))
                    },
                )
            }
        }
    }
}


@Composable
internal fun StampRalliesInferred(
    state: StampRallyFormState,
    inferRallies: (List<String>, List<String>) -> Flow<StampRallyInference.Output>,
    seriesById: () -> Map<String, SeriesInfo>,
    modifier: Modifier = Modifier,
    callerId: String? = null,
) {
    OutlinedCard(modifier = modifier) {
        val inferredRallies by produceState(
            StampRallyInference.Output(emptyMap(), emptyMap(), emptyMap()),
            state,
            inferRallies,
            callerId,
        ) {
            snapshotFlow { state.tables.map { it.booth } to state.series.map { it.id } }
                .flatMapLatest { inferRallies(it.first, it.second) }
                .mapLatest {
                    if (callerId == null) return@mapLatest it
                    StampRallyInference.Output(
                        byTables = it.byTables
                            .mapValues { it.value.filter { it.id != callerId } }
                            .filterValues { it.isNotEmpty() },
                        bySeries = it.bySeries
                            .mapValues { it.value.filter { it.id != callerId } }
                            .filterValues { it.isNotEmpty() },
                        byBoth = it.byBoth
                            .mapValues { it.value.filter { it.id != callerId } }
                            .filterValues { it.isNotEmpty() },
                    )
                }
                .collectLatest { value = it }
        }

        InferredRalliesSection(
            title = Res.string.alley_edit_stamp_rally_inference_by_both,
            rallies = inferredRallies.byBoth,
            seriesById = seriesById,
            expandByDefault = true,
            key = {
                FlexBox(config = FlexBoxConfig { columnGap(8.dp) }) {
                    it.tables.forEach { AssistChip(label = { Text(it) }, onClick = {}) }
                    it.seriesIds.forEach { AssistChip(label = { Text(it) }, onClick = {}) }
                }
            },
        )

        InferredRalliesSection(
            title = Res.string.alley_edit_stamp_rally_inference_by_series,
            rallies = inferredRallies.bySeries,
            seriesById = seriesById,
            key = {
                FlexBox(config = FlexBoxConfig { columnGap(8.dp) }) {
                    it.forEach { AssistChip(label = { Text(it) }, onClick = {}) }
                }
            },
        )

        InferredRalliesSection(
            title = Res.string.alley_edit_stamp_rally_inference_by_tables,
            rallies = inferredRallies.byTables,
            seriesById = seriesById,
            key = {
                FlexBox(config = FlexBoxConfig { columnGap(8.dp) }) {
                    it.forEach { AssistChip(label = { Text(it) }, onClick = {}) }
                }
            },
        )
    }
}

@Composable
private fun <Key> InferredRalliesSection(
    title: StringResource,
    rallies: Map<Key, List<StampRallySummary>>,
    seriesById: () -> Map<String, SeriesInfo>,
    expandByDefault: Boolean = false,
    key: @Composable (Key) -> Unit,
) {
    // Save this before returning so that expansion state is kept between inferences
    var expanded by remember { mutableStateOf(expandByDefault) }
    if (rallies.isEmpty()) return
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { expanded = !expanded }
        ) {
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            TrailingDropdownIconButton(
                expanded = expanded,
                contentDescription = null,
                onClick = { expanded = !expanded },
            )
        }
        if (expanded) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                rallies.forEach {
                    key(it.key)
                    it.value.forEachIndexed { index, summary ->
                        StampRallySummaryRow(
                            stampRally = summary,
                            seriesById = seriesById,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        if (index != it.value.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
        HorizontalDivider()
    }
}
