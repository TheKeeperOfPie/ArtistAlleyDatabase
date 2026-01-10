package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.allCaps
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
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
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_portfolio_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_revert_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_revert_action_dismiss
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_revert_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_revert_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_duplicate_entry
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_label
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_placeholder
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_delete_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_row_edit_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online_tooltip
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.name
import com.thekeeperofpie.artistalleydatabase.alley.series.otherTitles
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionMedia
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomIcons
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
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

@LayoutScopeMarker
@Immutable
interface ArtistFormScope : EntryFormScope {
    val initialArtist: ArtistDatabaseEntry.Impl?

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
    fun BoothSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
        errorText: (() -> String?)? = null,
    )

    @Composable
    fun NameSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    )

    @Composable
    fun SummarySection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
    )

    @Composable
    fun LinkSections(
        state: ArtistFormState.LinksState,
        linksErrorMessage: () -> String? = { null },
        storeLinksErrorMessage: () -> String? = { null },
        portfolioLinksErrorMessage: () -> String? = { null },
        catalogLinksErrorMessage: () -> String? = { null },
    )

    @Composable
    fun SocialLinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)? = null,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun StoreLinksSection(
        state: EntryForm2.SingleTextState,
        storeLinks: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)? = null,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun PortfolioLinksSection(
        state: EntryForm2.SingleTextState,
        portfolioLinks: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)? = null,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun CatalogLinksSection(
        state: EntryForm2.SingleTextState,
        catalogLinks: SnapshotStateList<String>,
        label: @Composable (() -> Unit)? = null,
        pendingErrorMessage: () -> String? = { null },
    )

    @Composable
    fun CommissionsSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)? = null,
        commissions: SnapshotStateList<CommissionModel>,
    )

    @Composable
    fun TagSections(
        series: ArtistFormState.SeriesState,
        merch: ArtistFormState.MerchState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    )

    @Composable
    fun SeriesSection(
        state: ArtistFormState.SeriesState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        showConfirmed: Boolean = true,
    )

    @Composable
    fun MerchSection(
        state: ArtistFormState.MerchState,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        showConfirmed: Boolean = true,
    )

    @Composable
    fun NotesSection(
        state: EntryForm2.SingleTextState,
        initialValue: String?,
        header: StringResource = Res.string.alley_edit_artist_edit_notes,
        label: @Composable (() -> Unit)? = null,
    )
}

