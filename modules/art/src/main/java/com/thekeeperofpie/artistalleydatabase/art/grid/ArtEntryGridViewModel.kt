package com.thekeeperofpie.artistalleydatabase.art.grid

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryUtils
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridSelectionController
import com.thekeeperofpie.artistalleydatabase.form.grid.EntryGridViewModel

abstract class ArtEntryGridViewModel(
    protected val application: Application,
    protected val artEntryDao: ArtEntryDao
) : ViewModel(), EntryGridViewModel<ArtEntryGridModel> {

    override val entryGridSelectionController =
        EntryGridSelectionController<ArtEntryGridModel>({ viewModelScope }) {
            val toDelete = it.map { it.value }
            toDelete.forEach {
                ArtEntryUtils.getImageFile(application, it.id).delete()
            }
            artEntryDao.delete(toDelete)
        }
}