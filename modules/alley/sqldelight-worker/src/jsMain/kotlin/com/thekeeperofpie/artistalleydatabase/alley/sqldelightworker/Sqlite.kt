package com.thekeeperofpie.artistalleydatabase.alley.sqldelightworker

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

fun sqlite3InitModule(options: Sqlite3InitOptions): Promise<Sqlite3> {
    val sqliteModule = js("require('@sqlite.org/sqlite-wasm')")
    val initFunction = if (js("typeof sqliteModule === 'function'").unsafeCast<Boolean>()) {
        sqliteModule
    } else {
        sqliteModule.default
    }
    return initFunction(options).unsafeCast<Promise<Sqlite3>>()
}

external interface Sqlite3InitOptions {
    var print: ((msg: dynamic) -> Unit)?
    var printErr: ((msg: dynamic) -> Unit)?
}

fun Sqlite3InitOptions(
    print: ((msg: dynamic) -> Unit)? = null,
    printErr: ((msg: dynamic) -> Unit)? = null,
): Sqlite3InitOptions {
    val o = js("{}").unsafeCast<Sqlite3InitOptions>()
    if (print != null) o.print = print
    if (printErr != null) o.printErr = printErr
    return o
}

external interface Sqlite3 {
    val oo1: Sqlite3Oo1
    val wasm: Sqlite3Wasm
    val capi: Sqlite3Capi
}

external interface Sqlite3Oo1 {
    val OpfsDb: Sqlite3OpfsDbClass?
}

@Suppress("UNUSED_PARAMETER", "UnusedVariable")
fun Sqlite3Oo1.createOpfsDb(path: String, mode: String = "c"): Sqlite3Db {
    val oo1 = this
    return js("new oo1.OpfsDb(path, mode)").unsafeCast<Sqlite3Db>()
}

@Suppress("UNUSED_PARAMETER", "UnusedVariable")
fun Sqlite3Oo1.createDb(path: String? = null, mode: String? = null): Sqlite3Db {
    val oo1 = this
    return js("new oo1.DB(path, mode)").unsafeCast<Sqlite3Db>()
}

external interface Sqlite3OpfsDbClass {
    fun importDb(path: String, buffer: ArrayBuffer): Promise<Unit>
}

external interface Sqlite3DbClass

external interface Sqlite3Db {
    val pointer: dynamic
    fun exec(sql: String): dynamic
    fun exec(options: Sqlite3ExecOptions): dynamic
    fun checkRc(rc: Int)
}

external interface Sqlite3ExecOptions {
    var sql: String?
    var bind: dynamic
    var returnValue: String?
}

fun Sqlite3ExecOptions(
    sql: String? = null,
    bind: dynamic = null,
    returnValue: String? = null,
): Sqlite3ExecOptions {
    val o = js("{}").unsafeCast<Sqlite3ExecOptions>()
    if (sql != null) o.sql = sql
    if (bind != null) o.bind = bind
    if (returnValue != null) o.returnValue = returnValue
    return o
}

external interface Sqlite3Wasm {
    fun allocFromTypedArray(buffer: ArrayBuffer): dynamic
}

@Suppress("PropertyName", "FunctionName")
external interface Sqlite3Capi {
    val SQLITE_DESERIALIZE_FREEONCLOSE: Int
    val SQLITE_DESERIALIZE_RESIZEABLE: Int
    fun sqlite3_deserialize(
        db: dynamic,
        schema: String,
        data: dynamic,
        szDb: Int,
        szBuf: Int,
        mFlags: Int,
    ): Int
}
