package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.allCaps
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_hide_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_action_show_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_portfolio_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_label
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_paste_link_placeholder
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_on_site_tooltip
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online
import artistalleydatabase.modules.alley.generated.resources.alley_artist_commission_online_tooltip
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.EntryEditMetadata
import com.thekeeperofpie.artistalleydatabase.alley.edit.MetadataSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.BasicMultiTextSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.FormHeaderIconAndTitle
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.LinksSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ListFieldRevertDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.NotesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.RevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.SeriesSection
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ShowRevertIconButton
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.rememberListRevertDialogState
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkCategory
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.category
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.ui.UnrecognizedTagIcon
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.entry.EntryLockState
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils_compose.CustomIcons
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import artistalleydatabase.modules.alley.generated.resources.Res as AlleyRes

@LayoutScopeMarker
@Immutable
interface ArtistFormScope : EntryFormScope {
    val initialArtist: ArtistDatabaseEntry.Impl?

    @Composable
    fun PasteLinkSection(state: ArtistFormState.LinksState)

    @Composable
    fun StatusSection(
        state: EntryForm2.DropdownState,
        metadata: EntryEditMetadata,
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
        metadata: EntryEditMetadata,
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
                FormHeaderIconAndTitle(
                    Icons.Default.TableRestaurant,
                    Res.string.alley_edit_artist_edit_booth
                )
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
                FormHeaderIconAndTitle(Icons.Default.Badge, Res.string.alley_edit_artist_edit_name)
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
                FormHeaderIconAndTitle(
                    CustomIcons.TextAd,
                    Res.string.alley_edit_artist_edit_summary
                )
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
        LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_social_links,
            header = {
                FormHeaderIconAndTitle(
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
        LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_store_links,
            header = {
                FormHeaderIconAndTitle(
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
        LinksSection(
            state = state,
            title = Res.string.alley_edit_artist_edit_portfolio_links,
            header = {
                FormHeaderIconAndTitle(
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
        val listRevertDialogState = rememberListRevertDialogState(initialArtist?.catalogLinks)
        BasicMultiTextSection(
            state = state,
            header = {
                FormHeaderIconAndTitle(
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
            listRevertDialogState = listRevertDialogState,
        )

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
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
        val listRevertDialogState = rememberListRevertDialogState(initialCommissions)
        fun itemToText(model: CommissionModel): String =
            when (model) {
                is CommissionModel.Link -> model.host ?: model.link
                CommissionModel.OnSite -> onSiteText
                CommissionModel.Online -> onlineText
                is CommissionModel.Unknown -> model.value
            }
        BasicMultiTextSection(
            state = state,
            header = {
                FormHeaderIconAndTitle(
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
                }?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                    )
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
            listRevertDialogState = listRevertDialogState,
        )

        ListFieldRevertDialog(
            dialogState = listRevertDialogState,
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

        SeriesSection(
            state = state.stateInferred,
            title = Res.string.alley_edit_artist_edit_series_inferred,
            header = {
                FormHeaderIconAndTitle(
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
            },
        )

        if (showConfirmed) {
            val initialConfirmed = remember(seriesById, initialArtist?.seriesConfirmed) {
                initialArtist?.seriesConfirmed
                    ?.map { seriesById[it] ?: SeriesInfo.fake(it) }
                    .orEmpty()
            }
            val revertDialogStateConfirmed = rememberListRevertDialogState(initialConfirmed)
            SeriesSection(
                state = state.stateConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                header = {
                    FormHeaderIconAndTitle(
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
        val listRevertDialogStateInferred = rememberListRevertDialogState(initialInferred)
        BasicMultiTextSection(
            state = state.stateInferred,
            header = {
                FormHeaderIconAndTitle(
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
            listRevertDialogState = listRevertDialogStateInferred,
            additionalHeaderActions = {
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
            dialogState = listRevertDialogStateInferred,
            label = Res.string.alley_edit_artist_edit_merch_inferred,
            items = state.inferred,
            itemsToText = { it.joinToString { it.name } },
        )

        if (showConfirmed) {
            val initialConfirmed = remember(merchById, initialArtist?.merchConfirmed) {
                initialArtist?.merchConfirmed?.map { merchById[it] ?: MerchInfo.fake(it) }.orEmpty()
            }

            val listRevertDialogStateConfirmed = rememberListRevertDialogState(initialConfirmed)
            BasicMultiTextSection(
                state = state.stateConfirmed,
                header = {
                    FormHeaderIconAndTitle(
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
                listRevertDialogState = listRevertDialogStateConfirmed,
            )

            ListFieldRevertDialog(
                dialogState = listRevertDialogStateConfirmed,
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
        NotesSection(state, revertDialogState, header, label)
    }

    @Composable
    private fun rememberRevertDialogState(initialValue: String?): RevertDialogState {
        val positiveColor = AlleyTheme.colorScheme.positive
        return remember(initialArtist, initialValue) {
            RevertDialogState(positiveColor, initialArtist, initialValue.orEmpty())
        }
    }
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
