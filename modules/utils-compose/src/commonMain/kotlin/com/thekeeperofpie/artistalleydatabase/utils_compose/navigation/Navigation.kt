package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalNavHostController = staticCompositionLocalOf<NavHostController> { throw IllegalStateException("No NavHostController provided")}
