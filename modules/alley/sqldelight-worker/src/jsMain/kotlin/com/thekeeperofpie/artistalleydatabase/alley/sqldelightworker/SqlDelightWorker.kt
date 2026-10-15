@file:OptIn(ExperimentalStdlibApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.sqldelightworker

import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.fetch.Response
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

external val self: DedicatedWorkerGlobalScope
external val navigator: Navigator

external fun fetch(input: String): Promise<Response>
external fun postMessage(message: dynamic, transfer: Array<dynamic> = definedExternally)

external class TextDecoder(encoding: String = definedExternally) {
    fun decode(buffer: DataView = definedExternally): String
}

external class TextEncoder {
    fun encode(input: String): Uint8Array
}

private const val MUTABLE_DATABASE_PATH = "alleyUser.sqlite"
private const val READ_ONLY_INPUT_PATH =
    "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/database.sqlite"
private const val READ_ONLY_INPUT_HASH_PATH =
    "composeResources/artistalleydatabase.modules.alley.data.generated.resources/files/databaseHash.txt"
private const val READ_ONLY_DATABASE_PATH = "alleyArtist.sqlite"
private const val READ_ONLY_DATABASE_HASH_PATH = "alleyArtistHash.txt"

private lateinit var database: Sqlite3Db
private val mutex = Mutex()

fun main() {
    if (js("typeof importScripts === 'function'").unsafeCast<Boolean>()) {
        self.onmessage = { event: dynamic ->
            val data = event.data
            promise { createDatabase() }
                .then { handleMessage(data) }
                .catch { postMessage(WorkerErrorResponse(id = data?.id, error = it)) }
        }
    }
}

private suspend fun createDatabase(): Unit = mutex.withLock {
    if (::database.isInitialized) return@withLock
    val initOptions = Sqlite3InitOptions(print = console::log, printErr = console::error)
    val sqlite3 = sqlite3InitModule(initOptions).await()
    var opfsRoot: FileSystemDirectoryHandle? = null
    val forceNoOpfs = try {
        localStorage.getItem("forceNoOpfs")
    } catch (_: Throwable) {
        null
    }

    try {
        if (forceNoOpfs != "true" && sqlite3.oo1.OpfsDb != null) {
            opfsRoot = navigator.storage.getDirectory().await()
        }
    } catch (_: Throwable) {
        console.log("Failed to load OPFS")
    }

    if (opfsRoot != null) {
        val hashFile = opfsRoot.getFileHandle(
            name = READ_ONLY_DATABASE_HASH_PATH,
            options = FileGetHandleOptions(create = true),
        ).await()
        val hashHandle = hashFile.createSyncAccessHandle().await()
        val oldSize = hashHandle.getSize()
        val oldHash = ifOrNull(oldSize > 0) {
            val oldHashDataView = DataView(ArrayBuffer(oldSize))
            hashHandle.read(oldHashDataView)
            TextDecoder().decode(oldHashDataView)
        }

        // Works without caching check because ServiceWorker.kt will force read the right revision
        // using the hash in its filesToCacheAndRevisions array
        val newHashResponse = fetch(READ_ONLY_INPUT_HASH_PATH).await()
        val newHash = newHashResponse.text().await()

        console.log("Old database hash", oldHash)
        console.log("New database hash", newHash)

        if (oldHash != newHash) {
            console.log("Importing new database")
            val response = fetch(READ_ONLY_INPUT_PATH).await()
            val fileBuffer = response.arrayBuffer().await()
            sqlite3.oo1.OpfsDb?.importDb(READ_ONLY_DATABASE_PATH, fileBuffer)?.await()
            val encoded = TextEncoder().encode(newHash)
            hashHandle.truncate(0)
            hashHandle.write(encoded)
            hashHandle.flush()
        }
        hashHandle.close()

        database = sqlite3.oo1.createOpfsDb(MUTABLE_DATABASE_PATH, "c")
        database.exec("ATTACH DATABASE 'file:$READ_ONLY_DATABASE_PATH?vfs=opfs&immutable=1' AS readOnly;")
    } else {
        // TODO: Show warning that persistence is not supported for favorites (or just disable)
        console.log("OPFS not initialized")
        database = sqlite3.oo1.createDb()
        val response = fetch(READ_ONLY_INPUT_PATH).await()
        val fileBuffer = response.arrayBuffer().await()
        val data = sqlite3.wasm.allocFromTypedArray(fileBuffer)
        val deserializeFlags =
            sqlite3.capi.SQLITE_DESERIALIZE_FREEONCLOSE or sqlite3.capi.SQLITE_DESERIALIZE_RESIZEABLE
        val byteLength = fileBuffer.byteLength
        val rc = sqlite3.capi.sqlite3_deserialize(
            db = database.pointer,
            schema = "main",
            data = data,
            szDb = byteLength,
            szBuf = byteLength,
            mFlags = deserializeFlags,
        )
        database.checkRc(rc)
    }
}

private fun handleMessage(data: dynamic) {
    if (!::database.isInitialized) throw IllegalStateException("Database not initialized")
    when (val action = data?.action?.unsafeCast<String?>()) {
        "exec" -> {
            val sql = data["sql"]
            if (sql == null || sql == undefined) {
                throw IllegalStateException("exec: Missing query string")
            }
            val execOptions = Sqlite3ExecOptions(
                sql = sql.unsafeCast<String>(),
                bind = data.params,
                returnValue = "resultRows",
            )
            val values = database.exec(execOptions)
            postSuccess(data.id, WorkerExecResults(values))
        }
        "begin_transaction" -> postSuccess(data.id, database.exec("BEGIN TRANSACTION;"))
        "end_transaction" -> postSuccess(data.id, database.exec("END TRANSACTION;"))
        "rollback_transaction" -> postSuccess(data.id, database.exec("ROLLBACK TRANSACTION;"))
        else -> throw IllegalStateException("Unsupported action: $action")
    }
}

private fun postSuccess(id: String, results: dynamic) =
    postMessage(WorkerSuccessResponse(id = id, results = results))

private fun <T> promise(block: suspend () -> T): Promise<T> =
    Promise { resolve, reject ->
        block.startCoroutine(completion = object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                result.fold(resolve, reject)
            }
        })
    }
