package com.thekeeperofpie.artistalleydatabase.form

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.pager.rememberPagerState
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
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.compose.AddBackPressTransitionStage
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.pullRefresh
import com.thekeeperofpie.artistalleydatabase.compose.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.compose.topBorder
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object EntryDetailsScreen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    operator fun invoke(
        imageState: () -> ImageState,
        onImageClickOpen: (index: Int) -> Unit = {},
        areSectionsLoading: () -> Boolean = { false },
        sections: () -> List<EntrySection> = { emptyList() },
        saving: () -> Boolean = { false },
        onClickSave: () -> Unit = {},
        onLongClickSave: () -> Unit = {},
        errorRes: () -> Pair<Int, Exception?>? = { null },
        onErrorDismiss: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
        cropState: CropUtils.CropState,
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarErrorText(
                    errorRes()?.first,
                    errorRes()?.second,
                    onErrorDismiss = onErrorDismiss
                )
            },
            modifier = Modifier.imePadding()
        ) {
            var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
            var showCropDialogIndex by rememberSaveable { mutableStateOf(-1) }
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                ?.onBackPressedDispatcher
            val pullRefreshState = rememberPullRefreshState(
                refreshing = false,
                onRefresh = { backPressedDispatcher?.onBackPressed() }
            )

            val context = LocalContext.current
            val alphaAnimation = remember { Animatable(1f) }
            AddBackPressTransitionStage(terminal = true) {
                it.launch {
                    delay(AnimationUtils.multipliedByAnimatorScale(context, 150L))
                    remove()
                    backPressedDispatcher?.onBackPressed()
                }
                alphaAnimation.animateTo(0f, tween(200))
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
                        imageState = imageState,
                        loading = areSectionsLoading,
                        onImageClickOpen = onImageClickOpen,
                        cropState = {
                            cropState.copy(onImageRequestCrop = { index ->
                                if (cropState.imageCropNeedsDocument()) {
                                    showCropDialogIndex = index
                                } else {
                                    cropState.onImageRequestCrop(index)
                                }
                            })
                        },
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

                        TextButton(
                            onClick = onClickSave,
                            modifier = Modifier.combinedClickable(
                                onClick = onClickSave,
                                onLongClick = onLongClickSave,
                                onLongClickLabel = stringResource(R.string.save_skip_errors)
                            )
                        ) {
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

            if (showDeleteDialog) {
                EntryGrid.DeleteDialog(
                    { showDeleteDialog = false },
                    onConfirmDelete
                )
            } else if (showCropDialogIndex != -1) {
                CropUtils.InstructionsDialog(
                    onDismiss = { showCropDialogIndex = -1 },
                    onConfirm = { cropState.onCropConfirmed(showCropDialogIndex) },
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HeaderImage(
        imageState: () -> ImageState,
        loading: () -> Boolean = { false },
        onImageClickOpen: (index: Int) -> Unit = {},
        cropState: () -> CropUtils.CropState,
    ) {
        Box {
            val alphaAnimation = remember { Animatable(1f) }
            AddBackPressTransitionStage { alphaAnimation.animateTo(0f, tween(250)) }
            val pagerState = rememberPagerState()
            MultiImageSelectBox(
                pagerState = pagerState,
                imageState = imageState,
                cropState = cropState,
                loading = loading,
            ) {
                val uri = it.croppedUri ?: it.uri
                if (uri != null) {
                    SharedElement(
                        key = "${it.entryId}_image",
                        screenKey = "artEntryDetails",
                        // Try to disable the fade animation
                        transitionSpec = SharedElementsTransitionSpec(
                            fadeMode = FadeMode.In,
                            fadeProgressThresholds = ProgressThresholds(0f, 0f),
                        )
                    ) {
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        val minimumHeight = screenWidth * (it.height / it.width)
                        AsyncImage(
                            ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(false)
                                .placeholderMemoryCacheKey(EntryUtils.getImageCacheKey(it))
                                .listener { _, result ->
                                    imageState().onSizeResult(
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

            AnimatedVisibility(
                visible = !loading() && imageState().images().isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .graphicsLayer { alpha = alphaAnimation.value }
                    .align(Alignment.BottomEnd)
            ) {
                FloatingActionButton(
                    onClick = { onImageClickOpen(pagerState.currentPage) },
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