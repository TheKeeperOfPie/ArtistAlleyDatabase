package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_action_confirm
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_artists_affected_header
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_resolution_error_generic
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.models.ArtistSummary
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryVerticalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.TooltipIconButton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> TagResolutionScreen(
    tagId: String,
    title: StringResource,
    artists: () -> List<ArtistSummary>,
    chosenValue: () -> T?,
    onChosenValueChange: (T) -> Unit,
    predictions: suspend (String) -> Flow<List<T>>,
    prediction: @Composable (query: String, T) -> Unit,
    progress: MutableStateFlow<JobProgress<ArtistSave.Response>>,
    onClickBack: () -> Unit,
    onClickDone: () -> Unit,
    content: @Composable () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(progress) {
        progress.collectLatest {
            when (it) {
                is JobProgress.Finished.Result<ArtistSave.Response> -> {
                    when (val response = it.value) {
                        is ArtistSave.Response.Failed ->
                            snackbarHostState.showSnackbar(
                                message = getString(
                                    Res.string.alley_edit_tag_resolution_error_generic,
                                    response.errorMessage
                                )
                            )
                        is ArtistSave.Response.Outdated ->
                            snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_tag_resolution_error_generic))
                        ArtistSave.Response.Success -> {
                            progress.value = JobProgress.Idle()
                            onClickBack()
                        }
                    }
                }
                is JobProgress.Finished.UnhandledError<*> ->
                    snackbarHostState.showSnackbar(message = getString(Res.string.alley_edit_tag_resolution_error_generic))
                is JobProgress.Idle<*>,
                is JobProgress.Loading<*>,
                    -> Unit
            }
        }
    }

    Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { ArrowBackIconButton(onClickBack) },
                    title = { Text(stringResource(title)) },
                    actions = {
                        TooltipIconButton(
                            icon = Icons.Default.Done,
                            tooltipText = stringResource(Res.string.alley_edit_tag_resolution_action_confirm),
                            enabled = chosenValue() != null,
                            onClick = onClickDone,
                        )
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.widthIn(max = 600.dp)
        ) {
            val progress by progress.collectAsStateWithLifecycle()
            ContentSavingBox(saving = progress is JobProgress.Loading<ArtistSave.Response>) {
                val scrollState = rememberScrollState()
                val scrollAreaState = rememberScrollAreaState(scrollState)
                ScrollArea(scrollAreaState) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(it)
                            .padding(24.dp)
                    ) {
                        OutlinedTextField(
                            value = tagId,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null
                        )

                        val textState = rememberTextFieldState()
                        var expanded by remember { mutableStateOf(false) }
                        var isFocused by remember { mutableStateOf(false) }
                        LaunchedEffect(textState.text, predictions, isFocused) {
                            if (isFocused) {
                                expanded = true
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                        ) {
                            OutlinedTextField(
                                state = textState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isFocused = it.isFocused }
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            )

                            val predictions by produceState(emptyList(), predictions) {
                                snapshotFlow { textState.text.toString() }
                                    .flatMapLatest(predictions)
                                    .flowOn(PlatformDispatchers.IO)
                                    .collectLatest { value = it }
                            }
                            if (predictions.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.heightIn(max = 240.dp).focusable(false)
                                ) {
                                    predictions.forEach {
                                        DropdownMenuItem(
                                            text = {
                                                prediction(textState.text.toString(),it)
                                            },
                                            onClick = {
                                                onChosenValueChange(it)
                                                expanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        content()

                        val chosenValue = chosenValue()
                        FilledTonalButton(
                            enabled = chosenValue != null,
                            onClick = { chosenValue?.let { onClickDone() } },
                        ) {
                            Text(stringResource(Res.string.alley_edit_tag_resolution_action_confirm))
                        }

                        Text(text = stringResource(Res.string.alley_edit_tag_resolution_artists_affected_header))

                        artists().forEach {
                            Text(
                                text = it.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                            )
                        }
                    }

                    PrimaryVerticalScrollbar()
                }
            }
        }
    }
}
