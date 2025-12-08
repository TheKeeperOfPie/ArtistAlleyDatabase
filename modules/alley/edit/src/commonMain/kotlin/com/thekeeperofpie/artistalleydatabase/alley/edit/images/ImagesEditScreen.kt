package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_drag_handle_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_action_add
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_action_change
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_action_delete
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_action_save_content_description
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_header_added
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_header_deleted
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_header_moved
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_save_changes_moved
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_title
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_image_width_and_height
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_action_exit
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_action_save
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_save_changes_title
import artistalleydatabase.modules.utils_compose.generated.resources.more_actions_content_description
import coil3.compose.AsyncImage
import com.thekeeperofpie.artistalleydatabase.alley.edit.AlleyEditDestination
import com.thekeeperofpie.artistalleydatabase.alley.edit.ArtistAlleyEditGraph
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.DraggableItem
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.dragContainer
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResults
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberDragDropState
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import artistalleydatabase.modules.utils_compose.generated.resources.Res as UtilsComposeRes

object ImagesEditScreen {

    val RESULT_KEY = NavigationResults.Key<List<EditImage>>("ImagesEditScreen")

    @Composable
    operator fun invoke(
        route: AlleyEditDestination.ImagesEdit,
        graph: ArtistAlleyEditGraph,
        onClickBack: (force: Boolean) -> Unit,
        viewModel: ImagesEditViewModel = viewModel {
            graph.imagesEditViewModelFactory.create(route, createSavedStateHandle())
        },
    ) {
        val navigationResults = LocalNavigationResults.current
        ImagesEditScreen(
            route = route,
            images = viewModel.images,
            onClickBack = { onClickBack(false) },
            onClickSave = {
                navigationResults.launchSave(RESULT_KEY) { viewModel.images.toList() }
                onClickBack(true)
            },
        )
    }

    @Composable
    operator fun invoke(
        route: AlleyEditDestination.ImagesEdit,
        images: SnapshotStateList<EditImage>,
        onClickBack: () -> Unit,
        onClickSave: () -> Unit,
    ) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxWidth()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(
                                    Res.string.alley_edit_image_title,
                                    stringResource(route.dataYear.shortName),
                                    route.displayName,
                                )
                            )
                        },
                        navigationIcon = { ArrowBackIconButton(onClick = onClickBack) },
                        actions = {
                            IconButton(onClick = onClickSave) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = stringResource(Res.string.alley_edit_image_action_save_content_description),
                                )
                            }
                        },
                        modifier = Modifier.widthIn(max = 960.dp)
                    )
                },
                modifier = Modifier.widthIn(max = 960.dp).fillMaxWidth()
            ) {
                val addLauncher = rememberFilePickerLauncher(
                    type = FileKitType.Image,
                    mode = FileKitMode.Multiple(),
                ) {
                    if (it != null) {
                        images += it
                            .map(PlatformImageCache::add)
                            .map(EditImage::LocalImage)
                    }
                }
                val listState = rememberLazyListState()
                val dragDropState =
                    rememberDragDropState(listState) { fromIndex, toIndex ->
                        Snapshot.withMutableSnapshot {
                            images.add(toIndex, images.removeAt(fromIndex))
                        }
                    }
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(it)
                        .dragContainer(dragDropState)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = images,
                        key = { _, image -> image.name },
                        contentType = { _, _ -> "image" },
                    ) { index, image ->
                        DraggableItem(dragDropState, index) { isDragging ->
                            Column(
                                modifier = Modifier.conditionally(
                                    isDragging,
                                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = stringResource(Res.string.alley_edit_drag_handle_content_description),
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    Text(
                                        text = index.toString(),
                                        style = MaterialTheme.typography.headlineMediumEmphasized,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    val width = image.width
                                    val height = image.height
                                    AsyncImage(
                                        model = image.coilImageModel,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(240.dp)
                                            .conditionally(width != null && height != null) {
                                                aspectRatio(width!!.toFloat() / height!!)
                                            }
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.weight(1f)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = image.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (width != null && height != null) {
                                            Text(
                                                text = stringResource(
                                                    Res.string.alley_edit_image_width_and_height,
                                                    width,
                                                    height,
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }

                                    val swapLauncher = rememberFilePickerLauncher(
                                        type = FileKitType.Image,
                                        mode = FileKitMode.Single,
                                    ) {
                                        if (it != null) {
                                            images[index] =
                                                EditImage.LocalImage(PlatformImageCache.add(it))
                                        }
                                    }

                                    Box {
                                        var showMenu by remember { mutableStateOf(false) }
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = stringResource(
                                                    UtilsComposeRes.string.more_actions_content_description
                                                ),
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.alley_edit_image_action_delete)) },
                                                onClick = { images.remove(image) },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(Res.string.alley_edit_image_action_change)) },
                                                onClick = swapLauncher::launch,
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    item(key = "addImage", contentType = "addImage") {
                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledTonalButton(
                                onClick = addLauncher::launch,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(stringResource(Res.string.alley_edit_image_action_add))
                            }
                        }
                    }
                }
            }

            ExitDialog(
                route = route,
                images = images,
                onClickBack = onClickBack,
                onClickSave = onClickSave,
            )
        }
    }

    @Composable
    private fun ExitDialog(
        route: AlleyEditDestination.ImagesEdit,
        images: SnapshotStateList<EditImage>,
        onClickBack: () -> Unit,
        onClickSave: () -> Unit,
    ) {
        val hasEdited by remember { derivedStateOf { route.images != images } }
        var showBackDialog by rememberSaveable { mutableStateOf(false) }
        var hasConfirmedExit by remember { mutableStateOf(false) }
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = hasEdited && !hasConfirmedExit,
        ) {
            showBackDialog = true
        }
        if (showBackDialog) {
            val diff by produceState<EditImage.Diff?>(null, key1 = images) {
                value = EditImage.generateDiffs(route.images, images)
            }
            AlertDialog(
                onDismissRequest = { showBackDialog = false },
                title = {
                    Text(stringResource(Res.string.alley_edit_save_changes_title))
                },
                text = {
                    val diff = diff
                    if (diff == null) {
                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            if (diff.added.isNotEmpty()) {
                                Text(stringResource(Res.string.alley_edit_image_save_changes_header_added))
                                diff.added.forEach {
                                    Text(it.name)
                                }
                            }
                            if (diff.deleted.isNotEmpty()) {
                                Text(stringResource(Res.string.alley_edit_image_save_changes_header_deleted))
                                diff.deleted.forEach {
                                    Text(it.name)
                                }
                            }
                            if (diff.moved.isNotEmpty()) {
                                Text(stringResource(Res.string.alley_edit_image_save_changes_header_moved))
                                diff.moved.forEach { (indexDiff, image) ->
                                    Text(
                                        stringResource(
                                            Res.string.alley_edit_image_save_changes_moved,
                                            image.name,
                                            indexDiff.fromIndex,
                                            indexDiff.toIndex,
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            hasConfirmedExit = true
                            showBackDialog = false
                            onClickSave()
                        },
                    ) {
                        Text(stringResource(Res.string.alley_edit_save_changes_action_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        hasConfirmedExit = true
                        showBackDialog = false
                        onClickBack()
                    }) {
                        Text(stringResource(Res.string.alley_edit_save_changes_action_exit))
                    }
                }
            )
        }
    }
}
