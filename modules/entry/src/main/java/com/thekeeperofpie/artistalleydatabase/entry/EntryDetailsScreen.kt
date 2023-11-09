package com.thekeeperofpie.artistalleydatabase.entry

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mxalbert.sharedelements.FadeMode
import com.mxalbert.sharedelements.ProgressThresholds
import com.mxalbert.sharedelements.SharedElement
import com.mxalbert.sharedelements.SharedElementsTransitionSpec
import com.thekeeperofpie.artistalleydatabase.android_utils.AnimationUtils
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.compose.AddBackPressInvokeTogether
import com.thekeeperofpie.artistalleydatabase.compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.pullRefresh
import com.thekeeperofpie.artistalleydatabase.compose.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.compose.topBorder
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGrid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object EntryDetailsScreen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    operator fun invoke(
        screenKey: String,
        viewModel: EntryDetailsViewModel<*, *>,
        onClickBack: () -> Unit,
        imageCornerDp: Dp? = null,
        onImageClickOpen: (index: Int) -> Unit = {},
        onClickSave: () -> Unit = {},
        onLongClickSave: () -> Unit = {},
        onConfirmDelete: () -> Unit = {},
        onClickSaveTemplate: (() -> Unit)? = null,
        onExitConfirm: () -> Unit = {},
        onNavigate: (String) -> Unit,
    ) {
        Scaffold(
            snackbarHost = {
                val errorRes = viewModel.errorResource
                SnackbarErrorText(
                    errorRes?.first,
                    errorRes?.second,
                    onErrorDismiss = { viewModel.errorResource = null },
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
            AddBackPressInvokeTogether(terminal = true, label = "Form sections alpha") {
                it.launch {
                    delay(AnimationUtils.multipliedByAnimatorScale(context, 150L))
                    enabled = false
                }
                alphaAnimation.animateTo(0f, tween(200))
            }

            val offsetY = LocalDensity.current.run { pullRefreshState.position.toDp() }

            Box {
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
                            screenKey = screenKey,
                            imageState = { viewModel.entryImageController.imageState },
                            imageCornerDp = imageCornerDp,
                            loading = { viewModel.sectionsLoading },
                            onImageClickOpen = onImageClickOpen,
                            cropState = {
                                val cropState = viewModel.entryImageController.cropState
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
                            EntryForm(
                                areSectionsLoading = { viewModel.sectionsLoading },
                                sections = { viewModel.sections },
                                onNavigate = onNavigate,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !viewModel.sectionsLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .topBorder(MaterialTheme.colorScheme.inversePrimary)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            if (onClickSaveTemplate != null) {
                                TextButton(onClick = onClickSaveTemplate) {
                                    Text(
                                        text = stringResource(R.string.save_template),
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            end = 16.dp,
                                            top = 10.dp,
                                            bottom = 10.dp
                                        )
                                    )
                                }
                            }

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
                                Crossfade(
                                    targetState = viewModel.saving,
                                    label = "Entry details save indicator crossfade"
                                ) {
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

                val backAlphaAnimation = remember { Animatable(0f) }
                LaunchedEffect(true) {
                    delay(
                        AnimationUtils.multipliedByAnimatorScale(
                            context,
                            EntryUtils.SLIDE_DURATION_MS.toLong()
                        )
                    )
                    backAlphaAnimation.animateTo(1f)
                }

                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer { alpha = backAlphaAnimation.value }
                        .background(
                            Color.DarkGray.copy(alpha = 0.33f),
                            RoundedCornerShape(bottomEnd = 8.dp),
                        )
                ) {
                    ArrowBackIconButton(onClickBack)
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
                    onConfirm = {
                        viewModel.entryImageController.cropState
                            .onCropConfirmed(showCropDialogIndex)
                    },
                )
            } else if (viewModel.showExitPrompt) {
                ConfirmExitDialog(
                    onDismiss = viewModel::onExitDismiss,
                    onConfirmExit = onExitConfirm,
                    diffText = viewModel::entrySerializedFormDiff,
                )
            }
        }
    }

    @Composable
    fun ConfirmExitDialog(
        onDismiss: () -> Unit,
        onConfirmExit: () -> Unit,
        diffText: () -> Pair<String, String>,
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.entry_confirm_exit_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.entry_confirm_exit_description))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        val (oldText, newText) = diffText()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CustomHtmlText(text = oldText)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CustomHtmlText(text = newText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                    onConfirmExit()
                }) {
                    Text(stringResource(UtilsStringR.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(UtilsStringR.cancel))
                }
            },
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HeaderImage(
        screenKey: String,
        imageState: () -> ImageState,
        imageCornerDp: Dp?,
        loading: () -> Boolean = { false },
        onImageClickOpen: (index: Int) -> Unit = {},
        cropState: () -> CropUtils.CropState,
    ) {
        Box {
            val alphaAnimation = remember { Animatable(1f) }
            AddBackPressInvokeTogether(label = "FAB alpha") {
                alphaAnimation.animateTo(0f, tween(250))
            }
            val images = imageState().images()
            val addAllowed = imageState().addAllowed()
            val size = if (addAllowed) images.size + 1 else images.size
            val pagerState = rememberPagerState(pageCount = { size })
            MultiImageSelectBox(
                pagerState = pagerState,
                imageState = imageState,
                cropState = cropState,
                loading = loading,
                onClickOpenImage = onImageClickOpen,
            ) {
                val uri = it.croppedUri ?: it.uri
                if (uri != null) {
                    var transitionProgress by remember { mutableStateOf(1f) }
                    val cornerDp = imageCornerDp?.let { lerp(it, 0.dp, transitionProgress) }
                    SharedElement(
                        key = "${it.entryId?.scopedId}_image",
                        screenKey = screenKey,
                        // Try to disable the fade animation
                        transitionSpec = SharedElementsTransitionSpec(
                            fadeMode = FadeMode.In,
                            fadeProgressThresholds = ProgressThresholds(0f, 0f),
                        ),
                        onFractionChanged = { transitionProgress = it },
                    ) {
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        val minimumHeight = screenWidth * it.widthToHeightRatio
                        AsyncImage(
                            ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(false)
                                .placeholderMemoryCacheKey(
                                    EntryUtils.getImageCacheKey(
                                        it,
                                        it.croppedWidth ?: it.width,
                                        it.croppedHeight ?: it.height,
                                    )
                                )
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
                                .conditionally(imageCornerDp != null) {
                                    clip(RoundedCornerShape(cornerDp ?: 0.dp))
                                }
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
    }
}
