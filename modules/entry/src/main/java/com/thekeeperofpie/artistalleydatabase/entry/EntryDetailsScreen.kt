package com.thekeeperofpie.artistalleydatabase.entry

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.utils_compose.generated.resources.cancel
import artistalleydatabase.modules.utils_compose.generated.resources.exit
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridDeleteDialog
import com.thekeeperofpie.artistalleydatabase.utils_compose.ArrowBackIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.SnackbarErrorText
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.animateEnterExit
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.topBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
object EntryDetailsScreen {

    @Composable
    operator fun invoke(
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
        var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
        var showCropDialogIndex by rememberSaveable { mutableIntStateOf(-1) }
        val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
            ?.onBackPressedDispatcher
        val pullRefreshState = rememberPullRefreshState(
            refreshing = false,
            onRefresh = { backPressedDispatcher?.onBackPressed() },
            resetAfterPull = false,
        )

        val density = LocalDensity.current
        val offsetY = density.run { pullRefreshState.position.toDp() }
        val scaffoldState = rememberBottomSheetScaffoldState()
        val sectionsLoading = viewModel.sectionsLoading
        var previousValue by remember { mutableStateOf(sectionsLoading) }
        LaunchedEffect(sectionsLoading) {
            if (previousValue != sectionsLoading) {
                previousValue = sectionsLoading
                if (!sectionsLoading) {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }

        val sheetPeekHeight = density.run { BottomSheetDefaults.SheetPeekHeight.roundToPx() }
        var scaffoldHeightPixels by remember { mutableIntStateOf(0) }
        var imageHeightPixels by remember { mutableIntStateOf(0) }

        val coroutineScope = rememberCoroutineScope()
        BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            coroutineScope.launch {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = density.run {
                // Add sheetPeekHeight to offset the spacer below the image that allows scrolling
                // the full image into view, even the part that would've been covered by the sheet
                (scaffoldHeightPixels - imageHeightPixels + sheetPeekHeight)
                    .coerceAtLeast(sheetPeekHeight).toDp()
            },
            sheetDragHandle = null,
            sheetContent = {
                BottomSheet(
                    viewModel = viewModel,
                    onClickSave = onClickSave,
                    onLongClickSave = onLongClickSave,
                    onClickSaveTemplate = onClickSaveTemplate,
                    onNavigate = onNavigate, onShowDeleteDialogChange = { showDeleteDialog = it },
                )
            },
            snackbarHost = {
                val errorRes = viewModel.errorResource
                SnackbarErrorText(
                    error = {
                        errorRes?.first?.leftOrNull()
                            ?.let { ComposeResourceUtils.stringResource(it) }
                            ?: errorRes?.first?.rightOrNull()?.let { stringResource(it) }
                    },
                    exception = errorRes?.second,
                    onErrorDismiss = { viewModel.errorResource = null },
                )
            },
            modifier = Modifier
                .imePadding()
                .offset(y = offsetY)
                .pullRefresh(pullRefreshState)
                .fillMaxSize()
                .onSizeChanged { scaffoldHeightPixels = it.height }
        ) {
            Box {
                HeaderImage(
                    imageState = { viewModel.entryImageController.imageState },
                    imageCornerDp = imageCornerDp,
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
                    modifier = Modifier.onSizeChanged { imageHeightPixels = it.height }
                )

                // TODO: NavHostController#navigateUp doesn't invoke the back press handler
                ArrowBackIconButton(
                    onClick = {
                        if (viewModel.onNavigateBack()) {
                            onClickBack()
                        }
                    },
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                        .animateEnterExit()
                        .align(Alignment.TopStart)
                        .background(
                            Color.DarkGray.copy(alpha = 0.33f),
                            RoundedCornerShape(bottomEnd = 8.dp),
                        )
                )
            }
        }

        if (showDeleteDialog) {
            EntryGridDeleteDialog(
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

    @Composable
    private fun ColumnScope.BottomSheet(
        viewModel: EntryDetailsViewModel<*, *>,
        onClickSave: () -> Unit = {},
        onLongClickSave: () -> Unit = {},
        onClickSaveTemplate: (() -> Unit)? = null,
        onNavigate: (String) -> Unit,
        onShowDeleteDialogChange: (Boolean) -> Unit,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                .animateEnterExit(
                    enter = slideInVertically { it * 2 },
                    // TODO: Exit doesn't work
                    exit = slideOutVertically { it * 2 },
                )
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            // TODO: Semantics
            BottomSheetDefaults.DragHandle()

            EntryForm(
                areSectionsLoading = { viewModel.sectionsLoading },
                sections = { viewModel.sections },
                onNavigate = onNavigate,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
            )
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
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                    .animateEnterExit(
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it },
                    )
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

                TextButton(onClick = { onShowDeleteDialogChange(true) }) {
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

    @Composable
    private fun ConfirmExitDialog(
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
                            Text(text = oldText)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(text = newText)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                    onConfirmExit()
                }) {
                    Text(ComposeResourceUtils.stringResource(UtilsStrings.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(ComposeResourceUtils.stringResource(UtilsStrings.cancel))
                }
            },
        )
    }

    @Composable
    private fun HeaderImage(
        imageState: () -> EntryImageState,
        imageCornerDp: Dp?,
        cropState: () -> CropUtils.CropState,
        modifier: Modifier = Modifier,
        onImageClickOpen: (index: Int) -> Unit = {},
    ) {
        Box(modifier = modifier) {
            val images = imageState().images()
            val addAllowed = imageState().addAllowed()
            val size = if (addAllowed) images.size + 1 else images.size
            val pagerState = rememberPagerState(pageCount = { size })
            MultiImageSelectBox(
                pagerState = pagerState,
                imageState = imageState,
                cropState = cropState,
                onClickOpenImage = onImageClickOpen,
            ) { entryImage, zoomPanState ->
                val uri = entryImage.croppedUri ?: entryImage.uri
                if (uri != null) {
                    val cornerDp = imageCornerDp?.let {
                        LocalAnimatedVisibilityScope.current.transition
                            .animateDp(label = "HeaderImage corner radius") {
                                // TODO: This doesn't actually work
                                when (it) {
                                    EnterExitState.PreEnter -> imageCornerDp
                                    EnterExitState.Visible -> 0.dp
                                    EnterExitState.PostExit -> imageCornerDp
                                }
                            }
                    }?.value
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val minimumHeight = screenWidth * entryImage.widthToHeightRatio
                    val sharedTransitionKey = entryImage.entryId?.scopedId
                        ?.let { SharedTransitionKey.makeKeyForId(it) }
                    Column {
                        ZoomPanBox(state = zoomPanState) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uri)
                                    .crossfade(false)
                                    .placeholderMemoryCacheKey(
                                        EntryUtils.getImageCacheKey(
                                            entryImage,
                                            entryImage.croppedWidth ?: entryImage.width,
                                            entryImage.croppedHeight ?: entryImage.height,
                                        )
                                    )
                                    .listener { _, result ->
                                        imageState().onSizeResult(
                                            result.image.width,
                                            result.image.height,
                                        )
                                    }
                                    .build(),
                                contentDescription = stringResource(
                                    R.string.entry_image_content_description
                                ),
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .sharedElement(sharedTransitionKey, "entry_image")
                                    .fillMaxWidth()
                                    .heightIn(min = minimumHeight)
                                    .conditionally(imageCornerDp != null) {
                                        clip(RoundedCornerShape(cornerDp ?: 0.dp))
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(BottomSheetDefaults.SheetPeekHeight))
                    }
                } else {
                    Spacer(
                        Modifier
                            .height(200.dp)
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
