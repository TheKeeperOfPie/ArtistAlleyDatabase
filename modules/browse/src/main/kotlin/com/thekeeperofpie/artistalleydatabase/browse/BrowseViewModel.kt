package com.thekeeperofpie.artistalleydatabase.browse

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    tabViewModels: Set<@JvmSuppressWildcards BrowseTabViewModel>,
) : ViewModel() {

    private val sortedModels = tabViewModels.toList()
        .sortedWith(compareBy({ it.priorityMajor }, { it.priorityMinor }))

    val tabs = sortedModels.map { it.tab }

    fun onSelectEntry(
        navController: NavHostController,
        tabContent: BrowseScreen.TabContent,
        entry: BrowseEntryModel
    ) {
        tabContent.onSelected(navController, entry)
    }

    fun onPageRequested(page: Int) {
        ((page - 1).coerceAtLeast(0)..(page + 1).coerceAtMost(sortedModels.size - 1)).forEach {
            sortedModels[it].startLoad()
        }
    }
}