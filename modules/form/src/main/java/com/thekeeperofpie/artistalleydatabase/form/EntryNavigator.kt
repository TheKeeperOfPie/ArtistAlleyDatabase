package com.thekeeperofpie.artistalleydatabase.form

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

interface EntryNavigator {

    fun initialize(navHostController: NavHostController, navGraphBuilder: NavGraphBuilder)
}