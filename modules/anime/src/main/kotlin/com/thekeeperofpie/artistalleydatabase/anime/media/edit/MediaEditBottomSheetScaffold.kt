package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.MediaDetailsQuery
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
object MediaEditBottomSheetScaffold {

    @Composable
    operator fun invoke(
        screenKey: String,
        viewModel: MediaEditViewModel,
        media: MediaDetailsQuery.Data.Media?,
        topBar: @Composable (() -> Unit)? = null,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        LaunchedEffect(media) {
            val maxProgress = media?.episodes ?: media?.volumes ?: media?.nextAiringEpisode
                ?.episode?.let { (it - 1).coerceAtLeast(1) }
            viewModel.initialize(
                mediaId = media?.id?.toString().orEmpty(),
                media = null, // Don't show preview when context is preserved
                mediaListEntry = media?.mediaListEntry,
                mediaType = media?.type,
                status = media?.mediaListEntry?.status,
                maxProgress = maxProgress,
            )
        }

        Scaffold(
            screenKey = screenKey,
            viewModel = viewModel,
            topBar = topBar,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            clearData = false,
            content = content,
        )
    }

    @Composable
    operator fun invoke(
        screenKey: String,
        viewModel: MediaEditViewModel,
        topBar: @Composable (() -> Unit)? = null,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = viewModel::onEditSheetValueChange,
            skipHiddenState = false,
        )

        Scaffold(
            screenKey = screenKey,
            viewModel = viewModel,
            topBar = topBar,
            colorCalculationState = colorCalculationState,
            navigationCallback = navigationCallback,
            bottomNavigationState = bottomNavigationState,
            sheetState = sheetState,
            clearData = true,
            content = content,
        )
    }

    @Composable
    private fun Scaffold(
        screenKey: String,
        viewModel: MediaEditViewModel,
        topBar: @Composable (() -> Unit)? = null,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
        bottomNavigationState: BottomNavigationState? = null,
        sheetState: SheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            confirmValueChange = viewModel::onEditSheetValueChange,
            skipHiddenState = false,
        ),
        clearData: Boolean,
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(sheetState)

        val bottomSheetShowing = viewModel.editData.showing
        LaunchedEffect(bottomSheetShowing) {
            if (bottomSheetShowing) {
                launch { sheetState.expand() }
            } else {
                launch { sheetState.hide() }
                    .invokeOnCompletion { viewModel.hide(clearData) }
            }
        }

        val currentValue = sheetState.currentValue
        LaunchedEffect(currentValue) {
            if (currentValue != SheetValue.Expanded) {
                viewModel.hide(clearData)
            }
        }

        val scope = rememberCoroutineScope()
        BackHandler(enabled = sheetState.currentValue != SheetValue.Hidden) {
            if (viewModel.onEditSheetValueChange(SheetValue.Hidden)) {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion { viewModel.hide(clearData) }
            }
        }

        val error = viewModel.editData.error
        val errorString = error?.first?.let { stringResource(it) }
        LaunchedEffect(error) {
            if (error != null) {
                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                    message = errorString.orEmpty(),
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )

                viewModel.editData.error = null
            }
        }

        val bottomPadding = bottomNavigationState
            ?.run { bottomNavBarPadding() + bottomOffset() } ?: 0.dp
        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            topBar = topBar,
            sheetPeekHeight = 0.dp,
            snackbarHost = {
                SnackbarHost(it, modifier = Modifier.padding(bottom = bottomPadding))
            },
            sheetContent = {
                AnimeMediaEditBottomSheet(
                    screenKey = screenKey,
                    viewModel,
                    colorCalculationState = colorCalculationState,
                    navigationCallback = navigationCallback,
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    viewModel.hide(clearData)
                                }
                            }
                    },
                    modifier = Modifier.padding(bottom = bottomPadding)
                )
            },
            content = content,
        )
    }
}
