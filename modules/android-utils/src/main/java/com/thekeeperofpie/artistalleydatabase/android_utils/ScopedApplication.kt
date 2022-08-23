package com.thekeeperofpie.artistalleydatabase.android_utils

import kotlinx.coroutines.CoroutineScope

interface ScopedApplication {
    val scope: CoroutineScope
}