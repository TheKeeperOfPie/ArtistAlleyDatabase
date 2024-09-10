package com.thekeeperofpie.artistalleydatabase.cds.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively

abstract class CdEntryGridViewModel(
    protected val appFileSystem: AppFileSystem,
    protected val cdEntryDao: CdEntryDao,
): ViewModel(), EntryGridViewModel<CdEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<CdEntryGridModel>({ viewModelScope }) {
            it.forEach {
                appFileSystem.deleteRecursively(EntryUtils.getEntryImageFolder(appFileSystem, it.id))
                cdEntryDao.delete(it.id.valueId)
            }
        }
}
