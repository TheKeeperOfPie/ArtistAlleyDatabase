package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
@Inject
actual class PageVisibility {
    actual val isVisible = MutableStateFlow(true).asStateFlow()
}
