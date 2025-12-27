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
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_delete
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_paste_link_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_delete_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.links.Logo
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
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
    fun PasteLinkSection(
        links: SnapshotStateList<LinkModel>,
        storeLinks: SnapshotStateList<LinkModel>,
        commissions: SnapshotStateList<CommissionModel>,
    )

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
    fun BoothSection(state: EntryForm2.SingleTextState, errorText: (() -> String?)? = null)

    @Composable
    fun NameSection(state: EntryForm2.SingleTextState)

    @Composable
    fun SummarySection(state: EntryForm2.SingleTextState)

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
    fun SeriesSection(
        stateSeriesInferred: EntryForm2.SingleTextState,
        stateSeriesConfirmed: EntryForm2.SingleTextState,
        seriesInferred: SnapshotStateList<SeriesInfo>,
        seriesConfirmed: SnapshotStateList<SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
    )

    @Composable
    fun MerchSection(
        stateMerchInferred: EntryForm2.SingleTextState,
        stateMerchConfirmed: EntryForm2.SingleTextState,
        merchInferred: SnapshotStateList<MerchInfo>,
        merchConfirmed: SnapshotStateList<MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    )

    @Composable
    fun NotesSection(state: EntryForm2.SingleTextState)

    @Composable
    fun EditorNotesSection(state: EntryForm2.SingleTextState)
}

@LayoutScopeMarker
@Immutable
private class ArtistFormScopeImpl(entryFormScope: EntryFormScope) : ArtistFormScope,
    EntryFormScope by entryFormScope {

    @Composable
    override fun MetadataSection(metadata: ArtistFormState.Metadata) {
        val lastEditTime = metadata.lastEditTime
        if (lastEditTime != null) {
            ArtistForm.LastEditedText(metadata.lastEditor, lastEditTime)
        }
    }

    @Composable
    override fun PasteLinkSection(
        links: SnapshotStateList<LinkModel>,
        storeLinks: SnapshotStateList<LinkModel>,
        commissions: SnapshotStateList<CommissionModel>,
    ) {
        if (!forceLocked) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {
                        ArtistForm.processPastedLink(
                            links = links,
                            storeLinks = storeLinks,
                            commissions = commissions,
                            link = it,
                        )
                    },
                    placeholder = {
                        Text(stringResource(Res.string.alley_edit_artist_form_paste_link_prompt))
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
            errorText = errorText,
        )
    }

    @Composable
    override fun BoothSection(state: EntryForm2.SingleTextState, errorText: (() -> String?)?) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_booth)) },
            inputTransformation = InputTransformation.maxLength(3),
            errorText = errorText,
        )
    }

    @Composable
    override fun NameSection(state: EntryForm2.SingleTextState) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_name)) },
        )
    }

    @Composable
    override fun SummarySection(state: EntryForm2.SingleTextState) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_summary)) },
        )
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
        with(ArtistForm) {
            MultiTextSection(
                state = state,
                title = Res.string.alley_edit_artist_edit_commissions,
                items = commissions,
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
            )
        }
    }

    @Composable
    override fun SeriesSection(
        stateSeriesInferred: EntryForm2.SingleTextState,
        stateSeriesConfirmed: EntryForm2.SingleTextState,
        seriesInferred: SnapshotStateList<SeriesInfo>,
        seriesConfirmed: SnapshotStateList<SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
    ) {
        val hasConfirmedSeries by derivedStateOf { seriesConfirmed.isNotEmpty() }
        var requestedShowSeriesInferred by rememberSaveable { mutableStateOf(false) }
        val showSeriesInferred =
            forceLocked || !hasConfirmedSeries || requestedShowSeriesInferred
        with(ArtistForm) {
            SeriesSection(
                state = stateSeriesInferred,
                title = Res.string.alley_edit_artist_edit_series_inferred,
                items = seriesInferred,
                showItems = { showSeriesInferred },
                predictions = seriesPredictions,
                image = seriesImage,
            )

            if (!forceLocked) {
                ArtistForm.ShowInferredButton(
                    hasConfirmed = hasConfirmedSeries,
                    showingInferred = showSeriesInferred,
                    onClick = { requestedShowSeriesInferred = it },
                )
            }

            SeriesSection(
                state = stateSeriesConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                items = seriesConfirmed,
                predictions = seriesPredictions,
                image = seriesImage,
            )
        }
    }

    @Composable
    override fun MerchSection(
        stateMerchInferred: EntryForm2.SingleTextState,
        stateMerchConfirmed: EntryForm2.SingleTextState,
        merchInferred: SnapshotStateList<MerchInfo>,
        merchConfirmed: SnapshotStateList<MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    ) {
        val hasConfirmedMerch by derivedStateOf { merchConfirmed.isNotEmpty() }
        var requestedShowMerchInferred by rememberSaveable { mutableStateOf(false) }
        val showMerchInferred = forceLocked || !hasConfirmedMerch || requestedShowMerchInferred
        with(ArtistForm) {
            MultiTextSection(
                state = stateMerchInferred,
                title = Res.string.alley_edit_artist_edit_merch_inferred,
                items = merchInferred,
                showItems = { showMerchInferred },
                predictions = merchPredictions,
                itemToText = { it.name },
                itemToSerializedValue = { it.name },
            )

            if (!forceLocked) {
                ArtistForm.ShowInferredButton(
                    hasConfirmed = hasConfirmedMerch,
                    showingInferred = showMerchInferred,
                    onClick = { requestedShowMerchInferred = it },
                )
            }

            MultiTextSection(
                state = stateMerchConfirmed,
                title = Res.string.alley_edit_artist_edit_merch_confirmed,
                items = merchConfirmed,
                predictions = merchPredictions,
                itemToText = { it.name },
                itemToSerializedValue = { it.name },
            )
        }
    }

    @Composable
    override fun NotesSection(state: EntryForm2.SingleTextState) {
        LongTextSection(
            state = state,
            headerText = {
                Text(stringResource(Res.string.alley_edit_artist_edit_notes))
            },
        )
    }

    @Composable
    override fun EditorNotesSection(state: EntryForm2.SingleTextState) {
        LongTextSection(
            state = state,
            headerText = {
                Text(stringResource(Res.string.alley_edit_artist_edit_editor_notes))
            },
        )
    }
}

