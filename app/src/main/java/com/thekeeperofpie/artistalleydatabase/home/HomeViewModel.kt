package com.thekeeperofpie.artistalleydatabase.home

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
) : SearchViewModel(application, artEntryDao) {

    fun onDeleteSelected() {
        super.deleteSelected()
    }
}