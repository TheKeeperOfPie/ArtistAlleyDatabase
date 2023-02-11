package com.thekeeperofpie.artistalleydatabase.entry.grid

interface EntryGridViewModel<T: EntryGridModel> {

    val entryGridSelectionController: EntryGridSelectionController<T>

    val selectedEntries get() = entryGridSelectionController.selectedEntries

    fun clearSelected() = entryGridSelectionController.clearSelected()

    fun selectEntry(index: Int, entry: T) =
        entryGridSelectionController.selectEntry(index, entry)

    fun deleteSelected() = entryGridSelectionController.deleteSelected()
}