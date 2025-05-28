package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import kotlinx.browser.window

internal actual fun NavigationControllerImpl.navigateBack(): Boolean {
    val canGoBack = window.history.length > 1
    window.history.back()
    return canGoBack
}