object ArtistForm {

    @Composable
    operator fun invoke(
        state: ArtistFormState,
        errorState: ArtistErrorState,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
        forceLockId: Boolean = false,
        showStatus: Boolean = true,
        showEditorNotes: Boolean = true,
        forceLocked: Boolean = false,
    ) {
        val textState = state.textState
        val focusState = rememberFocusState(
            listOfNotNull(
                textState.status.takeIf { showStatus },
                textState.id,
                textState.booth,
                textState.name,
                textState.summary,
                textState.links,
                textState.storeLinks,
                textState.catalogLinks,
                textState.commissions,
                textState.seriesInferred,
                textState.seriesConfirmed,
                textState.merchInferred,
                textState.merchConfirmed,
                textState.notes,
                textState.editorNotes.takeIf { showEditorNotes },
            )
        )
        ArtistForm(focusState, forceLocked, modifier) {
            MetadataSection(state.metadata)
            PasteLinkSection(
                links = state.links,
                storeLinks = state.storeLinks,
                commissions = state.commissions,
            )
            if (showStatus) {
                StatusSection(state = textState.status, metadata = state.metadata)
            }
            IdSection(
                state = textState.id,
                forceLock = forceLockId,
                errorText = errorState.idErrorMessage,
            )
            BoothSection(state = textState.booth, errorText = errorState.boothErrorMessage)
            NameSection(textState.name)
            SummarySection(textState.summary)
            LinksSection(
                state = textState.links,
                links = state.links,
                pendingErrorMessage = errorState.linksErrorMessage,
            )
            StoreLinksSection(
                state = textState.storeLinks,
                storeLinks = state.storeLinks,
                pendingErrorMessage = errorState.storeLinksErrorMessage,
            )
            CatalogLinksSection(
                state = textState.catalogLinks,
                catalogLinks = state.catalogLinks,
                pendingErrorMessage = errorState.catalogLinksErrorMessage,
            )
            CommissionsSection(textState.commissions, state.commissions)
            SeriesSection(
                stateSeriesInferred = textState.seriesInferred,
                stateSeriesConfirmed = textState.seriesConfirmed,
                seriesInferred = state.seriesInferred,
                seriesConfirmed = state.seriesConfirmed,
                seriesPredictions = seriesPredictions,
                seriesImage = seriesImage,
            )
            MerchSection(
                stateMerchInferred = textState.merchInferred,
                stateMerchConfirmed = textState.merchConfirmed,
                merchInferred = state.merchInferred,
                merchConfirmed = state.merchConfirmed,
                merchPredictions = merchPredictions,
            )
            NotesSection(textState.notes)
            if (showEditorNotes) {
                EditorNotesSection(textState.editorNotes)
            }
        }
    }

