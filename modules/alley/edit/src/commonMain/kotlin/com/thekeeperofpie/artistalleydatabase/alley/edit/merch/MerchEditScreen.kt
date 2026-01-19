package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_action_cancel
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_failed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_failed_outdated
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_text
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_edit_delete_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_uuid
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendRequest
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

object MerchEditScreen {

    @Composable
    operator fun invoke(
        merchId: Uuid,
        initialInfo: MerchInfo?,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: MerchEditViewModel = viewModel {
            graph.merchEditViewModelFactory.create(
                merchId = merchId,
                merch = initialInfo,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        MerchEditScreen(
            state = viewModel.state,
            onClickBack = onClickBack,
            onConfirmDelete = viewModel::onConfirmDelete.takeIf { viewModel.initialMerch != null },
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        onClickBack: (force: Boolean) -> Unit,
        onConfirmDelete: (() -> Unit)? = null,
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<BackendRequest.MerchSave.Response>) {
                    when (val result = it.value) {
                        is BackendRequest.MerchSave.Response.Failed ->
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                        is BackendRequest.MerchSave.Response.Outdated -> {
                            // TODO
                        }
                        BackendRequest.MerchSave.Response.Success -> {
                            state.savingState.value = JobProgress.Idle()
                            onClickBack(true)
                        }
                    }
                }
            }
        }

        DeleteProgressEffect(state.deleteProgress, snackbarHostState, onClickBack)

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.id.value.text.toString()) },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
                        if (onConfirmDelete != null) {
                            DeleteButton(onConfirmDelete = onConfirmDelete)
                        }
                        IconButton(onClick = onClickSave) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.alley_edit_merch_action_save_content_description),
                            )
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { scaffoldPadding ->
            val jobProgress by state.savingState.collectAsStateWithLifecycle()
            ContentSavingBox(
                saving = jobProgress is JobProgress.Loading,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
                    EntryForm2(
                        focusState = EntryForm2.rememberFocusState(
                            listOf(state.id, state.uuid, state.notes)
                        ),
                        modifier = Modifier.width(600.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SingleTextSection(
                            state = state.id,
                            headerText = { Text(stringResource(Res.string.alley_edit_merch_header_canonical)) },
                        )

                        val uuidErrorMessage by rememberUuidValidator(state.uuid)
                        SingleTextSection(
                            state = state.uuid,
                            headerText = { Text(stringResource(Res.string.alley_edit_merch_header_uuid)) },
                            errorText = { uuidErrorMessage },
                        )
                        SingleTextSection(
                            state = state.notes,
                            headerText = { Text(stringResource(Res.string.alley_edit_merch_header_notes)) },
                        )
                    }
                }
            }

            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickSave,
            )
        }
    }

    @Composable
    private fun DeleteButton(onConfirmDelete: () -> Unit, modifier: Modifier = Modifier) {
        var showDialog by remember { mutableStateOf(false) }

        IconButton(onClick = { showDialog = true }, modifier = modifier) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(Res.string.alley_edit_merch_edit_delete_action_delete),
            )
        }

        var loading by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(Res.string.alley_edit_merch_edit_delete_title)) },
                text = { Text(stringResource(Res.string.alley_edit_merch_edit_delete_text)) },
                confirmButton = {
                    val countdown by produceState(5) {
                        (4 downTo 0).forEach {
                            delay(1.seconds)
                            value = it
                        }
                    }
                    TextButton(
                        onClick = {
                            if (countdown <= 0) {
                                loading = true
                                onConfirmDelete()
                            }
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val textAlpha by animateFloatAsState(if (countdown <= 0) 1f else 0f)
                            Text(
                                text = stringResource(Res.string.alley_edit_merch_edit_delete_action_confirm),
                                modifier = Modifier.graphicsLayer { alpha = textAlpha }
                            )
                            androidx.compose.animation.AnimatedVisibility(
                                visible = countdown > 0,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Text(countdown.toString())
                            }
                            androidx.compose.animation.AnimatedVisibility(
                                visible = loading,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                CircularWavyProgressIndicator()
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(Res.string.alley_edit_merch_edit_delete_action_cancel))
                    }
                },
            )
        }
    }

    @Composable
    fun DeleteProgressEffect(
        deleteProgress: StateFlow<JobProgress<BackendRequest.MerchDelete.Response>>,
        snackbarHostState: SnackbarHostState,
        onClickBack: (force: Boolean) -> Unit,
    ) {
        LaunchedEffect(deleteProgress) {
            deleteProgress.collectLatest {
                when (it) {
                    is JobProgress.Finished.Result<BackendRequest.MerchDelete.Response> ->
                        when (it.value) {
                            is BackendRequest.MerchDelete.Response.Failed -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_merch_edit_delete_failed)
                            )
                            is BackendRequest.MerchDelete.Response.Outdated -> snackbarHostState.showSnackbar(
                                message = getString(Res.string.alley_edit_merch_edit_delete_failed_outdated)
                            )
                            BackendRequest.MerchDelete.Response.Success -> onClickBack(true)
                        }
                    is JobProgress.Finished.UnhandledError<*> ->
                        snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_merch_edit_delete_failed))
                    is JobProgress.Idle<*>,
                    is JobProgress.Loading<*>,
                        -> Unit
                }
            }
        }
    }

    @Stable
    class State(
        val id: EntryForm2.SingleTextState,
        val uuid: EntryForm2.SingleTextState,
        val notes: EntryForm2.SingleTextState,
        val savingState: MutableStateFlow<JobProgress<BackendRequest.MerchSave.Response>>,
        val deleteProgress: StateFlow<JobProgress<BackendRequest.MerchDelete.Response>>,
    )
}
