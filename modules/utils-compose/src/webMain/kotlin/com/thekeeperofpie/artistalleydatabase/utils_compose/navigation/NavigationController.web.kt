package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import kotlinx.browser.window

// TODO: Merge into webMain, can't import kotlinx.browser for some reason
internal actual fun NavigationControllerImpl.navigateBack(): Boolean {
    val canGoBack = window.history.length > 1
    window.history.back()
    return canGoBack
}

actual fun interceptNavigateBack(): Boolean {
    window.history.back()
    return true
}
