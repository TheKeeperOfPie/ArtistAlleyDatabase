package com.thekeeperofpie.artistalleydatabase.alley.edit.form

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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_action_save_tooltip
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_action_submit_private_key
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_private_key_prompt
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_form_saved_changes
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistForm
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.ArtistFormState
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberBoothValidator
import com.thekeeperofpie.artistalleydatabase.alley.edit.form.ArtistFormScreen.State.ErrorState
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.links.CommissionModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistDatabaseEntry
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendFormRequest
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.TooltipIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2.rememberFocusState
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TaskState
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.StateUtils
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
                                message = getString(Res.string.alley_edit_artist_form_saved_changes),
                                duration = SnackbarDuration.Long,
                            )
                            saveTaskState.clearResult()
                        }
                    }
                }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val errorState = rememberErrorState(state.textState)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(
                                Res.string.alley_edit_artist_edit_title_editing,
                                stringResource(dataYear.shortName),
                                state.textState.name.value.text,
                            )
                        )
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        val enabled = !errorState.hasAnyError
                        TooltipIconButton(
                            icon = Icons.Default.DoneAll,
                            tooltipText = stringResource(Res.string.alley_edit_artist_form_action_save_tooltip),
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
                        Form(
                            saveTaskState = saveTaskState,
                            formState = state.formState,
                            textState = state.textState,
                            errorState = errorState,
                            seriesPredictions = seriesPredictions,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            modifier = Modifier.padding(scaffoldPadding),
                        )

                        val errorMessage =
                            stringResource(Res.string.alley_edit_artist_error_saving_bad_fields)
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
        textState: State.TextState,
        errorState: State.ErrorState,
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
                        textState.booth,
                        textState.name,
                        textState.summary,
                        textState.links,
                        textState.storeLinks,
                        textState.catalogLinks,
                        textState.commissions,
                        formState.series.stateInferred,
                        formState.series.stateConfirmed,
                        formState.merch.stateInferred,
                        formState.merch.stateConfirmed,
                        textState.notes,
                    )
                )

                ArtistForm(
                    focusState = focusState,
                    modifier = Modifier.fillMaxHeight()
                        .width(960.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    PasteLinkSection(
                        links = formState.links,
                        storeLinks = formState.storeLinks,
                        commissions = formState.commissions,
                    )
                    BoothSection(state = textState.booth, errorText = errorState.boothErrorMessage)
                    NameSection(textState.name)
                    SummarySection(textState.summary)
                    LinksSection(
                        state = textState.links,
                        links = formState.links,
                        pendingErrorMessage = errorState.linksErrorMessage,
                    )
                    StoreLinksSection(
                        state = textState.storeLinks,
                        storeLinks = formState.storeLinks,
                        pendingErrorMessage = errorState.storeLinksErrorMessage,
                    )
                    CatalogLinksSection(
                        state = textState.catalogLinks,
                        catalogLinks = formState.catalogLinks,
                        pendingErrorMessage = errorState.catalogLinksErrorMessage,
                    )
                    CommissionsSection(textState.commissions, formState.commissions)
                    SeriesSection(
                        formState.series,
                        seriesPredictions = seriesPredictions,
                        seriesImage = seriesImage,
                    )
                    MerchSection(
                        formState.merch,
                        merchPredictions = merchPredictions,
                    )
                    NotesSection(textState.notes)
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
                    text = stringResource(Res.string.alley_edit_artist_form_private_key_prompt),
                    style = MaterialTheme.typography.titleMedium,
                )

                // Do not use rememberTextFieldState to avoid key being persisted
                val state = remember { TextFieldState("") }
                OutlinedTextField(state = state, modifier = Modifier.fillMaxWidth())

                // TODO: Show a new error message if the key is incorrect
                FilledTonalButton(onClick = { onSubmitKey(state.text.toString()) }) {
                    Text(stringResource(Res.string.alley_edit_artist_form_action_submit_private_key))
                }
            }
        }
    }

    @Stable
    @Composable
    fun rememberErrorState(state: State.TextState): ErrorState {
        val boothErrorMessage by rememberBoothValidator(state.booth)
        val linksErrorMessage by rememberLinkValidator(state.links)
        val storeLinksErrorMessage by rememberLinkValidator(state.storeLinks)
        val catalogLinksErrorMessage by rememberLinkValidator(state.catalogLinks)
        return ErrorState(
            boothErrorMessage = { boothErrorMessage },
            linksErrorMessage = { linksErrorMessage },
            storeLinksErrorMessage = { storeLinksErrorMessage },
            catalogLinksErrorMessage = { catalogLinksErrorMessage },
        )
    }

    @Stable
    class State(
        val progress: StateFlow<Progress>,
        val formState: FormState,
        val textState: TextState,
        val saveTaskState: TaskState<BackendFormRequest.ArtistSave.Response>,
    ) {
        fun applyDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
            seriesById: Map<String, SeriesInfo>,
            merchById: Map<String, MerchInfo>,
            force: Boolean,
        ) = apply {
            val links = artist.links.map(LinkModel.Companion::parse).sortedBy { it.logo }
            val storeLinks =
                artist.storeLinks.map(LinkModel.Companion::parse).sortedBy { it.logo }
            val commissions = artist.commissions.map(CommissionModel.Companion::parse)

            val seriesInferred =
                artist.seriesInferred.map { seriesById[it] ?: SeriesInfo.fake(it) }
            val seriesConfirmed =
                artist.seriesConfirmed.map { seriesById[it] ?: SeriesInfo.fake(it) }
            val merchInferred =
                artist.merchInferred.map { merchById[it] ?: MerchInfo.fake(it) }
            val merchConfirmed =
                artist.merchConfirmed.map { merchById[it] ?: MerchInfo.fake(it) }

            ArtistFormState.applyValue(textState.booth, artist.booth, force)
            ArtistFormState.applyValue(textState.name, artist.name, force)
            ArtistFormState.applyValue(textState.summary, artist.summary, force)
            ArtistFormState.applyValue(textState.notes, artist.notes, force)

            ArtistFormState.applyValue(textState.links, formState.links, links, force)
            ArtistFormState.applyValue(
                state = textState.storeLinks,
                list = formState.storeLinks,
                value = storeLinks,
                force = force,
            )
            ArtistFormState.applyValue(
                state = textState.catalogLinks,
                list = formState.catalogLinks,
                value = artist.catalogLinks,
                force = force,
            )
            ArtistFormState.applyValue(
                state = textState.commissions,
                list = formState.commissions,
                value = commissions,
                force = force,
            )

            formState.series.applyValue(
                inferred = seriesInferred,
                confirmed = seriesConfirmed,
                force = force,
            )
            formState.merch.applyValue(
                inferred = merchInferred,
                confirmed = merchConfirmed,
                force = force,
            )
        }

        fun captureDatabaseEntry(
            artist: ArtistDatabaseEntry.Impl,
        ): Pair<List<EditImage>, ArtistDatabaseEntry.Impl> {
            val booth = textState.booth.value.text.toString()
            val name = textState.name.value.text.toString()
            val summary = textState.summary.value.text.toString()

            // TODO: Include pending value?
            val links = formState.links.toList().map { it.link }
                .plus(textState.links.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            val storeLinks = formState.storeLinks.toList().map { it.link }
                .plus(textState.storeLinks.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()
            val catalogLinks = formState.catalogLinks.toList()
                .plus(textState.catalogLinks.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()

            val notes = textState.notes.value.text.toString()
            val commissions = formState.commissions.toList().map { it.serializedValue }
                .plus(textState.commissions.value.text.toString().takeIf { it.isNotBlank() })
                .filterNotNull()
                .distinct()

            val (seriesInferred, seriesConfirmed) = formState.series.captureInferredAndConfirmed()
            val (merchInferred, merchConfirmed) = formState.merch.captureInferredAndConfirmed()

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
            val links: SnapshotStateList<LinkModel> = SnapshotStateList(),
            val storeLinks: SnapshotStateList<LinkModel> = SnapshotStateList(),
            val catalogLinks: SnapshotStateList<String> = SnapshotStateList(),
            val commissions: SnapshotStateList<CommissionModel> = SnapshotStateList(),
            val series: ArtistFormState.SeriesState = ArtistFormState.SeriesState(),
            val merch: ArtistFormState.MerchState = ArtistFormState.MerchState(),
        ) {
            object Saver : ComposeSaver<FormState, List<Any?>> {
                override fun SaverScope.save(value: FormState) = listOf(
                    with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.links) },
                    with(StateUtils.snapshotListJsonSaver<LinkModel>()) { save(value.storeLinks) },
                    with(StateUtils.snapshotListJsonSaver<String>()) { save(value.catalogLinks) },
                    with(StateUtils.snapshotListJsonSaver<CommissionModel>()) { save(value.commissions) },
                    with(ArtistFormState.SeriesState.Saver) { save(value.series) },
                    with(ArtistFormState.MerchState.Saver) { save(value.merch) },
                )

                override fun restore(value: List<Any?>) = FormState(
                    links = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[0] as String) }!!,
                    storeLinks = with(StateUtils.snapshotListJsonSaver<LinkModel>()) { restore(value[1] as String) }!!,
                    catalogLinks = with(StateUtils.snapshotListJsonSaver<String>()) { restore(value[2] as String) }!!,
                    commissions = with(StateUtils.snapshotListJsonSaver<CommissionModel>()) {
                        restore(
                            value[3] as String
                        )
                    }!!,
                    series = with(ArtistFormState.SeriesState.Saver) { restore(value[4] as List<Any?>) },
                    merch = with(ArtistFormState.MerchState.Saver) { restore(value[5] as List<Any?>) },
                )
            }
        }

        @Stable
        class TextState(
            val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val links: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val storeLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val catalogLinks: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val commissions: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val notes: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
        ) {
            object Saver : ComposeSaver<TextState, List<Any>> {
                override fun SaverScope.save(value: TextState) = listOf(
                    with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.links) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.storeLinks) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.catalogLinks) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.commissions) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.notes) },
                )

                override fun restore(value: List<Any>) = TextState(
                    booth = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                    name = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                    summary = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                    links = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                    storeLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[4]) },
                    catalogLinks = with(EntryForm2.SingleTextState.Saver) { restore(value[5]) },
                    commissions = with(EntryForm2.SingleTextState.Saver) { restore(value[6]) },
                    notes = with(EntryForm2.SingleTextState.Saver) { restore(value[7]) },
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
