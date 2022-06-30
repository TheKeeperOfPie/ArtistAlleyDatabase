package com.thekeeperofpie.artistalleydatabase.art

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class ArtEntryGridViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao
) : ViewModel() {

    val selectedEntries = mutableStateMapOf<Int, ArtEntryModel>()

    fun clearSelected() {
        synchronized(selectedEntries) {
            selectedEntries.clear()
        }
    }

    fun selectEntry(index: Int, entry: ArtEntryModel) {
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