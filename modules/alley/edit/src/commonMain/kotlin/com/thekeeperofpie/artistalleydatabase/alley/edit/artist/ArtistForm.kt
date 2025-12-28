package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_hide_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_show_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_author_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_duplicate_entry
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_label
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_placeholder
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_delete_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import artistalleydatabase.modules.alley.generated.resources.Res as AlleyRes
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

@LayoutScopeMarker
@Immutable
interface ArtistFormScope : EntryFormScope {
    @Composable
    fun MetadataSection(metadata: ArtistFormState.Metadata)

    @Composable
    fun PasteLinkSection(state: ArtistFormState.LinksState)

    @Composable
    fun StatusSection(
        state: EntryForm2.DropdownState,
        metadata: ArtistFormState.Metadata,
    )

    @Composable
    fun IdSection(
        state: EntryForm2.SingleTextState,
        forceLock: Boolean = false,
        errorText: (() -> String?)? = null,
    )

    @Composable
    fun InfoSections(state: ArtistFormState.InfoState, boothErrorMessage: (() -> String?)? = null)

    @Composable
    fun BoothSection(state: EntryForm2.SingleTextState, errorText: (() -> String?)? = null)

    @Composable
    fun NameSection(state: EntryForm2.SingleTextState)

    @Composable
    fun SummarySection(state: EntryForm2.SingleTextState)

    @Composable
    fun LinkSections(
        state: ArtistFormState.LinksState,
        linksErrorMessage: () -> String? = { null },
        storeLinksErrorMessage: () -> String? = { null },
        catalogLinksErrorMessage: () -> String? = { null },
    )

