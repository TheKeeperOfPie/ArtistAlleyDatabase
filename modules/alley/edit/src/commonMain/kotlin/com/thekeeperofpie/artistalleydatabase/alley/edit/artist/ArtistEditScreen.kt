package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_history_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_saving_bad_fields
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid?,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
        onClickHistory: (() -> Unit)? = null,
        viewModel: ArtistEditViewModel = viewModel {
            graph.artistEditViewModelFactory.create(
                dataYear = dataYear,
                artistId = artistId ?: Uuid.random(),
                mode = if (artistId == null) Mode.ADD else Mode.EDIT,
                savedStateHandle = createSavedStateHandle(),
            )
        },
    ) {
        NavigationResultEffect(ImagesEditScreen.RESULT_KEY) { images ->
            Snapshot.withMutableSnapshot {
                viewModel.state.artistFormState.images.clear()
                viewModel.state.artistFormState.images += images
            }
        }
        NavigationResultEffect(ArtistHistoryScreen.RESULT_KEY) {
            viewModel.initialize(force = true)
        }
        ArtistEditScreen(
            dataYear = dataYear,
            mode = viewModel.mode,
            state = viewModel.state,
            seriesPredictions = viewModel::seriesPredictions,
            merchPredictions = viewModel::merchPredictions,
            seriesImage = viewModel::seriesImage,
            onClickBack = onClickBack,
            onClickEditImages = {
                onClickEditImages(
                    viewModel.state.artistFormState.textState.name.value.text.toString(),
                    it
                )
            },
            onClickHistory = onClickHistory,
            onClickSave = viewModel::onClickSave,
        )

        LaunchedEffect(viewModel) {
            viewModel.initialize()
        }
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        mode: Mode,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickHistory: (() -> Unit)? = null,
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<ArtistSave.Response.Result>) {
                    when (val result = it.value) {
                        is ArtistSave.Response.Result.Failed ->
                            snackbarHostState.showSnackbar(message = result.throwable.message.orEmpty())
                        is ArtistSave.Response.Result.Outdated -> {
                            // TODO
                        }
                        is ArtistSave.Response.Result.Success -> {
                            state.savingState.value = JobProgress.Idle()
                            onClickBack(true)
                        }
                    }
                }
            }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
        val errorState = rememberErrorState(state.artistFormState.textState)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val text = when (mode) {
                            Mode.ADD ->
                                stringResource(
                                    Res.string.alley_edit_artist_edit_title_adding,
                                    stringResource(dataYear.shortName),
                                )
                            Mode.EDIT ->
                                stringResource(
                                    Res.string.alley_edit_artist_edit_title_editing,
                                    stringResource(dataYear.shortName),
                                    state.artistFormState.textState.name.value.text.ifBlank { state.artistFormState.textState.id.value.text.toString() },
                                )
                        }
                        Text(text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        if (mode == Mode.EDIT && onClickHistory != null) {
                            IconButton(onClick = onClickHistory) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = stringResource(Res.string.alley_edit_artist_action_history_content_description),
                                )
                            }
                        }
                        val enabled = !errorState.hasAnyError
                        IconButton(onClick = onClickSave, enabled = enabled) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = stringResource(Res.string.alley_edit_artist_action_save_content_description),
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
            val jobProgress by state.savingState.collectAsStateWithLifecycle()
            ContentSavingBox(
                saving = jobProgress is JobProgress.Loading,
                modifier = Modifier.padding(scaffoldPadding)
            ) {
                val imagePagerState = rememberImagePagerState(state.artistFormState.images, 0)
                val form = remember {
                    movableContentOf { modifier: Modifier ->
                        ArtistForm(
                            state = state.artistFormState,
                            errorState = errorState,
                            seriesPredictions = seriesPredictions,
                            merchPredictions = merchPredictions,
                            seriesImage = seriesImage,
                            modifier = modifier,
                        )
                    }
                }
                if (isExpanded) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        form(
                            Modifier.fillMaxHeight()
                                .width(800.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        Column {
                            EditImagesButton(
                                images = state.artistFormState.images,
                                onClickEdit = { onClickEditImages(state.artistFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImageGrid(
                                images = state.artistFormState.images,
                                onClickImage = {},
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 960.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            EditImagesButton(
                                images = state.artistFormState.images,
                                onClickEdit = { onClickEditImages(state.artistFormState.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImagePager(
                                images = state.artistFormState.images,
                                pagerState = imagePagerState,
                                sharedElementId = state.artistFormState.textState.id.value.text.toString(),
                                onClickPage = {
                                    // TODO: Open images screen
                                },
                            )

                            form(Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            val errorMessage = stringResource(Res.string.alley_edit_artist_error_saving_bad_fields)
            GenericExitDialog(
                onClickBack = { onClickBack(true) },
                onClickSave = onClickSave,
                saveErrorMessage = { errorMessage.takeIf { errorState.hasAnyError } },
            )
        }
    }

    @Composable
    private fun EditImagesButton(
        images: SnapshotStateList<EditImage>,
        onClickEdit: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (images.isEmpty()) {
            val launcher = rememberFilePickerLauncher(
                type = FileKitType.Image,
                mode = FileKitMode.Multiple(),
            ) {
                if (it != null) {
                    images += it
                        .map(PlatformImageCache::add)
                        .map(EditImage::LocalImage)
                }
            }
            FilledTonalButton(onClick = { launcher.launch() }, modifier = modifier.padding(16.dp)) {
                Text(stringResource(Res.string.alley_edit_artist_action_add_images))
            }
        } else {
            FilledTonalButton(onClick = onClickEdit, modifier = modifier.padding(16.dp)) {
                Text(stringResource(Res.string.alley_edit_artist_action_edit_images))
            }
        }
    }

    enum class Mode {
        ADD, EDIT
    }

    @Stable
    class State(
        val artistFormState: ArtistFormState,
        val savingState: MutableStateFlow<JobProgress<ArtistSave.Response.Result>>,
    )
}
