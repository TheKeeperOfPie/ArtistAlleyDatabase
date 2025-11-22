package com.thekeeperofpie.artistalleydatabase.utils

import co.touchlab.kermit.Logger

actual object ConsoleLogger {
    actual fun log(message: String) = Logger.d { message }
}
