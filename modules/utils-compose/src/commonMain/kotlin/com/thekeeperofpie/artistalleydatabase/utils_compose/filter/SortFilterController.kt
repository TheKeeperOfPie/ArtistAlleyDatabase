package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import app.cash.molecule.launchMolecule
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeUiDispatcher
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.debounce
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration.Companion.seconds

abstract class SortFilterController<FilterParams : Any>(scope: CoroutineScope) {
    protected abstract val sections: List<SortFilterSection>
    val state = State()

    private val moleculeScope = CoroutineScope(scope.coroutineContext + ComposeUiDispatcher.Main)
    val filterParams by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(ComposeUiDispatcher.recompositionMode) {
            debounce(currentValue = filterParams(), duration = 1.seconds)
        }
    }

    @Composable
    protected abstract fun filterParams(): FilterParams

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }

    @Deprecated("Use method inside State")
    @Composable
    fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyGridState.scrollToItem(0)
        }
    }

    @Deprecated("Use method inside State")
    @Composable
    fun ImmediateScrollResetEffect(lazyStaggeredGridState: LazyStaggeredGridState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyStaggeredGridState.scrollToItem(0)
        }
    }

    @Deprecated("Use method inside State")
    @Composable
    fun ImmediateScrollResetEffect(lazyListState: LazyListState) {
        OnChangeEffect(currentValue = filterParams.collectAsState().value) {
            lazyListState.scrollToItem(0)
        }
    }

    @Composable
    open fun collapseOnClose() = false

    @Stable
    inner class State internal constructor() {
        val sections get() = this@SortFilterController.sections
        val expanded = SortFilterSection.ExpandedState()
        val collapseOnClose @Composable get() = this@SortFilterController.collapseOnClose()

        @Composable
        fun ImmediateScrollResetEffect(lazyGridState: LazyGridState) {
            OnChangeEffect(currentValue = filterParams.collectAsState().value) {
                lazyGridState.scrollToItem(0)
            }
        }
    }
}
