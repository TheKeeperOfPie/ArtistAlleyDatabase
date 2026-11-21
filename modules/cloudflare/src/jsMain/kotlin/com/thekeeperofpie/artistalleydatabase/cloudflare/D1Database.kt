package com.thekeeperofpie.artistalleydatabase.cloudflare

import kotlin.js.Promise

@OptIn(ExperimentalWasmJsInterop::class)
external interface D1Database {
    fun prepare(statement: String): D1PreparedStatement
    fun batch(statements: JsArray<D1PreparedStatement>): Promise<JsArray<*>>
}

external interface D1PreparedStatement {
    fun bind(vararg values: Any?): D1PreparedStatement
    fun run(): Promise<D1Result>
    fun raw(): Promise<Array<Array<dynamic>>>
}

external interface D1Result {
    val meta: D1ResultMeta
    val results: Array<Array<dynamic>>
}

external interface D1ResultMeta {
    val changes: Int
}
