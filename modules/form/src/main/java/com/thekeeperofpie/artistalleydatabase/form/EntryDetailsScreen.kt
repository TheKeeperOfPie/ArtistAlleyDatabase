package com.thekeeperofpie.artistalleydatabase.form

import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mxalbert.sharedelements.FadeMode
import com.mxalbert.sharedelements.ProgressThresholds
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsTransitionSpec
import com.thekeeperofpie.artistalleydatabase.compose.AddBackPressTransitionStage
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.pullRefresh
import com.thekeeperofpie.artistalleydatabase.compose.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.compose.topBorder
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

object EntryDetailsScreen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    operator fun invoke(
        entryId: () -> String = { "" },
        entryImageFile: () -> File? = { null },
        entryImageRatio: () -> Float = { 1f },
        imageUri: () -> Uri? = { null },
        onImageSelected: (Uri?) -> Unit = {},
        onImageSelectError: (Exception?) -> Unit = {},
        onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
        onImageClickOpen: () -> Unit = {},
        areSectionsLoading: () -> Boolean = { false },
        sections: () -> List<EntrySection> = { emptyList() },
        saving: () -> Boolean = { false },
        onClickSave: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(errorRes()?.first, onErrorDismiss = onErrorDismiss)
            },
            modifier = Modifier.imePadding()
        ) {
            var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                ?.onBackPressedDispatcher
            val pullRefreshState = rememberPullRefreshState(
                refreshing = false,
                onRefresh = { backPressedDispatcher?.onBackPressed() }
            )

            val alphaAnimation = remember { Animatable(1f) }
            AddBackPressTransitionStage(terminal = true) {
                it.launch {
                    delay(100)
                    remove()
                    backPressedDispatcher?.onBackPressed()
                }
                alphaAnimation.animateTo(0f, tween(250))
            }

            val offsetY = LocalDensity.current.run { pullRefreshState.position.toDp() }

            Column(
                Modifier
                    .padding(it)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .offset(y = offsetY)
                        .weight(1f, true)
                        .fillMaxWidth()
                        .pullRefresh(pullRefreshState)
                        .verticalScroll(rememberScrollState())
                ) {
                    HeaderImage(
                        entryId = entryId,
                        entryImageFile = entryImageFile,
                        entryImageRatio = entryImageRatio,
                        loading = areSectionsLoading,
                        imageUri = imageUri,
                        onImageSelected = onImageSelected,
                        onImageSelectError = onImageSelectError,
                        onImageSizeResult = onImageSizeResult,
                        onImageClickOpen = onImageClickOpen,
                    )

                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.graphicsLayer { alpha = alphaAnimation.value }
                    ) {
                        EntryForm(areSectionsLoading, sections)
                    }
                }

                AnimatedVisibility(
                    visible = !areSectionsLoading(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .topBorder(1.dp, MaterialTheme.colorScheme.inversePrimary)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text(
                                text = stringResource(R.string.delete),
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 10.dp
                                )
                            )
                        }

                        TextButton(onClick = onClickSave) {
                            Crossfade(targetState = saving()) {
                                if (it) {
                                    CircularProgressIndicator()
                                } else {
                                    Text(
                                        text = stringResource(R.string.save),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 10.dp,
                                            bottom = 10.dp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            EntryGrid.DeleteDialog(
                showDeleteDialog,
                { showDeleteDialog = false },
                onConfirmDelete
            )
        }
    }

    @Composable
    private fun HeaderImage(
        entryId: () -> String,
        entryImageFile: () -> File?,
        entryImageRatio: () -> Float,
        loading: () -> Boolean = { false },
        imageUri: () -> Uri?,
        onImageSelected: (Uri?) -> Unit,
        onImageSelectError: (Exception?) -> Unit,
        onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
        onImageClickOpen: () -> Unit,
    ) {
        Box {
            val alphaAnimation = remember { Animatable(1f) }
            AddBackPressTransitionStage { alphaAnimation.animateTo(0f, tween(250)) }

            ImageSelectBox(onImageSelected, onImageSelectError, loading) {
                @Suppress("NAME_SHADOWING")
                val imageUri = imageUri()
                if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .listener { _, result ->
                                onImageSizeResult(
                                    result.drawable.intrinsicWidth,
                                    result.drawable.intrinsicHeight,
                                )
                            }
                            .build(),
                        contentDescription = stringResource(
                            R.string.entry_image_content_description
                        ),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                } else {
                    @Suppress("NAME_SHADOWING")
                    val entryImageFile = entryImageFile()
                    if (entryImageFile != null) {
                        @Suppress("NAME_SHADOWING")
                        val entryImageRatio = entryImageRatio()

                        @Suppress("NAME_SHADOWING")
                        val entryId = entryId()
                        SharedElement(
                            key = "${entryId}_image",
                            screenKey = "artEntryDetails",
                            // Try to disable the fade animation
                            transitionSpec = SharedElementsTransitionSpec(
                                fadeMode = FadeMode.In,
                                fadeProgressThresholds = ProgressThresholds(0f, 0f),
                            )
                        ) {
                            val configuration = LocalConfiguration.current
                            val screenWidth = configuration.screenWidthDp.dp
                            val minimumHeight = screenWidth * entryImageRatio
                            AsyncImage(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(entryImageFile)
                                    .crossfade(false)
                                    .placeholderMemoryCacheKey("coil_memory_entry_image_home_$entryId")
                                    .build(),
                                contentDescription = stringResource(
                                    R.string.entry_image_content_description
                                ),
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = minimumHeight)
                            )
                        }
                    } else {
                        Spacer(
                            Modifier
                                .heightIn(200.dp, 200.dp)
                                .fillMaxWidth()
                                .background(Color.LightGray)
                        )
                        Icon(
                            imageVector = Icons.Default.ImageNotSupported,
                            contentDescription = stringResource(
                                R.string.entry_no_image_content_description
                            ),
                            Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !loading() && entryImageFile() != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .graphicsLayer { alpha = alphaAnimation.value }
                    .align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = onImageClickOpen,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = stringResource(
                            R.string.entry_open_full_image_content_description
                        ),
                    )
                }
            }
        }
    }
}