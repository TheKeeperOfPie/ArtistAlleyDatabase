package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@SingleIn(AppScope::class)
@Inject
actual class PageVisibility {
    actual val isVisible: StateFlow<Boolean>
        field = MutableStateFlow(getVisibilityState() == "visible")

    init {
        window.document.addEventListener("visibilitychange", {
            isVisible.value = getVisibilityState() == "visible"
        })
    }
}

internal expect fun getVisibilityState(): String
