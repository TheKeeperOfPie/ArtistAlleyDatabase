package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import kotlinx.coroutines.CoroutineScope

interface ScopedApplication {
    val scope: CoroutineScope
    val app: Application
}