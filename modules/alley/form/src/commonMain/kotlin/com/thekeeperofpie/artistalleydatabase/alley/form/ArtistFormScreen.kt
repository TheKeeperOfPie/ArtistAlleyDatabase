package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_stamp_rally_edit_notes
import artistalleydatabase.modules.alley.form.generated.resources.Res
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_done
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_save_tooltip
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_submit_private_key
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_booth_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_commissions_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_form_notes_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_header
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_instructions_footer
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_instructions_header
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_name_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_notes_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_portfolio_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_social_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_store_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_summary_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_title
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_catalog_action_add_images
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_catalog_action_edit_images
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_catalog_header
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_add_to_calendar_action
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_add_to_calendar_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_thanks_subtitle
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_thanks_title
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_update_action_edit
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_update_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_done_validation_text
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_error_saving_bad_fields
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_last_response_restored
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_log_out_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_log_out_prompt_action_log_in
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_log_out_prompt_action_log_out
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_notes
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_previous_year_action_confirm
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_previous_year_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_1
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_2
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt_link
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_saved_changes
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rallies_action_add
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rallies_header
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_action_delete
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_action_restore
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_links_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_name_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_notes_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_prize_limit_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_prize_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_table_min_placeholder
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_stamp_rally_tables_placeholder
import coil3.compose.AsyncImage
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistInferenceField
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.ArtistPreviousYearData
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.inference.MergeArtistPrompt
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberBoothValidator
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.FormMergeBehavior
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallyFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.StampRallySummaryRow
import com.thekeeperofpie.artistalleydatabase.alley.edit.rallies.rememberErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ScrollableSideBySide
import com.thekeeperofpie.artistalleydatabase.alley.edit.utils.PreventUnloadEffect
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormScreen.State.ErrorState
import com.thekeeperofpie.artistalleydatabase.alley.fullName
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageRowActions
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistEntryDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryHorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.alley.ui.theme.AlleyTheme
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ThemeAwareElevatedCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TrailingDropdownIcon
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRequestKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.rememberNavigationRequestKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

object ArtistFormScreen {

