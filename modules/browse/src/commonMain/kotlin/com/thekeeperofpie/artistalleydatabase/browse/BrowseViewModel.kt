package com.thekeeperofpie.artistalleydatabase.browse

import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import dev.zacsweers.metro.Inject

@Inject
class BrowseViewModel(
    tabViewModels: Set<BrowseTabViewModel>,
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
