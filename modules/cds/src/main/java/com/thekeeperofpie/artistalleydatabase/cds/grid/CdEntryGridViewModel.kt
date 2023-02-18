package com.thekeeperofpie.artistalleydatabase.cds.grid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel

abstract class CdEntryGridViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDao,
): ViewModel(), EntryGridViewModel<CdEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            it.forEach {
                EntryUtils.getImageFile(application, it.id).delete()
                cdEntryDao.delete(it.id.valueId)
            }
        }
}