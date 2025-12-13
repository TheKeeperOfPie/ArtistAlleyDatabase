package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_duplicate_entry
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_delete_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
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

@Stable
class ArtistFormState(
    val metadata: Metadata,
    val images: SnapshotStateList<EditImage>,
    val links: SnapshotStateList<LinkModel>,
    val storeLinks: SnapshotStateList<LinkModel>,
    val catalogLinks: SnapshotStateList<String>,
    val commissions: SnapshotStateList<CommissionModel>,
    val seriesInferred: SnapshotStateList<SeriesInfo>,
    val seriesConfirmed: SnapshotStateList<SeriesInfo>,
    val merchInferred: SnapshotStateList<MerchInfo>,
    val merchConfirmed: SnapshotStateList<MerchInfo>,
    val textState: TextState,
) {
    fun applyDatabaseEntry(
        artist: ArtistDatabaseEntry,
        seriesById: Map<String, SeriesInfo>,
        merchById: Map<String, MerchInfo>,
    ) {
        val links = artist.links.map(LinkModel::parse).sortedBy { it.logo }
        val storeLinks = artist.storeLinks.map(LinkModel::parse).sortedBy { it.logo }
        val commissions = artist.commissions.map(CommissionModel::parse)

        val seriesInferred = artist.seriesInferred.mapNotNull { seriesById[it] }
        val seriesConfirmed = artist.seriesConfirmed.mapNotNull { seriesById[it] }
        val merchInferred = artist.merchInferred.mapNotNull { merchById[it] }
        val merchConfirmed = artist.merchConfirmed.mapNotNull { merchById[it] }

        val status = artist.status
        textState.status.selectedIndex = ArtistStatus.entries.indexOf(status)

        val booth = artist.booth.orEmpty()
        if (booth.isNotBlank() || status.shouldStartLocked) {
            textState.booth.value.setTextAndPlaceCursorAtEnd(booth)
            textState.booth.lockState = EntryLockState.LOCKED
        }

        val name = artist.name
        if (name.isNotBlank() || status.shouldStartLocked) {
            textState.name.value.setTextAndPlaceCursorAtEnd(name)
            textState.name.lockState = EntryLockState.LOCKED
        }

        val summary = artist.summary.orEmpty()
        if (summary.isNotBlank() || status.shouldStartLocked) {
            textState.summary.value.setTextAndPlaceCursorAtEnd(summary)
            textState.summary.lockState = EntryLockState.LOCKED
        }

        val notes = artist.notes.orEmpty()
        if (notes.isNotBlank() || status.shouldStartLocked) {
            textState.notes.value.setTextAndPlaceCursorAtEnd(notes)
            textState.notes.lockState = EntryLockState.LOCKED
        }

        val editorNotes = artist.editorNotes.orEmpty()
        if (editorNotes.isNotBlank() || status.shouldStartLocked) {
            textState.editorNotes.value.setTextAndPlaceCursorAtEnd(editorNotes)
            textState.editorNotes.lockState = EntryLockState.LOCKED
        }

        if (links.isNotEmpty() || status.shouldStartLocked) {
            this.links.replaceAll(links)
            textState.links.lockState = EntryLockState.LOCKED
        }
        if (storeLinks.isNotEmpty() || status.shouldStartLocked) {
            this.storeLinks.replaceAll(storeLinks)
            textState.storeLinks.lockState = EntryLockState.LOCKED
        }
        if (artist.catalogLinks.isNotEmpty() || status.shouldStartLocked) {
            this.catalogLinks.replaceAll(artist.catalogLinks)
            textState.catalogLinks.lockState = EntryLockState.LOCKED
        }
        if (artist.commissions.isNotEmpty() || status.shouldStartLocked) {
            this.commissions.replaceAll(commissions)
            textState.commissions.lockState = EntryLockState.LOCKED
        }

        if (seriesInferred.isNotEmpty() || status.shouldStartLocked) {
            this.seriesInferred.replaceAll(seriesInferred)
            textState.seriesInferred.lockState = EntryLockState.LOCKED
        }
        if (seriesConfirmed.isNotEmpty() || status.shouldStartLocked) {
            this.seriesConfirmed.replaceAll(seriesConfirmed)
            textState.seriesConfirmed.lockState = EntryLockState.LOCKED
        }
        if (merchInferred.isNotEmpty() || status.shouldStartLocked) {
            this.merchInferred.replaceAll(merchInferred)
            textState.merchInferred.lockState = EntryLockState.LOCKED
        }
        if (merchConfirmed.isNotEmpty() || status.shouldStartLocked) {
            this.merchConfirmed.replaceAll(merchConfirmed)
            textState.merchConfirmed.lockState = EntryLockState.LOCKED
        }

        metadata.lastEditor = artist.lastEditor
        metadata.lastEditTime = artist.lastEditTime
    }

    @Stable
    class Metadata(
        lastEditor: String? = null,
        lastEditTime: Instant? = null,
    ) {
        var lastEditor by mutableStateOf(lastEditor)
        var lastEditTime by mutableStateOf(lastEditTime)

        object Saver : ComposeSaver<Metadata, List<Any?>> {
            override fun SaverScope.save(value: Metadata) = listOf(
                value.lastEditor,
                value.lastEditTime?.toString()
            )

            override fun restore(value: List<Any?>): Metadata {
                val (lastEditor, lastEditTime) = value
                return Metadata(
                    lastEditor = lastEditor as String?,
                    lastEditTime = (lastEditTime as? String?)?.let(Instant::parseOrNull)
                )
            }
        }
    }

    @Stable
    class TextState(
        val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val status: EntryForm2.DropdownState = EntryForm2.DropdownState(),
        val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val links: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val storeLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val catalogLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val commissions: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val seriesInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val seriesConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val merchInferred: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val merchConfirmed: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        val editorNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
    ) {
        object Saver : ComposeSaver<TextState, List<Any>> {
            override fun SaverScope.save(value: TextState) = listOf(
                with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                with(EntryForm2.DropdownState.Saver) { save(value.status) },
                with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                with(EntryForm2.SingleTextState.Saver) { save(value.links) },
                with(EntryForm2.SingleTextState.Saver) { save(value.storeLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.catalogLinks) },
                with(EntryForm2.SingleTextState.Saver) { save(value.commissions) },
                with(EntryForm2.SingleTextState.Saver) { save(value.seriesInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.seriesConfirmed) },
                with(EntryForm2.SingleTextState.Saver) { save(value.merchInferred) },
                with(EntryForm2.SingleTextState.Saver) { save(value.merchConfirmed) },
                with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                with(EntryForm2.SingleTextState.Saver) { save(value.editorNotes) },
            )

            override fun restore(value: List<Any>) = TextState(
                id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                status = with(EntryForm2.DropdownState.Saver) { restore(value[1]) },
                booth = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                name = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                summary = with(EntryForm2.SingleTextState.Saver) { restore(value[4]) },
                links = with(EntryForm2.SingleTextState.Saver) { restore(value[5]) },
                storeLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[6]) },
                catalogLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[7]) },
                commissions = with(EntryForm2.SingleTextState.Saver) { restore(value[8]) },
                seriesInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[9]) },
                seriesConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[10]) },
                merchInferred = with(EntryForm2.SingleTextState.Saver) { restore(value[11]) },
                merchConfirmed = with(EntryForm2.SingleTextState.Saver) { restore(value[12]) },
                notes = with(EntryForm2.SingleTextState.Saver) { restore(value[13]) },
                editorNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[14]) },
            )
        }
    }
}

