@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalJsCollectionsApi::class)

import org.w3c.fetch.Request
import kotlin.js.Promise

external interface EventContext<Env> {
    val request: Request
    val waitUntil: (promise: Promise<Any>) -> Unit
    val env: Env
    val functionPath: String?
}

external interface Env {
    val ANIME_EXPO_2026_DB: D1Database
}

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
