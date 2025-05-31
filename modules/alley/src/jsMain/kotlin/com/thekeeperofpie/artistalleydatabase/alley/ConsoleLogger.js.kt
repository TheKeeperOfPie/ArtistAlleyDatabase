package com.thekeeperofpie.artistalleydatabase.alley

actual object ConsoleLogger {
    actual fun log(message: String) = console.log(message)
}
