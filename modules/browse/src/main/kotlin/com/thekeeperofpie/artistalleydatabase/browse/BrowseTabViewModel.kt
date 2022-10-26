package com.thekeeperofpie.artistalleydatabase.browse

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

abstract class BrowseTabViewModel : ViewModel() {

    abstract val priorityMajor: Int
    abstract val priorityMinor: Int

    abstract val tab: BrowseScreen.TabContent

    private val initializationBarrier = MutableStateFlow(false)

    protected fun initializationBarrier() = initializationBarrier.filter { it }.map { /* Unit */ }

    fun startLoad() {
        initializationBarrier.tryEmit(true)
    }

    protected fun Iterable<BrowseEntryModel>.sortedByText() = sortedWith { first, second ->
        String.CASE_INSENSITIVE_ORDER.compare(first.text, second.text)
    }
}