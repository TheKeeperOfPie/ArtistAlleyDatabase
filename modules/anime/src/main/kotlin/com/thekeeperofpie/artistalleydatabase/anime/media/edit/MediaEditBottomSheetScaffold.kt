package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ClickableBottomSheetDragHandle
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
object MediaEditBottomSheetScaffold {

    @Composable
    operator fun invoke(
        screenKey: String,
        viewModel: MediaEditViewModel,
        modifier: Modifier = Modifier,
        topBar: @Composable (() -> Unit)? = null,
        colorCalculationState: ColorCalculationState,
        bottomNavigationState: BottomNavigationState? = null,
        sheetState: androidx.compose.material3.SheetState = androidx.compose.material3.rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = viewModel::onEditSheetValueChange,
            skipHiddenState = false,
        ),
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val bottomSheetScaffoldState = androidx.compose.material3.rememberBottomSheetScaffoldState(
            bottomSheetState = sheetState,
            snackbarHostState = snackbarHostState,
        )

        val bottomSheetShowing = viewModel.editData.showing
        LaunchedEffect(bottomSheetShowing) {
            if (bottomSheetShowing) {
                launch { sheetState.expand() }
            } else {
                launch { sheetState.hide() }
                    .invokeOnCompletion { viewModel.hide() }
            }
        }

        val currentValue = sheetState.currentValue
        LaunchedEffect(currentValue) {
            if (currentValue != SheetValue.Expanded) {
                viewModel.hide()
            }
        }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = sheetState.targetValue == SheetValue.Expanded
                && !WindowInsets.isImeVisible) {
            if (viewModel.onEditSheetValueChange(SheetValue.Hidden)) {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { viewModel.hide() }
            }
        }

        val error = viewModel.editData.error
        val errorString = error?.first?.let { stringResource(it) }
        LaunchedEffect(error) {
            if (error != null) {
                snackbarHostState.showSnackbar(
                    message = errorString.orEmpty(),
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )

                viewModel.editData.error = null
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
                    screenKey = screenKey,
                    viewModel,
                    colorCalculationState = colorCalculationState,
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.hide()
                                }
                            }
                    },
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
            },
            modifier = modifier,
            content = content,
        )
    }
}
