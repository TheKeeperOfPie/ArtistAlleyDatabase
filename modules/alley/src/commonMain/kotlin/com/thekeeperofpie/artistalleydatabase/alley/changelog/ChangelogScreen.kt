package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_added
import artistalleydatabase.modules.alley.generated.resources.alley_changelog_title_updated
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleyGraph
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.artist.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.stringResource

internal object ChangelogScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyGraph,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistEntryAnimeExpo2026Changelog) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
        viewModel: ChangelogViewModel = viewModel {
            graph.changelogViewModelFactory.create(createSavedStateHandle())
        },
    ) {
        val changes by viewModel.changes.collectAsStateWithLifecycle()
        val seriesTitles by viewModel.seriesEntryCache.series.collectAsStateWithLifecycle()
        ChangelogScreen(
            changes = { changes },
            seriesTitles = { seriesTitles },
            onClickBack = onClickBack,
            onClickArtist = onClickArtist,
            onClickSeries = onClickSeries,
            onClickMerch = onClickMerch,
        )
    }

    @Composable
    operator fun invoke(
        changes: () -> List<DayChange>,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClickBack: () -> Unit,
        onClickArtist: (ArtistEntryAnimeExpo2026Changelog) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(Res.string.alley_changelog_title)) },
                )
            },
        ) {
            val scrollState = rememberLazyListState()
            val scrollAreaState = rememberScrollAreaState(scrollState)
            ScrollArea(state = scrollAreaState, modifier = Modifier.fillMaxSize().padding(it)) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
                    val changes = changes()
                    LazyColumn(modifier = Modifier.widthIn(max = 960.dp)) {
                        changes.forEach {
                            day(
                                dayChange = it,
                                seriesTitles = seriesTitles,
                                onClickArtist = onClickArtist,
                                onClickSeries = onClickSeries,
                                onClickMerch = onClickMerch,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun LazyListScope.day(
        dayChange: DayChange,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClickArtist: (ArtistEntryAnimeExpo2026Changelog) -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
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
                key = { _, change -> listOf(change.artistId, change.date) },
                contentType = { _, _ -> "artistRow" },
            ) { index, change ->
                ArtistRow(
                    artist = change,
                    isLast = index == dayChange.added.lastIndex,
                    seriesTitles = seriesTitles,
                    onClick = { onClickArtist(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                )
            }
        }

        if (dayChange.updated.isNotEmpty()) {
            if (dayChange.added.isNotEmpty()) {
                item(key = listOf("dividerUpdated", dayChange.date), contentType = "dividerUpdated") {
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
                key = { _, change -> listOf(change.artistId, change.date) },
                contentType = { _, _ -> "artistRow" },
            ) { index, change ->
                ArtistRow(
                    artist = change,
                    isLast = index == dayChange.updated.lastIndex,
                    seriesTitles = seriesTitles,
                    onClick = { onClickArtist(change) },
                    onClickSeries = onClickSeries,
                    onClickMerch = onClickMerch,
                )
            }
        }

        item(key = listOf("divider", dayChange.date), contentType = "divider") {
            HorizontalDivider(thickness = 2.dp)
        }
    }

    @Composable
    private fun ArtistRow(
        artist: ArtistEntryAnimeExpo2026Changelog,
        isLast: Boolean,
        seriesTitles: () -> Map<String, GetSeriesTitles>,
        onClick: () -> Unit,
        onClickSeries: (String) -> Unit,
        onClickMerch: (String) -> Unit,
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    )
                    val booth = artist.booth
                    Text(
                        // Always render 3 characters
                        text = booth?.ifEmpty { null } ?: "   ",
                        style = MaterialTheme.typography.titleMedium
                            .copy(
                                fontFamily = FontFamily.Monospace,
                                lineHeightStyle = lineHeightStyle,
                            ),
                    )

                    Text(
                        text = artist.name,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                            .copy(lineHeightStyle = lineHeightStyle),
                    )
                }

                val series = TagUtils.combineForDisplay(
                    inferred = artist.seriesInferred.orEmpty(),
                    confirmed = artist.seriesConfirmed.orEmpty(),
                    randomSeed = LocalStableRandomSeed.current,
                )
                val merch = TagUtils.combineForDisplay(
                    inferred = artist.merchInferred.orEmpty(),
                    confirmed = artist.merchConfirmed.orEmpty(),
                    randomSeed = LocalStableRandomSeed.current,
                )

                if (series.isNotEmpty() || merch.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                }

                val seriesTitles = seriesTitles()
                if (series.isNotEmpty()) {
                    SeriesRow(
                        series = series.take(TagUtils.TAGS_TO_SHOW).mapNotNull { seriesTitles[it] },
                        hasMoreSeries = series.size > TagUtils.TAGS_TO_SHOW,
                        onSeriesClick = onClickSeries,
                        onMoreClick = onClick,
                    )
                }

                if (merch.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                            .fadingEdgeEnd(
                                startOpaque = 12.dp,
                                endOpaque = 32.dp,
                                endTransparent = 16.dp,
                            )
                    ) {
                        Spacer(Modifier.width(12.dp))
                        val colors = AssistChipDefaults.assistChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        val border = AssistChipDefaults.assistChipBorder(false)
                        merch.take(TagUtils.TAGS_TO_SHOW).forEach {
                            AssistChip(
                                colors = colors,
                                border = border,
                                onClick = { onClickMerch(it) },
                                label = { Text(text = it) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (merch.size > TagUtils.TAGS_TO_SHOW) {
                            AssistChip(
                                colors = colors,
                                border = border,
                                onClick = onClick,
                                label = { Text("...") },
                                modifier = Modifier.height(24.dp)
                            )
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
        val added: List<ArtistEntryAnimeExpo2026Changelog>,
        val updated: List<ArtistEntryAnimeExpo2026Changelog>,
    )
}
