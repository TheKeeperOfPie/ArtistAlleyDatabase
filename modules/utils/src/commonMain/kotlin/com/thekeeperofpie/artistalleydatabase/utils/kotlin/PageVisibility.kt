package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlinx.coroutines.flow.StateFlow

expect class PageVisibility {
    val isVisible: StateFlow<Boolean>
}
