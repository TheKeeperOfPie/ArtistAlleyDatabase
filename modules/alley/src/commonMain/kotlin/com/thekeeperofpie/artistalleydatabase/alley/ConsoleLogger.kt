package com.thekeeperofpie.artistalleydatabase.alley

// This is required because Kermit tries to compile stackTraceToString, which fails on wasmJs
expect object ConsoleLogger {
    fun log(message: String)
}
