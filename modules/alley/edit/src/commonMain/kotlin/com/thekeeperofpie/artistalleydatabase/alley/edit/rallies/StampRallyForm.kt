package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.allCaps
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_host_table
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_prize
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_prize_limit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_table_min
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_tables
import com.thekeeperofpie.artistalleydatabase.alley.edit.MetadataSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.BasicMultiTextSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormEditActions
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormHeaderIconAndTitle
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.LinksSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.NotesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.RevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.SeriesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowListRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.rememberListRevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.UnrecognizedTagIcon
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomIcons
import com.thekeeperofpie.artistalleydatabase.utils_compose.digits
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal object StampRallyForm {

    @Composable
    operator fun invoke(
        state: StampRallyFormState,
        errorState: StampRallyErrorState,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        val focusState = rememberFocusState(
            listOf(
                state.editorState.id,
                state.fandom,
                state.hostTable,
                state.stateTables,
                state.stateLinks,
                state.tableMin,
                state.prize,
                state.prizeLimit,
                state.stateSeries,
                state.stateMerch,
                state.editorState.editorNotes,
            )
        )

        EntryForm2(focusState = focusState, modifier = modifier) {
            val scope = remember(this, initialStampRally) {
                object : StampRallyFormScope(this) {
                    override val initialStampRally: StampRallyDatabaseEntry?
                        get() = initialStampRally()
                }
            }

            with(scope) {
                MetadataSection(state.metadata)
                IdSection(state.editorState.id, errorState.idErrorMessage)
                FandomSection(state.fandom)
                HostTableSection(state.hostTable)
                TablesSection(state.stateTables, state.tables)
                LinksSection(state.stateLinks, state.links, errorState.linksErrorMessage)
                TableMinSection(state.tableMin)
                PrizeSection(state.prize)
                PrizeLimitSection(state.prizeLimit)
                SeriesSection(
                    state = state.stateSeries,
                    series = state.series,
                    seriesById = seriesById,
                    seriesPredictions = seriesPredictions,
                    seriesImage = seriesImage,
                )
                MerchSection(state.stateMerch, state.merch, merchById, merchPredictions)
                NotesSection(
                    state.notes,
                    scope.initialStampRally?.notes,
                    Res.string.alley_edit_stamp_rally_edit_notes
                )
                NotesSection(
                    state = state.editorState.editorNotes,
                    initialValue = scope.initialStampRally?.editorNotes,
                    header = Res.string.alley_edit_stamp_rally_edit_editor_notes,
                )
            }
        }
    }
}

