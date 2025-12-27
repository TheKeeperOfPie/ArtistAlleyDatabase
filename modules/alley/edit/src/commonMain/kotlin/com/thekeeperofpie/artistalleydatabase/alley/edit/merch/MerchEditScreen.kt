package com.thekeeperofpie.artistalleydatabase.alley.edit.merch

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_canonical
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_merch_header_uuid
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.MerchSave
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
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
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        state: State,
        onClickBack: (force: Boolean) -> Unit,
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<MerchSave.Response.Result>) {
                    when (val result = it.value) {
                        is MerchSave.Response.Result.Failed ->
                            snackbarHostState.showSnackbar(message = result.errorMessage)
                        is MerchSave.Response.Result.Outdated -> {
                            // TODO
                        }
                        MerchSave.Response.Result.Success -> {
                            state.savingState.value = JobProgress.Idle()
                            onClickBack(true)
                        }
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(state.id.value.text.toString()) },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(true) }) },
                    actions = {
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

    @Stable
    class State(
        val id: EntryForm2.SingleTextState,
        val uuid: EntryForm2.SingleTextState,
        val notes: EntryForm2.SingleTextState,
        val savingState: MutableStateFlow<JobProgress<MerchSave.Response.Result>>,
    )
}
