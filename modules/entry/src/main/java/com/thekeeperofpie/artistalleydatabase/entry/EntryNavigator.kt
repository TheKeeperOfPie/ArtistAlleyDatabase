package com.thekeeperofpie.artistalleydatabase.entry

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface EntryNavigator {

    fun initialize(navHostController: NavHostController, navGraphBuilder: NavGraphBuilder)
}