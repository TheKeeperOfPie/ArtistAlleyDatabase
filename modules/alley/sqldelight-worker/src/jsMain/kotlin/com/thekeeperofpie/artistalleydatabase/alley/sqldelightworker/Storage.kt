package com.thekeeperofpie.artistalleydatabase.alley.sqldelightworker

import org.khronos.webgl.DataView
import org.khronos.webgl.Uint8Array
import kotlin.js.Promise

external interface Navigator {
    val storage: StorageManager
}

external interface StorageManager {
    fun getDirectory(): Promise<FileSystemDirectoryHandle>
}

external interface FileGetHandleOptions {
    var create: Boolean?
}

fun FileGetHandleOptions(create: Boolean = false): FileGetHandleOptions {
    val o = js("{}").unsafeCast<FileGetHandleOptions>()
    o.create = create
    return o
}

external interface FileSystemDirectoryHandle {
    fun getFileHandle(
        name: String,
        options: FileGetHandleOptions = definedExternally,
    ): Promise<FileSystemFileHandle>
}

external interface FileSystemFileHandle {
    fun createSyncAccessHandle(): Promise<FileSystemSyncAccessHandle>
}

external interface FileSystemSyncAccessHandle {
    fun getSize(): Int
    fun read(buffer: DataView): Int
    fun write(buffer: Uint8Array): Int
    fun truncate(newSize: Int)
    fun flush()
    fun close()
}
