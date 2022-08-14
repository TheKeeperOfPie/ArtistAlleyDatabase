/**
 * Redirects Material non-3 APIs so that the non-3 dependencies are not accidentally imported over
 * the Material3 variants.
 */

package com.thekeeperofpie.artistalleydatabase.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalMaterialApi::class)
class DismissState(internal val dismissState: androidx.compose.material.DismissState)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberDismissState(@StringRes errorRes: Int?, onErrorDismiss: () -> Unit): DismissState {
    val dismissState = androidx.compose.material.rememberDismissState()
    if (dismissState.currentValue != DismissValue.Default) {
        LaunchedEffect(errorRes) {
            onErrorDismiss()
            dismissState.snapTo(DismissValue.Default)
        }
    }
    return DismissState(dismissState)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismiss(
    state: DismissState,
    modifier: Modifier = Modifier,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit
) {
    androidx.compose.material.SwipeToDismiss(
        state = state.dismissState,
        modifier = modifier,
        background = background,
        dismissContent = dismissContent,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerIndicator(pagerState: PagerState, modifier: Modifier) {
    com.google.accompanist.pager.HorizontalPagerIndicator(
        pagerState = pagerState,
        modifier = modifier
    )
}