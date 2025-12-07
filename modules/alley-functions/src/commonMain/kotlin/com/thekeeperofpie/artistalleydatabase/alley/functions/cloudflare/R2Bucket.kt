package com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare

import kotlin.js.Promise

external interface R2Bucket {
    fun list(options: R2ListOptions?): Promise<R2Objects>
    fun get(key: String, options: R2GetOptions? = definedExternally): Promise<R2ObjectBody?>
    fun put(key: String, value: dynamic, options: R2PutOptions? = definedExternally): Promise<R2Object>
}

external interface ReadableStream

external interface R2ListOptions {
    val prefix: String?
}

external interface R2Objects {
    val objects: Array<R2Object>
    val truncated: Boolean // TODO: Implement multi-request?
    val cursor: String?
}

@Suppress("NOTHING_TO_INLINE")
inline fun R2ListOptions(prefix: String): R2ListOptions {
    val o = js("({})")
    o["prefix"] = prefix
    return o
}

external interface R2Object {
    val key: String
}

external interface R2ObjectBody {
    val body: ReadableStream
    val httpMetadata: R2HttpMetadata
}

external interface R2HttpMetadata {
    val contentType: String?
}

external interface R2GetOptions
external interface R2PutOptions

external interface ResponseWithBody {
    val body: ReadableStream
}

external interface KeyValueStore {
    fun get(key: String): Promise<String?>
    fun put(key: String, value: String): Promise<dynamic>
    fun delete(key: String): Promise<dynamic>
}
