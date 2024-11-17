package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.ScoreFormat
import com.thekeeperofpie.artistalleydatabase.utils_compose.BackHandler
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ClickableBottomSheetDragHandle
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object MediaEditBottomSheetScaffold {

    @Composable
    operator fun invoke(
        viewModel: MediaEditViewModel,
        modifier: Modifier = Modifier,
        topBar: @Composable() (() -> Unit)? = null,
        bottomNavigationState: BottomNavigationState? = null,
        sheetState: SheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = viewModel::onEditSheetValueChange,
            skipHiddenState = false,
        ),
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val initialParams by viewModel.initialParams.collectAsState()
        val scoreFormat by viewModel.scoreFormat.collectAsState()
        MediaEditBottomSheetScaffold(
            editData = viewModel.editData,
            onEditSheetValueChange = viewModel::onEditSheetValueChange,
            onHide = viewModel::hide,
            onAttemptDismiss = viewModel::attemptDismiss,
            initialParams = { initialParams },
            onClickSave = viewModel::onClickSave,
            onClickDelete = viewModel::onClickDelete,
            onStatusChange = viewModel::onStatusChange,
            scoreFormat = { scoreFormat },
            onDateChange = viewModel::onDateChange,
            modifier= modifier,
            topBar = topBar,
            bottomNavigationState = bottomNavigationState,
            sheetState = sheetState,
            snackbarHostState = snackbarHostState,
            content = content,
        )
    }

    @Composable
    operator fun invoke(
        editData: MediaEditState,
        onEditSheetValueChange: (SheetValue) -> Boolean,
        onHide: () -> Unit,
        onAttemptDismiss: () -> Boolean,
        initialParams: () -> MediaEditState.InitialParams?,
        onClickSave: () -> Unit,
        onClickDelete: () -> Unit,
        onStatusChange: (MediaListStatus?) -> Unit,
        onDateChange: (start: Boolean, Long?) -> Unit,
        scoreFormat: () -> ScoreFormat,
        modifier: Modifier = Modifier,
        topBar: @Composable() (() -> Unit)? = null,
        bottomNavigationState: BottomNavigationState? = null,
        sheetState: SheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = onEditSheetValueChange,
            skipHiddenState = false,
        ),
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState,
            snackbarHostState = snackbarHostState,
        )

        val bottomSheetShowing = editData.showing
        LaunchedEffect(bottomSheetShowing) {
            if (bottomSheetShowing) {
                launch { sheetState.expand() }
            } else {
                launch { sheetState.hide() }
                    .invokeOnCompletion { onHide() }
            }
        }

        val scope = rememberCoroutineScope()
        val currentValue = sheetState.currentValue
        LaunchedEffect(currentValue) {
            if (currentValue != SheetValue.Expanded) {
                if (onAttemptDismiss()) {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { onHide() }
                } else {
                    sheetState.expand()
                }
            }
        }

        BackHandler(enabled = sheetState.targetValue == SheetValue.Expanded
                && !WindowInsets.isImeVisibleKmp) {
            if (onAttemptDismiss()) {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { onHide() }
            }
        }

        val error = editData.error
        val errorString = error?.first?.let { stringResource(it) }
        LaunchedEffect(error) {
            if (error != null) {
                snackbarHostState.showSnackbar(
                    message = errorString.orEmpty(),
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )

                editData.error = null
            }
        }

        val bottomPadding = bottomNavigationState?.bottomOffsetPadding() ?: 0.dp
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            topBar = topBar,
            sheetPeekHeight = 0.dp,
            sheetDragHandle = { ClickableBottomSheetDragHandle(scope, sheetState) },
            snackbarHost = {
                SnackbarHost(it, modifier = Modifier.padding(bottom = bottomPadding))
            },
            sheetContent = {
                AnimeMediaEditBottomSheet(
                    initialParams = initialParams,
                    editData = { editData },
                    onClickSave = onClickSave,
                    onClickDelete = onClickDelete,
                    onStatusChange = onStatusChange,
                    scoreFormat = scoreFormat,
                    onDateChange = onDateChange,
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onHide()
                                }
                            }
                    },
                    modifier = Modifier
                        .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1.1f)
                        .padding(bottom = bottomPadding),
                )
            },
            modifier = modifier,
            content = content,
        )
    }
}