    @Composable
    fun LinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun StoreLinksSection(
        state: EntryForm2.SingleTextState,
        storeLinks: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun CatalogLinksSection(
        state: EntryForm2.SingleTextState,
        catalogLinks: SnapshotStateList<String>,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun CommissionsSection(
        state: EntryForm2.SingleTextState,
        commissions: SnapshotStateList<CommissionModel>,
    )

    @Composable
    fun TagSections(
        series: ArtistFormState.SeriesState,
        merch: ArtistFormState.MerchState,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    )

    @Composable
    fun SeriesSection(
        state: ArtistFormState.SeriesState,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        showConfirmed: Boolean = true,
        allowCustomInput: Boolean = false,
    )

    @Composable
    fun MerchSection(
        state: ArtistFormState.MerchState,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        showConfirmed: Boolean = true,
        allowCustomInput: Boolean = false,
    )

    @Composable
    fun NotesSection(
        state: EntryForm2.SingleTextState,
        headerText: @Composable () -> Unit = {
            Text(stringResource(Res.string.alley_edit_artist_edit_notes))
        },
    )
}

@LayoutScopeMarker
@Immutable
private class ArtistFormScopeImpl(
    entryFormScope: EntryFormScope,
    private val initialArtist: ArtistDatabaseEntry.Impl?,
) : ArtistFormScope, EntryFormScope by entryFormScope {

    @Composable
    override fun MetadataSection(metadata: ArtistFormState.Metadata) {
        val lastEditTime = metadata.lastEditTime
        if (lastEditTime != null) {
            ArtistForm.LastEditedText(metadata.lastEditor, lastEditTime)
        }
    }

    @Composable
    override fun PasteLinkSection(state: ArtistFormState.LinksState) {
        if (!forceLocked) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {
                        ArtistForm.processPastedLink(
                            links = state.links,
                            storeLinks = state.storeLinks,
                            commissions = state.commissions,
                            link = it,
                        )
                    },
                    label = {
                        Text(stringResource(Res.string.alley_edit_paste_link_label))
                    },
                    placeholder = {
                        Text(stringResource(Res.string.alley_edit_paste_link_placeholder))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    @Composable
    override fun StatusSection(
        state: EntryForm2.DropdownState,
        metadata: ArtistFormState.Metadata,
    ) {
        DropdownSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_status)) },
            options = ArtistStatus.entries,
            optionToText = { stringResource(it.title) },
            leadingIcon = { Icon(imageVector = it.icon, null) },
            expandedItemText = {
                Column {
                    Text(stringResource(it.title))
                    Text(
                        text = it.description(metadata.lastEditor),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            },
        )
    }

    @Composable
    override fun IdSection(
        state: EntryForm2.SingleTextState,
        forceLock: Boolean,
        errorText: (() -> String?)?,
    ) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_id)) },
            forceLocked = forceLock || forceLocked,
            outputTransformation = rememberOnChangedOutputTransformation(initialArtist?.id),
            errorText = errorText,
        )
    }

    @Composable
    override fun InfoSections(
        state: ArtistFormState.InfoState,
        boothErrorMessage: (() -> String?)?,
    ) {
        BoothSection(state = state.booth, errorText = boothErrorMessage)
        NameSection(state.name)
        SummarySection(state.summary)
    }

    @Composable
    override fun BoothSection(state: EntryForm2.SingleTextState, errorText: (() -> String?)?) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_booth)) },
            inputTransformation = InputTransformation.maxLength(3),
            outputTransformation = rememberOnChangedOutputTransformation(initialArtist?.booth),
            errorText = errorText,
        )
    }

    @Composable
    override fun NameSection(state: EntryForm2.SingleTextState) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_name)) },
            outputTransformation = rememberOnChangedOutputTransformation(initialArtist?.name),
        )
    }

    @Composable
    override fun SummarySection(state: EntryForm2.SingleTextState) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_summary)) },
            outputTransformation = rememberOnChangedOutputTransformation(initialArtist?.summary),
        )
    }

    @Composable
    override fun LinkSections(
        state: ArtistFormState.LinksState,
        linksErrorMessage: () -> String?,
        storeLinksErrorMessage: () -> String?,
        catalogLinksErrorMessage: () -> String?,
    ) {
        LinksSection(
            state = state.stateLinks,
            links = state.links,
            pendingErrorMessage = linksErrorMessage,
        )
        StoreLinksSection(
            state = state.stateStoreLinks,
            storeLinks = state.storeLinks,
            pendingErrorMessage = storeLinksErrorMessage,
        )
        CatalogLinksSection(
            state = state.stateCatalogLinks,
            catalogLinks = state.catalogLinks,
            pendingErrorMessage = catalogLinksErrorMessage,
        )
        CommissionsSection(state.stateCommissions, state.commissions)
    }

    @Composable
    override fun LinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_links,
            initialItems = initialArtist?.links,
            items = links,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun StoreLinksSection(
        state: EntryForm2.SingleTextState,
        storeLinks: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_store_links,
            initialItems = initialArtist?.storeLinks,
            items = storeLinks,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun CatalogLinksSection(
        state: EntryForm2.SingleTextState,
        catalogLinks: SnapshotStateList<String>,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.MultiTextSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_catalog_links,
            initialItems = initialArtist?.catalogLinks,
            items = catalogLinks,
            itemToText = { it },
            itemToSerializedValue = { it },
            itemToCommitted = { it },
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun CommissionsSection(
        state: EntryForm2.SingleTextState,
        commissions: SnapshotStateList<CommissionModel>,
    ) {
        val onSiteText = stringResource(AlleyRes.string.alley_artist_commission_on_site)
        val onlineText = stringResource(AlleyRes.string.alley_artist_commission_online)
        val initialCommissions = remember(initialArtist?.commissions) {
            initialArtist?.commissions?.map(CommissionModel::parse).orEmpty()
        }
        ArtistForm.MultiTextSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_commissions,
            items = commissions,
            initialItems = initialCommissions,
            leadingIcon = {
                when (it) {
                    is CommissionModel.Link -> Icons.Default.Link
                    CommissionModel.OnSite -> Icons.Default.TableRestaurant
                    CommissionModel.Online -> Icons.AutoMirrored.Default.Assignment
                    is CommissionModel.Unknown -> null
                }
            },
            itemToText = {
                when (it) {
                    is CommissionModel.Link -> it.host ?: it.link
                    CommissionModel.OnSite -> onSiteText
                    CommissionModel.Online -> onlineText
                    is CommissionModel.Unknown -> it.value
                }
            },
            itemToSerializedValue = { it.serializedValue },
            itemToCommitted = CommissionModel::parse,
            predictions = { flowOf(listOf(CommissionModel.Online, CommissionModel.OnSite)) },
            preferPrediction = false,
        )
    }

    @Composable
    override fun TagSections(
        series: ArtistFormState.SeriesState,
        merch: ArtistFormState.MerchState,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    ) {
        SeriesSection(
            state = series,
            seriesPredictions = seriesPredictions,
            seriesImage = seriesImage,
        )
        MerchSection(state = merch, merchPredictions = merchPredictions)
    }

    @Composable
    override fun SeriesSection(
        state: ArtistFormState.SeriesState,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        showConfirmed: Boolean,
        allowCustomInput: Boolean,
    ) {
        val hasConfirmedSeries by derivedStateOf { state.confirmed.isNotEmpty() }
        var requestedShowSeriesInferred by rememberSaveable { mutableStateOf(false) }
        val showSeriesInferred =
            forceLocked || !hasConfirmedSeries || requestedShowSeriesInferred || !showConfirmed

        val initialInferred = remember(initialArtist?.seriesInferred) {
            initialArtist?.seriesInferred?.map(SeriesInfo::fake).orEmpty()
        }
        val initialConfirmed = remember(initialArtist?.seriesConfirmed) {
            initialArtist?.seriesConfirmed?.map(SeriesInfo::fake).orEmpty()
        }

        ArtistForm.SeriesSection(
            state = state.stateInferred,
            title = Res.string.alley_edit_artist_edit_series_inferred,
            initialItems = initialInferred,
            items = state.inferred,
            showItems = { showSeriesInferred },
            predictions = seriesPredictions,
            image = seriesImage,
            allowCustomInput = allowCustomInput,
        )

        if (!forceLocked && showConfirmed) {
            ArtistForm.ShowInferredButton(
                hasConfirmed = hasConfirmedSeries,
                showingInferred = showSeriesInferred,
                onClick = { requestedShowSeriesInferred = it },
            )
        }

        if (showConfirmed) {
            ArtistForm.SeriesSection(
                state = state.stateConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                initialItems = initialConfirmed,
                items = state.confirmed,
                predictions = seriesPredictions,
                image = seriesImage,
                allowCustomInput = allowCustomInput,
            )
        }
    }

    @Composable
    override fun MerchSection(
        state: ArtistFormState.MerchState,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        showConfirmed: Boolean,
        allowCustomInput: Boolean,
    ) {
        val hasConfirmedMerch by derivedStateOf { state.confirmed.isNotEmpty() }
        var requestedShowMerchInferred by rememberSaveable { mutableStateOf(false) }
        val showMerchInferred =
            forceLocked || !hasConfirmedMerch || requestedShowMerchInferred || !showConfirmed

        val initialInferred = remember(initialArtist?.merchInferred) {
            initialArtist?.merchInferred?.map(MerchInfo::fake).orEmpty()
        }
        val initialConfirmed = remember(initialArtist?.merchConfirmed) {
            initialArtist?.merchConfirmed?.map(MerchInfo::fake).orEmpty()
        }

        ArtistForm.MultiTextSection(
            state = state.stateInferred,
            title = Res.string.alley_edit_artist_edit_merch_inferred,
            initialItems = initialInferred,
            equalsComparison = { it.name },
            items = state.inferred,
            showItems = { showMerchInferred },
            predictions = merchPredictions,
            itemToCommitted = { MerchInfo.fake(it) },
            itemToText = { it.name },
            itemToSerializedValue = { it.name },
        )

        if (!forceLocked && showConfirmed) {
            ArtistForm.ShowInferredButton(
                hasConfirmed = hasConfirmedMerch,
                showingInferred = showMerchInferred,
                onClick = { requestedShowMerchInferred = it },
            )
        }

        if (showConfirmed) {
            ArtistForm.MultiTextSection(
                state = state.stateConfirmed,
                title = Res.string.alley_edit_artist_edit_merch_confirmed,
                initialItems = initialConfirmed,
                equalsComparison = { it.name },
                items = state.confirmed,
                predictions = merchPredictions,
                itemToCommitted = { MerchInfo.fake(it) },
                itemToText = { it.name },
                itemToSerializedValue = { it.name },
            )
        }
    }

    @Composable
    override fun NotesSection(
        state: EntryForm2.SingleTextState,
        headerText: @Composable () -> Unit,
    ) {
        LongTextSection(
            state = state,
            headerText = headerText,
            outputTransformation = rememberOnChangedOutputTransformation(initialArtist?.notes),
        )
    }

    @Stable
    @Composable
    private fun rememberOnChangedOutputTransformation(initialValue: String?): OutputTransformation? {
        return if (initialArtist != null) {
            remember(initialValue) { GreenOnChangedOutputTransformation(initialValue.orEmpty()) }
        } else {
            null
        }
    }

    @Immutable
    private class GreenOnChangedOutputTransformation(
        private val initialValue: String,
    ) : OutputTransformation {
        override fun TextFieldBuffer.transformOutput() {
            if (originalText.toString() != initialValue) {
                addStyle(SpanStyle(color = Color.Green), 0, length)
            }
        }
    }
}

