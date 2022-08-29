package com.thekeeperofpie.artistalleydatabase.cds.grid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.CdEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridViewModel

abstract class CdGridViewModel(
    protected val application: Application,
    protected val cdEntryDao: CdEntryDao,
): ViewModel(), EntryGridViewModel<CdEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                CdEntryUtils.getImageFile(application, it.id).delete()
            }
            cdEntryDao.delete(toDelete)
        }
}