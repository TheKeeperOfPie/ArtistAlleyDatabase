@file:OptIn(ExperimentalFlexBoxApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_end_tables
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_fandom
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_images_subtitle_megabytes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_merch
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_prize
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_prize_limit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_series
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_start_tables
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_table_min
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_tables
import artistalleydatabase.modules.alley.generated.resources.alley_artist_booth_and_table_name
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistTable
import com.thekeeperofpie.artistalleydatabase.alley.edit.MetadataSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImagesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.BasicMultiTextSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormEditActions
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormHeaderIconAndTitle
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.LinksSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ListFieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.NotesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.RevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.SeriesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowListRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.rememberListRevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ImageUploadUtils
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.ui.UnrecognizedTagIcon
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.SectionHeader
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.filled.AttachMoney
import com.thekeeperofpie.artistalleydatabase.icons.filled.HandPackage
import com.thekeeperofpie.artistalleydatabase.icons.filled.Link
import com.thekeeperofpie.artistalleydatabase.icons.filled.ProductionQuantityLimits
import com.thekeeperofpie.artistalleydatabase.icons.filled.ShoppingBag
import com.thekeeperofpie.artistalleydatabase.icons.filled.Start
import com.thekeeperofpie.artistalleydatabase.icons.filled.TableRestaurant
import com.thekeeperofpie.artistalleydatabase.utils.asBytes
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.toggle
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomIcons
import com.thekeeperofpie.artistalleydatabase.utils_compose.digits
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRequestKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationRequestKey
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.alley.edit.generated.resources.Res as AlleyEditRes
import artistalleydatabase.modules.alley.generated.resources.Res as AlleyRes

object StampRallyForm {

    @Composable
    operator fun invoke(
        state: StampRallyFormState,
        errorState: StampRallyErrorState,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        tablePredictions: suspend (String) -> Flow<List<ArtistTable>>,
        seriesImage: (SeriesInfo) -> String?,
        showImages: Boolean = false,
        forceLocked: Boolean = false,
        onClickEditImages: ((NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit)? = null,
        modifier: Modifier = Modifier,
    ) {
        val focusState = rememberFocusState(
            listOf(
                state.editorState.id,
                state.fandom,
                state.stateTables,
                state.stateLinks,
                state.tableMin,
                state.prize,
                state.prizeLimit,
                state.stateSeries,
                state.stateMerch,
                state.notes,
                state.editorState.editorNotes,
            )
        )
        StampRallyForm(
            state = state,
            errorState = errorState,
            focusState = focusState,
            initialStampRally = initialStampRally,
            seriesById = seriesById,
            seriesPredictions = seriesPredictions,
            merchById = merchById,
            merchPredictions = merchPredictions,
            tablePredictions = tablePredictions,
            seriesImage = seriesImage,
            forceLocked = forceLocked,
            modifier = modifier,
        ) {
            MetadataSection(state.metadata)

            if (showImages) {
                val requestKey = rememberNavigationRequestKey(ImagesEditScreen.REQUEST_KEY)
                ImagesSection(
                    images = state.images,
                    requestKey = requestKey,
                    onClickEditImages = if (onClickEditImages == null) null else {
                        {
                            onClickEditImages(requestKey, state.images.toList())
                        }
                    },
                )
            }

            IdSection(state.editorState.id, errorState.idErrorMessage)
            FandomSection(state.fandom)
            TablesSection(state.stateTables, state.tables, tablePredictions)
            TableCheckboxesSection(
                state = state.stateStartTables,
                tables = state.tables,
                selectedTables = state.startTables,
                icon = Icons.Filled.Start,
                title = Res.string.alley_edit_stamp_rally_edit_start_tables,
            )
            TableCheckboxesSection(
                state = state.stateEndTables,
                tables = state.tables,
                selectedTables = state.endTables,
                icon = Icons.Filled.HandPackage,
                title = Res.string.alley_edit_stamp_rally_edit_end_tables,
            )
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
                this.initialStampRally?.notes,
                Res.string.alley_edit_stamp_rally_edit_notes
            )
            NotesSection(
                state = state.editorState.editorNotes,
                initialValue = this.initialStampRally?.editorNotes,
                header = Res.string.alley_edit_stamp_rally_edit_editor_notes,
            )
        }
    }

    @Composable
    operator fun invoke(
        state: StampRallyFormState,
        errorState: StampRallyErrorState,
        focusState: EntryForm2.FocusState,
        initialStampRally: () -> StampRallyDatabaseEntry?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        tablePredictions: suspend (String) -> Flow<List<ArtistTable>>,
        seriesImage: (SeriesInfo) -> String?,
        forceLocked: Boolean = false,
        modifier: Modifier = Modifier,
        content: @Composable StampRallyFormScope.() -> Unit,
    ) {
        EntryForm2(forceLocked = forceLocked, focusState = focusState, modifier = modifier) {
            val scope = remember(this, initialStampRally) {
                object : StampRallyFormScope(this) {
                    override val initialStampRally: StampRallyDatabaseEntry?
                        get() = initialStampRally()
                }
            }

            with(scope) {
                content()
            }
        }
    }
}

@LayoutScopeMarker
@Stable
abstract class StampRallyFormScope(
    entryFormScope: EntryFormScope,
) : EntryFormScope by entryFormScope {
    abstract val initialStampRally: StampRallyDatabaseEntry?

    @Composable
    fun ImagesSection(
        images: SnapshotStateList<EditImage>,
        requestKey: NavigationRequestKey<List<EditImage>>,
        onClickEditImages: (() -> Unit)?,
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                    Text(
                        text = stringResource(Res.string.alley_edit_stamp_rally_edit_images),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    val showError by remember {
                        derivedStateOf {
                            val images = images.toList()
                            images.size > ImageUploadUtils.MAX_STAMP_RALLY_UPLOAD_COUNT || images.any {
                                it is EditImage.LocalImage && PlatformImageCache[it.key]?.size()
                                    ?.asBytes()?.let { it > ImageUtils.MAX_UPLOAD_SIZE } == true
                            }
                        }
                    }
                    if (showError) {
                        Text(
                            text = stringResource(
                                Res.string.alley_edit_stamp_rally_edit_images_subtitle_megabytes,
                                ImageUploadUtils.MAX_STAMP_RALLY_UPLOAD_COUNT,
                                ImageUtils.MAX_UPLOAD_SIZE.inWholeMegabytes
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlleyTheme.colorScheme.negative,
                        )
                    }
                }

                if (images.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { onClickEditImages?.invoke() },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            stringResource(AlleyEditRes.string.alley_edit_artist_action_edit_images)
                        )
                    }
                }
            }
            EditImagesSection(
                images = images,
                requestKey = requestKey,
                onClickEditImages = onClickEditImages.takeIf { images.isEmpty() },
            )
        }
    }

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
    fun TablesSection(
        state: EntryForm2.SingleTextState,
        tables: SnapshotStateList<ArtistTable>,
        predictions: suspend (String) -> Flow<List<ArtistTable>>,
        icon: ImageVector = Icons.Default.TableRestaurant,
        title: StringResource = Res.string.alley_edit_stamp_rally_edit_tables,
        label: @Composable (() -> Unit)? = null,
    ) {
        val initialTables = remember(initialStampRally) {
            initialStampRally?.tables?.map { ArtistTable(booth = it, name = null) }
        }
        val listRevertDialogState = rememberListRevertDialogState(initialTables)
        MultiTextSection(
            state = state,
            header = { FormHeaderIconAndTitle(icon, title) },
            items = tables,
            itemToCommitted = {
                StampRallyUtils.toValidBooth(it)?.let {
                    ArtistTable(booth = it, name = null)
                }
            },
            removeLastItem = { tables.removeLastOrNull()?.booth },
            label = label,
            item = { _, value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    val existed = initialTables?.any { it.booth == value.booth }
                    val tableName = value.name
                    Text(
                        text = if (tableName == null) {
                            value.booth
                        } else {
                            stringResource(
                                AlleyRes.string.alley_artist_booth_and_table_name,
                                value.booth,
                                tableName,
                            )
                        },
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
                        itemToText = { it.booth },
                    )
                }
            },
            prediction = { _, value ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    val tableName = value.name
                    Text(
                        text = if (tableName == null) {
                            value.booth
                        } else {
                            stringResource(
                                AlleyRes.string.alley_artist_booth_and_table_name,
                                value.booth,
                                tableName,
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            inputTransformation = InputTransformation {
                if (!asCharSequence().all { it.isLetterOrDigit() || it == '-' }) {
                    revertAllChanges()
                }
            },
            additionalHeaderActions = {
                with(this@StampRallyFormScope) {
                    ShowListRevertIconButton(listRevertDialogState, tables)
                }
            },
            entryPredictions = predictions,
        )

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
            label = Res.string.alley_edit_stamp_rally_edit_tables,
            items = tables,
            itemsToText = { it.joinToString() },
        )
    }

    context(scope: EntryFormScope)
    @Composable
    fun TableCheckboxesSection(
        state: EntryForm2.EmptyState,
        tables: SnapshotStateList<ArtistTable>,
        selectedTables: SnapshotStateSet<String>,
        icon: ImageVector,
        title: StringResource,
        label: @Composable (() -> Unit)? = null,
    ) {
        if (tables.isEmpty()) return
        Column {
            scope.SectionHeader(
                state = state,
                text = { FormHeaderIconAndTitle(icon, title) },
            )
            if (label != null) {
                Box(
                    modifier = Modifier.padding(
                        start = 32.dp,
                        end = 16.dp,
                    )
                ) {
                    label()
                }
            }
            FlexBox(Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)) {
                tables.forEach {
                    val booth = it.booth
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { selectedTables.toggle(booth) }
                            .padding(end = 16.dp)
                    ) {
                        Checkbox(
                            enabled = !scope.forceLocked && state.lockState.editable,
                            checked = booth in selectedTables,
                            onCheckedChange = {
                                if (it) {
                                    selectedTables += booth
                                } else {
                                    selectedTables -= booth
                                }
                            },
                        )

                        Text(text = it.booth)
                    }
                }
            }
        }
    }

    @Composable
    fun LinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
        label: @Composable (() -> Unit)? = null,
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
            label = label,
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
        showUnknownIndicator: Boolean = true,
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
            showUnknownIndicator = showUnknownIndicator,
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

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
            label = Res.string.alley_edit_stamp_rally_edit_merch,
            items = merch,
            itemsToText = { it.joinToString() },
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
