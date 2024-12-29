package com.thekeeperofpie.artistalleydatabase.utils_compose.filter

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class SortFilterController2<FilterParams, Filtered> {
    protected abstract val sections: MutableStateFlow<List<SortFilterSection>>
    protected abstract val filterParams: StateFlow<FilterParams>
    protected abstract val collapseOnClose: StateFlow<Boolean>

    // TODO: Find a better way to do this
    @Composable
    open fun PromptDialog() {
    }
}
