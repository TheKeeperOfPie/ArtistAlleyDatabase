package com.thekeeperofpie.artistalleydatabase.browse

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    tabViewModels: Set<@JvmSuppressWildcards BrowseTabViewModel>,
) : ViewModel() {

    val tabs = tabViewModels.sortedWith(compareBy({ it.priorityMajor }, { it.priorityMinor }))
            .map { it.tab }

    fun onSelectEntry(
        navController: NavHostController,
        tabContent: BrowseScreen.TabContent,
        entry: BrowseEntryModel
    ) {
        tabContent.onSelected(navController, entry)
    }
}