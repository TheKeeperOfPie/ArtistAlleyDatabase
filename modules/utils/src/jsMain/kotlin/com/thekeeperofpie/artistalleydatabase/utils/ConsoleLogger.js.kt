package com.thekeeperofpie.artistalleydatabase.utils

actual object ConsoleLogger {
    actual fun log(message: String) = console.log(message)
}
