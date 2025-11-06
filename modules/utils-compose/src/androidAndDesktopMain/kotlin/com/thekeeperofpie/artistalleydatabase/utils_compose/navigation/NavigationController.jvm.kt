package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

internal actual fun NavigationControllerImpl.navigateBack() = navHostController.popBackStack()
actual fun interceptNavigateBack() = false
