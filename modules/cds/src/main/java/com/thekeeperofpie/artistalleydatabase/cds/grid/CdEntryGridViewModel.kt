package com.thekeeperofpie.artistalleydatabase.cds.grid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.utils.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel

abstract class CdEntryGridViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDao,
): ViewModel(), EntryGridViewModel<CdEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                CdEntryUtils.getImageFile(application, it.entryId).delete()
            }
            cdEntryDao.delete(toDelete)
        }
}