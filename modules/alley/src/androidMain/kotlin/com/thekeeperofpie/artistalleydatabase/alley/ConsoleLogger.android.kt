package com.thekeeperofpie.artistalleydatabase.alley

import co.touchlab.kermit.Logger

actual object ConsoleLogger {
    actual fun log(message: String) = Logger.d { message }
}