@LayoutScopeMarker
@Immutable
private abstract class ArtistFormScopeImpl(
    entryFormScope: EntryFormScope,
) : ArtistFormScope, EntryFormScope by entryFormScope {

    abstract override val initialArtist: ArtistDatabaseEntry.Impl?

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
            val placeholder =
                AnnotatedString(stringResource(Res.string.alley_edit_paste_link_placeholder))
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {
                        ArtistForm.processPastedLink(state, link = it)
                    },
                    label = {
                        Text(stringResource(Res.string.alley_edit_paste_link_label))
                    },
                    visualTransformation = VisualTransformation {
                        TransformedText(placeholder, object : OffsetMapping {
                            override fun originalToTransformed(offset: Int) = 0
                            override fun transformedToOriginal(offset: Int) = 0
                        })
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
        val revertDialogState = rememberRevertDialogState(initialArtist?.id)
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(Res.string.alley_edit_artist_edit_id)) },
            forceLocked = forceLock || forceLocked,
            outputTransformation = revertDialogState.outputTransformation,
            errorText = errorText,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_artist_edit_id)
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
    override fun BoothSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)?,
        errorText: (() -> String?)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialArtist?.booth)
        SingleTextSection(
            state = state,
            headerText = {
                HeaderIconAndTitle(Icons.Default.TableRestaurant, Res.string.alley_edit_artist_edit_booth)
            },
            inputTransformation = InputTransformation.maxLength(3).allCaps(Locale.current),
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            errorText = errorText,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_artist_edit_booth)
    }

    @Composable
    override fun NameSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialArtist?.name)
        SingleTextSection(
            state = state,
            headerText = {
                HeaderIconAndTitle(Icons.Default.Badge, Res.string.alley_edit_artist_edit_name)
            },
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_artist_edit_name)
    }

    @Composable
    override fun SummarySection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialArtist?.summary)
        SingleTextSection(
            state = state,
            headerText = {
                HeaderIconAndTitle(CustomIcons.TextAd, Res.string.alley_edit_artist_edit_summary)
            },
            outputTransformation = revertDialogState.outputTransformation,
            label = label,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )

        FieldRevertDialog(revertDialogState, state, Res.string.alley_edit_artist_edit_summary)
    }

    @Composable
    override fun LinkSections(
        state: ArtistFormState.LinksState,
        linksErrorMessage: () -> String?,
        storeLinksErrorMessage: () -> String?,
        portfolioLinksErrorMessage: () -> String?,
        catalogLinksErrorMessage: () -> String?,
    ) {
        SocialLinksSection(
            state = state.stateSocialLinks,
            links = state.socialLinks,
            pendingErrorMessage = linksErrorMessage,
        )
        StoreLinksSection(
            state = state.stateStoreLinks,
            storeLinks = state.storeLinks,
            pendingErrorMessage = storeLinksErrorMessage,
        )
        PortfolioLinksSection(
            state = state.statePortfolioLinks,
            portfolioLinks = state.portfolioLinks,
            pendingErrorMessage = portfolioLinksErrorMessage,
        )
        CatalogLinksSection(
            state = state.stateCatalogLinks,
            catalogLinks = state.catalogLinks,
            pendingErrorMessage = catalogLinksErrorMessage,
        )
        CommissionsSection(state = state.stateCommissions, commissions = state.commissions)
    }

    @Composable
    override fun SocialLinksSection(
        state: EntryForm2.SingleTextState,
        links: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)?,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_social_links,
            header = {
                HeaderIconAndTitle(
                    Icons.Default.Diversity3,
                    Res.string.alley_edit_artist_edit_social_links
                )
            },
            listRevertDialogState =
                rememberListRevertDialogState(initialArtist?.socialLinks?.map(LinkModel::parse)),
            items = links,
            label = label,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun StoreLinksSection(
        state: EntryForm2.SingleTextState,
        storeLinks: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)?,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_store_links,
            header = {
                HeaderIconAndTitle(
                    Icons.Default.Store,
                    Res.string.alley_edit_artist_edit_store_links
                )
            },
            listRevertDialogState =
                rememberListRevertDialogState(initialArtist?.storeLinks?.map(LinkModel::parse)),
            items = storeLinks,
            label = label,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun PortfolioLinksSection(
        state: EntryForm2.SingleTextState,
        portfolioLinks: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)?,
        pendingErrorMessage: () -> String?,
    ) {
        ArtistForm.LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_portfolio_links,
            header = {
                HeaderIconAndTitle(
                    CustomIcons.GalleryThumbnail,
                    Res.string.alley_edit_artist_edit_portfolio_links
                )
            },
            listRevertDialogState =
                rememberListRevertDialogState(initialArtist?.portfolioLinks?.map(LinkModel::parse)),
            items = portfolioLinks,
            label = label,
            pendingErrorMessage = pendingErrorMessage,
        )
    }

    @Composable
    override fun CatalogLinksSection(
        state: EntryForm2.SingleTextState,
        catalogLinks: SnapshotStateList<String>,
        label: @Composable (() -> Unit)?,
        pendingErrorMessage: () -> String?,
    ) {
        val revertDialogState = rememberListRevertDialogState(initialArtist?.catalogLinks)
        ArtistForm.MultiTextSection(
            state = state,
            header = {
                HeaderIconAndTitle(
                    CustomIcons.Browse,
                    Res.string.alley_edit_artist_edit_catalog_links
                )
            },
            initialItems = initialArtist?.catalogLinks,
            items = catalogLinks,
            itemToText = { it },
            itemToSubText = { null },
            itemToSerializedValue = { it },
            itemToCommitted = { it },
            label = label,
            pendingErrorMessage = pendingErrorMessage,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowListRevertIconButton(revertDialogState, catalogLinks)
                }
            },
        )

        ListFieldRevertDialog(
            dialogState = revertDialogState,
            label = Res.string.alley_edit_artist_edit_catalog_links,
            items = catalogLinks,
            itemsToText = { it.joinToString() },
        )
    }

    @Composable
    override fun CommissionsSection(
        state: EntryForm2.SingleTextState,
        label: @Composable (() -> Unit)?,
        commissions: SnapshotStateList<CommissionModel>,
    ) {
        val onSiteText = stringResource(AlleyRes.string.alley_artist_commission_on_site)
        val onSiteSubText = stringResource(AlleyRes.string.alley_artist_commission_on_site_tooltip)
        val onlineText = stringResource(AlleyRes.string.alley_artist_commission_online)
        val onlineSubText = stringResource(AlleyRes.string.alley_artist_commission_online_tooltip)
        val initialCommissions = remember(initialArtist?.commissions) {
            initialArtist?.commissions?.map(CommissionModel::parse).orEmpty()
        }
        val revertDialogState = rememberListRevertDialogState(initialCommissions)
        fun itemToText(model: CommissionModel): String =
            when (model) {
                is CommissionModel.Link -> model.host ?: model.link
                CommissionModel.OnSite -> onSiteText
                CommissionModel.Online -> onlineText
                is CommissionModel.Unknown -> model.value
            }
        ArtistForm.MultiTextSection(
            state = state,
            header = {
                HeaderIconAndTitle(
                    Icons.Default.Brush,
                    Res.string.alley_edit_artist_edit_commissions
                )
            },
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
            itemToText = ::itemToText,
            itemToSubText = {
                when (it) {
                    is CommissionModel.Link -> if (it.host == null) null else it.link
                    CommissionModel.OnSite -> onSiteSubText
                    CommissionModel.Online -> onlineSubText
                    is CommissionModel.Unknown -> null
                }
            },
            itemToSerializedValue = { it.serializedValue },
            itemToCommitted = CommissionModel::parse,
            label = label,
            predictions = {
                flowOf(
                    if (it.isBlank()) {
                        listOf(CommissionModel.Online, CommissionModel.OnSite)
                    } else {
                        emptyList()
                    }
                )
            },
            preferPrediction = false,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowListRevertIconButton(revertDialogState, commissions)
                }
            },
        )

        ListFieldRevertDialog(
            dialogState = revertDialogState,
            label = Res.string.alley_edit_artist_edit_commissions,
            items = commissions,
            itemsToText = { it.joinToString { itemToText(it) } },
        )
    }

    @Composable
    override fun TagSections(
        series: ArtistFormState.SeriesState,
        merch: ArtistFormState.MerchState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
    ) {
        SeriesSection(
            state = series,
            seriesById = seriesById,
            seriesPredictions = seriesPredictions,
            seriesImage = seriesImage,
        )
        MerchSection(state = merch, merchById = merchById, merchPredictions = merchPredictions)
    }

    @Composable
    override fun SeriesSection(
        state: ArtistFormState.SeriesState,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        showConfirmed: Boolean,
    ) {
        val hasConfirmedSeries by derivedStateOf { state.confirmed.isNotEmpty() }
        var requestedShowSeriesInferred by rememberSaveable { mutableStateOf(false) }
        val showSeriesInferred =
            forceLocked || !hasConfirmedSeries || requestedShowSeriesInferred || !showConfirmed

        val seriesById = seriesById()
        val initialInferred = remember(seriesById, initialArtist?.seriesInferred) {
            initialArtist?.seriesInferred?.map { seriesById[it] ?: SeriesInfo.fake(it) }.orEmpty()
        }

        val revertDialogStateInferred = rememberListRevertDialogState(initialInferred)

        ArtistForm.SeriesSection(
            state = state.stateInferred,
            title = Res.string.alley_edit_artist_edit_series_inferred,
            header = {
                HeaderIconAndTitle(
                    CustomIcons.TvGen,
                    Res.string.alley_edit_artist_edit_series_inferred
                )
            },
            listRevertDialogState = revertDialogStateInferred,
            items = state.inferred,
            showItems = { showSeriesInferred },
            predictions = seriesPredictions,
            image = seriesImage,
            additionalHeaderActions = {
                if (!this@ArtistFormScopeImpl.forceLocked && showConfirmed) {
                    ArtistForm.ShowInferredButton(
                        hasConfirmed = hasConfirmedSeries,
                        showingInferred = showSeriesInferred,
                        onClick = { requestedShowSeriesInferred = it },
                    )
                }
            }
        )

        if (showConfirmed) {
            val initialConfirmed = remember(seriesById, initialArtist?.seriesConfirmed) {
                initialArtist?.seriesConfirmed
                    ?.map { seriesById[it] ?: SeriesInfo.fake(it) }
                    .orEmpty()
            }
            val revertDialogStateConfirmed = rememberListRevertDialogState(initialConfirmed)
            ArtistForm.SeriesSection(
                state = state.stateConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                header = {
                    HeaderIconAndTitle(
                        CustomIcons.TvGen,
                        Res.string.alley_edit_artist_edit_series_confirmed
                    )
                },
                listRevertDialogState = revertDialogStateConfirmed,
                items = state.confirmed,
                predictions = seriesPredictions,
                image = seriesImage,
            )
        }
    }

    @Composable
    override fun MerchSection(
        state: ArtistFormState.MerchState,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        showConfirmed: Boolean,
    ) {
        val hasConfirmedMerch by derivedStateOf { state.confirmed.isNotEmpty() }
        var requestedShowMerchInferred by rememberSaveable { mutableStateOf(false) }
        val showMerchInferred =
            forceLocked || !hasConfirmedMerch || requestedShowMerchInferred || !showConfirmed

        val merchById = merchById()
        val initialInferred = remember(merchById, initialArtist?.merchInferred) {
            initialArtist?.merchInferred?.map { merchById[it] ?: MerchInfo.fake(it) }.orEmpty()
        }
        val revertDialogStateInferred = rememberListRevertDialogState(initialInferred)
        ArtistForm.MultiTextSection(
            state = state.stateInferred,
            header = {
                HeaderIconAndTitle(
                    CustomIcons.Package2,
                    Res.string.alley_edit_artist_edit_merch_inferred
                )
            },
            initialItems = initialInferred,
            equalsComparison = { it.name },
            items = state.inferred,
            showItems = { showMerchInferred },
            predictions = merchPredictions,
            itemToCommitted = MerchInfo::fake,
            itemToText = { it.name },
            itemToSubText = { it.notes },
            itemToSerializedValue = { it.name },
            predictionToText = {
                if (it.faked) {
                    "\"${it.name}\""
                } else {
                    it.name
                }
            },
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowListRevertIconButton(revertDialogStateInferred, state.inferred)
                }
                if (!this@ArtistFormScopeImpl.forceLocked && showConfirmed) {
                    ArtistForm.ShowInferredButton(
                        hasConfirmed = hasConfirmedMerch,
                        showingInferred = showMerchInferred,
                        onClick = { requestedShowMerchInferred = it },
                    )
                }
            },
        )

        ListFieldRevertDialog(
            dialogState = revertDialogStateInferred,
            label = Res.string.alley_edit_artist_edit_merch_inferred,
            items = state.inferred,
            itemsToText = { it.joinToString { it.name } },
        )

        if (showConfirmed) {
            val initialConfirmed = remember(merchById, initialArtist?.merchConfirmed) {
                initialArtist?.merchConfirmed?.map { merchById[it] ?: MerchInfo.fake(it) }.orEmpty()
            }

            val revertDialogStateConfirmed = rememberListRevertDialogState(initialConfirmed)
            ArtistForm.MultiTextSection(
                state = state.stateConfirmed,
                header = {
                    HeaderIconAndTitle(
                        CustomIcons.Package2,
                        Res.string.alley_edit_artist_edit_merch_confirmed
                    )
                },
                initialItems = initialConfirmed,
                equalsComparison = { it.name },
                items = state.confirmed,
                predictions = merchPredictions,
                itemToCommitted = MerchInfo::fake,
                itemToText = { it.name },
                itemToSubText = { it.notes },
                itemToSerializedValue = { it.name },
                predictionToText = {
                    if (it.faked) {
                        "\"${it.name}\""
                    } else {
                        it.name
                    }
                },
                additionalHeaderActions = {
                    with(this@ArtistFormScopeImpl) {
                        ShowListRevertIconButton(revertDialogStateConfirmed, state.confirmed)
                    }
                },
            )

            ListFieldRevertDialog(
                dialogState = revertDialogStateConfirmed,
                label = Res.string.alley_edit_artist_edit_merch_confirmed,
                items = state.confirmed,
                itemsToText = { it.joinToString { it.name } },
            )
        }
    }

    @Composable
    override fun NotesSection(
        state: EntryForm2.SingleTextState,
        initialValue: String?,
        header: StringResource,
        label: @Composable (() -> Unit)?,
    ) {
        val revertDialogState = rememberRevertDialogState(initialValue)
        LongTextSection(
            state = state,
            headerText = { HeaderIconAndTitle(Icons.AutoMirrored.Default.Notes, header) },
            label = label,
            outputTransformation = revertDialogState.outputTransformation,
            additionalHeaderActions = {
                with(this@ArtistFormScopeImpl) {
                    ShowRevertIconButton(revertDialogState, state)
                }
            },
        )
        FieldRevertDialog(revertDialogState, state, header)
    }

    @Composable
    private fun HeaderIconAndTitle(icon: ImageVector, title: StringResource) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(stringResource(title))
        }
    }

    @Composable
    private fun rememberRevertDialogState(initialValue: String?) =
        remember(initialArtist, initialValue) {
            RevertDialogState(initialArtist, initialValue.orEmpty())
        }

    @Composable
    private fun <T> rememberListRevertDialogState(initialItems: List<T>?) =
        remember(initialItems) { ListRevertDialogState(initialItems.orEmpty()) }
}

