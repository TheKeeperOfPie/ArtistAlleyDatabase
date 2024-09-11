package com.thekeeperofpie.artistalleydatabase.art.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.art.data.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.entry.EntryUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridViewModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.deleteRecursively

abstract class ArtEntryGridViewModel(
    protected val appFileSystem: AppFileSystem,
    protected val artEntryDao: ArtEntryDao
) : ViewModel(), EntryGridViewModel<ArtEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            it.forEach {
                appFileSystem.deleteRecursively(EntryUtils.getEntryImageFolder(appFileSystem, it.id))
                artEntryDao.delete(it.id.valueId)
            }
        }
}