object ArtistForm {

    @Composable
    operator fun invoke(
        state: ArtistFormState,
        errorState: ArtistErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
        forceLockId: Boolean = false,
        showStatus: Boolean = true,
        showEditorNotes: Boolean = true,
        forceLocked: Boolean = false,
    ) {
        val focusState = rememberFocusState(
            listOfNotNull(
                state.editorState.status.takeIf { showStatus },
                state.editorState.id,
                state.info.booth,
                state.info.name,
                state.info.summary,
                state.links.stateLinks,
                state.links.stateStoreLinks,
                state.links.stateCatalogLinks,
                state.links.stateCommissions,
                state.series.stateInferred,
                state.series.stateConfirmed,
                state.merch.stateInferred,
                state.merch.stateConfirmed,
                state.info.notes,
                state.editorState.editorNotes.takeIf { showEditorNotes },
            )
        )
        ArtistForm(
            focusState = focusState,
            initialArtist = initialArtist,
            forceLocked = forceLocked,
            modifier = modifier
        ) {
            MetadataSection(state.metadata)
            PasteLinkSection(state = state.links)
            if (showStatus) {
                StatusSection(state = state.editorState.status, metadata = state.metadata)
            }
            IdSection(
                state = state.editorState.id,
                forceLock = forceLockId,
                errorText = errorState.idErrorMessage,
            )
            InfoSections(state.info)

            LinkSections(
                state.links,
                linksErrorMessage = errorState.linksErrorMessage,
                storeLinksErrorMessage = errorState.storeLinksErrorMessage,
                catalogLinksErrorMessage = errorState.catalogLinksErrorMessage,
            )

            TagSections(
                series = state.series,
                merch = state.merch,
                seriesPredictions = seriesPredictions,
                seriesImage = seriesImage,
                merchPredictions = merchPredictions
            )

            NotesSection(state.info.notes)
            if (showEditorNotes) {
                NotesSection(
                    state = state.editorState.editorNotes,
                    headerText = {
                        Text(stringResource(Res.string.alley_edit_artist_edit_editor_notes))
                    },
                )
            }
        }
    }

