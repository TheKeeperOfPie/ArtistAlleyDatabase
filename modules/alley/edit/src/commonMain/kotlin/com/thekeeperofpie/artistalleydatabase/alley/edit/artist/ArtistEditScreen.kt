package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_editor_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_author_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_last_modified_prefix
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_status
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_delete_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.PlatformImageCache
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.ContentSavingBox
import com.thekeeperofpie.artistalleydatabase.alley.edit.ui.GenericExitDialog
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.network.ArtistSave
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.entry.form.DropdownSection
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.ArtistStatus
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.JobProgress
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalDateTimeFormatter
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlin.uuid.Uuid
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid?,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        onClickEditImages: (displayName: String, List<EditImage>) -> Unit,
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
                viewModel.state.images.clear()
                viewModel.state.images += images
            }
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
                onClickEditImages(viewModel.state.formState.name.value.text.toString(), it)
            },
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
        onClickSave: () -> Unit,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            state.savingState.collectLatest {
                if (it is JobProgress.Finished.Result<*>) {
                    when (val result = it.value as? ArtistSave.Response.Result) {
                        is ArtistSave.Response.Result.Failed ->
                            snackbarHostState.showSnackbar(message = result.throwable.message.orEmpty())
                        is ArtistSave.Response.Result.Outdated -> {
                            // TODO
                        }
                        is ArtistSave.Response.Result.Success -> {
                            state.savingState.value = JobProgress.Idle
                            onClickBack(true)
                        }
                        null -> Unit
                    }
                }
            }
        }

        val windowSizeClass = currentWindowSizeClass()
        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
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
                                    state.formState.name.value.text.ifBlank { state.formState.id.value.text.toString() },
                                )
                        }
                        Text(text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = { onClickBack(false) }) },
                    actions = {
                        IconButton(onClick = onClickSave) {
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
                val imagePagerState = rememberImagePagerState(state.images, 0)
                val form = remember {
                    movableContentOf { modifier: Modifier ->
                        Form(
                            state = state,
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
                                images = state.images,
                                onClickEdit = { onClickEditImages(state.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImageGrid(
                                images = state.images,
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
                                images = state.images,
                                onClickEdit = { onClickEditImages(state.images.toList()) },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            ImagesPager(
                                sharedElementId = state.formState.id.value.text.toString(),
                                images = state.images,
                                imagePagerState = imagePagerState,
                                onClickPage = {
                                    // TODO: Open images screen
                                },
                            )
                            form(Modifier.fillMaxWidth())
                        }
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

    @Composable
    private fun ImagesPager(
        sharedElementId: Any,
        images: List<EditImage>,
        imagePagerState: PagerState,
        onClickPage: (page: Int) -> Unit,
    ) {
        ImagePager(
            images = images,
            pagerState = imagePagerState,
            sharedElementId = sharedElementId,
            onClickPage = onClickPage,
            imageContentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(400.dp)
        )
    }

    @Composable
    private fun Form(
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        modifier: Modifier = Modifier,
    ) {
        val formState = state.formState
        EntryForm2(modifier = modifier) {
            val metadata = state.artistMetadata
            val lastEditTime = metadata.lastEditTime
            if (lastEditTime != null) {
                OutlinedCard(modifier = Modifier.padding(16.dp)) {
                    val textColorDim = LocalContentColor.current.copy(alpha = 0.6f)
                    val colorPrimary = MaterialTheme.colorScheme.primary
                    val lastEditor = metadata.lastEditor
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

            DropdownSection(
                state = formState.status,
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
                nextFocus = formState.id.focusRequester
            )

            val idErrorMessage by rememberUuidValidator(formState.id)
            SingleTextSection(
                state = formState.id,
                title = Res.string.alley_edit_artist_edit_id,
                previousFocus = formState.status.focusRequester,
                nextFocus = formState.booth.focusRequester,
                errorText = { idErrorMessage },
            )

            val boothErrorMessage by rememberBoothValidator(formState.booth)
            SingleTextSection(
                state = formState.booth,
                title = Res.string.alley_edit_artist_edit_booth,
                previousFocus = formState.id.focusRequester,
                nextFocus = formState.name.focusRequester,
                inputTransformation = InputTransformation.maxLength(3),
                errorText = { boothErrorMessage },
            )
            SingleTextSection(
                state = formState.name,
                title = Res.string.alley_edit_artist_edit_name,
                previousFocus = formState.booth.focusRequester,
                nextFocus = formState.summary.focusRequester,
            )
            SingleTextSection(
                state = formState.summary,
                title = Res.string.alley_edit_artist_edit_summary,
                previousFocus = formState.name.focusRequester,
                nextFocus = formState.links.focusRequester,
            )
            LinksSection(
                state = formState.links,
                title = Res.string.alley_edit_artist_edit_links,
                items = state.links,
                previousFocus = formState.summary.focusRequester,
                nextFocus = formState.storeLinks.focusRequester,
            )
            LinksSection(
                state = formState.storeLinks,
                title = Res.string.alley_edit_artist_edit_store_links,
                items = state.storeLinks,
                previousFocus = formState.links.focusRequester,
                nextFocus = formState.catalogLinks.focusRequester,
            )
            MultiTextSection(
                state = formState.catalogLinks,
                title = Res.string.alley_edit_artist_edit_catalog_links,
                items = state.catalogLinks,
                itemToText = { it },
                onItemCommitted = {
                    state.catalogLinks.add(it)
                    formState.catalogLinks.pendingValue.clearText()
                },
                previousFocus = formState.storeLinks.focusRequester,
                nextFocus = formState.commissions.focusRequester,
            )
            MultiTextSection(
                state = formState.commissions,
                title = Res.string.alley_edit_artist_edit_commissions,
                items = state.commissions,
                itemToText = { it },
                onItemCommitted = {
                    state.commissions.add(it)
                    formState.commissions.pendingValue.clearText()
                },
                previousFocus = formState.catalogLinks.focusRequester,
                nextFocus = formState.seriesInferred.focusRequester,
            )
            SeriesSection(
                state = formState.seriesInferred,
                title = Res.string.alley_edit_artist_edit_series_inferred,
                items = state.seriesInferred,
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = formState.commissions.focusRequester,
                nextFocus = formState.seriesConfirmed.focusRequester,
            )
            SeriesSection(
                state = formState.seriesConfirmed,
                title = Res.string.alley_edit_artist_edit_series_confirmed,
                items = state.seriesConfirmed,
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = formState.seriesInferred.focusRequester,
                nextFocus = formState.merchInferred.focusRequester,
            )
            MultiTextSection(
                state = formState.merchInferred,
                title = Res.string.alley_edit_artist_edit_merch_inferred,
                items = state.merchInferred,
                predictions = merchPredictions,
                itemToText = { it.name },
                previousFocus = formState.seriesConfirmed.focusRequester,
                nextFocus = formState.merchConfirmed.focusRequester,
            )
            MultiTextSection(
                state = formState.merchConfirmed,
                title = Res.string.alley_edit_artist_edit_merch_confirmed,
                items = state.merchConfirmed,
                predictions = merchPredictions,
                itemToText = { it.name },
                previousFocus = formState.merchInferred.focusRequester,
                nextFocus = formState.notes.focusRequester,
            )
            LongTextSection(
                formState.notes,
                headerText = {
                    Text(stringResource(Res.string.alley_edit_artist_edit_notes))
                },
            )
            LongTextSection(
                formState.editorNotes,
                headerText = {
                    Text(stringResource(Res.string.alley_edit_artist_edit_editor_notes))
                },
            )
        }
    }

    @Composable
    private fun <T> EntryFormScope.MultiTextSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        itemToText: (T) -> String,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
        onItemCommitted: (String) -> Unit = {},
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            predictions = predictions,
            onItemCommitted = onItemCommitted,
            removeLastItem = { items.removeLastOrNull()?.let { itemToText(it) } },
            item = { _, item ->
                Box {
                    TextField(
                        value = itemToText(item),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Box {
                                var showMenu by remember { mutableStateOf(false) }
                                MenuIcon(
                                    visible = state.lockState.editable,
                                    onClick = { showMenu = true },
                                )

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(Res.string.alley_edit_artist_action_delete)) },
                                        onClick = {
                                            items.remove(item)
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            prediction = { _, value -> Text(text = itemToText(value)) },
            onTab = {
                val focusRequester = if (it) nextFocus else previousFocus
                focusRequester?.requestFocus()
            },
        )
    }

    @Composable
    private fun <T> EntryFormScope.MultiTextSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<T>,
        predictions: suspend (String) -> Flow<List<T>> = { emptyFlow() },
        removeLastItem: () -> String?,
        item: @Composable (index: Int, T) -> Unit,
        prediction: @Composable (index: Int, T) -> Unit,
        onTab: (next: Boolean) -> Unit,
        onItemCommitted: (String) -> Unit = {},
    ) {
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            entryPredictions = predictions,
            items = items,
            onItemCommitted = onItemCommitted,
            removeLastItem = removeLastItem,
            prediction = prediction,
            preferPrediction = true,
            item = item,
            onTab = onTab,
        )
    }

    @Composable
    private fun EntryFormScope.SeriesSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<SeriesInfo>,
        predictions: suspend (String) -> Flow<List<SeriesInfo>>,
        image: (SeriesInfo) -> String?,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            predictions = predictions,
            removeLastItem = { items.removeLastOrNull()?.titlePreferred },
            prediction = { _, value -> Text(value.titlePreferred) },
            item = { _, value ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeriesRow(
                        series = value,
                        image = { image(value) },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedVisibility(
                        visible = state.lockState.editable,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        val contentDescription = stringResource(
                            Res.string.alley_edit_tag_delete_content_description
                        )
                        IconButtonWithTooltip(
                            imageVector = Icons.Default.Delete,
                            tooltipText = contentDescription,
                            onClick = { items -= value },
                            contentDescription = contentDescription,
                        )
                    }
                }
            },
            onTab = {
                val focusRequester = if (it) nextFocus else previousFocus
                focusRequester?.requestFocus()
            },
        )
    }

    @Composable
    private fun MenuIcon(visible: Boolean, onClick: () -> Unit) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(
                        UtilsComposeRes.string.more_actions_content_description
                    ),
                )
            }
        }
    }

    @Composable
    private fun EntryFormScope.LinksSection(
        state: EntryForm2.PendingTextState,
        title: StringResource,
        items: SnapshotStateList<LinkModel>,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
    ) {
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            items = items,
            onItemCommitted = {
                items.add(LinkModel.parse(it))
                state.pendingValue.clearText()
            },
            removeLastItem = { items.removeLastOrNull()?.link },
            item = { _, value -> LinkRow(value, isLast = false) },
            onTab = {
                val focusRequester = if (it) nextFocus else previousFocus
                focusRequester?.requestFocus()
            }
        )
    }

    @Stable
    @Composable
    private fun rememberBoothValidator(boothState: EntryForm2.SingleTextState): androidx.compose.runtime.State<String?> {
        val errorMessage = stringResource(Res.string.alley_edit_artist_error_booth)
        return remember {
            derivedStateOf {
                val booth = boothState.value.text.toString()
                if (booth.isNotBlank() && (
                            booth.length != 3 ||
                                    !booth.first().isLetter() ||
                                    booth.drop(1).toIntOrNull() == null)
                ) {
                    errorMessage
                } else {
                    null
                }
            }
        }
    }

    enum class Mode {
        ADD, EDIT
    }

    @Stable
    class State(
        val artistMetadata: ArtistMetadata,
        val images: SnapshotStateList<EditImage>,
        val links: SnapshotStateList<LinkModel>,
        val storeLinks: SnapshotStateList<LinkModel>,
        val catalogLinks: SnapshotStateList<String>,
        val commissions: SnapshotStateList<String>,
        val seriesInferred: SnapshotStateList<SeriesInfo>,
        val seriesConfirmed: SnapshotStateList<SeriesInfo>,
        val merchInferred: SnapshotStateList<MerchInfo>,
        val merchConfirmed: SnapshotStateList<MerchInfo>,
        val formState: FormState,
        val savingState: MutableStateFlow<JobProgress>,
    ) {
        @Stable
        class ArtistMetadata(
            lastEditor: String? = null,
            lastEditTime: Instant? = null,
        ) {
            var lastEditor by mutableStateOf(lastEditor)
            var lastEditTime by mutableStateOf(lastEditTime)

            object Saver : ComposeSaver<ArtistMetadata, List<Any?>> {
                override fun SaverScope.save(value: ArtistMetadata) = listOf(
                    value.lastEditor,
                    value.lastEditTime?.toString()
                )

                override fun restore(value: List<Any?>): ArtistMetadata {
                    val (lastEditor, lastEditTime) = value
                    return ArtistMetadata(
                        lastEditor = lastEditor as String?,
                        lastEditTime = (lastEditTime as? String?)?.let(Instant::parseOrNull)
                    )
                }

            }
        }

        @Stable
        class FormState(
            val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val status: EntryForm2.DropdownState = EntryForm2.DropdownState(),
            val booth: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val name: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val summary: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
            val links: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val storeLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val catalogLinks: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val commissions: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val seriesInferred: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val seriesConfirmed: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val merchInferred: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val merchConfirmed: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val notes: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
            val editorNotes: EntryForm2.PendingTextState = EntryForm2.PendingTextState(),
        ) {
            object Saver : ComposeSaver<FormState, List<Any>> {
                override fun SaverScope.save(value: FormState) = listOf(
                    with(EntryForm2.SingleTextState.Saver) { save(value.id) },
                    with(EntryForm2.DropdownState.Saver) { save(value.status) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.booth) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.name) },
                    with(EntryForm2.SingleTextState.Saver) { save(value.summary) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.links) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.storeLinks) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.catalogLinks) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.commissions) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.seriesInferred) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.seriesConfirmed) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.merchInferred) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.merchConfirmed) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.notes) },
                    with(EntryForm2.PendingTextState.Saver) { save(value.editorNotes) },
                )

                override fun restore(value: List<Any>) = FormState(
                    id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                    status = with(EntryForm2.DropdownState.Saver) { restore(value[1]) },
                    booth = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                    name = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                    summary = with(EntryForm2.SingleTextState.Saver) { restore(value[4]) },
                    links = with(EntryForm2.PendingTextState.Saver) { restore(value[5]) },
                    storeLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[6]) },
                    catalogLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[7]) },
                    commissions = with(EntryForm2.PendingTextState.Saver) { restore(value[8]) },
                    seriesInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[9]) },
                    seriesConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[10]) },
                    merchInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[11]) },
                    merchConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[12]) },
                    notes = with(EntryForm2.PendingTextState.Saver) { restore(value[13]) },
                    editorNotes = with(EntryForm2.PendingTextState.Saver) { restore(value[14]) },
                )
            }
        }
    }
}