@Stable
internal class RevertDialogState(
    initialArtist: ArtistDatabaseEntry.Impl?,
    val initialValue: String,
) {
    var show by mutableStateOf(false)
    val outputTransformation: OutputTransformation? = if (initialArtist == null) null else {
        GreenOnChangedOutputTransformation(initialValue)
    }
}

@Stable
internal class ListRevertDialogState<T>(val initialItems: List<T>) {
    var show by mutableStateOf(false)
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

context(scope: ArtistFormScope)
@Composable
private fun ShowRevertIconButton(
    dialogState: RevertDialogState,
    textState: EntryForm2.SingleTextState,
) {
    val show by remember(dialogState, textState) {
        derivedStateOf { textState.value.text.toString() != dialogState.initialValue }
    }
    if (show && !scope.forceLocked) {
        TooltipIconButton(
            icon = Icons.AutoMirrored.Default.Undo,
            tooltipText = stringResource(Res.string.alley_edit_artist_edit_revert_tooltip),
            onClick = { dialogState.show = true },
        )
    }
}

context(scope: ArtistFormScope)
@Composable
private fun <T> ShowListRevertIconButton(
    dialogState: ListRevertDialogState<T>,
    items: SnapshotStateList<T>,
) {
    val show by remember(items, dialogState) {
        derivedStateOf {
            items.toList().toSet() != dialogState.initialItems.toSet()
        }
    }
    if (show && !scope.forceLocked) {
        TooltipIconButton(
            icon = Icons.AutoMirrored.Default.Undo,
            tooltipText = stringResource(Res.string.alley_edit_artist_edit_revert_tooltip),
            onClick = { dialogState.show = true },
        )
    }
}

@Composable
private fun FieldRevertDialog(
    dialogState: RevertDialogState,
    textState: EntryForm2.SingleTextState,
    label: StringResource,
) {
    if (dialogState.show) {
        RevertDialog(
            label = label,
            text = dialogState.initialValue,
            onDismiss = { dialogState.show = false },
            onRevert = { textState.value.setTextAndPlaceCursorAtEnd(dialogState.initialValue) },
        )
    }
}

@Composable
private fun <T> ListFieldRevertDialog(
    dialogState: ListRevertDialogState<T>,
    label: StringResource,
    items: SnapshotStateList<T>,
    itemsToText: (List<T>) -> String,
) {
    if (dialogState.show) {
        RevertDialog(
            label = label,
            text = itemsToText(dialogState.initialItems),
            onDismiss = { dialogState.show = false },
            onRevert = { items.replaceAll(dialogState.initialItems.toList()) },
        )
    }
}

@Composable
private fun RevertDialog(
    label: StringResource,
    text: String,
    onDismiss: () -> Unit,
    onRevert: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onRevert()
                onDismiss()
            }) {
                Text(stringResource(Res.string.alley_edit_artist_edit_revert_action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.alley_edit_artist_edit_revert_action_dismiss))
            }
        },
        icon = { Icon(Icons.AutoMirrored.Default.Undo, null) },
        title = {
            Text(
                stringResource(
                    Res.string.alley_edit_artist_edit_revert_title,
                    stringResource(label),
                )
            )
        },
        text = if (text.isNotBlank()) {
            { Text(text) }
        } else null,
    )
}

