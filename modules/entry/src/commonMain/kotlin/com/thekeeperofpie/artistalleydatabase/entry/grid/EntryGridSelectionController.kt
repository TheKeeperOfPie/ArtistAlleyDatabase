package com.thekeeperofpie.artistalleydatabase.entry.grid

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryGridSelectionController<T : EntryGridModel>(
    private val scopeProvider: () -> CoroutineScope,
    private val deleteSelected: suspend (List<T>) -> Unit
) {
    val selectedEntries = mutableStateMapOf<Int, T>()

    fun clearSelected() {
        synchronized(selectedEntries) {
            selectedEntries.clear()
        }
    }

    fun selectEntry(index: Int, entry: T) {
        synchronized(selectedEntries) {
            if (selectedEntries.containsKey(index)) {
                selectedEntries.remove(index)
            } else {
                selectedEntries.put(index, entry)
            }
        }
    }

    fun deleteSelected() {
        synchronized(selectedEntries) {
            scopeProvider().launch(Dispatchers.IO) {
                val toDelete: List<T>
                withContext(Dispatchers.Main) {
                    toDelete = selectedEntries.values.toMutableList()
                    selectedEntries.clear()
                }
                deleteSelected(toDelete)
            }
        }
    }
}