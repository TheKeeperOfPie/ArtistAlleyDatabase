package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_list_added
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_list_deleted
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_label_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_history_title
import com.kmpalette.bodyTextColor
import com.kmpalette.palette.graphics.Palette
import com.materialkolor.ktx.harmonize
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistHistoryEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.time.Instant
import kotlin.uuid.Uuid

object ArtistHistoryScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
        viewModel: ArtistHistoryViewModel = viewModel {
            graph.artistHistoryViewModelFactory.create(dataYear, artistId)
        },
    ) {
        val history by viewModel.history.collectAsStateWithLifecycle()
        val seriesById by viewModel.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.merchById.collectAsStateWithLifecycle()
        ArtistHistoryScreen(
            history = { history },
            dataYear = dataYear,
            artistId = artistId,
            seriesById = { seriesById },
            merchById = { merchById },
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
        )
    }

    @Composable
    operator fun invoke(
        history: () -> List<ArtistHistoryEntryWithDiff>,
        dataYear: DataYear,
        artistId: Uuid,
        seriesById: () -> Map<String, SeriesInfo>,
        merchById: () -> Map<String, MerchInfo>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val history = history()
                        val name = remember(history) {
                            history.firstNotNullOfOrNull { it.entry.name }.orEmpty()
                        }
                        Text(
                            stringResource(
                                Res.string.alley_edit_artist_history_title,
                                stringResource(dataYear.shortName),
                                name,
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack() }) },
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(scaffoldPadding)
            ) {
                Row(modifier = Modifier.widthIn(max = 1200.dp)) {
                    val history = history()
                    var selectedIndex by rememberSaveable(history) { mutableStateOf(0) }
                    val seriesById = seriesById()
                    val merchById = merchById()
                    Column(modifier = Modifier.weight(1f)) {
                        val artistFormState by produceState<ArtistFormState?>(
                            initialValue = null,
                            history,
                            selectedIndex,
                            seriesById,
                            merchById,
                        ) {
                            if (seriesById.isNotEmpty() && merchById.isNotEmpty()) {
                                withContext(PlatformDispatchers.IO) {
                                    val list = history.drop(selectedIndex).map { it.entry }
                                    val artistFormState = ArtistFormState(
                                        metadata = ArtistFormState.Metadata(),
                                        images = SnapshotStateList(),
                                        links = SnapshotStateList(),
                                        storeLinks = SnapshotStateList(),
                                        catalogLinks = SnapshotStateList(),
                                        commissions = SnapshotStateList(),
                                        seriesInferred = SnapshotStateList(),
                                        seriesConfirmed = SnapshotStateList(),
                                        merchInferred = SnapshotStateList(),
                                        merchConfirmed = SnapshotStateList(),
                                        textState = ArtistFormState.TextState(),
                                    )
                                    artistFormState.applyDatabaseEntry(
                                        ArtistHistoryEntry.rebuild(dataYear, artistId, list),
                                        seriesById,
                                        merchById,
                                    )
                                    value = artistFormState
                                }
                            }
                        }

                        val formState = artistFormState
                        if (formState != null) {
                            ArtistForm(
                                state = formState,
                                errorState = rememberErrorState(formState.textState),
                                seriesPredictions = { emptyFlow() },
                                merchPredictions = { emptyFlow() },
                                seriesImage = seriesImage,
                                forceLocked = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    ArtistHistoryTimeline(
                        history = history,
                        selectedIndex = { selectedIndex },
                        onSelectedIndexChange = { selectedIndex = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    @Composable
    fun ArtistHistoryTimeline(
        history: List<ArtistHistoryEntryWithDiff>,
        selectedIndex: () -> Int,
        onSelectedIndexChange: (Int) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier,
        ) {
            itemsIndexed(history) { index, entry ->
                HistoryEntryCard(
                    entryWithDiff = entry,
                    selected = selectedIndex() == index,
                    onClick = { onSelectedIndexChange(index) },
                )
            }
        }
    }

    @Composable
    private fun HistoryEntryCard(
        entryWithDiff: ArtistHistoryEntryWithDiff,
        selected: Boolean,
        onClick: () -> Unit,
    ) {
        val cardContent = movableContentOf {
            Column(modifier = Modifier.padding(16.dp)) {
                val entry = entryWithDiff.entry
                CardHeader(editor = entry.lastEditor, timestamp = entry.timestamp)

                Spacer(modifier = Modifier.height(12.dp))

                SingleChangeRow(
                    label = Res.string.alley_edit_artist_history_label_status,
                    entry.status?.title?.let { stringResource(it) })
                SingleChangeRow(
                    label = Res.string.alley_edit_artist_history_label_booth,
                    entry.booth
                )
                SingleChangeRow(label = Res.string.alley_edit_artist_history_label_name, entry.name)
                SingleChangeRow(
                    label = Res.string.alley_edit_artist_history_label_summary,
                    entry.summary
                )
                SingleChangeRow(
                    label = Res.string.alley_edit_artist_history_label_notes,
                    entry.notes
                )
                SingleChangeRow(
                    label = Res.string.alley_edit_artist_history_label_editor_notes,
                    entry.editorNotes
                )

                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_links,
                    entryWithDiff.linksDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_store_links,
                    entryWithDiff.storeLinksDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_catalog_links,
                    entryWithDiff.catalogLinksDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_commissions,
                    entryWithDiff.commissionsDiff,
                )

                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_series_inferred,
                    entryWithDiff.seriesInferredDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_series_confirmed,
                    entryWithDiff.seriesConfirmedDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_merch_inferred,
                    entryWithDiff.merchInferredDiff,
                )
                ListChangeRow(
                    label = Res.string.alley_edit_artist_history_label_merch_confirmed,
                    entryWithDiff.merchConfirmedDiff,
                )
            }
        }
        val modifier = Modifier.fillMaxWidth()
        if (selected) {
            OutlinedCard(onClick = onClick, modifier = modifier) {
                cardContent()
            }
        } else {
            ElevatedCard(onClick = onClick, modifier = modifier) {
                cardContent()
            }
        }
    }

    @Composable
    private fun CardHeader(editor: String?, timestamp: Instant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val (color, textColor) = remember(editor, primaryColor) {
                val color = Random(editor.hashCode()).let {
                    Color(it.nextFloat(), it.nextFloat(), it.nextFloat())
                }.harmonize(primaryColor, true)
                color to Palette.Swatch(color.toArgb(), 1).bodyTextColor()
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, primaryColor, CircleShape)
                    .padding(4.dp)
            ) {
                Text(
                    text = editor?.take(2)?.uppercase().orEmpty(),
                    color = textColor,
                    autoSize = TextAutoSize.StepBased(minFontSize = 12.sp),
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmallEmphasized,
                )
            }

            Column {
                if (editor != null) {
                    Text(
                        text = editor,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                    )
                }
                Text(
                    text = LocalDateTimeFormatter.current.formatDateTime(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }

    @Composable
    private fun SingleChangeRow(label: StringResource, value: String?) {
        if (value != null) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    ChangeLabel(label)
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                HorizontalDivider()
            }
        }
    }

    @Composable
    private fun ListChangeRow(label: StringResource, diff: ArtistHistoryEntryWithDiff.Diff?) {
        if (diff != null) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    ChangeLabel(label)
                    Column(modifier = Modifier.weight(1f)) {
                        diff.added.forEach {
                            Text(
                                text = stringResource(
                                    Res.string.alley_edit_artist_history_label_list_added,
                                    it
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        diff.deleted.forEach {
                            Text(
                                text = stringResource(
                                    Res.string.alley_edit_artist_history_label_list_deleted,
                                    it
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red,
                            )
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }

    @Composable
    private fun ChangeLabel(label: StringResource) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
            maxLines = 1,
            modifier = Modifier.width(120.dp)
                .padding(start = 4.dp, end = 16.dp)
        )
    }
}