// Not saved since it's purely derived from input fields
@Stable
class ArtistErrorState(
    val idErrorMessage: () -> String?,
    val boothErrorMessage: () -> String?,
    val linksErrorMessage: () -> String?,
    val storeLinksErrorMessage: () -> String?,
    val catalogLinksErrorMessage: () -> String?,
) {
    val hasAnyError by derivedStateOf {
        idErrorMessage() != null ||
                boothErrorMessage() != null ||
                linksErrorMessage() != null ||
                storeLinksErrorMessage() != null ||
                catalogLinksErrorMessage() != null
    }
}

@Stable
@Composable
fun rememberErrorState(state: ArtistFormState.TextState): ArtistErrorState {
    val idErrorMessage by rememberUuidValidator(state.id)
    val boothErrorMessage by rememberBoothValidator(state.booth)
    val linksErrorMessage by rememberLinkValidator(state.links)
    val storeLinksErrorMessage by rememberLinkValidator(state.storeLinks)
    val catalogLinksErrorMessage by rememberLinkValidator(state.catalogLinks)
    return ArtistErrorState(
        idErrorMessage = { idErrorMessage },
        boothErrorMessage = { boothErrorMessage },
        linksErrorMessage = { linksErrorMessage },
        storeLinksErrorMessage = { storeLinksErrorMessage },
        catalogLinksErrorMessage = { catalogLinksErrorMessage },
    )
}

