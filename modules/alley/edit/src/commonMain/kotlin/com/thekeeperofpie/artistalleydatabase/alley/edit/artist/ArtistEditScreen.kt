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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_booth
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_catalog_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_commissions
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_id
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_merch_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_merch_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_name
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_notes
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_series_confirmed
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_series_inferred
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_store_links
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_summary
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_title_adding
import artistalleydatabase.modules.alley.edit.generated.resources.alley_artist_edit_title_editing
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_tag_delete_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.EditImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.images.ImagesEditScreen
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.images.rememberImagePagerState
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkModel
import com.thekeeperofpie.artistalleydatabase.alley.links.LinkRow
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesRow
import com.thekeeperofpie.artistalleydatabase.alley.ui.IconButtonWithTooltip
import com.thekeeperofpie.artistalleydatabase.alley.ui.currentWindowSizeClass
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryFormScope
import com.thekeeperofpie.artistalleydatabase.entry.form.LongTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.MultiTextSection
import com.thekeeperofpie.artistalleydatabase.entry.form.SingleTextSection
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

object ArtistEditScreen {

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        artistId: Uuid?,
        graph: ArtistAlleyEditGraph,
        onClickBack: () -> Unit,
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
        NavigationResultEffect(ImagesEditScreen.RESULT_KEY) {
            val images = withContext(PlatformDispatchers.IO) {
                Json.decodeFromString<List<EditImage>>(it)
            }
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
                onClickEditImages(viewModel.state.textState.name.value.text.toString(), it)
            },
            onClickSave = viewModel::onClickSave,
        )
    }

    @Composable
    operator fun invoke(
        dataYear: DataYear,
        mode: Mode,
        state: State,
        seriesPredictions: suspend (String) -> Flow<List<SeriesInfo>>,
        merchPredictions: suspend (String) -> Flow<List<MerchInfo>>,
        seriesImage: (SeriesInfo) -> String?,
        onClickBack: () -> Unit,
        onClickEditImages: (List<EditImage>) -> Unit,
        onClickSave: () -> Unit,
    ) {
        val saved by state.saved.collectAsStateWithLifecycle()
        LaunchedEffect(saved) {
            if (saved == true) {
                onClickBack()
                state.saved.value = null
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
                                    Res.string.alley_artist_edit_title_adding,
                                    stringResource(dataYear.shortName),
                                )
                            Mode.EDIT ->
                                stringResource(
                                    Res.string.alley_artist_edit_title_editing,
                                    stringResource(dataYear.shortName),
                                    state.textState.name.value.text.ifBlank { state.textState.id.value.text.toString() },
                                )
                        }
                        Text(text)
                    },
                    navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
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
            modifier = Modifier.fillMaxWidth()
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
                        .padding(it)
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
                        .padding(it)
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
                            sharedElementId = state.textState.id.value.text.toString(),
                            images = state.images,
                            imagePagerState = imagePagerState,
                            onClickImage = {},
                        )
                        form(Modifier.fillMaxWidth())
                    }
                }
            }
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
                    images += it.map(EditImage::LocalImage)
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
        onClickImage: (EditImage) -> Unit,
    ) {
        ImagePager(
            images = images,
            pagerState = imagePagerState,
            sharedElementId = sharedElementId,
            onClickPage = { onClickImage(images[it]) },
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
        val textState = state.textState
        EntryForm2(modifier = modifier) {
            SingleTextSection(
                state = textState.id,
                title = Res.string.alley_artist_edit_id,
                previousFocus = null,
                nextFocus = textState.booth.focusRequester,
            )
            SingleTextSection(
                state = textState.booth,
                title = Res.string.alley_artist_edit_booth,
                previousFocus = textState.id.focusRequester,
                nextFocus = textState.name.focusRequester,
            )
            SingleTextSection(
                state = textState.name,
                title = Res.string.alley_artist_edit_name,
                previousFocus = textState.booth.focusRequester,
                nextFocus = textState.summary.focusRequester,
            )
            SingleTextSection(
                state = textState.summary,
                title = Res.string.alley_artist_edit_summary,
                previousFocus = textState.name.focusRequester,
                nextFocus = textState.links.focusRequester,
            )
            LinksSection(
                state = textState.links,
                title = Res.string.alley_artist_edit_links,
                items = state.links,
                previousFocus = textState.summary.focusRequester,
                nextFocus = textState.storeLinks.focusRequester,
            )
            LinksSection(
                state = textState.storeLinks,
                title = Res.string.alley_artist_edit_store_links,
                items = state.storeLinks,
                previousFocus = textState.links.focusRequester,
                nextFocus = textState.catalogLinks.focusRequester,
            )
            MultiTextSection(
                state = textState.catalogLinks,
                title = Res.string.alley_artist_edit_catalog_links,
                items = state.catalogLinks,
                itemToText = { it },
                previousFocus = textState.storeLinks.focusRequester,
                nextFocus = textState.commissions.focusRequester,
            )
            MultiTextSection(
                state = textState.commissions,
                title = Res.string.alley_artist_edit_commissions,
                items = state.commissions,
                itemToText = { it },
                previousFocus = textState.catalogLinks.focusRequester,
                nextFocus = textState.seriesInferred.focusRequester,
            )
            SeriesSection(
                state = textState.seriesInferred,
                title = Res.string.alley_artist_edit_series_inferred,
                items = state.seriesInferred,
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = textState.commissions.focusRequester,
                nextFocus = textState.seriesConfirmed.focusRequester,
            )
            SeriesSection(
                state = textState.seriesConfirmed,
                title = Res.string.alley_artist_edit_series_confirmed,
                items = state.seriesConfirmed,
                predictions = seriesPredictions,
                image = seriesImage,
                previousFocus = textState.seriesInferred.focusRequester,
                nextFocus = textState.merchInferred.focusRequester,
            )
            MultiTextSection(
                state = textState.merchInferred,
                title = Res.string.alley_artist_edit_merch_inferred,
                items = state.merchInferred,
                predictions = merchPredictions,
                itemToText = { it.name },
                previousFocus = textState.seriesConfirmed.focusRequester,
                nextFocus = textState.merchConfirmed.focusRequester,
            )
            MultiTextSection(
                state = textState.merchConfirmed,
                title = Res.string.alley_artist_edit_merch_confirmed,
                items = state.merchConfirmed,
                predictions = merchPredictions,
                itemToText = { it.name },
                previousFocus = textState.merchInferred.focusRequester,
                nextFocus = textState.notes.focusRequester,
            )
            LongTextSection(
                textState.notes,
                headerText = {
                    Text(stringResource(Res.string.alley_artist_edit_notes))
                },
            )
        }
    }

    @Composable
    private fun EntryFormScope.SingleTextSection(
        state: EntryForm2.SingleTextState,
        title: StringResource,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
    ) {
        SingleTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
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
        itemToText: (T) -> String,
        previousFocus: FocusRequester?,
        nextFocus: FocusRequester?,
    ) {
        MultiTextSection(
            state = state,
            title = title,
            items = items,
            predictions = predictions,
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
    ) {
        MultiTextSection(
            state = state,
            headerText = { Text(stringResource(title)) },
            entryPredictions = predictions,
            items = items,
            onItemCommitted = { },
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
            onItemCommitted = {},
            removeLastItem = { items.removeLastOrNull()?.link },
            item = { _, value -> LinkRow(value, isLast = false) },
            onTab = {
                val focusRequester = if (it) nextFocus else previousFocus
                focusRequester?.requestFocus()
            }
        )
    }

    enum class Mode {
        ADD, EDIT
    }

    @Stable
    class State(
        val images: SnapshotStateList<EditImage>,
        val links: SnapshotStateList<LinkModel>,
        val storeLinks: SnapshotStateList<LinkModel>,
        val catalogLinks: SnapshotStateList<String>,
        val commissions: SnapshotStateList<String>,
        val seriesInferred: SnapshotStateList<SeriesInfo>,
        val seriesConfirmed: SnapshotStateList<SeriesInfo>,
        val merchInferred: SnapshotStateList<MerchInfo>,
        val merchConfirmed: SnapshotStateList<MerchInfo>,
        val textState: TextState,
        val saved: MutableStateFlow<Boolean?>,
    ) {
        @Stable
        class TextState(
            val id: EntryForm2.SingleTextState = EntryForm2.SingleTextState(),
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
        ) {
            object Saver : ComposeSaver<TextState, List<Any>> {
                override fun SaverScope.save(value: TextState) = listOf(
                    with(EntryForm2.SingleTextState.Saver) { save(value.id) },
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
                )

                override fun restore(value: List<Any>) = TextState(
                    id = with(EntryForm2.SingleTextState.Saver) { restore(value[0]) },
                    booth = with(EntryForm2.SingleTextState.Saver) { restore(value[1]) },
                    name = with(EntryForm2.SingleTextState.Saver) { restore(value[2]) },
                    summary = with(EntryForm2.SingleTextState.Saver) { restore(value[3]) },
                    links = with(EntryForm2.PendingTextState.Saver) { restore(value[4]) },
                    storeLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[5]) },
                    catalogLinks = with(EntryForm2.PendingTextState.Saver) { restore(value[6]) },
                    commissions = with(EntryForm2.PendingTextState.Saver) { restore(value[7]) },
                    seriesInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[8]) },
                    seriesConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[9]) },
                    merchInferred = with(EntryForm2.PendingTextState.Saver) { restore(value[10]) },
                    merchConfirmed = with(EntryForm2.PendingTextState.Saver) { restore(value[11]) },
                    notes = with(EntryForm2.PendingTextState.Saver) { restore(value[12]) },
                )
            }
        }
    }
}
