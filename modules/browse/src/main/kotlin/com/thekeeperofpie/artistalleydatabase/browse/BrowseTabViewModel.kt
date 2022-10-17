package com.thekeeperofpie.artistalleydatabase.browse

import androidx.lifecycle.ViewModel

abstract class BrowseTabViewModel : ViewModel() {

    abstract val priorityMajor: Int
    abstract val priorityMinor: Int

    abstract val tab: BrowseScreen.TabContent

    protected fun Iterable<BrowseEntryModel>.sortedByText() = sortedWith { first, second ->
        String.CASE_INSENSITIVE_ORDER.compare(first.text, second.text)
    }
}