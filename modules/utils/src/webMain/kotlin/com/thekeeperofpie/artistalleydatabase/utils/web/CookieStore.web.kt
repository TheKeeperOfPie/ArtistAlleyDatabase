package com.thekeeperofpie.artistalleydatabase.utils.web

import kotlin.js.Promise

external val cookieStore: CookieStore

external interface CookieStore {
    fun get(name: String): Promise<CookieObject?>
    fun addEventListener(type: String, callback: (CookieChangeEvent) -> Unit)
}

external interface CookieObject : JsAny {
    val name: String
    val value: String
    val expires: Int?
}

external interface CookieChangeEvent {
    val changed: JsArray<CookieObject>
    val deleted: JsArray<CookieObject>
}
