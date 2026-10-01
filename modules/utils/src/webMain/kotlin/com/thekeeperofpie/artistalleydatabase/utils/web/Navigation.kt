package com.thekeeperofpie.artistalleydatabase.utils_compose.navigation

import com.thekeeperofpie.artistalleydatabase.utils.ConsoleLogger
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

external class Navigation {

    fun addEventListener(type: String, callback: ((Event) -> Unit)?)
    fun removeEventListener(type: String, callback: ((Event) -> Unit)?)

    // TODO: Is this actually nullable?
    val currentEntry: NavigationHistoryEntry?

    fun entries(): JsArray<NavigationHistoryEntry>
    fun back(options: NavigationOptions? = definedExternally): NavigationResult
    fun forward(options: NavigationOptions? = definedExternally): NavigationResult
    fun navigate(url: String): NavigationResult
    fun traverseTo(key: String): NavigationResult
    fun traverseTo(key: String, options: NavigationOptions): NavigationResult
}

external class NavigationResult : JsAny {
    val committed: Promise<*>
    val finished: Promise<*>
}


private fun navigationOptions(): NavigationOptions = js("({})")

external interface NavigationOptions : JsAny {
    var info: JsAny?
}

fun NavigationOptions(info: JsAny?): NavigationOptions =
    navigationOptions().apply { this.info = info }

// TODO: Companion val when 2.5.0+
val TestNavigationOptions = NavigationOptions("IN_TEST".toJsString())

val Window.navigation: Navigation?
    get() = try {
        this["navigation"] as? Navigation
    } catch (t: Throwable) {
        t.printStackTrace()
        ConsoleLogger.log("Failed to read browser Navigation API")
        null
    }

external class NavigateEvent : JsAny {
    val canIntercept: Boolean
    val destination: NavigationDestination
    val navigationType: String
    val userInitiated: Boolean
    val info: JsAny?

    fun intercept(options: InterceptOptions = definedExternally)
}

private fun interceptOptions(): InterceptOptions = js("({})")

external interface InterceptOptions : JsAny {
    var handler: (() -> Promise<*>)?
    var focusReset: String?
    var scroll: String?
}

fun InterceptOptions(
    focusReset: String? = null,
    scroll: String? = null,
    handler: suspend () -> Unit,
): InterceptOptions = interceptOptions().apply {
    this.handler = { promise { handler(); null as JsAny? } }
    if (focusReset != null) this.focusReset = focusReset
    if (scroll != null) this.scroll = scroll
}

fun <T : JsAny?> promise(block: suspend () -> T) =
    Promise { resolve, reject ->
        block.startCoroutine(completion = object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                result.fold(
                    onSuccess = resolve,
                    onFailure = {
                        reject((it.message ?: "Error").toJsString().unsafeCast())
                    },
                )
            }
        })
    }

external class NavigationDestination {
    val id: String
    val index: Int
    val key: String?
    val url: String
}

object NavigationType {
    const val PUSH = "push"
    const val RELOAD = "reload"
    const val REPLACE = "replace"
    const val TRAVERSE = "traverse"
}

external interface NavigationHistoryEntry : JsAny {
    val id: String
    val index: Int
    val key: String?
    val url: String
}