    @Composable
    fun ArtistForm(
        focusState: EntryForm2.FocusState,
        forceLocked: Boolean = false,
        modifier: Modifier = Modifier,
        content: @Composable ArtistFormScope.() -> Unit,
    ) {
        EntryForm2(forceLocked = forceLocked, focusState = focusState, modifier = modifier) {
            ArtistFormScopeImpl(this).content()
        }
    }

    @Composable
    internal fun LastEditedText(lastEditor: String?, lastEditTime: Instant) {
        OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
        itemToSerializedValue: (T) -> String,
        itemToCommitted: ((String) -> T)? = null,
        pendingErrorMessage: () -> String? = { null },
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
                    TextField(
                        value = itemToText(item),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Box {
                                var showMenu by remember { mutableStateOf(false) }
                                MenuIcon(
                                    visible = state.lockState.editable,
                                    onClick = { showMenu = true },
                                )

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.alley_edit_artist_action_delete)) },
                                        onClick = {
                                            items.remove(item)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            prediction = { _, value -> Text(text = itemToText(value)) },
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    internal fun EntryFormScope.SeriesSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        items: SnapshotStateList<SeriesInfo>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            itemToCommitted = null,
            showItems = showItems,
            entryPredictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.titlePreferred },
            prediction = { _, value -> Text(value.titlePreferred) },
            sortValue = { it.titlePreferred },
            item = { _, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeriesRow(
                        series = value,
                        image = { image(value) },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = state.lockState.editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        val contentDescription = stringResource(
                            Res.string.alley_edit_tag_delete_content_description
                        )
                        TooltipIconButton(
                            icon = Icons.Default.Delete,
                            tooltipText = contentDescription,
                            onClick = { items.remove(value) },
                            contentDescription = contentDescription,
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
    ) {
        val addUniqueErrorState =
            rememberAddUniqueErrorState(state = state, items = items, sortValue = sortValue)
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            items = items.takeIf { showItems() },
            onItemCommitted = {
                if (itemToCommitted != null) {
                    addUniqueErrorState.addAndEnforceUnique(itemToCommitted(it))
                }
            },
            removeLastItem = removeLastItem,
            item = item,
            entryPredictions = entryPredictions,
            prediction = prediction,
            preferPrediction = true,
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
        items: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
    ) {
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
                    additionalActions = {
                        Box {
                            var showMenu by remember { mutableStateOf(false) }
                            MenuIcon(
                                visible = state.lockState.editable,
                                onClick = { showMenu = true },
                            )

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.alley_edit_artist_action_delete)) },
                                    onClick = {
                                        items.removeAt(index)
                                        showMenu = false
                                    }
                                )
                            }
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