    @Composable
    operator fun invoke(
        focusState: EntryForm2.FocusState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        forceLocked: Boolean = false,
        modifier: Modifier = Modifier,
        content: @Composable ArtistFormScope.() -> Unit,
    ) {
        EntryForm2(forceLocked = forceLocked, focusState = focusState, modifier = modifier) {
            ArtistFormScopeImpl(this, initialArtist()).content()
        }
    }

    @Composable
    internal fun LastEditedText(lastEditor: String?, lastEditTime: Instant) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val textColorDim = LocalContentColor.current.copy(alpha = 0.6f)
            val colorPrimary = MaterialTheme.colorScheme.primary
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = textColorDim)) {
                        append(stringResource(Res.string.alley_edit_artist_edit_last_modified_prefix))
                    }
                    append(' ')
                    withStyle(SpanStyle(color = colorPrimary)) {
                        append(LocalDateTimeFormatter.current.formatDateTime(lastEditTime))
                    }
                    if (lastEditor != null) {
                        append(' ')
                        withStyle(SpanStyle(color = textColorDim)) {
                            append(stringResource(Res.string.alley_edit_artist_edit_last_modified_author_prefix))
                        }
                        append(' ')
                        append(lastEditor)
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }

    internal fun processPastedLink(
        links: SnapshotStateList<LinkModel>,
        storeLinks: SnapshotStateList<LinkModel>,
        commissions: SnapshotStateList<CommissionModel>,
        link: String,
    ) {
        if (link.length < 6) return
        val fixedLink = Uri.parseOrNull(link)
            ?.buildUpon()
            ?.clearQuery()
            ?.scheme("https")
            ?.toString()
            ?: link
        val linkModel = LinkModel.parse(fixedLink)
        when (linkModel.logo) {
            Logo.BIG_CARTEL,
            Logo.ETSY,
            Logo.FAIRE,
            Logo.GALLERY_NUCLEUS,
            Logo.GUMROAD,
            Logo.INPRNT,
            Logo.ITCH_IO,
            Logo.REDBUBBLE,
            Logo.SHOPIFY,
            Logo.STORENVY,
            Logo.THREADLESS,
                -> storeLinks += linkModel

            Logo.VGEN -> commissions += CommissionModel.parse(fixedLink)

            Logo.ART_STATION,
            Logo.BLUESKY,
            Logo.CARRD,
            Logo.DEVIANT_ART,
            Logo.DISCORD,
            Logo.FACEBOOK,
            Logo.GAME_JOLT,
            Logo.GITHUB,
            Logo.INSTAGRAM,
            Logo.KICKSTARTER,
            Logo.KO_FI,
            Logo.LINKTREE,
            Logo.PATREON,
            Logo.PIXIV,
            Logo.SUBSTACK,
            Logo.THREADS,
            Logo.TIK_TOK,
            Logo.TUMBLR,
            Logo.TWITCH,
            Logo.WEEBLY,
            Logo.X,
            Logo.YOU_TUBE,
            null,
                -> links += linkModel
        }
    }

    context(formScope: EntryFormScope)
    @Composable
    internal fun <T> MultiTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        initialItems: List<T>?,
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
        itemToSerializedValue: (T) -> String,
        itemToCommitted: ((String) -> T)? = null,
        leadingIcon: (T) -> ImageVector? = { null },
        pendingErrorMessage: () -> String? = { null },
        preferPrediction: Boolean = true,
        equalsComparison: (T) -> Any? = { it },
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            showItems = showItems,
            entryPredictions = predictions,
            itemToCommitted = itemToCommitted,
            removeLastItem = { items.removeLastOrNull()?.let { itemToSerializedValue(it) } },
            sortValue = itemToText,
            item = { _, item ->
                Box {
                    val leadingIcon = leadingIcon(item)
                    val existed = initialItems?.any {
                        equalsComparison(it) == equalsComparison(item)
                    }
                    TextField(
                        value = itemToText(item),
                        onValueChange = {},
                        readOnly = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = if (existed != false) {
                                LocalTextStyle.current.color
                            } else {
                                Color.Green
                            }
                        ),
                        leadingIcon = leadingIcon?.let {
                            {
                                Icon(
                                    imageVector = leadingIcon,
                                    contentDescription = null,
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = state.lockState.editable,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                TooltipIconButton(
                                    icon = Icons.Default.Delete,
                                    tooltipText = stringResource(Res.string.alley_edit_row_delete_tooltip),
                                    onClick = { items.remove(item) },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            prediction = { _, value -> Text(text = itemToText(value)) },
            pendingErrorMessage = pendingErrorMessage,
            preferPrediction = preferPrediction,
        )
    }

    context(formScope: EntryFormScope)
    @Composable
    internal fun SeriesSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        initialItems: List<SeriesInfo>?,
        items: SnapshotStateList<SeriesInfo>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
        allowCustomInput: Boolean,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            itemToCommitted = if (allowCustomInput) {
                { SeriesInfo.fake(it) }
            } else null,
            showItems = showItems,
            entryPredictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.titlePreferred },
            prediction = { _, value -> Text(value.titlePreferred) },
            sortValue = { it.titlePreferred },
            item = { _, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val existed = initialItems?.any { it.id == value.id }
                    val textStyle = if (existed != false) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(color = Color.Green)
                    }
                    SeriesRow(
                        series = value,
                        image = { image(value) },
                        textStyle = textStyle,
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = state.lockState.editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        TooltipIconButton(
                            icon = Icons.Default.Delete,
                            tooltipText = stringResource(Res.string.alley_edit_row_delete_tooltip),
                            onClick = { items.remove(value) },
                        )
                    }
                }
            },
        )
    }

    context(formScope: EntryFormScope)
    @Composable
    internal fun <T> MultiTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        itemToCommitted: ((String) -> T)? = null,
        removeLastItem: () -> String?,
        item: @Composable (index: Int, T) -> Unit,
        entryPredictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        prediction: @Composable (index: Int, T) -> Unit = item,
        sortValue: ((T) -> String)? = null,
        pendingErrorMessage: () -> String? = { null },
        preferPrediction: Boolean = true,
    ) {
        val addUniqueErrorState =
            rememberAddUniqueErrorState(state = state, items = items, sortValue = sortValue)
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            items = items.takeIf { showItems() },
            onItemCommitted = if (itemToCommitted != null) {
                {
                    addUniqueErrorState.addAndEnforceUnique(itemToCommitted(it))
                }
            } else null,
            removeLastItem = removeLastItem,
            item = item,
            entryPredictions = entryPredictions,
            prediction = prediction,
            preferPrediction = preferPrediction,
            onPredictionChosen = addUniqueErrorState::addAndEnforceUnique,
            pendingErrorMessage = { addUniqueErrorState.errorMessage ?: pendingErrorMessage() },
        )
    }

    @Composable
    private fun MenuIcon(visible: Boolean, onClick: () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(
                        UtilsComposeRes.string.more_actions_content_description
                    ),
                )
            }
        }
    }

    @Composable
    internal fun ShowInferredButton(
        hasConfirmed: Boolean,
        showingInferred: Boolean,
        onClick: (requestShowInferred: Boolean) -> Unit,
    ) {
        if (hasConfirmed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
            ) {
                FilledTonalButton(onClick = { onClick(!showingInferred) }) {
                    Text(
                        stringResource(
                            if (showingInferred) {
                                Res.string.alley_edit_artist_edit_action_hide_inferred
                            } else {
                                Res.string.alley_edit_artist_edit_action_show_inferred
                            }
                        )
                    )
                }
            }
        }
    }

    context(scope: EntryFormScope)
    @Composable
    internal fun LinksSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        initialItems: List<String>?,
        items: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
    ) {
        val initialLinks = remember(initialItems) {
            initialItems?.map(LinkModel::parse).orEmpty()
        }
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            itemToCommitted = LinkModel::parse,
            removeLastItem = { items.removeLastOrNull()?.link },
            item = { index, value ->
                LinkRow(
                    link = value,
                    isLast = index == items.lastIndex && !state.lockState.editable,
                    color = if (initialLinks.contains(value)) Color.Unspecified else Color.Green,
                    additionalActions = {
                        AnimatedVisibility(
                            visible = state.lockState.editable,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            TooltipIconButton(
                                icon = Icons.Default.Delete,
                                tooltipText = stringResource(Res.string.alley_edit_row_delete_tooltip),
                                onClick = { items.remove(value) },
                            )
                        }
                    },
                )
            },
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Stable
    class AddUniqueErrorState<T, R : Comparable<R>>(
        private val items: SnapshotStateList<T>,
        private val state: EntryForm2.SingleTextState,
        private val sortValue: ((T) -> R)?,
        private val scope: CoroutineScope,
        private val errorMessageText: String,
    ) {
        var errorMessage by mutableStateOf<String?>(null)
            private set

        fun addAndEnforceUnique(value: T) {
            // TODO: There must be a better way to do this
            val addSuccessful = Snapshot.withMutableSnapshot {
                val addSuccessful = if (sortValue == null) {
                    if (items.contains(value)) {
                        false
                    } else {
                        items.add(value)
                    }
                } else {
                    items.insertSorted(value, sortValue)
                }
                if (!addSuccessful) {
                    errorMessage = errorMessageText
                }
                // Still clear existing input so that if editor enters a duplicate,
                // they don't have to manually clear the text to move on
                state.value.clearText()
                addSuccessful
            }
            if (!addSuccessful) {
                scope.launch {
                    delay(2.seconds)
                    errorMessage = null
                }
            }
        }
    }

    @Composable
    private fun <T, R : Comparable<R>> rememberAddUniqueErrorState(
        items: SnapshotStateList<T>,
        state: EntryForm2.SingleTextState,
        sortValue: ((T) -> R)?,
    ): AddUniqueErrorState<T, R> {
        val scope = rememberCoroutineScope()
        val errorMessageText = stringResource(Res.string.alley_edit_artist_error_duplicate_entry)
        return remember(items, state, sortValue, scope, errorMessageText) {
            AddUniqueErrorState(
                items = items,
                state = state,
                sortValue = sortValue,
                scope = scope,
                errorMessageText = errorMessageText,
            )
        }
    }

    /**
     * Workaround a potential concurrent modification bug:
     * https://issuetracker.google.com/issues/272334463
     *
     * Should be called inside [Snapshot.withMutableSnapshot]
     */
    private fun <T, R : Comparable<R>> SnapshotStateList<T>.insertSorted(
        value: T,
        sortValue: (T) -> R,
    ): Boolean {
        return try {
            val list = this.toList()
            if (list.contains(value)) return false
            val updated = (list + value).sortedBy(sortValue)
            val insertIndex = updated.indexOf(value)
            add(index = insertIndex, element = value)
            if (toList() != updated) {
                clear()
                addAll(updated)
            }
            true
        } catch (t: Throwable) {
            t.printStackTrace()
            true
        }
    }
}
