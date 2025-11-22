package com.thekeeperofpie.artistalleydatabase.utils

@JsFun("(output) => console.log(output)")
external fun consoleLog(output: String)

actual object ConsoleLogger {
    actual fun log(message: String) = consoleLog(message)
}
