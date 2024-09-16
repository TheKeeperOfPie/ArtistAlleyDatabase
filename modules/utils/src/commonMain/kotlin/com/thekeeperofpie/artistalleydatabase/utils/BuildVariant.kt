package com.thekeeperofpie.artistalleydatabase.utils

import co.touchlab.kermit.Logger

object BuildVariant

expect fun BuildVariant.isDebug(): Boolean

// TODO: Use setMinSeverity?
fun Logger.debug(tag: String, throwable: Throwable? = null, message: () -> String) {
    if (BuildVariant.isDebug()) {
        Logger.d(tag, throwable, message)
    }
}
