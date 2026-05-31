package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_added
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_updated
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallySeriesImage
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource

internal object StampRallyChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        dataYear: DataYear,
        onClickBack: () -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
        viewModel: StampRallyChangelogViewModel = viewModel {
            graph.stampRallyChangelogViewModelFactory.create(dataYear)
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        StampRallyChangelogScreen(
            changes = { changes },
            seriesTitles = { seriesTitles },
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickStampRally = onClickStampRally,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
            onClickImage = onClickImage,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        seriesImage: (seriesId: String) -> String?,
        onClickBack: () -> Unit,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(Res.string.alley_changelog_title)) },
                )
            },
        ) {
            val listState = rememberLazyListState()
            val scrollAreaState = rememberScrollAreaState(listState)
            ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize().padding(it)) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(bottom = 200.dp),
                        modifier = Modifier.widthIn(max = 960.dp)
                    ) {
                        changes().forEach {
                            day(
                                dayChange = it,
                                seriesTitles = seriesTitles,
                                seriesImage = seriesImage,
                                onClickStampRally = onClickStampRally,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                                onClickImage = onClickImage,
                            )
                        }
                    }

                    PrimaryVerticalScrollbar(listState)
                }
            }
        }
    }

    private fun LazyListScope.day(
        dayChange: DayChange,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        seriesImage: (seriesId: String) -> String?,
        onClickStampRally: (StampRallyChangelogEntry) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (StampRallyChangelogEntry, CatalogImage) -> Unit,
    ) {
        item(key = listOf("header", dayChange.date), contentType = "header") {
            Text(
                text = dayChange.date.format(LocalDate.Formats.ISO),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                )
            )
        }
        if (dayChange.added.isNotEmpty()) {
            item(key = listOf("headerAdded", dayChange.date), contentType = "headerAdded") {
                Text(
                    text = stringResource(Res.string.alley_changelog_title_added),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(
                items = dayChange.added,
                key = { _, change -> listOf(change.stampRallyId, change.date) },
                contentType = { _, _ -> "stampRallyRow" },
            ) { index, change ->
                StampRallyRow(
                    stampRally = change,
                    isLast = index == dayChange.added.lastIndex,
                    seriesTitles = seriesTitles,
                    seriesImage = seriesImage,
                    onClick = { onClickStampRally(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                    onClickImage = { onClickImage(change, it) },
                )
            }
        }

        if (dayChange.updated.isNotEmpty()) {
            if (dayChange.added.isNotEmpty()) {
                item(
                    key = listOf("dividerUpdated", dayChange.date),
                    contentType = "dividerUpdated"
                ) {
                    HorizontalDivider(modifier = Modifier.padding(start = 8.dp))
                }
            }
            item(key = listOf("headerUpdated", dayChange.date), contentType = "headerUpdated") {
                Text(
                    text = stringResource(Res.string.alley_changelog_title_updated),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(
                items = dayChange.updated,
                key = { _, change -> listOf(change.stampRallyId, change.date) },
                contentType = { _, _ -> "stampRallyRow" },
            ) { index, change ->
                StampRallyRow(
                    stampRally = change,
                    isLast = index == dayChange.updated.lastIndex,
                    seriesTitles = seriesTitles,
                    seriesImage = seriesImage,
                    onClick = { onClickStampRally(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                    onClickImage = { onClickImage(change, it) },
                )
            }
        }

        item(key = listOf("divider", dayChange.date), contentType = "divider") {
            HorizontalDivider(thickness = 2.dp)
        }
    }

    @Composable
    private fun StampRallyRow(
        stampRally: StampRallyChangelogEntry,
        isLast: Boolean,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        seriesImage: (seriesId: String) -> String?,
        onClick: () -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        onClickImage: (CatalogImage) -> Unit,
    ) {
        val seriesId = stampRally.rally.series.firstOrNull()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .conditionallyNonNull(seriesId, Modifier.heightIn(min = 80.dp))
                .height(IntrinsicSize.Min)
                .clickable(onClick = onClick)
        ) {
            Row {
                StampRallySeriesImage(
                    stampRallyId = stampRally.rally.id,
                    seriesId = seriesId,
                    hostTable = stampRally.rally.tables.firstOrNull(),
                    image = { seriesId?.let(seriesImage) }
                )

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    ) {
                        Text(
                            text = stampRally.rally.fandom,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    val images = stampRally.images
                    if (images.isNotEmpty()) {
                        ChangelogImages(
                            sharedElementId = stampRally.stampRallyId,
                            images = images,
                            onClickImage = onClickImage,
                        )
                    }

                    val series = stampRally.rally.series
                    val merch = stampRally.rally.merch

                    if (series.isNotEmpty() || merch.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .fadingEdgeEnd(
                                    startOpaque = 0.dp,
                                    startTransparent = 0.dp,
                                    endOpaque = 32.dp,
                                    endTransparent = 16.dp,
                                )
                        ) {
                            val colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            val border = AssistChipDefaults.assistChipBorder(false)
                            val seriesTitles = seriesTitles()
                            val languageOption = LocalLanguageOptionMedia.current
                            series.forEach {
                                AssistChip(
                                    colors = colors,
                                    border = border,
                                    onClick = { onClickSeries(it) },
                                    label = {
                                        Text(text = seriesTitles[it]?.name(languageOption) ?: it)
                                    },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                            merch.forEach {
                                AssistChip(
                                    colors = colors,
                                    border = border,
                                    onClick = { onClickMerch(it) },
                                    label = { Text(text = it) },
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!isLast) {
                HorizontalDivider(modifier = Modifier.padding(start = 12.dp))
            }
        }
    }

    data class DayChange(
        val date: LocalDate,
        val added: List<StampRallyChangelogEntry>,
        val updated: List<StampRallyChangelogEntry>,
    )
}
