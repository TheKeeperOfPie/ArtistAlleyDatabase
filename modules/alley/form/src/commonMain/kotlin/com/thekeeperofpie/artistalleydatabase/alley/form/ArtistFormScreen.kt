package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_social_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_field_label_summary
import artistalleydatabase.modules.alley.form.generated.resources.Res
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_confirm_merge
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_save_tooltip
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_submit_private_key
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_booth_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_commissions_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_form_notes_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_instructions_footer
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_instructions_header
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_name_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_notes_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_portfolio_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_social_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_store_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_summary_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_title
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_add_to_calendar_action
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_add_to_calendar_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_thanks_subtitle
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_thanks_title
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_update_action_edit
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_update_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_validation_text
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_error_saving_bad_fields
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_last_response_restored
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_notes
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_previous_year_action_confirm
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_previous_year_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_previous_year_select_all
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_1
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_2
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_link
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_saved_changes
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistInference
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberBoothValidator
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.PreventUnloadEffect
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormScreen.State.ErrorState
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import artistalleydatabase.modules.alley.edit.generated.resources.Res as EditRes

object ArtistFormScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ArtistFormViewModel,
    ) {
        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle(emptyMap())
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle(emptyMap())
        ArtistFormScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesById = { seriesById },
            seriesPredictions = viewModel::seriesPredictions,
            merchById = { merchById },
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickDone = viewModel::onClickDone,
            onConfirmMerge = viewModel::onConfirmMerge,
            onSubmitPrivateKey = viewModel::onSubmitPrivateKey,
            onClickEditAgain = viewModel::onClickEditAgain,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickDone: () -> Unit,
        onConfirmMerge: (Map<ArtistField, Boolean>) -> Unit,
        onSubmitPrivateKey: (String) -> Unit,
        onClickEditAgain: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val saveTaskState = state.saveTaskState
        LaunchedEffect(saveTaskState) {
            snapshotFlow { saveTaskState.lastResult }
                .map { it?.second }
                .filterNotNull()
                .collectLatest {
                    when (it) {
                        is BackendFormRequest.ArtistSave.Response.Failed -> {
                            snackbarHostState.showSnackbar(
                                message = it.errorMessage,
                                withDismissAction = true,
                                duration = SnackbarDuration.Indefinite,
                            )
                            saveTaskState.clearError()
                        }
                        is BackendFormRequest.ArtistSave.Response.Success -> {
                            snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_form_saved_changes),
                                duration = SnackbarDuration.Long,
                            )
                            saveTaskState.clearResult()
                        }
                    }
                }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val errorState = rememberErrorState(state.formState)
        val progress = state.progress.collectAsStateWithLifecycle().value
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                Res.string.alley_form_artist_title,
                                stringResource(dataYear.shortName),
                                state.formState.info.name.value.text,
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        if (progress == State.Progress.LOADED) {
                            val enabled = !errorState.hasAnyError
                            TooltipIconButton(
                                icon = Icons.Default.DoneAll,
                                tooltipText = stringResource(Res.string.alley_form_action_save_tooltip),
                                enabled = enabled,
                                onClick = onClickDone,
                            )
                        }
                    },
                    modifier = Modifier
                        .conditionally(!isExpanded, Modifier.widthIn(max = 960.dp))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            Box(Modifier.padding(scaffoldPadding)) {
                when (progress) {
                    State.Progress.LOADING ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    State.Progress.LOADED -> {
                        val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
                        val initialFormDiff by state.initialFormDiff.collectAsStateWithLifecycle()
                        val previousYearEntry by state.previousYearData.collectAsStateWithLifecycle()
                        Form(
                            saveTaskState = saveTaskState,
                            formState = state.formState,
                            errorState = errorState,
                            initialArtist = { initialArtist },
                            initialFormDiff = { initialFormDiff },
                            previousYearData = { previousYearEntry },
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            onConfirmMerge = onConfirmMerge,
                        )

                        val errorMessage =
                            stringResource(Res.string.alley_form_error_saving_bad_fields)
                        GenericExitDialog(
                            onClickBack = { onClickBack(true) },
                            onClickSave = onClickDone,
                            saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
                        )
                        PreventUnloadEffect()
                    }
                    State.Progress.BAD_AUTH -> PrivateKeyPrompt(dataYear, onSubmitPrivateKey)
                    State.Progress.DONE -> DonePrompt(dataYear, onClickEditAgain)
                }
            }
        }
    }

    @Composable
    private fun Form(
        formState: State.FormState,
        errorState: ErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        initialFormDiff: () -> ArtistEntryDiff?,
        previousYearData: () -> ArtistInference.PreviousYearData?,
        saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onConfirmMerge: (Map<ArtistField, Boolean>) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        ContentSavingBox(
            saving = saveTaskState.isActive && saveTaskState.isManualTrigger,
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Row {
                    val focusState = rememberFocusState(
                        listOfNotNull(
                            formState.info.booth,
                            formState.info.name,
                            formState.info.summary,
                            formState.links.stateSocialLinks,
                            formState.links.stateStoreLinks,
                            formState.links.statePortfolioLinks,
                            formState.links.stateCommissions,
                            formState.series.stateInferred,
                            formState.series.stateConfirmed,
                            formState.merch.stateInferred,
                            formState.merch.stateConfirmed,
                            formState.notes,
                            formState.formNotes,
                        )
                    )

                    var showMerge by rememberSaveable { mutableStateOf(false) }

                    ArtistForm(
                        initialArtist = initialArtist,
                        focusState = focusState,
                        modifier = Modifier.fillMaxHeight()
                            .width(960.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        LastResponseHeader(initialFormDiff()?.timestamp)

                        if (previousYearData() != null) {
                            PreviousYearPrompt(onClickMerge = { showMerge = true })
                        }

                        InstructionsHeader()

                        PasteLinkSection(formState.links)
                        BoothSection(
                            state = formState.info.booth,
                            label = { Text(stringResource(Res.string.alley_form_artist_booth_placeholder)) },
                            errorText = errorState.boothErrorMessage,
                        )
                        NameSection(
                            state = formState.info.name,
                            label = { Text(stringResource(Res.string.alley_form_artist_name_placeholder)) },
                        )
                        SummarySection(
                            state = formState.info.summary,
                            label = { Text(stringResource(Res.string.alley_form_artist_summary_placeholder)) },
                        )
                        SocialLinksSection(
                            state = formState.links.stateSocialLinks,
                            links = formState.links.socialLinks,
                            label = { Text(stringResource(Res.string.alley_form_artist_social_links_placeholder)) },
                            pendingErrorMessage = errorState.socialLinksErrorMessage,
                        )
                        StoreLinksSection(
                            state = formState.links.stateStoreLinks,
                            storeLinks = formState.links.storeLinks,
                            label = { Text(stringResource(Res.string.alley_form_artist_store_links_placeholder)) },
                            pendingErrorMessage = errorState.storeLinksErrorMessage,
                        )
                        PortfolioLinksSection(
                            state = formState.links.statePortfolioLinks,
                            portfolioLinks = formState.links.portfolioLinks,
                            label = { Text(stringResource(Res.string.alley_form_artist_portfolio_links_placeholder)) },
                            pendingErrorMessage = errorState.portfolioLinksErrorMessage,
                        )
                        CommissionsSection(
                            state = formState.links.stateCommissions,
                            commissions = formState.links.commissions,
                            label = { Text(stringResource(Res.string.alley_form_artist_commissions_placeholder)) },
                        )

                        // TODO: Confirmed tag support
                        SeriesSection(
                            state = formState.series,
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            seriesImage = seriesImage,
                            showConfirmed = false,
                        )
                        MerchSection(
                            state = formState.merch,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            showConfirmed = false,
                        )
                        NotesSection(
                            state = formState.notes,
                            initialValue = this@ArtistForm.initialArtist?.notes,
                            label = { Text(stringResource(Res.string.alley_form_artist_notes_placeholder)) },
                        )
                        NotesSection(
                            state = formState.formNotes,
                            initialValue = initialFormDiff()?.notes,
                            header = Res.string.alley_form_notes,
                            label = { Text(stringResource(Res.string.alley_form_artist_form_notes_placeholder)) },
                        )

                        InstructionsFooter()
                    }

                    val previousYearData = previousYearData()
                    if (showMerge && previousYearData != null) {
                        MergeList(
                            previousYearData = previousYearData,
                            onConfirmMerge = {
                                onConfirmMerge(it)
                                showMerge = false
                            },
                        )
                    }
                }
                // TODO: Support images?
            }
        }
    }

    @Composable
    private fun LastResponseHeader(timestamp: Instant?) {
        if (timestamp != null) {
            val dateTimeFormatter = LocalDateTimeFormatter.current
            val dateTimeText = remember(dateTimeFormatter, timestamp) {
                dateTimeFormatter.formatDateTime(timestamp)
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        Res.string.alley_form_last_response_restored,
                        dateTimeText,
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    @Composable
    private fun PrivateKeyPrompt(dataYear: DataYear, onSubmitKey: (String) -> Unit) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.alley_form_private_key_prompt_1))
                        withLink(LinkAnnotation.Url(AlleyUtils.siteUrl)) {
                            append(stringResource(Res.string.alley_form_private_key_prompt_link))
                        }
                        append(
                            stringResource(
                                Res.string.alley_form_private_key_prompt_2,
                                stringResource(dataYear.fullName),
                                AlleyUtils.primaryContactDiscordUsername,
                            )
                        )
                    },
                    style = MaterialTheme.typography.titleMedium,
                )

                // Do not use rememberTextFieldState to avoid key being persisted
                val state = remember { TextFieldState("") }
                OutlinedTextField(state = state, modifier = Modifier.fillMaxWidth())

                // TODO: Show a new error message if the key is incorrect
                FilledTonalButton(onClick = { onSubmitKey(state.text.toString()) }) {
                    Text(stringResource(Res.string.alley_form_action_submit_private_key))
                }
            }
        }
    }

    @Stable
    @Composable
    private fun rememberErrorState(state: State.FormState): ErrorState {
        val boothErrorMessage by rememberBoothValidator(state.info.booth)
        val socialLinksErrorMessage by rememberLinkValidator(state.links.stateSocialLinks)
        val storeLinksErrorMessage by rememberLinkValidator(state.links.stateStoreLinks)
        val portfolioLinksErrorMessage by rememberLinkValidator(state.links.statePortfolioLinks)
        return ErrorState(
            boothErrorMessage = { boothErrorMessage },
            socialLinksErrorMessage = { socialLinksErrorMessage },
            storeLinksErrorMessage = { storeLinksErrorMessage },
            portfolioLinksErrorMessage = { portfolioLinksErrorMessage },
        )
    }

    @Composable
    private fun MergeList(
        previousYearData: ArtistInference.PreviousYearData,
        onConfirmMerge: (Map<ArtistField, Boolean>) -> Unit,
    ) {
        // TODO: There are 3 instances of a similar UI (history, form merge, add artist merge),
        //  can these share more code?
        Column {
            val fieldState = rememberFieldState()
            val groupState = when {
                fieldState.map.values.all { it } -> ToggleableState.On
                fieldState.map.values.any { it } -> ToggleableState.Indeterminate
                else -> ToggleableState.Off
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                TriStateCheckbox(
                    state = groupState,
                    onClick = {
                        val newValue = when (groupState) {
                            ToggleableState.On -> false
                            ToggleableState.Off,
                            ToggleableState.Indeterminate,
                                -> true
                        }
                        fieldState.map.keys.toSet().forEach {
                            fieldState[it] = newValue
                        }
                    },
                )
                Text(stringResource(Res.string.alley_form_previous_year_select_all))
            }

            ArtistField.entries.forEach { field ->
                val fieldText = when (field) {
                    ArtistField.SUMMARY -> previousYearData.summary?.ifBlank { null }
                    ArtistField.SOCIAL_LINKS -> previousYearData.socialLinks.ifEmpty { null }
                        ?.joinToString("\n")
                    ArtistField.STORE_LINKS -> previousYearData.storeLinks.ifEmpty { null }
                        ?.joinToString("\n")
                    ArtistField.SERIES -> previousYearData.seriesInferred.ifEmpty { null }
                        ?.joinToString()
                    ArtistField.MERCH -> previousYearData.merchInferred.ifEmpty { null }
                        ?.joinToString()
                }
                if (fieldText != null) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = fieldState[field],
                                onCheckedChange = { fieldState[field] = it },
                            )
                            Text(stringResource(field.label))
                            if (fieldText.length < 40) {
                                Text(text = fieldText)
                            }
                        }
                        if (fieldText.length >= 40) {
                            Text(text = fieldText, modifier = Modifier.padding(start = 80.dp))
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = { onConfirmMerge(fieldState.map.toMap()) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text(stringResource(Res.string.alley_form_action_confirm_merge))
            }
        }
    }

    @Composable
    private fun PreviousYearPrompt(onClickMerge: () -> Unit) {
        OutlinedCard(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.alley_form_previous_year_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(weight = 1f, fill = false)
                )
                FilledTonalButton(onClick = onClickMerge) {
                    Text(stringResource(Res.string.alley_form_previous_year_action_confirm))
                }
            }
        }
    }

    @Composable
    private fun InstructionsHeader() {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(Res.string.alley_form_artist_instructions_header),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }

    @Composable
    private fun InstructionsFooter() {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Text(
                text = stringResource(Res.string.alley_form_artist_instructions_footer),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }

    @Composable
    private fun DonePrompt(dataYear: DataYear, onClickEditAgain: () -> Unit) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            OutlinedCard(modifier = Modifier.widthIn(max = 600.dp).padding(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = stringResource(Res.string.alley_form_done_thanks_title),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                    Text(
                        text = stringResource(
                            Res.string.alley_form_done_thanks_subtitle,
                            stringResource(dataYear.shortName)
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(
                            Res.string.alley_form_done_validation_text,
                            AlleyUtils.primaryContactDiscordUsername,
                        ),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(Res.string.alley_form_done_add_to_calendar_prompt),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )

                    val conventionName = stringResource(dataYear.fullName)
                    val encryptedFormLink by produceState<String?>(null) {
                        value = FormUtils.generateEncryptedFormLink()
                    }
                    val uriHandler = LocalUriHandler.current
                    FilledTonalButton(
                        enabled = encryptedFormLink != null,
                        onClick = {
                            val link = encryptedFormLink ?: return@FilledTonalButton
                            uriHandler.openUri(
                                FormUtils.generateAddToCalendarLink(
                                    dataYear = dataYear,
                                    conventionName = conventionName,
                                    encryptedFormLink = link,
                                )
                            )
                        },
                    ) {
                        Text(stringResource(Res.string.alley_form_done_add_to_calendar_action))
                    }

                    Text(
                        text = stringResource(Res.string.alley_form_done_update_prompt),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    FilledTonalButton(onClick = onClickEditAgain) {
                        Text(stringResource(Res.string.alley_form_done_update_action_edit))
                    }
                }
            }
        }
    }

    @Stable
    private class FieldState(val map: SnapshotStateMap<ArtistField, Boolean>) {
        operator fun get(field: ArtistField) = map[field] ?: false
        operator fun set(field: ArtistField, checked: Boolean) = map.set(field, checked)
    }

    @Composable
    private fun rememberFieldState(): FieldState {
        val map = rememberSaveable {
            mutableStateMapOf<ArtistField, Boolean>().apply {
                ArtistField.entries.forEach { this[it] = true }
            }
        }
        return remember(map) { FieldState(map) }
    }

    enum class ArtistField(val label: StringResource) {
        SUMMARY(EditRes.string.alley_edit_artist_field_label_summary),
        SOCIAL_LINKS(EditRes.string.alley_edit_artist_field_label_social_links),
        STORE_LINKS(EditRes.string.alley_edit_artist_field_label_store_links),
        SERIES(EditRes.string.alley_edit_artist_field_label_series_inferred),
        MERCH(EditRes.string.alley_edit_artist_field_label_merch_inferred),
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val initialFormDiff: StateFlow<ArtistEntryDiff?>,
        val previousYearData: StateFlow<ArtistInference.PreviousYearData?>,
        val progress: StateFlow<Progress>,
        val formState: FormState,
        val saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
    ) {
        fun applyDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            mergeBehavior: ArtistFormState.MergeBehavior,
        ) = apply {
            formState.info.applyValues(
                booth = artist.booth,
                name = artist.name,
                summary = artist.summary,
                notes = artist.notes,
                mergeBehavior = mergeBehavior,
            )

            formState.links.applyRawValues(
                socialLinks = artist.socialLinks,
                storeLinks = artist.storeLinks,
                portfolioLinks = artist.portfolioLinks,
                catalogLinks = artist.catalogLinks,
                commissions = artist.commissions,
                mergeBehavior = mergeBehavior,
            )

            formState.series.applyValues(
                inferred = artist.seriesInferred,
                confirmed = artist.seriesConfirmed,
                seriesById = seriesById,
                mergeBehavior = mergeBehavior,
            )
            formState.merch.applyValues(
                inferred = artist.merchInferred,
                confirmed = artist.merchConfirmed,
                merchById = merchById,
                mergeBehavior = mergeBehavior,
            )
        }

        fun captureDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
        ): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
            val (booth, name, summary, notes) = formState.info.captureValues()
            val (socialLinks, storeLinks, portfolioLinks, catalogLinks, commissions) = formState.links.captureValues()

            val (seriesInferred, seriesConfirmed) = formState.series.captureValues()
            val (merchInferred, merchConfirmed) = formState.merch.captureValues()

            // TODO: Image support
            val images = emptyList<EditImage>()
            return images to artist.copy(
                booth = booth,
                name = name,
                summary = summary,
                socialLinks = socialLinks,
                storeLinks = storeLinks,
                portfolioLinks = portfolioLinks,
                catalogLinks = catalogLinks,
                notes = notes,
                commissions = commissions,
                seriesInferred = seriesInferred,
                seriesConfirmed = seriesConfirmed,
                merchInferred = merchInferred,
                merchConfirmed = merchConfirmed,
            )
        }

        @Serializable
        enum class Progress {
            LOADING, LOADED, BAD_AUTH, DONE,
        }

        @Stable
        class FormState(
            val info: ArtistFormState.InfoState = ArtistFormState.InfoState(),
            val links: ArtistFormState.LinksState = ArtistFormState.LinksState(),
            val series: ArtistFormState.SeriesState = ArtistFormState.SeriesState(),
            val merch: ArtistFormState.MerchState = ArtistFormState.MerchState(),
            val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val formNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        ) {
            object Saver : ComposeSaver<FormState, List<Any>> {
                override fun SaverScope.save(value: FormState) = listOf(
                    with(ArtistFormState.InfoState.Saver) { save(value.info) },
                    with(ArtistFormState.LinksState.Saver) { save(value.links) },
                    with(ArtistFormState.SeriesState.Saver) { save(value.series) },
                    with(ArtistFormState.MerchState.Saver) { save(value.merch) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.formNotes) },
                )

                @Suppress("UNCHECKED_CAST")
                override fun restore(value: List<Any>) = FormState(
                    info = with(ArtistFormState.InfoState.Saver) { restore(value[0] as List<Any>) },
                    links = with(ArtistFormState.LinksState.Saver) { restore(value[1] as List<Any>) },
                    series = with(ArtistFormState.SeriesState.Saver) { restore(value[2] as List<Any>) },
                    merch = with(ArtistFormState.MerchState.Saver) { restore(value[3] as List<Any>) },
                    notes = with(EntryForm2.SingleTextState.Saver) { restore(value[4]) },
                    formNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[5]) },
                )
            }
        }

        @Stable
        class ErrorState(
            val boothErrorMessage: () -> String?,
            val socialLinksErrorMessage: () -> String?,
            val storeLinksErrorMessage: () -> String?,
            val portfolioLinksErrorMessage: () -> String?,
        ) {
            val hasAnyError by derivedStateOf {
                boothErrorMessage() != null ||
                        socialLinksErrorMessage() != null ||
                        storeLinksErrorMessage() != null ||
                        portfolioLinksErrorMessage() != null
            }
        }
    }
}
