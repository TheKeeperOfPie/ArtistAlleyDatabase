package com.thekeeperofpie.artistalleydatabase.entry.grid

import androidx.compose.runtime.mutableStateMapOf
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntryGridSelectionController<T : EntryGridModel>(
    private val scopeProvider: () -> CoroutineScope,
    private val deleteSelected: suspend (List<T>) -> Unit,
) {
    val selectedEntries = mutableStateMapOf<Int, T>()

    fun clearSelected() = selectedEntries.clear()

    fun selectEntry(index: Int, entry: T) {
        if (selectedEntries.containsKey(index)) {
            selectedEntries.remove(index)
        } else {
            selectedEntries.put(index, entry)
        }
    }

    fun deleteSelected() {
        scopeProvider().launch(PlatformDispatchers.IO) {
            val toDelete: List<T>
            withContext(Dispatchers.Main) {
                toDelete = selectedEntries.values.toMutableList()
                selectedEntries.clear()
            }
            deleteSelected(toDelete)
        }
    }
}
