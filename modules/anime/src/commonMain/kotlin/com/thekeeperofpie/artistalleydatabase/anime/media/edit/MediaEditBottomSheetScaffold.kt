package com.thekeeperofpie.artistalleydatabase.anime.media.edit

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.utils_compose.BottomNavigationState
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.isImeVisibleKmp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class, ExperimentalSharedTransitionApi::class
)
object MediaEditBottomSheetScaffold {

    fun fromComponent(
        component: AnimeComponent,
    ): MediaEditBottomSheetScaffoldComposable = { content ->
        val editViewModel = viewModel { component.mediaEditViewModel() }
        MediaEditBottomSheetScaffold(
            state = { editViewModel.state },
            eventSink = editViewModel::onEvent,
        ) {
            content(it, editViewModel::initialize)
        }
    }

    @Deprecated("Use state variant instead")
    @Composable
    operator fun invoke(
        viewModel: MediaEditViewModel,
        modifier: Modifier = Modifier,
        topBar: @Composable() (() -> Unit)? = null,
        bottomNavigationState: BottomNavigationState? = null,
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val state = viewModel.state
        MediaEditBottomSheetScaffold(
            state = { state },
            eventSink = viewModel::onEvent,
            modifier = modifier,
            topBar = topBar,
            bottomNavigationState = bottomNavigationState,
            snackbarHostState = snackbarHostState,
            content = content,
        )
    }

    @Composable
    operator fun invoke(
        state: () -> MediaEditState,
        eventSink: (AnimeMediaEditBottomSheet.Event) -> Unit,
        modifier: Modifier = Modifier,
        topBar: @Composable() (() -> Unit)? = null,
        bottomNavigationState: BottomNavigationState? = null,
        snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
        content: @Composable (PaddingValues) -> Unit,
    ) {
        val bottomPadding = bottomNavigationState?.bottomOffsetPadding() ?: 0.dp
        Scaffold(
            topBar = { topBar?.invoke() },
            snackbarHost = {
                val state = state()
                val error = state.error
                val errorString = error?.first?.let { stringResource(it) }
                LaunchedEffect(error) {
                    if (error != null) {
                        snackbarHostState.showSnackbar(
                            message = errorString.orEmpty(),
                            withDismissAction = true,
                            duration = SnackbarDuration.Long,
                        )

                        state.error = null
                    }
                }
                SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = bottomPadding))
            },
            modifier = modifier
        ) {
            content(it)
            val state = state()
            if (state.showing) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val scope = rememberCoroutineScope()
                BackHandler(
                    enabled = sheetState.targetValue == SheetValue.Expanded
                            && !WindowInsets.isImeVisibleKmp
                ) {
                    scope.launch { sheetState.hide() }
                }
                ModalBottomSheet(
                    onDismissRequest = {
                        if (state.hasChanged() && !state.hasConfirmedClose) {
                            state.showConfirmClose = true
                            scope.launch { sheetState.expand() }
                        } else {
                            state.showing = false
                        }
                    },
                    sheetState = sheetState
                ) {
                    AnimeMediaEditBottomSheet(
                        state = { state },
                        eventSink = eventSink,
                        onConfirmExit = {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { state.showing = false }
                        },
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1.1f)
                            .padding(bottom = bottomPadding),
                    )
                }
            }
        }
    }
}
