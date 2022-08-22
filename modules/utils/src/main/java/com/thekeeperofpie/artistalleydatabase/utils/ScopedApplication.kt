package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.coroutines.CoroutineScope

interface ScopedApplication {
    val scope: CoroutineScope
}