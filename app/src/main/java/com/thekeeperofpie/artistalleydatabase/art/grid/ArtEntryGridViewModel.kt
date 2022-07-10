package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ArtEntryGridViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao
) : ViewModel() {

    val selectedEntries = mutableStateMapOf<Int, ArtEntryGridModel>()

    fun clearSelected() {
        synchronized(selectedEntries) {
            selectedEntries.clear()
        }
    }

    fun selectEntry(index: Int, entry: ArtEntryGridModel) {
        synchronized(selectedEntries) {
            if (selectedEntries.containsKey(index)) {
                selectedEntries.remove(index)
            } else {
                selectedEntries.put(index, entry)
            }
        }
    }

    protected fun deleteSelected() {
        synchronized(selectedEntries) {
            viewModelScope.launch(Dispatchers.IO) {
                val toDelete: List<ArtEntry>
                withContext(Dispatchers.Main) {
                    toDelete = selectedEntries.values.map { it.value }
                    selectedEntries.clear()
                }
                toDelete.forEach {
                    ArtEntryUtils.getImageFile(application, it.id).delete()
                }
                artEntryDao.delete(toDelete)
            }
        }
    }
}