    @Composable
    operator fun invoke(
        graph: ArtistAlleyFormGraph,
        dataYear: DataYear,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
        viewModel: ArtistFormViewModel = viewModel {
            graph.artistFormViewModelFactory.create(
                dataYear = dataYear,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
        val seriesById by viewModel.tagAutocomplete.seriesById.collectAsStateWithLifecycle()
        val merchById by viewModel.tagAutocomplete.merchById.collectAsStateWithLifecycle()
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
            onClickEditImages = onClickEditImages,
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
        onConfirmMerge: (Map<ArtistInferenceField, Boolean>) -> Unit,
        onSubmitPrivateKey: (String) -> Unit,
        onClickEditAgain: () -> Unit,
        onClickEditImages: (displayName: String, NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
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
        val errorState = rememberErrorState(state.artistFormState)
        val progress = state.progress.collectAsStateWithLifecycle().value
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                Res.string.alley_form_artist_title,
                                stringResource(dataYear.shortName),
                                state.artistFormState.info.name.value.text,
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
                        val initialRallies by state.initialRallies.collectAsStateWithLifecycle()
                        val initialFormDiff by state.initialFormDiff.collectAsStateWithLifecycle()
                        val previousYearEntry by state.previousYearData.collectAsStateWithLifecycle()
                        Form(
                            dataYear = dataYear,
                            saveTaskState = saveTaskState,
                            state = state,
                            errorState = errorState,
                            initialArtist = { initialArtist },
                            initialRallies = { initialRallies },
                            initialFormDiff = { initialFormDiff },
                            previousYearData = { previousYearEntry },
                            seriesById = seriesById,
                            seriesPredictions = seriesPredictions,
                            merchById = merchById,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            onClickDone = onClickDone,
                            onConfirmMerge = onConfirmMerge,
                            onClickEditImages = onClickEditImages,
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
        dataYear: DataYear,
        state: State,
        errorState: ErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        initialRallies: () -> List<StampRallyDatabaseEntry>,
        initialFormDiff: () -> ArtistEntryDiff?,
        previousYearData: () -> ArtistPreviousYearData?,
        saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickDone: () -> Unit,
        onConfirmMerge: (Map<ArtistInferenceField, Boolean>) -> Unit,
        onClickEditImages: (displayName: String, NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
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
                var showMerge by rememberSaveable { mutableStateOf(false) }

                Row {
                    val mergeList = remember {
                        movableContentOf {
                            previousYearData()?.let {
                                MergeArtistPrompt(
                                    previousYearData = it,
                                    onConfirmMerge = {
                                        onConfirmMerge(it)
                                        showMerge = false
                                    },
                                )
                            }
                        }
                    }

                    val previousYearData = previousYearData()
                    ScrollableSideBySide(
                        showSecondary = { showMerge && previousYearData != null },
                        primary = {
                            Column {
                                LastResponseHeader(initialFormDiff()?.timestamp)

                                if (previousYearData != null) {
                                    PreviousYearPrompt(onClickMerge = { showMerge = true })
                                }

                                InstructionsHeader()

                                CatalogSection(
                                    state = state,
                                    initialArtist = initialArtist,
                                    seriesById = seriesById,
                                    seriesPredictions = seriesPredictions,
                                    merchById = merchById,
                                    merchPredictions = merchPredictions,
                                    seriesImage = seriesImage,
                                    onClickEditImages = onClickEditImages,
                                    onClickImage = {
                                        // TODO: Open full size image?
                                    },
                                )

                                Text(
                                    text = stringResource(Res.string.alley_form_artist_header),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(16.dp)
                                )

                                ArtistForm(
                                    formState = state.artistFormState,
                                    errorState = errorState,
                                    initialArtist = initialArtist,
                                    initialFormDiff = initialFormDiff,
                                    seriesById = seriesById,
                                    seriesPredictions = seriesPredictions,
                                    merchById = merchById,
                                    merchPredictions = merchPredictions,
                                    seriesImage = seriesImage,
                                )

                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider()

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.alley_form_stamp_rallies_header),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    FilledTonalButton(
                                        onClick = {
                                            state.stampRallyStates += newStampRallyFormState(state)
                                        },
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Text(stringResource(Res.string.alley_form_stamp_rallies_action_add))
                                        }
                                    }
                                }

                                StampRallyForms(
                                    dataYear = dataYear,
                                    state = state,
                                    initialRallies = initialRallies,
                                    seriesById = seriesById,
                                    seriesPredictions = seriesPredictions,
                                    merchById = merchById,
                                    merchPredictions = merchPredictions,
                                    seriesImage = seriesImage,
                                    onClickEditImages = onClickEditImages,
                                    onClickImage = {
                                        // TODO: Open full size image?
                                    },
                                )

                                if (state.stampRallyStates.isNotEmpty()) {
                                    Spacer(Modifier.height(16.dp))
                                }

                                HorizontalDivider()
                                Spacer(Modifier.height(16.dp))

                                FilledTonalButton(
                                    onClick = onClickDone,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(stringResource(Res.string.alley_form_action_done))
                                }
                            }
                        },
                        secondary = { mergeList() },
                        secondaryExpanded = { mergeList() },
                    )
                }
            }
        }
    }

    private fun newStampRallyFormState(state: State) = StampRallyFormState().apply {
        val booth =
            state.artistFormState.info.booth.value.text.toString()
        if (booth.isNotBlank()) {
            tables += booth
        }
    }

