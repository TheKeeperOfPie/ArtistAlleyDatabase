package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.anilist.fragment.MediaPreview
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox

@OptIn(ExperimentalMaterialApi::class)
object AnimeMediaListScreen {

    @Composable
    operator fun <MediaType : MediaPreview> invoke(
        refreshing: Boolean,
        onRefresh: () -> Unit,
        modifier: Modifier = Modifier,
        tagShown: () -> AnimeMediaFilterController.TagSection.Tag? = { null },
        onTagDismiss: () -> Unit = {},
        pullRefreshTopPadding: @Composable () -> Dp = { 0.dp },
        listContent: @Composable (
            onLongPressImage: (AnimeMediaListRow.Entry<MediaType>) -> Unit
        ) -> Unit,
    ) {
        val pullRefreshState = rememberPullRefreshState(refreshing, onRefresh)

        Box(
            modifier = modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            var entryToPreview by remember {
                mutableStateOf<AnimeMediaListRow.Entry<MediaType>?>(null)
            }

            listContent { entryToPreview = it }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = pullRefreshTopPadding())
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
    fun Error(
        modifier: Modifier = Modifier,
        @StringRes errorTextRes: Int? = null,
        exception: Throwable? = null,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .wrapContentSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(errorTextRes ?: R.string.anime_media_list_error_loading),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            )

            if (exception != null) {
                Text(
                    text = exception.stackTraceToString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }

    @Composable
    fun NoResults(modifier: Modifier = Modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
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
    private fun <MediaType : MediaPreview> EntryImagePreview(
        entry: AnimeMediaListRow.Entry<MediaType>,
        onDismiss: () -> Unit,
    ) {
        Dialog(onDismissRequest = onDismiss) {
            ZoomPanBox(onClick = onDismiss) {
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
                    entry.media.bannerImage?.let {
                        AsyncImage(
                            model = it,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.anime_media_banner_image),
                            modifier = Modifier.wrapContentSize(),
                        )
                    }
                    AsyncImage(
                        model = entry.media.coverImage?.extraLarge,
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(min = 240.dp),
                    )
                }
            }
        }
    }
}
