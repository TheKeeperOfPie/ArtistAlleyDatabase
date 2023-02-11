package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.utils.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel

abstract class ArtEntryGridViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao
) : ViewModel(), EntryGridViewModel<ArtEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                ArtEntryUtils.getImageFile(application, it.entryId).delete()
            }
            artEntryDao.delete(toDelete)
        }
}