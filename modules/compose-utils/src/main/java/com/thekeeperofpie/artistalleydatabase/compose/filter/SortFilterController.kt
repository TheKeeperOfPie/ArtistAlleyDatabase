package com.thekeeperofpie.artistalleydatabase.compose.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.thekeeperofpie.artistalleydatabase.compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.compose.debounce
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.milliseconds

abstract class SortFilterController<FilterParams>(scope: CoroutineScope) {
    abstract val sections: List<SortFilterSection>
    val state = SortFilterSection.ExpandedState()

    private val moleculeScope = CoroutineScope(scope.coroutineContext + AndroidUiDispatcher.Main)
    val filterParams by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(RecompositionMode.ContextClock) {
            debounce(currentValue = filterParams(), duration = 500.milliseconds)
        }
    }

    @Composable
    protected abstract fun filterParams(): FilterParams

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyGridState.scrollToItem(0)
        }
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyStaggeredGridState: LazyStaggeredGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyStaggeredGridState.scrollToItem(0)
        }
    }

    @Composable
    fun ImmediateScrollResetEffect(lazyListState: LazyListState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyListState.scrollToItem(0)
        }
    }
}
