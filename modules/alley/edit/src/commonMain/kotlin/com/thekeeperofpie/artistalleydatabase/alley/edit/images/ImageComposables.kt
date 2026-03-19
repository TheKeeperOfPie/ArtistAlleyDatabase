package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_add_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_action_edit_images
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_images_size_megabytes
import coil3.compose.AsyncImage
import com.composables.core.ScrollArea
import com.composables.core.rememberScrollAreaState
import com.thekeeperofpie.artistalleydatabase.alley.images.ImageRowActions
import com.thekeeperofpie.artistalleydatabase.alley.ui.PrimaryHorizontalScrollbar
import com.thekeeperofpie.artistalleydatabase.utils.asBytes
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationRequestKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationResultEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberTintPainter
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.replaceAll
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditImagesSection(
    images: SnapshotStateList<EditImage>,
    requestKey: NavigationRequestKey<List<EditImage>>,
    onClickEditImages: (() -> Unit)?,
) {
    val listState = rememberLazyListState()
    val scrollAreaState = rememberScrollAreaState(listState)
    ScrollArea(state = scrollAreaState) {
        NavigationResultEffect(requestKey) {
            images.replaceAll(it)
        }

        Column {
            Box {
                val addLauncher = if (onClickEditImages == null) {
                    null
                } else {
                    val scope = rememberCoroutineScope()
                    rememberFilePickerLauncher(
                        type = FileKitType.Image,
                        mode = FileKitMode.Multiple(),
                    ) {
                        if (it != null) {
                            scope.launch {
                                images += it.map {
                                    val imageKey = PlatformImageCache.add(it)
                                    EditImage.LocalImage(imageKey, it)
                                }
                            }
                        }
                    }
                }

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
                    itemsIndexed(
                        items = images,
                        key = { _, image -> image.coilImageModel.toString() }) { index, image ->
                        Box {
                            val scope = rememberCoroutineScope()
                            val replaceLauncher = rememberFilePickerLauncher(
                                type = FileKitType.Image,
                                mode = FileKitMode.Single,
                            ) {
                                if (it != null) {
                                    scope.launch {
                                        val imageKey = PlatformImageCache.add(it)
                                        try {
                                            Snapshot.withMutableSnapshot {
                                                images[index] = EditImage.LocalImage(imageKey, it)
                                            }
                                        } catch (_: Throwable) {
                                        }
                                    }
                                }
                            }

                            val imageWidth = image.width
                            val imageHeight = image.height
                            val width = if (imageWidth == null || imageHeight == null) {
                                null
                            } else {
                                200.dp * (imageWidth.toFloat() / imageHeight)
                            }
                            val placeholder = rememberVectorPainter(image = Icons.Default.MoreHoriz)
                            AsyncImage(
                                model = image.coilImageModel,
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                placeholder = rememberTintPainter(placeholder),
                                error = rememberVectorPainter(Icons.Default.Error),
                                modifier = Modifier
                                    .conditionallyNonNull(width) { width(it) }
                                    .height(200.dp)
                                    .clickable(onClick = replaceLauncher::launch)
                            )
                            if (image is EditImage.LocalImage) {
                                val size = remember(image.key) {
                                    PlatformImageCache[image.key]?.size()?.asBytes()
                                }
                                if (size != null && size > ImageUtils.MAX_UPLOAD_SIZE) {
                                    Text(
                                        text = stringResource(
                                            Res.string.alley_edit_artist_images_size_megabytes,
                                            size.inWholeMegabytes,
                                        ),
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .background(
                                                MaterialTheme.colorScheme.errorContainer,
                                                RoundedCornerShape(topStart = 12.dp)
                                            )
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (addLauncher != null) {
                        item {
                            val onClick: () -> Unit = {
                                if (images.isEmpty()) {
                                    addLauncher.launch()
                                } else {
                                    onClickEditImages?.invoke()
                                }
                            }
                            OutlinedCard(onClick = onClick) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(200.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = onClick,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    ) {
                                        Text(
                                            stringResource(
                                                if (images.isEmpty()) {
                                                    Res.string.alley_edit_artist_action_add_images
                                                } else {
                                                    Res.string.alley_edit_artist_action_edit_images
                                                }
                                            )
                                        )
                                    }
                                }
                            }
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