@LayoutScopeMarker
@Stable
private abstract class StampRallyFormScope(
    entryFormScope: EntryFormScope,
) : EntryFormScope by entryFormScope {
    abstract val initialStampRally: StampRallyDatabaseEntry?

    @Composable
    fun IdSection(
        state: EntryForm2.SingleTextState,
        errorText: (() -> String?)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialStampRally?.id.orEmpty())
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_stamp_rally_edit_id)) },
            outputTransformation = revertDialogState.outputTransformation,
            errorText = errorText,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    if (initialStampRally != null) {
                        ShowRevertIconButton(revertDialogState, state)
                    }
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_stamp_rally_edit_id)
    }

    @Composable
    fun FandomSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        val revertDialogState = rememberRevertDialogState(initialStampRally?.fandom)
        SingleTextSection(
            state = state,
            headerText = {
                FormHeaderIconAndTitle(
                    CustomIcons.TableSign,
                    Res.string.alley_edit_stamp_rally_edit_fandom
                )
            },
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_stamp_rally_edit_fandom)
    }

    @Composable
    fun HostTableSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        BasicTextSection(
            state = state,
            initialValue = initialStampRally?.hostTable,
            icon = CustomIcons.ServerPerson,
            title = Res.string.alley_edit_stamp_rally_edit_host_table,
            inputTransformation = InputTransformation.maxLength(3).allCaps(Locale.current),
            label = label,
        )
    }

    @Composable
    fun TablesSection(
        state: EntryForm2.SingleTextState,
        tables: SnapshotStateList<String>,
    ) {
        val initialTables = initialStampRally?.tables
        val listRevertDialogState = rememberListRevertDialogState(initialTables)
        MultiTextSection(
            state = state,
            header = {
                FormHeaderIconAndTitle(
                    Icons.Default.TableRestaurant,
                    Res.string.alley_edit_stamp_rally_edit_tables
                )
            },
            items = tables,
            itemToCommitted = { it },
            removeLastItem = { tables.removeLastOrNull() },
            item = { _, value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val existed = initialTables?.any { it == value }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium
                            .copy(
                                fontFamily = FontFamily.Monospace,
                                color = if (initialTables == null || existed != false) {
                                    MaterialTheme.typography.bodyMedium.color
                                } else {
                                    AlleyTheme.colorScheme.positive
                                }
                            ),
                        modifier = Modifier.weight(1f)
                    )

                    FormEditActions(
                        state = state,
                        forceLocked = this@StampRallyFormScope.forceLocked,
                        items = tables,
                        item = value,
                        itemToText = { it },
                    )
                }
            },
            inputTransformation = InputTransformation {
                if (asCharSequence().any { it.isWhitespace() || it == ',' }) {
                    revertAllChanges()
                }
            },
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowListRevertIconButton(listRevertDialogState, tables)
                }
            },
        )
    }

    @Composable
    fun LinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
    ) {
        LinksSection(
            state = state,
            title = Res.string.alley_edit_stamp_rally_edit_links,
            header = {
                FormHeaderIconAndTitle(
                    Icons.Default.Link,
                    Res.string.alley_edit_stamp_rally_edit_links
                )
            },
            listRevertDialogState =
                rememberListRevertDialogState(initialStampRally?.links?.map(LinkModel::parse)),
            items = links,
            label = null,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    fun TableMinSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        BasicTextSection(
            state = state,
            initialValue = initialStampRally?.tableMin?.serializedValue?.toString(),
            icon = Icons.Default.AttachMoney,
            title = Res.string.alley_edit_stamp_rally_edit_table_min,
            label = label,
        )
    }

    @Composable
    fun PrizeSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        BasicTextSection(
            state = state,
            initialValue = initialStampRally?.prize,
            icon = Icons.Default.ShoppingBag,
            title = Res.string.alley_edit_stamp_rally_edit_prize,
            label = label,
        )
    }

    @Composable
    fun PrizeLimitSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    ) {
        BasicTextSection(
            state = state,
            initialValue = initialStampRally?.prizeLimit?.toString(),
            icon = Icons.Default.ProductionQuantityLimits,
            title = Res.string.alley_edit_stamp_rally_edit_prize_limit,
            label = label,
            inputTransformation = InputTransformation.digits(),
        )
    }

    @Composable
    fun SeriesSection(
        state: EntryForm2.SingleTextState,
        series: SnapshotStateList<SeriesInfo>,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
    ) {
        val seriesById = seriesById()
        val initialSeries = remember(seriesById, initialStampRally?.series) {
            initialStampRally?.series?.map { seriesById[it] ?: SeriesInfo.fake(it) }.orEmpty()
        }

        val listRevertDialogState = rememberListRevertDialogState(initialSeries)

        SeriesSection(
            state = state,
            title = Res.string.alley_edit_stamp_rally_edit_series,
            header = {
                FormHeaderIconAndTitle(
                    CustomIcons.TvGen,
                    Res.string.alley_edit_stamp_rally_edit_series
                )
            },
            listRevertDialogState = listRevertDialogState,
            items = series,
            predictions = seriesPredictions,
            image = seriesImage,
        )
    }

    @Composable
    fun MerchSection(
        state: EntryForm2.SingleTextState,
        merch: SnapshotStateList<MerchInfo>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    ) {
        val merchById = merchById()
        val initialMerch = remember(merchById, initialStampRally?.merch) {
            initialStampRally?.merch?.map { merchById[it] ?: MerchInfo.fake(it) }.orEmpty()
        }
        val listRevertDialogState = rememberListRevertDialogState(initialMerch)
        BasicMultiTextSection(
            state = state,
            header = {
                FormHeaderIconAndTitle(
                    CustomIcons.Package2,
                    Res.string.alley_edit_stamp_rally_edit_merch
                )
            },
            initialItems = initialMerch,
            equalsComparison = { it.name },
            items = merch,
            predictions = merchPredictions,
            itemToCommitted = MerchInfo::fake,
            itemToText = { it.name },
            itemToSubText = { it.notes },
            itemToSerializedValue = { it.name },
            leadingIcon = {
                if (it.faked) {
                    UnrecognizedTagIcon()
                }
            },
            predictionToText = {
                if (it.faked) {
                    "\"${it.name}\""
                } else {
                    it.name
                }
            },
            listRevertDialogState = listRevertDialogState,
        )
    }

    @Composable
    fun NotesSection(
        state: EntryForm2.SingleTextState,
        initialValue: String?,
        header: StringResource,
        label: @Composable (() -> Unit)? = null,
    ) {
        val revertDialogState = rememberRevertDialogState(initialValue)
        NotesSection(state, revertDialogState, header, label)
    }

    @Composable
    private fun BasicTextSection(
        state: EntryForm2.SingleTextState,
        initialValue: String?,
        icon: ImageVector,
        title: StringResource,
        inputTransformation: InputTransformation? = null,
        label: @Composable (() -> Unit)? = null,
    ) {
        val revertDialogState = rememberRevertDialogState(initialValue)
        SingleTextSection(
            state = state,
            headerText = { FormHeaderIconAndTitle(icon, title) },
            inputTransformation = inputTransformation,
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, title)
    }

    @Composable
    private fun rememberRevertDialogState(initialValue: String?): RevertDialogState {
        val positiveColor = AlleyTheme.colorScheme.positive
        return remember(initialStampRally, initialValue) {
            RevertDialogState(positiveColor, initialStampRally, initialValue.orEmpty())
        }
    }
}