object ArtistForm {

    @Composable
    operator fun invoke(
        state: ArtistFormState,
        errorState: ArtistErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
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
                state.links.stateSocialLinks,
                state.links.stateStoreLinks,
                state.links.statePortfolioLinks,
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
        ) ArtistFormScope@{
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
            InfoSections(state.info, boothErrorMessage = errorState.boothErrorMessage)

            LinkSections(
                state = state.links,
                linksErrorMessage = errorState.socialLinksErrorMessage,
                storeLinksErrorMessage = errorState.storeLinksErrorMessage,
                portfolioLinksErrorMessage = errorState.portfolioLinksErrorMessage,
                catalogLinksErrorMessage = errorState.catalogLinksErrorMessage,
            )

            TagSections(
                series = state.series,
                merch = state.merch,
                seriesById = seriesById,
                seriesPredictions = seriesPredictions,
                seriesImage = seriesImage,
                merchById = merchById,
                merchPredictions = merchPredictions
            )

            NotesSection(state.info.notes, this@ArtistFormScope.initialArtist?.notes)
            if (showEditorNotes) {
                NotesSection(
                    state = state.editorState.editorNotes,
                    initialValue = this@ArtistFormScope.initialArtist?.editorNotes,
                    header = Res.string.alley_edit_artist_edit_editor_notes,
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
            val scope = remember(this, initialArtist) {
                object : ArtistFormScopeImpl(this) {
                    override val initialArtist: ArtistDatabaseEntry.Impl?
                        get() = initialArtist()
                }
            }
            scope.content()
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
        state: ArtistFormState.LinksState,
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

        when (linkModel.type.category) {
            LinkCategory.PORTFOLIOS -> {
                if (!state.portfolioLinks.contains(linkModel)) {
                    state.portfolioLinks += linkModel
                    state.statePortfolioLinks.lockState = EntryLockState.UNLOCKED
                }
            }
            LinkCategory.SOCIALS,
            LinkCategory.SUPPORT,
                -> {
                if (!state.socialLinks.contains(linkModel)) {
                    state.socialLinks += linkModel
                    state.stateSocialLinks.lockState = EntryLockState.UNLOCKED
                }
            }
            LinkCategory.STORES -> {
                if (!state.storeLinks.contains(linkModel)) {
                    state.storeLinks += linkModel
                    state.stateStoreLinks.lockState = EntryLockState.UNLOCKED
                }
            }
            LinkCategory.OTHER -> {
                if (linkModel.type == Link.Type.VGEN) {
                    val commissionModel = CommissionModel.parse(fixedLink)
                    if (!state.commissions.contains(commissionModel)) {
                        state.commissions += commissionModel
                        state.stateCommissions.lockState = EntryLockState.UNLOCKED
                    }
                }
            }
        }
    }

    context(formScope: ArtistFormScope)
    @Composable
    internal fun <T> MultiTextSection(
        state: EntryForm2.SingleTextState,
        header: @Composable () -> Unit,
        initialItems: List<T>?,
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
        itemToSubText: (T) -> String?,
        itemToSerializedValue: (T) -> String,
        itemToCommitted: ((String) -> T)? = null,
        leadingIcon: (T) -> ImageVector? = { null },
        predictionToText: (T) -> String = itemToText,
        label: @Composable (() -> Unit)? = null,
        pendingErrorMessage: () -> String? = { null },
        preferPrediction: Boolean = true,
        equalsComparison: (T) -> Any? = { it },
        additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
    ) {
        MultiTextSection(
            state = state,
            header = header,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                                .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
                        ) {
                            Text(
                                text = itemToText(item),
                                style = LocalTextStyle.current.copy(
                                    color = if (formScope.initialArtist == null || existed != false) {
                                        LocalTextStyle.current.color
                                    } else {
                                        Color.Green
                                    }
                                ),
                            )
                            val subText = itemToSubText(item)
                            if (!subText.isNullOrBlank()) {
                                Text(
                                    text = subText,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                        }

                        EditActions(
                            state = state,
                            items = items,
                            item = item,
                            itemToText = itemToSerializedValue,
                        )
                    }
                }
            },
            prediction = { _, value ->
                Column {
                    Text(text = predictionToText(value))
                    val subText = itemToSubText(value)
                    if (!subText.isNullOrBlank()) {
                        Text(
                            text = subText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            },
            label = label,
            pendingErrorMessage = pendingErrorMessage,
            preferPrediction = preferPrediction,
            additionalHeaderActions = additionalHeaderActions,
        )
    }

    context(formScope: ArtistFormScope)
    @Composable
    internal fun SeriesSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        header: @Composable () -> Unit,
        listRevertDialogState: ListRevertDialogState<SeriesInfo>,
        items: SnapshotStateList<SeriesInfo>,
        showItems: () -> Boolean = { true },
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
        additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
    ) {
        MultiTextSection(
            state = state,
            header = header,
            items = items,
            itemToCommitted = SeriesInfo::fake,
            showItems = showItems,
            entryPredictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.titlePreferred },
            prediction = { _, value ->
                Column {
                    val languageOptionMedia = LocalLanguageOptionMedia.current
                    val query = state.value.text.toString()
                    val title = buildAnnotatedString {
                        val name = value.name(languageOptionMedia)
                        append(if (value.faked) "\"${name}\"" else name)
                        if (!value.faked) {
                            val startIndex = name.indexOf(query, ignoreCase = true)
                            if (startIndex >= 0) {
                                addStyle(
                                    style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                                    start = startIndex,
                                    end = startIndex + query.length,
                                )
                            }
                        }
                    }
                    Text(text = title)

                    if (!value.faked) {
                        val otherTitles = value.otherTitles(languageOptionMedia)
                        if (otherTitles.isNotEmpty()) {
                            val text = buildAnnotatedString {
                                val value = otherTitles.joinToString(" / ")
                                append(value)
                                val startIndex = value.indexOf(query, ignoreCase = true)
                                if (startIndex >= 0) {
                                    addStyle(
                                        style = SpanStyle(color = MaterialTheme.colorScheme.secondary),
                                        start = startIndex,
                                        end = startIndex + query.length,
                                    )
                                }
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            },
            sortValue = { it.titlePreferred },
            item = { _, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val existed = listRevertDialogState.initialItems.any { it.id == value.id }
                    val textStyle = if (formScope.initialArtist == null || existed) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(color = Color.Green)
                    }
                    SeriesRow(
                        series = value,
                        image = { image(value) },
                        textStyle = textStyle,
                        showAllTitles = true,
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
            additionalHeaderActions = {
                with(formScope) {
                    ShowListRevertIconButton(listRevertDialogState, items)
                }
                additionalHeaderActions?.invoke(this)
            },
        )

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
            label = title,
            items = items,
            itemsToText = { it.joinToString { it.titlePreferred } },
        )
    }

    context(formScope: EntryFormScope)
    @Composable
    internal fun <T> MultiTextSection(
        state: EntryForm2.SingleTextState,
        header: @Composable () -> Unit,
        items: SnapshotStateList<T>,
        showItems: () -> Boolean = { true },
        itemToCommitted: ((String) -> T)? = null,
        removeLastItem: () -> String?,
        item: @Composable (index: Int, T) -> Unit,
        entryPredictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        prediction: @Composable (index: Int, T) -> Unit = item,
        sortValue: ((T) -> String)? = null,
        label: @Composable (() -> Unit)? = null,
        inputTransformation: InputTransformation? = null,
        pendingErrorMessage: () -> String? = { null },
        preferPrediction: Boolean = true,
        additionalHeaderActions: @Composable (RowScope.() -> Unit)? = null,
    ) {
        val addUniqueErrorState =
            rememberAddUniqueErrorState(state = state, items = items, sortValue = sortValue)
        MultiTextSection(
            state = state,
            headerText = header,
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
            label = label,
            inputTransformation = inputTransformation,
            pendingErrorMessage = { addUniqueErrorState.errorMessage ?: pendingErrorMessage() },
            additionalHeaderActions = additionalHeaderActions,
        )
    }

    @Composable
    internal fun ShowInferredButton(
        hasConfirmed: Boolean,
        showingInferred: Boolean,
        onClick: (requestShowInferred: Boolean) -> Unit,
    ) {
        if (hasConfirmed) {
            TooltipIconButton(
                icon = if (showingInferred) {
                    Icons.Default.Visibility
                } else {
                    Icons.Default.VisibilityOff
                },
                tooltipText = stringResource(
                    if (showingInferred) {
                        Res.string.alley_edit_artist_edit_action_hide_inferred
                    } else {
                        Res.string.alley_edit_artist_edit_action_show_inferred
                    }
                ),
                onClick = { onClick(!showingInferred) },
            )
        }
    }

    context(formScope: ArtistFormScope)
    @Composable
    internal fun LinksSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        header: @Composable () -> Unit,
        listRevertDialogState: ListRevertDialogState<LinkModel>,
        items: SnapshotStateList<LinkModel>,
        label: @Composable (() -> Unit)?,
        pendingErrorMessage: () -> String?,
    ) {
        MultiTextSection(
            state = state,
            header = header,
            items = items,
            itemToCommitted = LinkModel::parse,
            removeLastItem = { items.removeLastOrNull()?.link },
            item = { index, value ->
                LinkRow(
                    link = value,
                    isLast = index == items.lastIndex && !state.lockState.editable,
                    color = if (formScope.initialArtist == null ||
                        listRevertDialogState.initialItems.contains(value)
                    ) {
                        Color.Unspecified
                    } else {
                        Color.Green
                    },
                    additionalActions = {
                        EditActions(
                            state = state,
                            items = items,
                            item = value,
                            itemToText = LinkModel::link,
                        )
                    },
                )
            },
            label = label,
            inputTransformation = InputTransformation {
                if (asCharSequence().any { it.isWhitespace() || it == ',' }) {
                    revertAllChanges()
                }
            },
            pendingErrorMessage = pendingErrorMessage,
            additionalHeaderActions = {
                with(formScope) {
                    ShowListRevertIconButton(listRevertDialogState, items)
                }
            },
        )

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
            label = title,
            items = items,
            itemsToText = { it.joinToString { it.link } },
        )
    }

    @Composable
    private fun <T> RowScope.EditActions(
        state: EntryForm2.SingleTextState,
        items: SnapshotStateList<T>,
        item: T,
        itemToText: (T) -> String,
    ) {
        AnimatedVisibility(
            visible = state.lockState.editable,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TooltipIconButton(
                icon = Icons.Default.Edit,
                tooltipText = stringResource(Res.string.alley_edit_row_edit_tooltip),
                onClick = {
                    items.remove(item)
                    state.value.setTextAndPlaceCursorAtEnd(itemToText(item))
                    state.focusRequester.requestFocus()
                },
            )
        }

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
