package com.thekeeperofpie.artistalleydatabase.alley.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.form.generated.resources.Res
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_save_tooltip
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_action_submit_private_key
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_artist_title
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_error_saving_bad_fields
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_notes
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_private_key_prompt
import artistalleydatabase.modules.alley.form.generated.resources.alley_form_saved_changes
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberBoothValidator
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.form.ArtistFormScreen.State.ErrorState
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
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
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

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
        ArtistFormScreen(
            dataYear = dataYear,
            state = viewModel.state,
            seriesPredictions = viewModel::seriesPredictions,
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickDone = viewModel::onClickDone,
            onSubmitPrivateKey = viewModel::onSubmitPrivateKey,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickDone: () -> Unit,
        onSubmitPrivateKey: (String) -> Unit,
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
                        val enabled = !errorState.hasAnyError
                        TooltipIconButton(
                            icon = Icons.Default.DoneAll,
                            tooltipText = stringResource(Res.string.alley_form_action_save_tooltip),
                            enabled = enabled,
                            onClick = onClickDone,
                        )
                    },
                    modifier = Modifier
                        .conditionally(!isExpanded, Modifier.widthIn(max = 960.dp))
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxWidth()
        ) { scaffoldPadding ->
            Box(Modifier.padding(scaffoldPadding)) {
                when (state.progress.collectAsStateWithLifecycle().value) {
                    State.Progress.LOADING ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularWavyProgressIndicator()
                        }
                    State.Progress.LOADED -> {
                        val initialArtist by state.initialArtist.collectAsStateWithLifecycle()
                        Form(
                            saveTaskState = saveTaskState,
                            formState = state.formState,
                            errorState = errorState,
                            initialArtist = { initialArtist },
                            seriesPredictions = seriesPredictions,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            modifier = Modifier.padding(scaffoldPadding),
                        )

                        val errorMessage =
                            stringResource(Res.string.alley_form_error_saving_bad_fields)
                        GenericExitDialog(
                            onClickBack = { onClickBack(true) },
                            onClickSave = onClickDone,
                            saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
                        )
                    }
                    State.Progress.BAD_AUTH -> PrivateKeyPrompt(onSubmitPrivateKey)
                }
            }
        }
    }

    @Composable
    private fun Form(
        formState: State.FormState,
        errorState: ErrorState,
        initialArtist: () -> ArtistDatabaseEntry.Impl?,
        saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
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
                val focusState = rememberFocusState(
                    listOfNotNull(
                        formState.info.booth,
                        formState.info.name,
                        formState.info.summary,
                        formState.links.stateLinks,
                        formState.links.stateStoreLinks,
                        formState.links.stateCatalogLinks,
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
                    modifier = Modifier.fillMaxHeight()
                        .width(960.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    PasteLinkSection(formState.links)
                    InfoSections(
                        state = formState.info,
                        boothErrorMessage = errorState.boothErrorMessage
                    )
                    LinkSections(
                        state = formState.links,
                        linksErrorMessage = errorState.linksErrorMessage,
                        storeLinksErrorMessage = errorState.storeLinksErrorMessage,
                        catalogLinksErrorMessage = errorState.catalogLinksErrorMessage,
                    )

                    // TODO: Confirmed tag support
                    SeriesSection(
                        state = formState.series,
                        seriesPredictions = seriesPredictions,
                        seriesImage = seriesImage,
                        showConfirmed = false,
                        allowCustomInput = true,
                    )
                    MerchSection(
                        state = formState.merch,
                        merchPredictions = merchPredictions,
                        showConfirmed = false,
                        allowCustomInput = true,
                    )
                    NotesSection(formState.notes)
                    NotesSection(
                        state = formState.formNotes,
                        headerText = {
                            Text(stringResource(Res.string.alley_form_notes))
                        },
                    )
                }
                // TODO: Support images?
            }
        }
    }

    @Composable
    private fun PrivateKeyPrompt(onSubmitKey: (String) -> Unit) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .widthIn(max = 960.dp)
            ) {
                Text(
                    text = stringResource(Res.string.alley_form_private_key_prompt),
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
    fun rememberErrorState(state: State.FormState): ErrorState {
        val boothErrorMessage by rememberBoothValidator(state.info.booth)
        val linksErrorMessage by rememberLinkValidator(state.links.stateLinks)
        val storeLinksErrorMessage by rememberLinkValidator(state.links.stateStoreLinks)
        val catalogLinksErrorMessage by rememberLinkValidator(state.links.stateCatalogLinks)
        return ErrorState(
            boothErrorMessage = { boothErrorMessage },
            linksErrorMessage = { linksErrorMessage },
            storeLinksErrorMessage = { storeLinksErrorMessage },
            catalogLinksErrorMessage = { catalogLinksErrorMessage },
        )
    }

    @Stable
    class State(
        val initialArtist: StateFlow<ArtistDatabaseEntry.Impl?>,
        val progress: StateFlow<Progress>,
        val formState: FormState,
        val saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
    ) {
        fun applyDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            force: Boolean,
        ) = apply {
            formState.info.applyValues(
                booth = artist.booth,
                name = artist.name,
                summary = artist.summary,
                notes = artist.notes,
                force = force,
            )

            formState.links.applyRawValues(
                links = artist.links,
                storeLinks = artist.storeLinks,
                catalogLinks = artist.catalogLinks,
                commissions = artist.commissions,
                force = force,
            )

            formState.series.applyValues(
                inferred = artist.seriesInferred,
                confirmed = artist.seriesConfirmed,
                seriesById = seriesById,
                force = force,
            )
            formState.merch.applyValues(
                inferred = artist.merchInferred,
                confirmed = artist.merchConfirmed,
                merchById = merchById,
                force = force,
            )
        }

        fun captureDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
        ): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
            val (booth, name, summary, notes) = formState.info.captureValues()
            val (links, storeLinks, catalogLinks, commissions) = formState.links.captureValues()

            val (seriesInferred, seriesConfirmed) = formState.series.captureValues()
            val (merchInferred, merchConfirmed) = formState.merch.captureValues()

            // TODO: Image support
            val images = emptyList<EditImage>()
            return images to artist.copy(
                booth = booth,
                name = name,
                summary = summary,
                links = links,
                storeLinks = storeLinks,
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
            LOADING, LOADED, BAD_AUTH,
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
            val linksErrorMessage: () -> String?,
            val storeLinksErrorMessage: () -> String?,
            val catalogLinksErrorMessage: () -> String?,
        ) {
            val hasAnyError by derivedStateOf {
                boothErrorMessage() != null ||
                        linksErrorMessage() != null ||
                        storeLinksErrorMessage() != null ||
                        catalogLinksErrorMessage() != null
            }
        }
    }
}
