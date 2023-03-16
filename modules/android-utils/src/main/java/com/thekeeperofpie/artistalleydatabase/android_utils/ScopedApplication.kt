package com.thekeeperofpie.artistalleydatabase.android_utils

import android.app.Application
import kotlinx.coroutines.CoroutineScope

abstract class ScopedApplication : Application() {
    abstract val scope: CoroutineScope
}