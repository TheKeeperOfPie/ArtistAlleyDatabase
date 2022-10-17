package com.thekeeperofpie.artistalleydatabase.browse

import androidx.navigation.NavHostController

interface BrowseSelectionNavigator {

    fun navigate(navHostController: NavHostController, entry: BrowseEntryModel)
}