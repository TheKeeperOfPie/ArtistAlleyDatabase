package com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare

import kotlin.js.Promise

external interface D1Database {
    fun prepare(statement: String): D1PreparedStatement
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
