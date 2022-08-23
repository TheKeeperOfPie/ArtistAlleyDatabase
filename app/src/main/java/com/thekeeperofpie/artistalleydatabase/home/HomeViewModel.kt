package com.thekeeperofpie.artistalleydatabase.home

import android.app.Application
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.search.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    artEntryDao: ArtEntryDao,
    appJson: AppJson,
) : SearchViewModel(application, artEntryDao, appJson) {

    fun onDeleteSelected() {
        super.deleteSelected()
    }
}