@Stable
@Composable
private fun rememberBoothValidator(boothState: EntryForm2.SingleTextState): androidx.compose.runtime.State<String?> {
    val errorMessage = stringResource(Res.string.alley_edit_artist_error_booth)
    return remember {
        derivedStateOf {
            val booth = boothState.value.text.toString()
            if (booth.isNotBlank() && (
                        booth.length != 3 ||
                                !booth.first().isLetter() ||
                                booth.drop(1).toIntOrNull() == null)
            ) {
                errorMessage
            } else {
                null
            }
        }
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
        forceLocked: Boolean = false,
    ) {
        val textState = state.textState
        EntryForm2(forceLocked = forceLocked, modifier = modifier) {
            val metadata = state.metadata
            val lastEditTime = metadata.lastEditTime
            if (lastEditTime != null) {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    val textColorDim = LocalContentColor.current.copy(alpha = 0.6f)
                    val colorPrimary = MaterialTheme.colorScheme.primary
                    val lastEditor = metadata.lastEditor
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

            DropdownSection(
                state = textState.status,
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
                nextFocus = textState.id.focusRequester
            )

            SingleTextSection(
                state = textState.id,
                headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_id)) },
                previousFocus = textState.status.focusRequester,
                nextFocus = textState.booth.focusRequester,
                errorText = { errorState.idErrorMessage() },
            )

            SingleTextSection(
                state = textState.booth,
                headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_booth)) },
                previousFocus = textState.id.focusRequester,
                nextFocus = textState.name.focusRequester,
                inputTransformation = InputTransformation.maxLength(3),
                errorText = { errorState.boothErrorMessage() },
            )
            SingleTextSection(
                state = textState.name,
                headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_name)) },
                previousFocus = textState.booth.focusRequester,
                nextFocus = textState.summary.focusRequester,
            )
            SingleTextSection(
                state = textState.summary,
                headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_summary)) },
                previousFocus = textState.name.focusRequester,
                nextFocus = textState.links.focusRequester,
            )
            LinksSection(
                state = textState.links,
                title = Res.string.alley_edit_artist_edit_links,
                items = state.links,
                pendingErrorMessage = { errorState.linksErrorMessage() },
                previousFocus = textState.summary.focusRequester,
                nextFocus = textState.storeLinks.focusRequester,
            )
            LinksSection(
                state = textState.storeLinks,
                title = Res.string.alley_edit_artist_edit_store_links,
                items = state.storeLinks,
                pendingErrorMessage = { errorState.storeLinksErrorMessage() },
                previousFocus = textState.links.focusRequester,
                nextFocus = textState.catalogLinks.focusRequester,
            )
            MultiTextSection(
                state = textState.catalogLinks,
                title = Res.string.alley_edit_artist_edit_catalog_links,
                items = state.catalogLinks,
                itemToText = { it },
                itemToSerializedValue = { it },
                itemToCommitted = { it },
                previousFocus = textState.storeLinks.focusRequester,
                nextFocus = textState.commissions.focusRequester,
                pendingErrorMessage = { errorState.catalogLinksErrorMessage() },
            )


            val onSiteText = stringResource(AlleyRes.string.alley_artist_commission_on_site)
            val onlineText = stringResource(AlleyRes.string.alley_artist_commission_online)
            MultiTextSection(
                state = textState.commissions,
                title = Res.string.alley_edit_artist_edit_commissions,
                items = state.commissions,
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
                previousFocus = textState.catalogLinks.focusRequester,
                nextFocus = textState.seriesInferred.focusRequester,
            )

            val hasConfirmedSeries by derivedStateOf { state.seriesConfirmed.isNotEmpty() }
            var requestedShowSeriesInferred by rememberSaveable { mutableStateOf(false) }
            val showSeriesInferred = !hasConfirmedSeries || requestedShowSeriesInferred
            SeriesSection(
                state = textState.seriesInferred,
                title = Res.string.alley_edit_artist_edit_series_inferred,
                items = state.seriesInferred,
                showItems = { showSeriesInferred },
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = textState.commissions.focusRequester,
                nextFocus = textState.seriesConfirmed.focusRequester,
            )
            ShowInferredButton(
                hasConfirmed = hasConfirmedSeries,
                showingInferred = showSeriesInferred,
                onClick = { requestedShowSeriesInferred = it },
            )

            SeriesSection(
                state = textState.seriesConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                items = state.seriesConfirmed,
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = textState.seriesInferred.focusRequester,
                nextFocus = textState.merchInferred.focusRequester,
            )

            val hasConfirmedMerch by derivedStateOf { state.merchConfirmed.isNotEmpty() }
            var requestedShowMerchInferred by rememberSaveable { mutableStateOf(false) }
            val showMerchInferred = !hasConfirmedMerch || requestedShowMerchInferred
            MultiTextSection(
                state = textState.merchInferred,
                title = Res.string.alley_edit_artist_edit_merch_inferred,
                items = state.merchInferred,
                showItems = { showMerchInferred },
                predictions = merchPredictions,
                itemToText = { it.name },
                itemToSerializedValue = { it.name },
                previousFocus = textState.seriesConfirmed.focusRequester,
                nextFocus = textState.merchConfirmed.focusRequester,
            )
            ShowInferredButton(
                hasConfirmed = hasConfirmedMerch,
                showingInferred = showMerchInferred,
                onClick = { requestedShowMerchInferred = it },
            )

            MultiTextSection(
                state = textState.merchConfirmed,
                title = Res.string.alley_edit_artist_edit_merch_confirmed,
                items = state.merchConfirmed,
                predictions = merchPredictions,
                itemToText = { it.name },
                itemToSerializedValue = { it.name },
                previousFocus = textState.merchInferred.focusRequester,
                nextFocus = textState.notes.focusRequester,
            )
            LongTextSection(
                textState.notes,
                headerText = {
                    Text(stringResource(Res.string.alley_edit_artist_edit_notes))
                },
            )
            LongTextSection(
                textState.editorNotes,
                headerText = {
                    Text(stringResource(Res.string.alley_edit_artist_edit_editor_notes))
                },
            )
        }
    }

    @Composable
    private fun <T> EntryFormScope.MultiTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
        itemToSerializedValue: (T) -> String,
        previousFocus: FocusRequester? = null,
        nextFocus: FocusRequester? = null,
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
            previousFocus = previousFocus,
            nextFocus = nextFocus,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    private fun EntryFormScope.SeriesSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        items: SnapshotStateList<SeriesInfo>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
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
                        IconButtonWithTooltip(
                            imageVector = Icons.Default.Delete,
                            tooltipText = contentDescription,
                            onClick = { items.remove(value) },
                            contentDescription = contentDescription,
                        )
                    }
                }
            },
            previousFocus = previousFocus,
            nextFocus = nextFocus,
        )
    }

    @Composable
    fun <T> EntryFormScope.MultiTextSection(
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
        previousFocus: FocusRequester? = null,
        nextFocus: FocusRequester? = null,
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
            previousFocus = previousFocus,
            nextFocus = nextFocus,
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
    private fun ShowInferredButton(
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

    @Composable
    private fun EntryFormScope.LinksSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        items: SnapshotStateList<LinkModel>,
        pendingErrorMessage: () -> String?,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
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
            previousFocus = previousFocus,
            nextFocus = nextFocus,
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
