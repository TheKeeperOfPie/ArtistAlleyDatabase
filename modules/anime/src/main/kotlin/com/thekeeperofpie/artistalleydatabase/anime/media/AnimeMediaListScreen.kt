package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.anilist.fragment.AniListListRowMedia
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalMaterialApi::class)
object AnimeMediaListScreen {

    @Composable
    operator fun invoke(
        refreshing: Boolean,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        tagShown: () -> AnimeMediaFilterController.TagSection.Tag? = { null },
        onTagDismiss: () -> Unit = {},
        listContent: @Composable (onLongPressImage: (AnimeMediaListRow.Entry) -> Unit) -> Unit,
    ) {
        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            var entryToPreview by remember { mutableStateOf<AnimeMediaListRow.Entry?>(null) }

            listContent { entryToPreview = it }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            entryToPreview?.let {
                EntryImagePreview(it) { entryToPreview = null }
            }

            tagShown()?.let {
                TagPreview(it, onTagDismiss)
            }
        }
    }

    @Composable
    fun Error() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun NoResults() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_no_results),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun AppendError(onRetry: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRetry),
        ) {
            Text(
                stringResource(id = R.string.anime_media_list_error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
    }

    @Composable
    fun LoadingMore() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun TagPreview(
        tag: AnimeMediaFilterController.TagSection.Tag,
        onDismiss: () -> Unit,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(tag.name) },
            text = {
                Text(
                    tag.description ?: stringResource(R.string.anime_media_tag_no_description_error)
                )
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(UtilsStringR.close))
                }
            }
        )
    }

    @Composable
    private fun EntryImagePreview(entry: AnimeMediaListRow.Entry, onDismiss: () -> Unit) {
        Dialog(onDismissRequest = onDismiss) {
            var imageTranslation by remember { mutableStateOf(Offset.Zero) }
            var imageScale by remember { mutableStateOf(1f) }
            var imageRotation by remember { mutableStateOf(1f) }

            var imageIntrinsicWidth by remember { mutableStateOf(0) }
            var imageIntrinsicHeight by remember { mutableStateOf(0) }

            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val maxTranslationX = remember(imageIntrinsicWidth, density) {
                density.run { configuration.screenWidthDp.dp.toPx() }
                    .coerceAtLeast(imageIntrinsicWidth.toFloat())
                    .times(0.9f)
            }
            val maxTranslationY = remember(imageIntrinsicHeight, density) {
                density.run { configuration.screenHeightDp.dp.toPx() }
                    .coerceAtLeast(imageIntrinsicHeight.toFloat())
                    .times(0.9f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures(panZoomLock = true) { _, pan, zoom, rotation ->
                            val translation = imageTranslation + pan
                            imageTranslation = translation.copy(
                                x = translation.x.coerceIn(
                                    -maxTranslationX,
                                    maxTranslationX
                                ),
                                y = translation.y.coerceIn(
                                    -maxTranslationY,
                                    maxTranslationY
                                ),
                            )
                            imageScale = (imageScale * zoom).coerceIn(0.25f, 5f)
                            imageRotation += rotation
                        }
                    }
                    .graphicsLayer(
                        translationX = imageTranslation.x,
                        translationY = imageTranslation.y,
                        scaleX = imageScale,
                        scaleY = imageScale,
                        rotationZ = imageRotation,
                    )
                    .clickable(onClick = onDismiss),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(
                            // Consume click events so that tapping image doesn't dismiss
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {}
                ) {
                    entry.imageBanner?.let {
                        AsyncImage(
                            model = it,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.anime_media_banner_image),
                            modifier = Modifier.wrapContentSize(),
                        )
                    }
                    AsyncImage(
                        model = entry.imageExtraLarge,
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(R.string.anime_media_cover_image),
                        onSuccess = {
                            imageIntrinsicWidth = it.result.drawable.intrinsicWidth
                            imageIntrinsicHeight = it.result.drawable.intrinsicHeight
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(min = 240.dp),
                    )
                }
            }
        }
    }

    sealed interface Entry {

        val id: EntryId

        class Item(media: AniListListRowMedia) : Entry, AnimeMediaListRow.MediaEntry(media)
    }
}