    @Composable
    private fun ArtistForm(
        formState: State.FormState,
        errorState: ErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        initialFormDiff: () -> ArtistEntryDiff?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
    ) {
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

        ArtistForm(
            initialArtist = initialArtist,
            focusState = focusState,
        ) {
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
            Spacer(Modifier.height(12.dp))
            PasteLinkSection(formState.links)
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
    }

    @Composable
    private fun StampRallyForms(
        dataYear: DataYear,
        state: State,
        initialRallies: () -> List<StampRallyDatabaseEntry>,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickEditImages: (displayName: String, NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
        onClickImage: (EditImage) -> Unit,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            state.stampRallyStates.forEach { formState ->
                var expanded by rememberSaveable { mutableStateOf(false) }
                ThemeAwareElevatedCard {
                    val errorState = key(formState) { rememberErrorState(formState) }
                    val focusState = key(formState) {
                        rememberFocusState(
                            listOf(
                                formState.fandom,
                                formState.stateTables,
                                formState.stateLinks,
                                formState.tableMin,
                                formState.prize,
                                formState.prizeLimit,
                                formState.stateSeries,
                                formState.stateMerch,
                                formState.notes,
                            )
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { expanded = !expanded }
                        ) {
                            val edited by remember {
                                derivedStateOf {
                                    // TODO: Is this too expensive?
                                    val before = initialRallies().find {
                                        it.id == formState.editorState.id.value.text.toString()
                                    }
                                    val after = formState.captureDatabaseEntry(dataYear).second
                                    StampRallyDatabaseEntry.hasChanged(before, after)
                                }
                            }
                            CompositionLocalProvider(
                                LocalContentColor provides when {
                                    formState.editorState.deleted -> AlleyTheme.colorScheme.negative
                                    edited -> AlleyTheme.colorScheme.positive
                                    else -> LocalContentColor.current
                                }
                            ) {
                                val booth =
                                    formState.tables.toList().firstOrNull()?.ifBlank { null }
                                        ?: formState.stateTables.value.text.toString()
                                StampRallySummaryRow(
                                    stampRallyId = formState.editorState.id.value.text.toString(),
                                    fandom = formState.fandom.value.text.toString(),
                                    hostTable = booth,
                                    series = formState.series.map { it.id }
                                        .plus(
                                            formState.stateSeries.value.text.toString()
                                                .ifBlank { null })
                                        .filterNotNull(),
                                    seriesById = seriesById,
                                    modifier = Modifier.weight(1f)
                                        .alpha(if (formState.editorState.deleted) 0.35f else 1f)
                                )
                            }

                            TooltipIconButton(
                                icon = if (formState.editorState.deleted) {
                                    Icons.Default.RestoreFromTrash
                                } else {
                                    Icons.Default.Delete
                                },
                                tooltipText = stringResource(
                                    if (formState.editorState.deleted) {
                                        Res.string.alley_form_stamp_rally_action_restore
                                    } else {
                                        Res.string.alley_form_stamp_rally_action_delete
                                    }
                                ),
                                onClick = {
                                    if (formState.editorState.deleted) {
                                        formState.editorState.deleted = false
                                    } else {
                                        val (images, entry) =
                                            formState.captureDatabaseEntry(dataYear)
                                        // If deleting an empty rally, just remove from list
                                        if (images.isEmpty() &&
                                            entry == newStampRallyFormState(state)
                                                .captureDatabaseEntry(dataYear)
                                                .second
                                        ) {
                                            state.stampRallyStates -= formState
                                        } else {
                                            formState.editorState.deleted = true
                                        }
                                    }
                                },
                            )

                            if (!formState.editorState.deleted) {
                                TrailingDropdownIcon(
                                    expanded = expanded,
                                    contentDescription = null,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        if (!formState.editorState.deleted && expanded) {
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            StampRallyForm(
                                state = formState,
                                errorState = errorState,
                                focusState = focusState,
                                initialStampRally = {
                                    initialRallies().find {
                                        it.id == formState.editorState.id.value.text.toString()
                                    }
                                },
                                seriesById = seriesById,
                                seriesPredictions = seriesPredictions,
                                merchById = merchById,
                                merchPredictions = merchPredictions,
                                seriesImage = seriesImage,
                            ) {
                                val requestKey =
                                    rememberNavigationRequestKey(ImagesEditScreen.REQUEST_KEY)
                                ImageSection(
                                    images = formState.images,
                                    requestKey = requestKey,
                                    onClickEditImages = { requestKey, images ->
                                        onClickEditImages(
                                            formState.fandom.value.text.toString(),
                                            requestKey,
                                            images,
                                        )
                                    },
                                    onClickImage = onClickImage,
                                )
                                FandomSection(
                                    state = formState.fandom,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_name_placeholder)) },
                                )
                                TablesSection(
                                    state = formState.stateTables,
                                    tables = formState.tables,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_tables_placeholder)) },
                                )
                                LinksSection(
                                    state = formState.stateLinks,
                                    links = formState.links,
                                    pendingErrorMessage = errorState.linksErrorMessage,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_links_placeholder)) },
                                )
                                TableMinSection(
                                    state = formState.tableMin,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_table_min_placeholder)) },
                                )
                                PrizeSection(
                                    state = formState.prize,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_prize_placeholder)) },
                                )
                                PrizeLimitSection(
                                    state = formState.prizeLimit,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_prize_limit_placeholder)) },
                                )
                                SeriesSection(
                                    state = formState.stateSeries,
                                    series = formState.series,
                                    seriesById = seriesById,
                                    seriesPredictions = seriesPredictions,
                                    seriesImage = seriesImage,
                                )
                                MerchSection(
                                    formState.stateMerch,
                                    formState.merch,
                                    merchById,
                                    merchPredictions
                                )
                                NotesSection(
                                    state = formState.notes,
                                    initialValue = this.initialStampRally?.notes,
                                    header = artistalleydatabase.modules.alley.edit.generated.resources.Res.string.alley_edit_stamp_rally_edit_notes,
                                    label = { Text(stringResource(Res.string.alley_form_stamp_rally_notes_placeholder)) },
                                )
                            }
                        }
                    }
                }
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            val scrollState = rememberScrollState()
            val scrollAreaState = rememberScrollAreaState(scrollState)
            OutlinedCard(modifier = Modifier.widthIn(max = 600.dp)) {
                ScrollArea(state = scrollAreaState) {
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.verticalScroll(scrollState)
                                .padding(start = 24.dp, end = 48.dp, top = 24.dp, bottom = 24.dp)
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

                            Spacer(Modifier.height(24.dp))

                            Text(
                                text = stringResource(Res.string.alley_form_log_out_prompt),
                                style = MaterialTheme.typography.titleMedium,
                            )

                            val uriHandler = LocalUriHandler.current
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                FilledTonalButton(onClick = { uriHandler.openUri(AlleyUtils.logOutUrl) }) {
                                    Text(stringResource(Res.string.alley_form_log_out_prompt_action_log_out))
                                }

                                FilledTonalButton(onClick = { uriHandler.openUri(AlleyUtils.formLogInUrl) }) {
                                    Text(stringResource(Res.string.alley_form_log_out_prompt_action_log_in))
                                }
                            }
                        }
                        Box(modifier = Modifier.matchParentSize()) {
                            PrimaryVerticalScrollbar(
                                modifier = Modifier.align(Alignment.TopEnd)
                            )
                        }
                    }
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
    private fun PreviousYearPrompt(onClickMerge: () -> Unit) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.alley_form_previous_year_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(weight = 1f)
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
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            OutlinedCard(modifier = Modifier.widthIn(max = 600.dp)) {
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

    @Composable
    private fun CatalogSection(
        state: State,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        seriesById: () -> Map<String, SeriesInfo>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchById: () -> Map<String, MerchInfo>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickEditImages: (displayName: String, NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
        onClickImage: (EditImage) -> Unit,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.alley_form_catalog_header),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            val requestKey = rememberNavigationRequestKey(ImagesEditScreen.REQUEST_KEY)
            ImageSection(
                images = state.artistFormState.images,
                requestKey = requestKey,
                onClickEditImages = { requestKey, images ->
                    onClickEditImages(
                        state.artistFormState.info.booth.value.text.toString(),
                        requestKey,
                        images,
                    )
                },
                onClickImage = onClickImage,
            )

            val focusState = rememberFocusState(
                listOf(
                    state.artistFormState.series.stateConfirmed,
                    state.artistFormState.merch.stateConfirmed,
                )
            )

            ArtistForm(
                initialArtist = initialArtist,
                focusState = focusState,
            ) {
                SeriesConfirmedSection(
                    state = state.artistFormState.series.stateConfirmed,
                    confirmed = state.artistFormState.series.confirmed,
                    seriesById = seriesById,
                    seriesPredictions = seriesPredictions,
                    seriesImage = seriesImage,
                )
                MerchConfirmedSection(
                    state = state.artistFormState.merch.stateConfirmed,
                    confirmed = state.artistFormState.merch.confirmed,
                    merchById = merchById,
                    merchPredictions = merchPredictions,
                )
            }
        }
    }

    @Composable
    private fun ImageSection(
        images: SnapshotStateList<EditImage>,
        requestKey: NavigationRequestKey<List<EditImage>>,
        onClickEditImages: (NavigationRequestKey<List<EditImage>>, List<EditImage>) -> Unit,
        onClickImage: (EditImage) -> Unit,
    ) {
        val listState = rememberLazyListState()
        val scrollAreaState = rememberScrollAreaState(listState)
        ScrollArea(state = scrollAreaState) {
            NavigationResultEffect(requestKey) {
                images.replaceAll(it)
            }

            Column {
                Box {
                    LazyRow(
                        state = listState,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.conditionally(
                            images.isNotEmpty(),
                            Modifier.height(200.dp)
                        )
                    ) {
                        items(items = images, key = { it.coilImageModel.toString() }) {
                            val imageWidth = it.width
                            val imageHeight = it.height
                            val width = if (imageWidth == null || imageHeight == null) {
                                null
                            } else {
                                200.dp * (imageWidth / imageHeight)
                            }
                            AsyncImage(
                                model = it.coilImageModel,
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier
                                    .conditionallyNonNull(width) { width(it) }
                                    .height(200.dp)
                                    .clickable { onClickImage(it) }
                            )
                        }
                        item {
                            FilledTonalButton(
                                onClick = { onClickEditImages(requestKey, images.toList()) },
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Text(
                                    stringResource(
                                        if (images.isEmpty()) {
                                            Res.string.alley_form_catalog_action_add_images
                                        } else {
                                            Res.string.alley_form_catalog_action_edit_images
                                        }
                                    )
                                )
                            }
                        }
                    }
                    ImageRowActions(listState)
                }

                PrimaryHorizontalScrollbar(
                    modifier = Modifier
                        .graphicsLayer {
                            val scrollIndicatorState = listState.scrollIndicatorState
                            alpha = if (scrollIndicatorState == null ||
                                scrollIndicatorState.contentSize <= scrollIndicatorState.viewportSize
                            ) {
                                0f
                            } else {
                                1f
                            }
                        }
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val initialRallies: StateFlow<List<StampRallyDatabaseEntry>>,
        val initialFormDiff: StateFlow<ArtistEntryDiff?>,
        val previousYearData: StateFlow<ArtistPreviousYearData?>,
        val progress: StateFlow<Progress>,
        val stampRallyStates: SnapshotStateList<StampRallyFormState>,
        val artistFormState: FormState,
        val saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
    ) {
        fun applyDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            mergeBehavior: FormMergeBehavior,
        ) = apply {
            artistFormState.info.applyValues(
                booth = artist.booth,
                name = artist.name,
                summary = artist.summary,
                notes = artist.notes,
                mergeBehavior = mergeBehavior,
            )

            artistFormState.links.applyRawValues(
                socialLinks = artist.socialLinks,
                storeLinks = artist.storeLinks,
                portfolioLinks = artist.portfolioLinks,
                catalogLinks = artist.catalogLinks,
                commissions = artist.commissions,
                mergeBehavior = mergeBehavior,
            )

            artistFormState.series.applyValues(
                inferred = artist.seriesInferred,
                confirmed = artist.seriesConfirmed,
                seriesById = seriesById,
                mergeBehavior = mergeBehavior,
            )
            artistFormState.merch.applyValues(
                inferred = artist.merchInferred,
                confirmed = artist.merchConfirmed,
                merchById = merchById,
                mergeBehavior = mergeBehavior,
            )
        }

        fun captureDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
        ): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
            val (booth, name, summary, notes) = artistFormState.info.captureValues()
            val (socialLinks, storeLinks, portfolioLinks, catalogLinks, commissions) = artistFormState.links.captureValues()

            val (seriesInferred, seriesConfirmed) = artistFormState.series.captureValues()
            val (merchInferred, merchConfirmed) = artistFormState.merch.captureValues()

            val images = artistFormState.images.toList()
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
            val images: SnapshotStateList<EditImage> = SnapshotStateList(),
            val info: ArtistFormState.InfoState = ArtistFormState.InfoState(),
            val links: ArtistFormState.LinksState = ArtistFormState.LinksState(),
            val series: ArtistFormState.SeriesState = ArtistFormState.SeriesState(),
            val merch: ArtistFormState.MerchState = ArtistFormState.MerchState(),
            val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val formNotes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        ) {
            object Saver : ComposeSaver<FormState, List<Any>> {
                override fun SaverScope.save(value: FormState) = listOf(
                    with(StateUtils.snapshotListJsonSaver<EditImage>()) { save(value.images) },
                    with(ArtistFormState.InfoState.Saver) { save(value.info) },
                    with(ArtistFormState.LinksState.Saver) { save(value.links) },
                    with(ArtistFormState.SeriesState.Saver) { save(value.series) },
                    with(ArtistFormState.MerchState.Saver) { save(value.merch) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.formNotes) },
                )

                @Suppress("UNCHECKED_CAST")
                override fun restore(value: List<Any>) = FormState(
                    images = with(StateUtils.snapshotListJsonSaver<EditImage>()) { restore(value[0] as String) },
                    info = with(ArtistFormState.InfoState.Saver) { restore(value[1] as List<Any>) },
                    links = with(ArtistFormState.LinksState.Saver) { restore(value[2] as List<Any>) },
                    series = with(ArtistFormState.SeriesState.Saver) { restore(value[3] as List<Any>) },
                    merch = with(ArtistFormState.MerchState.Saver) { restore(value[4] as List<Any>) },
                    notes = with(EntryForm2.SingleTextState.Saver) { restore(value[5]) },
                    formNotes = with(EntryForm2.SingleTextState.Saver) { restore(value[6]) },
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
