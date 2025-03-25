package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import kotlinx.browser.window

actual fun NavigationController.navigateBack(): Boolean {
    val canGoBack = window.history.length > 1
    window.history.back()
    return canGoBack
}
